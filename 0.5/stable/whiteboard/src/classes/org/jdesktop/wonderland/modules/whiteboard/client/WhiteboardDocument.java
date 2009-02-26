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
package org.jdesktop.wonderland.modules.whiteboard.client;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import org.jdesktop.wonderland.modules.hud.client.HUDComponent2D;
import org.jdesktop.wonderland.modules.hud.client.HUDInputDialog;
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

    private static final Logger logger =
            Logger.getLogger(WhiteboardDocument.class.getName());
    private static final int TEXT_FONT_SIZE = 30;
    private WhiteboardWindow whiteboardWindow;
    private Date now;
    private Date then;
    private String docURI;
    private SVGDocument svgDocument;
    private DocumentDialog svgDocumentDialog;
    private HUDInputDialog dialog;
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
                svgDocument = (SVGDocument) WhiteboardUtils.openDocument(uri);
                svgDocumentDialog.setDocumentURL(uri);
            }
        }
    }

    public WhiteboardDocument(WhiteboardWindow whiteboardWindow) {
        this.whiteboardWindow = whiteboardWindow;
        initSVGDialog();
    }

    private Element getDocumentElement() {
        return svgDocument.getDocumentElement();
    }

    public Element createElement(WhiteboardTool currentTool, Point pressedPoint, Point releasedPoint) {
        switch (currentTool) {
            case LINE:
                return this.createLineElement(pressedPoint, releasedPoint, whiteboardWindow.getCurrentColor(), whiteboardWindow.getStrokeWeight());
            case RECT:
                return this.createRectElement(pressedPoint, releasedPoint, whiteboardWindow.getToolManager().isFilled());
            case ELLIPSE:
                return this.createEllipseElement(pressedPoint, releasedPoint, whiteboardWindow.getToolManager().isFilled());
            case TEXT:
                return this.createTextElement(releasedPoint);
        }
        return null;
    }

    public Element createLineElement(Point start, Point end, Color lineColor, Float strokeWeight) {
        //Create the line element
        Element line = svgDocument.createElementNS(WhiteboardUtils.svgNS, "line");
        line.setAttributeNS(null, "x1", new Integer(start.x).toString());
        line.setAttributeNS(null, "y1", new Integer(start.y).toString());
        line.setAttributeNS(null, "x2", new Integer(end.x).toString());
        line.setAttributeNS(null, "y2", new Integer(end.y).toString());
        line.setAttributeNS(null, "stroke", WhiteboardUtils.constructRGBString(lineColor));
        line.setAttributeNS(null, "stroke-width", Float.toString(strokeWeight));

        String idString = whiteboardWindow.getCellUID(whiteboardWindow.getApp()) + System.currentTimeMillis();
        line.setAttributeNS(null, "id", idString);
        logger.fine("whiteboard: created line: " + line);
        return line;
    }

    public Element createRectElement(Point start, Point end, boolean filled) {
        //Create appropriate Rectangle from points
        Rectangle rect = WhiteboardUtils.constructRectObject(start, end);

        // Create the rectangle element
        Element rectangle = svgDocument.createElementNS(WhiteboardUtils.svgNS, "rect");
        rectangle.setAttributeNS(null, "x", new Integer(rect.x).toString());
        rectangle.setAttributeNS(null, "y", new Integer(rect.y).toString());
        rectangle.setAttributeNS(null, "width", new Integer(rect.width).toString());
        rectangle.setAttributeNS(null, "height", new Integer(rect.height).toString());
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
        ellipse.setAttributeNS(null, "cx", new Integer(centreX).toString());
        ellipse.setAttributeNS(null, "cy", new Integer(centreY).toString());
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

                dialog = new HUDInputDialog(whiteboardWindow.getApp());
                dialog.setLabelText("Enter the text to add to the whiteboard:");
            }
            PropertyChangeListener plistener = new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent pe) {
                    Element e = createTextElement(position, (String) pe.getNewValue());
                    whiteboardWindow.addNewElement(e, true);
                    dialog.setVisible(false);
                    dialog.removePropertyChangeListener(this);
                }
            };
            dialog.addPropertyChangeListener(plistener);
            dialog.setValueText("");
            dialog.setVisible(true);
        }
    };

    public Element createTextElement(Point end) {
        TextGetter getter = new TextGetter(end);
        try {
            SwingUtilities.invokeAndWait(getter);
        } catch (Exception e) {
        }
        return null;
    }

    public Element createTextElement(Point end, String text) {
        // Create the text element
        Element textElement = svgDocument.createElementNS(WhiteboardUtils.svgNS, "text");
        textElement.setAttributeNS(null, "x", new Integer(end.x).toString());
        textElement.setAttributeNS(null, "y", new Integer(end.y).toString());
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
                svgDocumentDialog.setVisible(true);
            }
        });
    }

    private void initSVGDialog() {
        svgDocumentDialog = new DocumentDialog(null, false);
        svgDocumentDialog.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideSVGDialog();
                if (evt.getActionCommand().equals("OK")) {
                    openDocument(svgDocumentDialog.getDocumentURL(), true);
                }
            }
        });
    }

    private void hideSVGDialog() {
        if (svgDocumentDialog != null) {
            svgDocumentDialog.setVisible(false);
        }
    }

    private void setSVGDialogDocumentURL(String docURI) {
        svgDocumentDialog.setDocumentURL(docURI);
    }

    /**
     *  DocumentLoaderListener methods
     */
    /**
     * Called when the loading of a document was started.
     */
    public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
        logger.fine("whiteboard: document loading started: " + e);
        whiteboardWindow.showHUDMessage("opening: " + docURI);
        setSVGDialogDocumentURL(docURI);
        then = new Date();
    }

    /**
     * Called when the loading of a document was completed.
     */
    public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
        logger.fine("whiteboard: document loading completed: " + e);
        now = new Date();
        logger.info("SVG loaded in: " + (now.getTime() - then.getTime()) / 1000 + " seconds");
        whiteboardWindow.hideHUDMessage(false);
    }

    /**
     * Called when the loading of a document was cancelled.
     */
    public void documentLoadingCancelled(SVGDocumentLoaderEvent e) {
        logger.fine("whiteboard: document loading cancelled: " + e);
    }

    /**
     * Called when the loading of a document has failed.
     */
    public void documentLoadingFailed(SVGDocumentLoaderEvent e) {
        logger.fine("whiteboard: document loading failed: " + e);
    }

    public Element importNode(Element importedNode, boolean deep) {
        if (svgDocument != null) {//Because it may not yet have been received from the server
            return (Element) svgDocument.importNode(importedNode, deep);
        } else {
            return null;
        }
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
}
