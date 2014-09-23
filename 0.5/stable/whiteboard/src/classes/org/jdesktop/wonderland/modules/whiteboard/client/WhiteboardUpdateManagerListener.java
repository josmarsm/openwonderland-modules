/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.whiteboard.client;

import java.net.URL;
import java.util.logging.Logger;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.UpdateManagerEvent;
import org.apache.batik.bridge.UpdateManagerListener;
import org.apache.batik.gvt.GraphicsNode;
import org.jdesktop.wonderland.modules.whiteboard.common.WhiteboardUtils;
import org.w3c.dom.Element;

/**
 *
 * @author bh37721
 * @author nsimpson
 * @author Abhishek Upadhyay
 */
public class WhiteboardUpdateManagerListener implements UpdateManagerListener {

    private static final Logger logger =
            Logger.getLogger(WhiteboardUpdateManagerListener.class.getName());
    private WhiteboardApp whiteboardApp;

    WhiteboardUpdateManagerListener(WhiteboardApp whiteboardApp) {
        this.whiteboardApp = whiteboardApp;
    }

    /**
     * Called when the manager was started.
     */
    public void managerStarted(UpdateManagerEvent e) {
        logger.fine("whiteboard: manager started: " + e);
        try {
            // attach white background
            if (whiteboardApp.getWindow().getDocument() != null) {
                Element bImage = whiteboardApp.getWindow().getDocument().getElementById("bImage");
                if (bImage == null) {
                    bImage = whiteboardApp.getWindow().getDocument().createElementNS(WhiteboardUtils.svgNS, "image");
                    bImage.setAttribute("xml:space", "preserve");
                    bImage.setAttributeNS(null, "id", "bImage");
                    bImage.setAttributeNS(null, "width", "800");
                    bImage.setAttributeNS(null, "height", "600");
                    bImage.setAttributeNS(null, "x", "0");
                    bImage.setAttributeNS(null, "y", "0");
                    URL url = getClass().getResource("resources/white-background.png");
                    bImage.setAttributeNS(WhiteboardUtils.xlinkNS, "xlink:href", url.toExternalForm());
                    if(whiteboardApp.getWindow().getBridgeContext()!=null 
                            && whiteboardApp.getWindow().getBridgeContext().getUpdateManager()!=null
                            &&whiteboardApp.getWindow().getBridgeContext().getUpdateManager().getUpdateRunnableQueue()!=null) {
                        
                        Element bImage1 = whiteboardApp.getWindow().getDocument().getElementById("bImage");
                        if(bImage1==null) {
                            whiteboardApp.getWindow().addBackgroundImage(bImage, true, false);
                        }
                    }
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Called when the manager was suspended.
     */
    public void managerSuspended(UpdateManagerEvent e) {
        logger.fine("whiteboard: manager suspended: " + e);
    }

    /**
     * Called when the manager was resumed.
     */
    public void managerResumed(UpdateManagerEvent e) {
        logger.fine("whiteboard: manager resumed: " + e);
    }

    /**
     * Called when the manager was stopped.
     */
    public void managerStopped(UpdateManagerEvent e) {
        logger.fine("whiteboard: manager stopped: " + e);
    }

    /**
     * Called when an update started.
     */
    public void updateStarted(UpdateManagerEvent e) {
        logger.fine("whiteboard: update started: " + e);
    }

    /**
     * Called when an update was completed.
     */
    public void updateCompleted(UpdateManagerEvent e) {
        logger.info("whiteboard: update completed:" + e);
        final WhiteboardWindow whiteboardWindow = whiteboardApp.getWindow();
        try {
            if (whiteboardWindow.getToolManager().getTool() == WhiteboardToolManager.WhiteboardTool.NEW_TEXT) {
                Element tempText = whiteboardWindow.getDocument().getElementById("tempText");
                if (tempText != null) {
                    if (tempText.getTextContent().equals("")) {
                        double lineX = Integer.parseInt(tempText.getAttribute("x")) + 2;
                        Element line = (Element) whiteboardWindow.getDocument().getElementById("cursor").cloneNode(true);
                        Element line1 = (Element) whiteboardWindow.getDocument().getElementById("cursor1").cloneNode(true);
                        double oldX = Double.parseDouble(line.getAttribute("x1"));
                        if (oldX != lineX) {
                            line.setAttribute("x1", String.valueOf(lineX));
                            line.setAttribute("x2", String.valueOf(lineX));
                            line1.setAttribute("x1", String.valueOf(lineX));
                            line1.setAttribute("x2", String.valueOf(lineX));
                            whiteboardWindow.updateElement(line1, false);
                            whiteboardWindow.updateElement(line, false);
                        }
                    } else {
                        double forSpace = 0;
                        String txt = tempText.getTextContent();
                        if(txt.substring(txt.length()-1, txt.length()).equals(" ")) {
                            forSpace = 10;
                        }
                        BridgeContext bc = whiteboardWindow.getBridgeContext();
                        GraphicsNode gNode = bc.getGraphicsNode(tempText);
                        if (gNode != null) {
                            if (gNode.getBounds() != null) {
                                double lineX = Integer.parseInt(tempText.getAttribute("x")) + gNode.getBounds().getWidth() + 2 + forSpace;
                                Element line = (Element) whiteboardWindow.getDocument().getElementById("cursor").cloneNode(true);
                                Element line1 = (Element) whiteboardWindow.getDocument().getElementById("cursor1").cloneNode(true);
                                double oldX = Double.parseDouble(line.getAttribute("x1"));
                                if (oldX != lineX) {
                                    line.setAttribute("x1", String.valueOf(lineX));
                                    line.setAttribute("x2", String.valueOf(lineX));
                                    line1.setAttribute("x1", String.valueOf(lineX));
                                    line1.setAttribute("x2", String.valueOf(lineX));
                                    whiteboardWindow.updateElement(line1, false);
                                    whiteboardWindow.updateElement(line, false);
                                }
                            }
                        }
                    }
                    whiteboardWindow.removeElement(tempText, true);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        whiteboardApp.repaintCanvas();
        whiteboardWindow.getBridgeContext().getUpdateManager().getUpdateRunnableQueue().invokeLater(new Runnable() {

            public void run() {
                whiteboardWindow.refreshSelection();
                whiteboardWindow.selectElements();
            }
        });
        
    }

    /**
     * Called when an update failed.
     */
    public void updateFailed(UpdateManagerEvent e) {
        logger.fine("whiteboard: update failed: " + e);
    }
}
