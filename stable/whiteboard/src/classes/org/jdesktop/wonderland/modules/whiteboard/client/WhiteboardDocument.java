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
package org.jdesktop.wonderland.modules.whiteboard.client;

import com.jme.math.Vector3f;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDDialog.MESSAGE_TYPE;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;
import org.jdesktop.wonderland.modules.hud.client.HUDDialogComponent;
import org.jdesktop.wonderland.modules.whiteboard.client.WhiteboardToolManager.WhiteboardTool;
import org.jdesktop.wonderland.modules.whiteboard.common.cell.WhiteboardCellMessage.Action;
import org.jdesktop.wonderland.modules.whiteboard.common.WhiteboardUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

/**
 * Wraps the SVG document
 * @author Bernard Horan
 */
public class WhiteboardDocument implements SVGDocumentLoaderListener {

    private static final Logger LOGGER =
            Logger.getLogger(WhiteboardDocument.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/whiteboard/client/resources/Bundle");
    private static final int TEXT_FONT_SIZE = 30;
    private WhiteboardWindow whiteboardWindow;
    private Date now;
    private Date then;
    private String docURI;
    private SVGDocument svgDocument;
    private DocumentDialog svgDocumentDialog;
    private HUDDialogComponent dialog;
    protected static final Object readyLock = new Object();

    /**
     * A class for handling the loading of SVG documents. This can be time
     * consuming, so load in a thread
     */
    private class DocumentLoader extends Thread {

        private String uri = null;

        public DocumentLoader(String uri) {
            this.uri = uri;
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
                break;
            case RECT:
                element = createRectElement(pressedPoint, releasedPoint, whiteboardWindow.getToolManager().isFilled());
                break;
            case ELLIPSE:
                element = createEllipseElement(pressedPoint, releasedPoint, whiteboardWindow.getToolManager().isFilled());
                break;
            case TEXT:
                element = createTextElement(releasedPoint);
                break;
            default:
                break;
        }

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
        }
    };

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
        textElement.setTextContent(text);

        String idString = whiteboardWindow.getCellUID(whiteboardWindow.getApp()) + System.currentTimeMillis();
        textElement.setAttributeNS(null, "id", idString);

        return textElement;
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
        }

        return afterMove;
    }

    /**
     * Loads an SVG document
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
     *  DocumentLoaderListener methods
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
}
