/**
 * iSocial Project http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights
 * Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as subject to the
 * "Classpath" exception as provided by the iSocial project in the License file
 * that accompanied this code.
 */
/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.mediaboard.client;

import com.jme.math.Vector3f;
//import com.sun.java.util.jar.pack.Attribute.Layout;
import java.awt.Color;
//import java.awt.DisplayMode;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.String;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
//import javax.vecmath.Vector3f;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDDialog.MESSAGE_TYPE;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
//import org.jdesktop.wonderland.modules.hud.client.HUDDialogComponent;
import org.jdesktop.wonderland.modules.mediaboard.client.WhiteboardToolManager.WhiteboardTool;
import org.jdesktop.wonderland.modules.mediaboard.client.cell.WhiteboardCell;
import org.jdesktop.wonderland.modules.mediaboard.client.hud.HUDDialogComponent;
import org.jdesktop.wonderland.modules.mediaboard.common.cell.WhiteboardCellMessage.Action;
import org.jdesktop.wonderland.modules.mediaboard.common.WhiteboardUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGDocument;

/**
 * Wraps the SVG document
 *
 * @author Bernard Horan
 */
public class WhiteboardDocument implements SVGDocumentLoaderListener {

    private static final Logger LOGGER =
            Logger.getLogger(WhiteboardDocument.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/mediaboard/client/resources/Bundle");
    private static final int TEXT_FONT_SIZE = 30;
    private WhiteboardWindow whiteboardWindow;
    private Date now;
    private Date then;
    private String docURI;
    private SVGDocument svgDocument;
    private DocumentDialog svgDocumentDialog;
    private HUDDialogComponent dialog;
    private HUDComponent mediaLibraryComponent = null; //used for MediaLibrary
    protected static final Object readyLock = new Object();
    //*******
    private static double TEXT_MARGIN = 17.0;
    HUDComponent pictureComponent = null;
    String URL;
    TakePhotoDialog panel;

    public void setTextMargin(Double margin) {
        TEXT_MARGIN = margin;
    }

    /**
     * A class for handling the loading of SVG documents. This can be time
     * consuming, so load in a thread
     */
    private class DocumentLoader extends Thread {

        private String uri = null;

        public DocumentLoader(String uri) {
            this.uri = uri;
            System.out.println("[iSocial] loading document from uri: " + uri);
            // Thread.dumpStack();
        }

        @Override
        public void run() {
            if (uri != null) {
                svgDocument = (SVGDocument) WhiteboardClientUtils.openDocument(uri);
                // loaded an external document
                whiteboardWindow.setDocument(svgDocument, false);
            }
        }
    }

    public WhiteboardDocument(WhiteboardWindow whiteboardWindow) {
        this.whiteboardWindow = whiteboardWindow;
    }

    private Element getDocumentElement() {
        return svgDocument.getDocumentElement();
    }

    public Element createElement(WhiteboardTool currentTool, Point pressedPoint, Point releasedPoint) {
        Element element = null;

        switch (currentTool) {
            case LINE:
                element = createLineElement(pressedPoint, releasedPoint, whiteboardWindow.getCurrentColor(), whiteboardWindow.getStrokeWeight());
                this.whiteboardWindow.getToolManager().selector();
                break;
            case RECT:
                element = createRectElement(pressedPoint, releasedPoint, whiteboardWindow.getToolManager().isFilled());
                break;
            case ELLIPSE:
                element = createEllipseElement(pressedPoint, releasedPoint, whiteboardWindow.getToolManager().isFilled());
                break;
            case TEXT:
                element = createTextElement(releasedPoint);
                this.whiteboardWindow.getToolManager().selector();
                break;
            case IMAGE:
                String urlString = "";// TakePhotoDialog.getPicture();

                //is this a photo?
                if (urlString.contains("photos")) {
                    int width = this.getWindow().getWidth();
                    if (width > 1200) {
                        element = createImageElement(150, 250, urlString);
                    } else {
                        element = createImageElement(300, 500, urlString);
                    }
                } else { //must be an image instead
                    try {
                        ImageIcon icon = new ImageIcon(new URL(urlString));
                        System.out.println("Icon Height: " + icon.getIconHeight()
                                + "\nIcon Width: " + icon.getIconWidth());

                        element = createImageElement(icon.getIconHeight(),
                                icon.getIconWidth(),
                                urlString);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                this.whiteboardWindow.getToolManager().selector();
                break;
            case SAVE: //TODO: this should be refactored out of this method
                //open save dialog


                break;
            case OPEN: //TODO: this should be refactored out of this method
                //open medialibrary dialog
                final Cell cell = this.whiteboardWindow.getCell();
                final WhiteboardDocument document = this;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (mediaLibraryComponent == null) {
                            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                            MediaLibrary libraryPanel = new MediaLibrary((WhiteboardCell) cell, document);
                            mediaLibraryComponent = mainHUD.createComponent(libraryPanel);
                            libraryPanel.setHUDComponent(mediaLibraryComponent);
                            mediaLibraryComponent.setPreferredLocation(Layout.CENTER);
                            mediaLibraryComponent.setDecoratable(true);
                            mainHUD.addComponent(mediaLibraryComponent);
                            mediaLibraryComponent.setVisible(true);
                        } else {

                            mediaLibraryComponent.setVisible(true);
                        }
                    }
                });
                break;
            case PICTURE:
                //open take picture dialog
                break;

            default:
                break;
        }
        this.whiteboardWindow.getToolManager().selector();

        return element;
    }

    public Element createLineElement(Point start, Point end, Color lineColor, Float strokeWeight) {
        //Create the line element
        Element line = svgDocument.createElementNS(WhiteboardUtils.svgNS, "line");
        line.setAttributeNS(null, "x1", Integer.valueOf(start.x).toString());
        line.setAttributeNS(null, "y1", Integer.valueOf(start.y).toString());
        line.setAttributeNS(null, "x2", Integer.valueOf(end.x).toString());
        line.setAttributeNS(null, "y2", Integer.valueOf(end.y).toString());
        line.setAttributeNS(null, "stroke", WhiteboardUtils.constructRGBString(lineColor));
        line.setAttributeNS(null, "stroke-width", Float.toString(strokeWeight));

        String idString = whiteboardWindow.getCellUID(whiteboardWindow.getApp()) + System.currentTimeMillis();
        line.setAttributeNS(null, "id", idString);
        LOGGER.fine("whiteboard: created line: " + line);
        return line;
    }

    public Element createRectElement(Point start, Point end, boolean filled) {
        //Create appropriate Rectangle from points
        Rectangle rect = WhiteboardUtils.constructRectObject(start, end);

        // Create the rectangle element
        Element rectangle = svgDocument.createElementNS(WhiteboardUtils.svgNS, "rect");
        rectangle.setAttributeNS(null, "x", Integer.valueOf(rect.x).toString());
        rectangle.setAttributeNS(null, "y", Integer.valueOf(rect.y).toString());
        rectangle.setAttributeNS(null, "width", Integer.valueOf(rect.width).toString());
        rectangle.setAttributeNS(null, "height", Integer.valueOf(rect.height).toString());
        rectangle.setAttributeNS(null, "stroke", WhiteboardUtils.constructRGBString(whiteboardWindow.getCurrentColor()));
        rectangle.setAttributeNS(null, "stroke-width", Float.toString(whiteboardWindow.getStrokeWeight()));
        rectangle.setAttributeNS(null, "fill", WhiteboardUtils.constructRGBString(whiteboardWindow.getCurrentColor()));
        if (!filled) {
            rectangle.setAttributeNS(null, "fill-opacity", "0");
        }

        String idString = whiteboardWindow.getCellUID(whiteboardWindow.getApp()) + System.currentTimeMillis();
        rectangle.setAttributeNS(null, "id", idString);

        return rectangle;
    }

    public Element createImageElement(int height, int width, String source) {

        Element image = svgDocument.createElementNS(WhiteboardUtils.svgNS, "image");
        System.out.println(source);

        image.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", source);
        image.setAttributeNS(null, "x", Integer.toString(50));
        image.setAttributeNS(null, "y", Integer.toString(50)); //, dialog, dialog)
        image.setAttributeNS(null, "height", Integer.toString(height));
        image.setAttributeNS(null, "width", Integer.toString(width));

        String idString = whiteboardWindow.getCellUID(whiteboardWindow.getApp()) + System.currentTimeMillis();
        image.setAttributeNS(null, "id", idString);

        return image;
    }

    public Element createEllipseElement(Point start, Point end, boolean filled) {
        //Create appropriate Rectangle from points
        Rectangle rect = WhiteboardUtils.constructRectObject(start, end);
        double radiusX = (rect.getWidth() / 2);
        double radiusY = (rect.getHeight() / 2);
        int centreX = (int) (rect.getX() + radiusX);
        int centreY = (int) (rect.getY() + radiusY);

        // Create the ellipse element
        Element ellipse = svgDocument.createElementNS(WhiteboardUtils.svgNS, "ellipse");
        ellipse.setAttributeNS(null, "cx", Integer.valueOf(centreX).toString());
        ellipse.setAttributeNS(null, "cy", Integer.valueOf(centreY).toString());
        ellipse.setAttributeNS(null, "rx", new Double(radiusX).toString());
        ellipse.setAttributeNS(null, "ry", new Double(radiusY).toString());
        ellipse.setAttributeNS(null, "stroke", WhiteboardUtils.constructRGBString(whiteboardWindow.getCurrentColor()));
        ellipse.setAttributeNS(null, "stroke-width", Float.toString(whiteboardWindow.getStrokeWeight()));
        ellipse.setAttributeNS(null, "fill", WhiteboardUtils.constructRGBString(whiteboardWindow.getCurrentColor()));
        if (!filled) {
            ellipse.setAttributeNS(null, "fill-opacity", "0");
        }

        String idString = whiteboardWindow.getCellUID(whiteboardWindow.getApp()) + System.currentTimeMillis();
        ellipse.setAttributeNS(null, "id", idString);

        return ellipse;
    }

    public WhiteboardWindow getWindow() {
        return this.whiteboardWindow;
    }

    public Element createTextElement(Point end) {
        TextGetter getter = new TextGetter(end);
        new Thread(getter).start();

        return null;
    }

    public Element createTextElement(Point end, String text) {
        // Create the text element
        Element textElement = svgDocument.createElementNS(WhiteboardUtils.svgNS, "text");
        textElement.setAttributeNS(null, "x", Integer.valueOf(end.x).toString());
        textElement.setAttributeNS(null, "y", Integer.valueOf(end.y).toString());
        textElement.setAttributeNS(null, "fill", WhiteboardUtils.constructRGBString(whiteboardWindow.getCurrentColor()));
        textElement.setAttributeNS(null, "font-size", String.valueOf(TEXT_FONT_SIZE));

        /**
         * This is the first of two passes. This pass tokenizes the text by
         * spaces.
         */
        String[] tokens = text.split("\\s");
        int lineNumbers = 1;
        String line = new String();
        for (String token : tokens) {
            //total length + space + token length
            if ((line.length() + 1 + token.length()) >= (TEXT_MARGIN * lineNumbers)) {
                line += "\n" + token;

                lineNumbers += 1;
            } else {
                line += " " + token;
            }
        }

        /**
         * This is the second of two passes. This pass tokenizes the text by
         * newlines.
         */
        tokens = line.split("\n");
        for (String token : tokens) {
            textElement.appendChild(addTSpan(end, token.trim()));
        }

        String idString = whiteboardWindow.getCellUID(whiteboardWindow.getApp()) + System.currentTimeMillis();
        textElement.setAttributeNS(null, "id", idString);

        return textElement;
    }

    /**
     * In order to achieve the illusion of word wrap in SVG, we'll need to
     * transform the create text element
     *
     * @param end where the text was dropped. It acts as the x attribute
     * @param stringlet a small piece of the original String
     */
    public Element addTSpan(Point end, String stringlet) {
        Element tspanElement = svgDocument.createElementNS(WhiteboardUtils.svgNS, "tspan");
        tspanElement.setAttributeNS(null, "x", Integer.valueOf(end.x).toString());
        tspanElement.setAttributeNS(null, "dy", String.valueOf(40));
        Text text = svgDocument.createTextNode(stringlet);
        tspanElement.appendChild(text);
        return tspanElement;
        //return WhiteboardUtils.elementToXMLString(tspanElement);
    }

    public Element moveElement(Element toMove) {
        int xDiff = (int) (whiteboardWindow.getCurrentPoint().getX() - whiteboardWindow.getPressedPoint().getX());
        int yDiff = (int) (whiteboardWindow.getCurrentPoint().getY() - whiteboardWindow.getPressedPoint().getY());
        return moveElement(toMove, xDiff, yDiff);
    }

    public Element moveElement(Element toMove, int xDiff, int yDiff) {
        Element afterMove = (Element) toMove.cloneNode(true);
        if (afterMove.getTagName().equals("line")) {
            int x1 = Integer.parseInt(afterMove.getAttributeNS(null, "x1"));
            int y1 = Integer.parseInt(afterMove.getAttributeNS(null, "y1"));
            int x2 = Integer.parseInt(afterMove.getAttributeNS(null, "x2"));
            int y2 = Integer.parseInt(afterMove.getAttributeNS(null, "y2"));

            afterMove.setAttributeNS(null, "x1", Integer.toString(x1 + xDiff));
            afterMove.setAttributeNS(null, "y1", Integer.toString(y1 + yDiff));
            afterMove.setAttributeNS(null, "x2", Integer.toString(x2 + xDiff));
            afterMove.setAttributeNS(null, "y2", Integer.toString(y2 + yDiff));
        } else if (afterMove.getTagName().equals("rect")) {
            int x = Integer.parseInt(afterMove.getAttributeNS(null, "x"));
            int y = Integer.parseInt(afterMove.getAttributeNS(null, "y"));

            afterMove.setAttributeNS(null, "x", Integer.toString(x + xDiff));
            afterMove.setAttributeNS(null, "y", Integer.toString(y + yDiff));


        } else if (afterMove.getTagName().equals("ellipse")) {
            int cx = Integer.parseInt(afterMove.getAttributeNS(null, "cx"));
            int cy = Integer.parseInt(afterMove.getAttributeNS(null, "cy"));

            afterMove.setAttributeNS(null, "cx", Integer.toString(cx + xDiff));
            afterMove.setAttributeNS(null, "cy", Integer.toString(cy + yDiff));
        } else if (afterMove.getTagName().equals("text")) {
            int x = Integer.parseInt(afterMove.getAttributeNS(null, "x"));
            int y = Integer.parseInt(afterMove.getAttributeNS(null, "y"));



            afterMove.setAttributeNS(null, "x", Integer.toString(x + xDiff));
            afterMove.setAttributeNS(null, "y", Integer.toString(y + yDiff));

            //edit x-position on document when text is moved and dragged.
            NodeList elements = afterMove.getElementsByTagName("tspan");
            for (int i = 0; i < elements.getLength(); i++) {
                if (elements.item(i) instanceof Element) {
                    Element e = (Element) elements.item(i);
                    e.setAttributeNS(null, "x", Integer.toString(x + xDiff));

                }
            }
        } else if (afterMove.getTagName().equals("image")) {
            int x = Integer.parseInt(afterMove.getAttributeNS(null, "x"));
            int y = Integer.parseInt(afterMove.getAttributeNS(null, "y"));

            afterMove.setAttributeNS(null, "x", Integer.toString(x + xDiff));
            afterMove.setAttributeNS(null, "y", Integer.toString(y + yDiff));
        }

        return afterMove;
    }

    /**
     * Loads an SVG document
     *
     * @param uri the URI of the SVG document to load
     * @param notify whether to notify other clients
     */
    public void openDocument(String uri, boolean notify) {
        if ((uri == null) || (uri.length() == 0)) {
            return;
        }

        new DocumentLoader(uri).start();

        if (whiteboardWindow.isSynced() && (notify == true)) {
            // notify other clients
            whiteboardWindow.sendRequest(Action.OPEN_DOCUMENT, null, uri, null, null);
        }
    }

    /**
     * Loads an SVG document
     *
     * @param uri the URI of the SVG document to load
     */
    public void openDocument(String uri) {
        openDocument(uri, false);
    }

    public void showSVGDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                svgDocumentDialog = new DocumentDialog(null, false);
                svgDocumentDialog.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        svgDocumentDialog.setVisible(false);
                        if (evt.getActionCommand().equals("OK")) {
                            openDocument(svgDocumentDialog.getDocumentURL(), true);
                        }
                        svgDocumentDialog = null;
                    }
                });
                svgDocumentDialog.setVisible(true);
            }
        });
    }

    private void setSVGDialogDocumentURL(String docURI) {
        if (svgDocumentDialog != null) {
            svgDocumentDialog.setDocumentURL(docURI);
        }
    }

    /**
     * DocumentLoaderListener methods
     */
    /**
     * Called when the loading of a document was started.
     */
    public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
        LOGGER.fine("whiteboard: document loading started: " + e);
        String message = BUNDLE.getString("Opening");
        message = MessageFormat.format(message, docURI);
        whiteboardWindow.showHUDMessage(message);
        setSVGDialogDocumentURL(docURI);
        then = new Date();
    }

    /**
     * Called when the loading of a document was completed.
     */
    public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
        LOGGER.fine("whiteboard: document loading completed: " + e);
        now = new Date();
        LOGGER.info("SVG loaded in: " + (now.getTime() - then.getTime()) / 1000 + " seconds");
        whiteboardWindow.hideHUDMessage(false);
    }

    /**
     * Called when the loading of a document was cancelled.
     */
    public void documentLoadingCancelled(SVGDocumentLoaderEvent e) {
        LOGGER.fine("whiteboard: document loading cancelled: " + e);
    }

    /**
     * Called when the loading of a document has failed.
     */
    public void documentLoadingFailed(SVGDocumentLoaderEvent e) {
        LOGGER.fine("whiteboard: document loading failed: " + e);
    }

    public Element importNode(Element importedNode, boolean deep) {
        Element element = null;

        if (svgDocument != null) {
            // because it may not yet have been received from the server
            element = (Element) svgDocument.importNode(importedNode, deep);
        }

        return element;
    }

    public Node appendChild(Element e) {
        return getDocumentElement().appendChild(e);
    }

    public void removeChild(Element rem) {
        getDocumentElement().removeChild(rem);
    }

    public Element getElementById(String attributeNS) {
        return svgDocument.getElementById(attributeNS);
    }

    public void replaceChild(Element afterMove, Element elementById) {
        getDocumentElement().replaceChild(afterMove, elementById);

    }

    public void setSVGDocument(SVGDocument svgDocument) {
        this.svgDocument = svgDocument;
    }

    public SVGDocument getSVGDocument() {
        return svgDocument;
    }

    private class PictureTaker implements Runnable {

        public void run() {
            if (pictureComponent == null) {
                // create a HUD text dialog
                //dialo = new HUDDialogComponent(whiteboardWindow.getCell());
                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                mainHUD.addComponent(pictureComponent);
                // pictureComponent = mainHUD.createComponent(panel);


//                dialog.setMessage(BUNDLE.getString("Enter_text"));
                //              dialog.setType(MESSAGE_TYPE.QUERY);
                pictureComponent.setPreferredLocation(Layout.CENTER);
                pictureComponent.setWorldLocation(new Vector3f(0.0f, 0.0f, 0.5f));

                // add the text dialog to the HUD

            }

            pictureComponent.setVisible(whiteboardWindow.getDisplayMode() == DisplayMode.HUD);
            pictureComponent.setWorldVisible(whiteboardWindow.getDisplayMode() != DisplayMode.HUD);

            String urlString = panel.getPicture();
            if (urlString.contains("photos")) {
                int width = 0;//this.getWindow().getWidth();
                if (width > 1200) {
                    //        element = createImageElement(150, 250, urlString);
                } else {
                    //element = createImageElement(300, 500, urlString);
                }
            } else { //must be an image instead
                try {
                    ImageIcon icon = new ImageIcon(new URL(urlString));
                    System.out.println("Icon Height: " + icon.getIconHeight()
                            + "\nIcon Width: " + icon.getIconWidth());

                    //element = createImageElement(icon.getIconHeight(),
                    //                   icon.getIconWidth(),
                    //                   urlString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private class TextGetter implements Runnable {

        private Point position;

        public TextGetter(Point position) {
            this.position = position;
        }

        public void run() {
            if (dialog == null) {
                // create a HUD text dialog
                dialog = new HUDDialogComponent(whiteboardWindow.getCell());
                dialog.setMessage(BUNDLE.getString("Enter_text"));
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
                                LOGGER.info("creating text element: " + value + " at " + position);
                                Element e = createTextElement(position, value);
                                whiteboardWindow.addNewElement(e, true);
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
           
            Window2D window = dialog.getWindow();
            App2D app = window.getApp();
            Entity e = app.getFocusEntity();
            
            WindowSwing swingWindow = (WindowSwing)window;
//            swingWindow.getEmbeddedPeer();
            LOGGER.warning("SETTING FOCUS TO DIALOG ENTITY!");
            InputManager.inputManager().addKeyMouseFocus(e);
            dialog.setFocused(true);
            
            HUDManagerFactory.getHUDManager().getHUD("");
//            WonderlandHUDComponentFactory
                   
        }
    };
}
