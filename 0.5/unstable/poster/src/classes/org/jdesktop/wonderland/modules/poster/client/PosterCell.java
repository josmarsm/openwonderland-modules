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
package org.jdesktop.wonderland.modules.poster.client;

import java.awt.Image;
import java.awt.Rectangle;
import java.lang.reflect.InvocationTargetException;
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
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;

/**
 * The cell that renders the poster.<br>
 * Adapted from the "generic" cell facility originally written by Jordan Slott
 * 
 * @author Bernard Horan
 */
public class PosterCell extends Cell {

    final private static String SHARED_MAP_KEY = "Poster";
    final private static String LABEL_KEY = "text";
    private static final Logger posterCellLogger = Logger.getLogger(PosterCell.class.getName());
    @UsesCellComponent
    private ContextMenuComponent contextComp = null;
    private ContextMenuFactorySPI menuFactory = null;
    // The "shared state" Cell component
    @UsesCellComponent
    protected SharedStateComponent sharedStateComp;
    // The Cell's renderer
    private PosterCellRenderer cellRenderer = null;
    // The listener for changes to the shared map
    private SharedMapListenerCli mapListener = null;
    private String posterText;
    private PosterForm posterForm;

    /** Constructor, takes Cell's ID and Cache */
    public PosterCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        mapListener = new MySharedMapListener();
        try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        posterForm = new PosterForm(PosterCell.this);
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
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        if (increasing && status == CellStatus.ACTIVE) {
            posterCellLogger.info("active and increasing");

            // Create the shared hash map and initialize the poster text
            // if it does not already exist.
            SharedMapCli sharedMap = sharedStateComp.get(SHARED_MAP_KEY);
            SharedString posterString = sharedMap.get(LABEL_KEY, SharedString.class);
            if (posterString == null) {
                posterString = SharedString.valueOf("Hello World!");
                sharedMap.put(LABEL_KEY, posterString);
            }

            posterText = posterString.toString();
            posterCellLogger.info("posterText: " + posterText);
            
            //Add menu item to edit the text from the context menu
            if (menuFactory == null) {
                final ContextMenuActionListener l = new ContextMenuActionListener() {

                    public void actionPerformed(ContextMenuItemEvent event) {
                        openPosterForm();
                    }
                };
                menuFactory = new ContextMenuFactorySPI() {

                    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                        return new ContextMenuItem[]{
                                    new SimpleContextMenuItem("Set Text...", l)
                                };
                    }
                };
                contextComp.addContextMenuFactory(menuFactory);
            }
        }
        if (status == CellStatus.RENDERING && increasing == true) {
            // Initialize the render with the current poster text
            posterCellLogger.info("rendering and increasing");
            posterForm.updateTextArea();
            cellRenderer.updateText();


            // Listen for changes in the poster text from other clients
            SharedMapCli sharedMap = sharedStateComp.get(SHARED_MAP_KEY);
            sharedMap.addSharedMapListener(LABEL_KEY, mapListener);

        }
        if (!increasing && status == CellStatus.DISK) {
            // Remove the listener for changes to the shared map
            SharedMapCli sharedMap = sharedStateComp.get(SHARED_MAP_KEY);
            sharedMap.removeSharedMapListener(LABEL_KEY, mapListener);
            //Cleanup menu
            if (menuFactory != null) {
                contextComp.removeContextMenuFactory(menuFactory);
                menuFactory = null;
            }
        }
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            cellRenderer = new PosterCellRenderer(this);
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
        posterCellLogger.info("text: " + text);
        if (!text.equals(posterText)) {
            posterCellLogger.info("not equal");
            posterText = text;
            SharedMapCli sharedMap = sharedStateComp.get(SHARED_MAP_KEY);
            SharedString labelTextString = SharedString.valueOf(posterText);
            sharedMap.put(LABEL_KEY, labelTextString);
        }
    }

    /**
     * Listens to changes in the shared map and updates the poster text
     */
    class MySharedMapListener implements SharedMapListenerCli {

        public void propertyChanged(SharedMapEventCli event) {
            posterCellLogger.info("property changed: " + event.getNewValue().toString());
            posterText = event.getNewValue().toString();
            posterForm.updateTextArea();
            cellRenderer.updateText();

        }
    }
}
