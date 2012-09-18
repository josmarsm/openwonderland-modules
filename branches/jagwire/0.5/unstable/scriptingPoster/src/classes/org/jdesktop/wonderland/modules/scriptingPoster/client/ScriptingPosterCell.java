/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.scriptingPoster.client;

import java.awt.Image;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingActionClass;
import org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingComponent;
import org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingRunnable;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;

/**
 * The cell that renders the poster.<br>
 * Adapted from the "generic" cell facility originally written by Jordan Slott
 * 
 * @author Bernard Horan
 */
public class ScriptingPosterCell extends Cell {

    final private static String SHARED_MAP_KEY = "Poster";
    final private static String TEXT_LABEL_KEY = "text";
    final private static String MODE_LABEL_KEY = "mode";
    private static final Logger posterCellLogger = Logger.getLogger(ScriptingPosterCell.class.getName());

    @UsesCellComponent
    private ContextMenuComponent contextComp = null;
    private static final ResourceBundle bundle = ResourceBundle.getBundle("org/jdesktop/wonderland/modules/scriptingPoster/client/resources/Bundle");

    private ContextMenuFactorySPI menuFactory = null;

    // The "shared state" Cell component
    @UsesCellComponent
    protected SharedStateComponent sharedStateComp;
    @UsesCellComponent
    private ScriptingComponent scriptingComponent;

    // The Cell's renderer
    private ScriptingPosterCellRenderer cellRenderer = null;

    // The listener for changes to the shared map
    private SharedMapListenerCli mapListener = null;
    
    private String posterText;
    private boolean billboardMode = false;
    private ScriptingPosterForm posterForm;

    /** Constructor, takes Cell's ID and Cache */
    public ScriptingPosterCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        mapListener = new MySharedMapListener();
        try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        posterForm = new ScriptingPosterForm(ScriptingPosterCell.this);
                    }
                });
            } catch (InterruptedException ex) {
                posterCellLogger.log(Level.SEVERE, "Failed to create poster form", ex);
            } catch (InvocationTargetException ex) {
                posterCellLogger.log(Level.SEVERE, "Failed to create poster form", ex);
            }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing)
        {
        super.setStatus(status, increasing);
        if (increasing && status == CellStatus.ACTIVE)
            {
            //posterCellLogger.severe("active and increasing");

            // Create the shared hash map and initialize the poster text
            // if it does not already exist.
            SharedMapCli sharedMap = sharedStateComp.get(SHARED_MAP_KEY);
            SharedString posterString = sharedMap.get(TEXT_LABEL_KEY, SharedString.class);
            if (posterString == null)
                {
                posterString = SharedString.valueOf(bundle.getString("HELLO_WORLD!"));
                sharedMap.put(TEXT_LABEL_KEY, posterString);
                }

            posterText = posterString.toString();
            //posterCellLogger.severe("posterText: " + posterText);

            SharedBoolean billboardModeBoolean = sharedMap.get(MODE_LABEL_KEY, SharedBoolean.class);
            if (billboardModeBoolean == null)
                {
                billboardModeBoolean = SharedBoolean.FALSE;
                sharedMap.put(MODE_LABEL_KEY, billboardModeBoolean);
                }
            billboardMode = billboardModeBoolean.getValue();
            //posterCellLogger.severe("billboardMode: " + billboardMode);

            //Add menu item to edit the text from the context menu
            if (menuFactory == null)
                {
                final ContextMenuActionListener l = new ContextMenuActionListener()
                    {

                    public void actionPerformed(ContextMenuItemEvent event)
                        {
                        openPosterForm();
                        }
                    };
                menuFactory = new ContextMenuFactorySPI()
                    {

                    public ContextMenuItem[] getContextMenuItems(ContextEvent event)
                        {
                        return new ContextMenuItem[]
                            {
                            new SimpleContextMenuItem(bundle.getString("SET_TEXT"), l)
                            };
                        }
                    };
                contextComp.addContextMenuFactory(menuFactory);
                }
            ScriptingActionClass sac = new ScriptingActionClass();
            sac.setName("poster");
            sac.insertCmdMap("testit", testitRun);
            sac.insertCmdMap("text", putTextRun);
            scriptingComponent.putActionObject(sac);

            BufferedReader in = null;

/*            String resource = null;

        Properties props = new Properties();
        in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("resources/" + "module.properties")));

        try
            {
            props.load(in);
            props.list(System.out);
            String propResource = props.getProperty("scripts");
            System.out.println("The property = " + props.getProperty("scripts"));
            if(propResource.equals(null));
                {
                System.out.println("Inside poster cell - Received null for cell scripts resource");
                propResource = "component";
                }
            }
        catch(Exception ex)
            {
            System.out.println("Exception in load properties" + ex);
//            resource = "component";
            }
//        System.out.println("Poster cell - Scripts property = " + resource);
*/
        }
        if (status == CellStatus.RENDERING && increasing == true) {
            // Initialize the render with the current poster text
            //posterCellLogger.severe("rendering and increasing");
            posterForm.updateForm();
            cellRenderer.updateNode();


            // Listen for changes in the poster text from other clients
            SharedMapCli sharedMap = sharedStateComp.get(SHARED_MAP_KEY);
            sharedMap.addSharedMapListener(TEXT_LABEL_KEY, mapListener);
            sharedMap.addSharedMapListener(MODE_LABEL_KEY, mapListener);

        }
        if (!increasing && status == CellStatus.DISK) {
            // Remove the listener for changes to the shared map
            SharedMapCli sharedMap = sharedStateComp.get(SHARED_MAP_KEY);
            sharedMap.removeSharedMapListener(TEXT_LABEL_KEY, mapListener);
            sharedMap.removeSharedMapListener(MODE_LABEL_KEY, mapListener);
            //Cleanup menu
            if (menuFactory != null) {
                contextComp.removeContextMenuFactory(menuFactory);
                menuFactory = null;
            }
        }
    }
    public void testit(float x, float y, float z)
        {
        System.out.println("testit x = " + x + " y = " + y + " z = " + z);
        }

    ScriptingRunnable testitRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            testit(x, y, z);
            System.out.println("ScriptingActionClass - enter testit");
            }
        };


    public void putText(String theText)
        {
        System.out.println("Enter putText");
        setPosterText(theText);
        }

    ScriptingRunnable putTextRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            putText(avatar);
            System.out.println("ScriptingActionClass - enter testit");
            }
        };


    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            cellRenderer = new ScriptingPosterCellRenderer(this);
            return cellRenderer;
        }
        return super.createCellRenderer(rendererType);
    }

    Image getPosterImage() {
        return posterForm.getPreviewImage();
    }

    String getPosterText() {
        return posterText;
    }

    boolean getBillboardMode() {
        return billboardMode;
    }

    void openPosterForm() {
        Rectangle parentBounds = getParentFrame().getBounds();
        Rectangle formBounds = posterForm.getBounds();
        posterForm.setLocation(parentBounds.width / 2 - formBounds.width / 2 + parentBounds.x, parentBounds.height - formBounds.height - parentBounds.y);

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                posterForm.setVisible(true);
            }
        });
    }

    private JFrame getParentFrame() {
        return JmeClientMain.getFrame().getFrame();
    }

    void setPosterText(String text) {
        //posterCellLogger.severe("text: " + text);
        if (!text.equals(posterText)) {
            //posterCellLogger.severe("not equal");
            posterText = text;
            SharedMapCli sharedMap = sharedStateComp.get(SHARED_MAP_KEY);
            SharedString labelTextString = SharedString.valueOf(posterText);
            sharedMap.put(TEXT_LABEL_KEY, labelTextString);
        }
    }

    void setBillboardMode(boolean mode) {
        //posterCellLogger.severe("mode: " + mode);
        if (billboardMode != mode) {
            //posterCellLogger.severe("not equal");
            billboardMode = mode;
            SharedMapCli sharedMap = sharedStateComp.get(SHARED_MAP_KEY);
            SharedBoolean billboardModeBoolean = SharedBoolean.valueOf(billboardMode);
            sharedMap.put(MODE_LABEL_KEY, billboardModeBoolean);
        }
    }

    /**
     * Listens to changes in the shared map and updates the poster text or billboard mode
     */
    class MySharedMapListener implements SharedMapListenerCli {

        public void propertyChanged(SharedMapEventCli event) {
            //posterCellLogger.severe("property changed: " + event.getPropertyName() + " -- " + event.getNewValue());
            if (event.getPropertyName().equals(TEXT_LABEL_KEY)) {
                SharedString posterTextString = (SharedString) event.getNewValue();
                posterText = posterTextString.getValue();
            }
            if (event.getPropertyName().equals(MODE_LABEL_KEY)) {
                SharedBoolean billboardModeBoolean = (SharedBoolean) event.getNewValue();
                billboardMode = billboardModeBoolean.getValue();
            }
            posterForm.updateForm();
            cellRenderer.updateNode();

        }
    }
}
