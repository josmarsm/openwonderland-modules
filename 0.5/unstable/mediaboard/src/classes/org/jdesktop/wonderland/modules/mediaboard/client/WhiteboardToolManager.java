/**
  * iSocial Project
  * http://isocial.missouri.edu
  *
  * Copyright (c) 2011, University of Missouri iSocial Project, All 
  * Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * The iSocial project designates this particular file as
  * subject to the "Classpath" exception as provided by the iSocial
  * project in the License file that accompanied this code.
  */

/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.mediaboard.client;

import com.jme.math.Vector3f;
import java.awt.Point;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDDialog.MESSAGE_TYPE;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.mediaboard.common.WhiteboardUtils;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
import org.jdesktop.wonderland.modules.hud.client.HUDDialogComponent;

/**
 * Class to manage the selected tool
 * @author bhoran
 * @author nsimpson
 */
public class WhiteboardToolManager implements WhiteboardCellMenuListener {

    private boolean hudState = false;
    private HUDDialogComponent dialog;

    public enum WhiteboardTool {

        NEW, SELECTOR, LINE, RECT, ELLIPSE, TEXT, FILL, DRAW, SYNC, UNSYNC, IMAGE,
        //added for iSocial
        SAVE, OPEN, PICTURE
    }

    public enum WhiteboardColor {

        BLACK(Color.BLACK),
        WHITE(Color.WHITE),
        RED(Color.RED),
        GREEN(Color.GREEN),
        BLUE(Color.BLUE);
        private final Color c;

        WhiteboardColor(Color c) {
            this.c = c;
        }

        Color getColor() {
            return c;
        }
    }
    private WhiteboardColor currentColor = null;
    private WhiteboardTool currentTool = null;
    private boolean filled = false;
    private WhiteboardWindow whiteboardWindow;

    WhiteboardToolManager(WhiteboardWindow whiteboardWindow) {
        this.whiteboardWindow = whiteboardWindow;
        setTool(WhiteboardTool.LINE);
        setColor(WhiteboardColor.BLACK);
    }

    // WhiteboardCellMenuListener methods
    public void newDoc() {

        final JFrame frame = ClientContextJME.getClientMain().getFrame().getFrame();
        int result;
        Object[] options = { "OK", "Cancel" };
        result = JOptionPane.showOptionDialog(frame,
                "Be Careful, you are about to clear the entire document...", //message to show
                "Warning", //title
                JOptionPane.OK_CANCEL_OPTION, //type of return values
                JOptionPane.QUESTION_MESSAGE, //type of message
                null,
                options,
                options[0]);
        if(result == JOptionPane.OK_OPTION) {
            whiteboardWindow.newDocument(true);
        }
        //whiteboardWindow.newDocument(true);
    }

    public void openDoc() {
        whiteboardWindow.showSVGDialog();
    }

    public void selector() {
        setTool(WhiteboardTool.SELECTOR);
    }

    public void line() {
        setTool(WhiteboardTool.LINE);
    }

    public void rect() {
        setTool(WhiteboardTool.RECT);
    }

    public void ellipse() {
        setTool(WhiteboardTool.ELLIPSE);
    }

    public void text() {
        setTool(WhiteboardTool.TEXT);
    }

    public void fill() {
        setFilled(true);
    }

    public void draw() {
        setFilled(false);
    }

    public void black() {
        setColor(WhiteboardColor.BLACK);
    }

    public void white() {
        setColor(WhiteboardColor.WHITE);
    }

    public void red() {
        setColor(WhiteboardColor.RED);
    }

    public void green() {
        //setColor(WhiteboardColor.GREEN);
        setTool(WhiteboardTool.IMAGE);
    }

    public void blue() {
        setColor(WhiteboardColor.BLUE);
    }

    public void zoomIn() {
        //zoomTo(1.1f, true);
    }

    public void zoomOut() {
        //zoomTo(0.9f, true);
    }

    public void sync() {
        hudState = !hudState;
        whiteboardWindow.sync(!whiteboardWindow.isSynced());
    }

    public void unsync() {
        hudState = !hudState;
        whiteboardWindow.sync(!whiteboardWindow.isSynced());
    }

    public void save() {
        System.out.println("Save button pushed!");

        String testMarkup = "<duh/>";
      String boardMarkup = WhiteboardUtils.documentToXMLString(whiteboardWindow.getDocument());
        try{
            ContentRepository repository = ContentRepositoryRegistry.getInstance().getRepository(whiteboardWindow.getCell().getCellCache().getSession().getSessionManager());
            
            ContentCollection groupsRoot = (ContentCollection)repository.getRoot().getChild("groups");
            if(groupsRoot == null) {
                System.out.println("groupsRoot is null");
            }
            ContentCollection mediaRoot = (ContentCollection)groupsRoot.getChild("media");
            if(mediaRoot == null) {
                System.out.println("mediaRoot is NULL!");
            }
            ContentCollection boardsRoot = (ContentCollection)mediaRoot.getChild("boards");
            if(boardsRoot == null) {
                System.out.println("boardsRoot is NULL!");
                return;
            }

           if(boardsRoot.canWrite() == true) {
               System.out.println("IS WRITABLE!");
           }

            TextGetter getter = new TextGetter(boardsRoot, boardMarkup);
            new Thread(getter).start();
            
        } catch(Exception e) {
            e.printStackTrace();
        }

        //setTool(WhiteboardTool.SAVE);
    }

    public void open() {
       // System.out.println("Open button pushed!");
        this.whiteboardWindow.getWhiteboardDocument().createElement(WhiteboardTool.OPEN, new Point(), new Point());
        setTool(WhiteboardTool.SELECTOR);
    }

    public void picture() {
        //System.out.println("Picture button pushed!");
        //setTool(WhiteboardTool.IMAGE);
        final WhiteboardDocument document = whiteboardWindow.getWhiteboardDocument();
        final Cell cell = whiteboardWindow.getCell();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                HUDComponent component;
                ImageCapturePanel panel = new ImageCapturePanel(document);
                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                component = mainHUD.createComponent(panel, cell);

                component.setPreferredLocation(Layout.CENTER);
                component.setEnabled(true);
                panel.setHUDComponent(component);
                mainHUD.addComponent(component);
                component.setWorldLocation(new Vector3f(0, 0, 0));
                component.setWorldVisible(true);
                
                //component.setVisible(true);
                setTool(WhiteboardTool.SELECTOR);
            }
        });
        
    }



    public void toggleHUD() {
        if (whiteboardWindow.getDisplayMode().equals(DisplayMode.HUD)) {
            whiteboardWindow.setDisplayMode(DisplayMode.WORLD);
        } else {
            whiteboardWindow.setDisplayMode(DisplayMode.HUD);
        }
        whiteboardWindow.showControls(true);
    }

    public boolean isOnHUD() {
        return (whiteboardWindow.getDisplayMode().equals(DisplayMode.HUD));
    }

    private void setTool(WhiteboardTool newTool) {
        if (currentTool == newTool) {
            //no change
            return;
        }
        if (currentTool != null) {
            //Untoggle the tool
            whiteboardWindow.deselectTool(currentTool);
            currentTool = null;
        }
        if (newTool != null) {
            //toggle the new tool
            whiteboardWindow.selectTool(newTool);
            currentTool = newTool;
        }
    }

    /**
     * @return the currentTool
     */
    public WhiteboardTool getTool() {
        return currentTool;
    }

    private void setColor(WhiteboardColor newColor) {
        if (currentColor == newColor) {
            // no change
            return;
        }
        if (currentColor != null) {
            // untoggle the color
            whiteboardWindow.deselectColor(currentColor);
            currentColor = null;
        }
        if (newColor != null) {
            // toggle the new tool
            whiteboardWindow.selectColor(newColor);
            currentColor = newColor;
        }
    }

    /**
     * @return the currentColor
     */
    public Color getColor() {
        return currentColor.getColor();
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
        whiteboardWindow.updateMenu();
    }

    public boolean isFilled() {
        return filled;
    }

    /**
     * Adapted from TextGetter in WhiteboardDocument.java
     * Thanks Bernard!
     * @author Bernard Horan
     * @author JagWire
     */
    private class TextGetter implements Runnable {

        private String markup;
        private ContentCollection boardsRoot;
        public TextGetter(ContentCollection boardsRoot, String markup) {
            this.markup = markup;
            this.boardsRoot = boardsRoot;

        }

        public void run() {
            if (dialog == null) {
                // create a HUD text dialog
                dialog = new HUDDialogComponent(whiteboardWindow.getCell());
                dialog.setMessage("Enter filename:");
                dialog.setType(MESSAGE_TYPE.QUERY);
                dialog.setPreferredLocation(Layout.CENTER);
                dialog.setWorldLocation(new Vector3f(0.0f, 0.0f, 0.5f));

                // add the text dialog to the HUD
                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                mainHUD.addComponent(dialog);

                PropertyChangeListener plistener = new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent pe) {
                        if (pe.getPropertyName().equals("ok")) {
                            String value = (String) pe.getNewValue();
                            if ((value != null) && (value.length() > 0)) {
                                try {
                                    String fileName = (String)pe.getNewValue();
                                    //just in case a user enters a space, we'll replace it with an underscore.
                                    fileName = fileName.replace(" ", "_");
                                    fileName = fileName + ".svg";
                                    ContentResource resource = (ContentResource)boardsRoot.createChild(fileName,Type.RESOURCE);
                                    resource.put(markup.getBytes());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (dialog.isVisible()) {
                            dialog.setVisible(false);
                        }
                        if (dialog.isWorldVisible()) {
                            dialog.setWorldVisible(false);
                        }
                        dialog.setValue("");
                        dialog.removePropertyChangeListener(this);
                        dialog = null;
                    }
                };
                dialog.addPropertyChangeListener(plistener);
            }

            dialog.setVisible(whiteboardWindow.getDisplayMode() == DisplayMode.HUD);
            dialog.setWorldVisible(whiteboardWindow.getDisplayMode() != DisplayMode.HUD);
        }
    };


}
