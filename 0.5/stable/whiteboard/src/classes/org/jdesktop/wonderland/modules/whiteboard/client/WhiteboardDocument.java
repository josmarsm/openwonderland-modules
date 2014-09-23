/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.whiteboard.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
import org.jdesktop.wonderland.client.hud.CompassLayout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.jme.dnd.FileListDataFlavorHandler;
import org.jdesktop.wonderland.client.jme.dnd.spi.DataFlavorHandlerSPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.modules.hud.client.HUDDialogComponent;
import org.jdesktop.wonderland.modules.whiteboard.client.WhiteboardToolManager.WhiteboardTool;
import static org.jdesktop.wonderland.modules.whiteboard.client.WhiteboardToolManager.WhiteboardTool.BACKGROUND_IMAGE;
import static org.jdesktop.wonderland.modules.whiteboard.client.WhiteboardToolManager.WhiteboardTool.IMAGE;
import org.jdesktop.wonderland.modules.whiteboard.common.cell.WhiteboardCellMessage.Action;
import org.jdesktop.wonderland.modules.whiteboard.common.WhiteboardUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

/**
 * Wraps the SVG document
 *
 * @author Bernard Horan
 * @author Abhishek Upadhyay
 */
public class WhiteboardDocument implements SVGDocumentLoaderListener {

    private static final Logger LOGGER
            = Logger.getLogger(WhiteboardDocument.class.getName());
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
    private HUDComponent fileChooserHUD;

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
            case RECT_FILL:
                element = createRectElement(pressedPoint, releasedPoint, whiteboardWindow.getToolManager().isFilled());
                break;
            case ELLIPSE:
            case ELLIPSE_FILL:
                element = createEllipseElement(pressedPoint, releasedPoint, whiteboardWindow.getToolManager().isFilled());
                break;
            case BACKGROUND_IMAGE:
                element = createImageElement();
                break;
            case IMAGE:
                element = createImageElement();
                break;
            case NEW_TEXT:
                element = createNewTextElement(releasedPoint);
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
        line.setAttributeNS(null, "stroke", whiteboardWindow.getToolManager().getGlobalColor());
        line.setAttributeNS(null, "stroke-width", Float.toString(strokeWeight));
        line.setAttributeNS(null, "name", start.x + "," + start.y + "," + end.x + "," + end.y + ",0");
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
        rectangle.setAttributeNS(null, "stroke", whiteboardWindow.getToolManager().getGlobalColor());
        rectangle.setAttributeNS(null, "stroke-width", Float.toString(whiteboardWindow.getStrokeWeight()));
        rectangle.setAttributeNS(null, "fill", whiteboardWindow.getToolManager().getGlobalColor());
        rectangle.setAttributeNS(null, "name", Integer.valueOf(rect.width) + "x" + Integer.valueOf(rect.height));

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
        ellipse.setAttributeNS(null, "stroke", whiteboardWindow.getToolManager().getGlobalColor());
        ellipse.setAttributeNS(null, "stroke-width", Float.toString(whiteboardWindow.getStrokeWeight()));
        ellipse.setAttributeNS(null, "fill", whiteboardWindow.getToolManager().getGlobalColor());
        ellipse.setAttributeNS(null, "name", new Double(radiusX).toString() + "x" + new Double(radiusY).toString());
        if (!filled) {
            ellipse.setAttributeNS(null, "fill-opacity", "0");
        }

        String idString = whiteboardWindow.getCellUID(whiteboardWindow.getApp()) + System.currentTimeMillis();
        ellipse.setAttributeNS(null, "id", idString);

        return ellipse;
    }

    public Element createNewTextElement(Point end) {
        //display(svgDocument.getDocumentElement(), 0);
        Element newTextElement = null;
        try {

            //check if we click on existing text element or not
            Element selectedElement = whiteboardWindow.getSelectedTextElement(end);
            if (selectedElement != null && !selectedElement.getTagName().equals("text")) {
                selectedElement = null;
            }
            int x = end.x;
            int y = end.y;
            whiteboardWindow.setInitialCursorPos(0);
            if (selectedElement == null) {
                //create a new one
                newTextElement = svgDocument.createElementNS(WhiteboardUtils.svgNS, "text");
                newTextElement.setAttributeNS(null, "x", Integer.valueOf(end.x).toString());
                newTextElement.setAttributeNS(null, "y", Integer.valueOf(end.y).toString());
                newTextElement.setAttributeNS(null, "font-size", whiteboardWindow.getToolManager().getFontSize() + "px");
                newTextElement.setAttributeNS(null, "font-family", whiteboardWindow.getToolManager().getFontName());
                newTextElement.setAttributeNS(null, "fill", whiteboardWindow.getToolManager().getFontColor());
                newTextElement.setAttributeNS(null, "font-style", whiteboardWindow.getToolManager().getFontStyle());
                newTextElement.setAttributeNS(null, "font-weight", whiteboardWindow.getToolManager().getFontWeight());
                newTextElement.setAttributeNS(null, "text-decoration", "underline");
                newTextElement.setAttributeNS(null, "name", whiteboardWindow.getToolManager().getFontSize());
                newTextElement.setTextContent("");

                String idString = whiteboardWindow.getCellUID(whiteboardWindow.getApp()) + System.currentTimeMillis();
                newTextElement.setAttributeNS(null, "id", idString);
                whiteboardWindow.setCurrentTextElement(newTextElement);
                whiteboardWindow.addNewTextElement(newTextElement, false);
                whiteboardWindow.setTextExist(false);
            } else {
                //there is already a text element at this location
                whiteboardWindow.setCurrentTextElement(selectedElement);
                x = Integer.parseInt(selectedElement.getAttribute("x"));
                y = Integer.parseInt(selectedElement.getAttribute("y"));
                whiteboardWindow.setTextExist(true);
            }

            //decide the cursor position
            String fontName = whiteboardWindow.getCurrentTextElement().getAttribute("font-family");
            String fontSize = whiteboardWindow.getCurrentTextElement().getAttribute("font-size");
            if (fontSize.contains("px")) {
                fontSize = fontSize.substring(0, fontSize.length() - 2);
            }
            if (whiteboardWindow.getCurrentTextElement().getTextContent() != null
                    && !whiteboardWindow.getCurrentTextElement().getTextContent().equals("")) {
                //calculate text width
                Font tempFont = new Font(fontName, Font.PLAIN, Integer.parseInt(fontSize));
                FontMetrics metrics = whiteboardWindow.getSurface().getGraphics().getFontMetrics(tempFont);
                char[] text = selectedElement.getTextContent().toCharArray();
                StringBuilder tempString = new StringBuilder();
                int stringW = 0;
                for (int i = 0; i < text.length; i++) {
                    int prevW = stringW;
                    stringW = (int) (metrics.stringWidth(tempString.append(text[i]).toString())) + (int) ((tempString.length() - 1) * 0.5);
                    int newX = x + stringW;
                    if (newX > end.x) {
                        x = x + prevW;
                        whiteboardWindow.setInitialCursorPos(i);
                        break;
                    }
                }
            }

            //create cursor
            Element line = svgDocument.getElementById("cursor");
            Element line1 = svgDocument.getElementById("cursor1");
            if (line == null) {
                line = svgDocument.createElementNS(WhiteboardUtils.svgNS, "line");
                line.setAttributeNS(null, "x1", Integer.valueOf(x).toString());
                line.setAttributeNS(null, "y1", Integer.valueOf(y - Integer.parseInt(fontSize)).toString());
                line.setAttributeNS(null, "x2", Integer.valueOf(x).toString());
                line.setAttributeNS(null, "y2", Integer.valueOf(y).toString());
                line.setAttributeNS(null, "stroke", "#000000");
                line.setAttributeNS(null, "stroke-width", Float.toString(3));
                line.setAttributeNS(null, "id", "cursor");
                line1 = (Element) line.cloneNode(true);
                line1.setAttributeNS(null, "id", "cursor1");
                line1.setAttributeNS(null, "stroke", "#FFFFFF");
                line1.setAttributeNS(null, "stroke-width", Float.toString(4));
                whiteboardWindow.addNewElement(line1, false);
                whiteboardWindow.addNewElement(line, false);
            } else {
                Element nLine = (Element) line.cloneNode(true);
                nLine.setAttributeNS(null, "x1", Integer.valueOf(x).toString());
                nLine.setAttributeNS(null, "y1", Integer.valueOf(y - Integer.parseInt(fontSize)).toString());
                nLine.setAttributeNS(null, "x2", Integer.valueOf(x).toString());
                nLine.setAttributeNS(null, "y2", Integer.valueOf(y).toString());
                Element nLine1 = (Element) line1.cloneNode(true);
                nLine1.setAttributeNS(null, "x1", Integer.valueOf(x).toString());
                nLine1.setAttributeNS(null, "y1", Integer.valueOf(y - Integer.parseInt(fontSize)).toString());
                nLine1.setAttributeNS(null, "x2", Integer.valueOf(x).toString());
                nLine1.setAttributeNS(null, "y2", Integer.valueOf(y).toString());
                whiteboardWindow.updateElement(nLine1, false);
                whiteboardWindow.updateElement(nLine, false);
            }
            whiteboardWindow.setNewText(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * choose & upload image for background & mavable images
     */
    private class ImageGetter implements Runnable {

        public void run() {
            try {

                // create a HUD filechooser
                final HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                final JFileChooser fileChooser = new JFileChooser();
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileFilter(new ImageFilter());
                fileChooserHUD = mainHUD.createComponent(fileChooser);
                //display(svgDocument,0);
                fileChooserHUD.setName("Select image file");
                fileChooserHUD.setPreferredLocation(CompassLayout.Layout.NORTH);
                System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
                mainHUD.addComponent(fileChooserHUD);

                fileChooserHUD.setVisible(true);

                fileChooserHUD.addEventListener(new HUDEventListener() {
                    public void HUDObjectChanged(HUDEvent event) {
                        if (event.getEventType().equals(HUDEvent.HUDEventType.DISAPPEARED)
                                || event.getEventType().equals(HUDEvent.HUDEventType.CLOSED)) {
                            fileChooserHUD.setVisible(false);
                        }
                    }
                });
                fileChooser.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (e.getActionCommand().equals("CancelSelection")) {
                            fileChooserHUD.setVisible(false);
                        } else {
                            ArrayList<File> files = new ArrayList<File>();
                            files.add(fileChooser.getSelectedFile());
                            final ActionListener al = this;
                            FileListDataFlavorHandler.importFile(files, false, new DataFlavorHandlerSPI.ImportResultListener() {
                                public void importSuccess(String uri) {
                                    if (whiteboardWindow.getCurrentTool().equals(IMAGE)) {
                                        uri = uri.split("wlcontent://")[1];
                                        String serverURL = LoginManager.getPrimary().getServerURL();
                                        uri = serverURL + "webdav/content/" + uri;
                                        uri = uri.replaceAll(" ", "%20");
                                        System.out.println("File successfully uploaded !! " + uri);
                                        final Element elt = createImageElementOrg(uri.replaceAll(" ", "%20"));
                                        elt.setAttribute("name", elt.getAttribute("width") + "x" + elt.getAttribute("height"));
                                        whiteboardWindow.addNewElement(elt, true);
                                        whiteboardWindow.getToolManager().selector();
                                    } else {
                                        uri = uri.split("wlcontent://")[1];
                                        String serverURL = LoginManager.getPrimary().getServerURL();
                                        uri = serverURL + "webdav/content/" + uri;
                                        uri = uri.replaceAll(" ", "%20");
                                        System.out.println("File successfully uploaded !! " + uri);
                                        final Element oldElt = getElementById("bImage");
                                        final Element elt = createImageElementOrg(uri.replaceAll(" ", "%20"));
                                        if (oldElt == null) {
                                            whiteboardWindow.addBackgroundImage(elt, true, true);
                                        } else {
                                            whiteboardWindow.updateBackgroundImage(elt, true);
                                        }
                                        whiteboardWindow.getToolManager().selector();
                                    }
                                }

                                public void importFailure(Throwable cause, String message) {
                                    System.out.println("Unable to upload the file !! " + message);
                                    cause.printStackTrace();
                                }
                            });
                            fileChooserHUD.setVisible(false);
                            mainHUD.removeComponent(fileChooserHUD);
                        }
                    }
                });
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
    };

    public Element createImageElement() {
        ImageGetter big = new ImageGetter();
        new Thread(big).start();
        return null;
    }

    public Element createImageElementOrg(String uri) {
        try {
            // Create the image element
            Element bImage = svgDocument.createElementNS(WhiteboardUtils.svgNS, "image");
            bImage.setAttributeNS(null, "x", "0");
            bImage.setAttributeNS(null, "y", "0");
            BufferedImage image = ImageIO.read(new URL(uri));
            int w = image.getWidth();
            int h = image.getHeight();
            int ww = 800;
            int wh = 600;
            int wdiff = ww - w;
            int hdiff = wh - h;
            if (wdiff < 0 && hdiff < 0) {
                if (Math.abs(wdiff) > Math.abs(hdiff)) {
                    h = (ww * h) / w;
                    w = ww;
                } else {
                    w = (wh * w) / h;
                    h = wh;
                }
            } else if (wdiff < 0) {
                h = (ww * h) / w;
                w = ww;
            } else if (hdiff < 0) {
                w = (wh * w) / h;
                h = wh;
            }

            bImage.setAttributeNS(null, "x", String.valueOf(0));
            bImage.setAttributeNS(null, "y", String.valueOf(0));
            bImage.setAttributeNS(null, "width", String.valueOf(w));
            bImage.setAttributeNS(null, "height", String.valueOf(h));
            bImage.setAttribute("xml:space", "preserve");
            bImage.setAttributeNS(WhiteboardUtils.xlinkNS, "xlink:href", uri);
            String idString = "";
            if (whiteboardWindow.getCurrentTool().equals(IMAGE)) {
                idString = whiteboardWindow.getCellUID(whiteboardWindow.getApp()) + System.currentTimeMillis();
            } else {
                idString = "bImage";
            }
            bImage.setAttributeNS(null, "id", idString);

            return bImage;
        } catch (Exception ex) {
            Logger.getLogger(WhiteboardDocument.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

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
            String[] name = afterMove.getAttribute("name").split(",");

            int newoX1 = Integer.parseInt(afterMove.getAttribute("x1"));
            int newoY1 = Integer.parseInt(afterMove.getAttribute("y1"));
            int newoX2 = Integer.parseInt(afterMove.getAttribute("x2"));
            int newoY2 = Integer.parseInt(afterMove.getAttribute("y2"));
            int percDiff = -Integer.parseInt(name[4]);
            int oldX1 = Integer.parseInt(name[0]);
            int oldY1 = Integer.parseInt(name[1]);
            int oldX2 = Integer.parseInt(name[2]);
            int oldY2 = Integer.parseInt(name[3]);

            double m = ((newoY2 - newoY1) / (newoX2 - newoX1));
            double c = newoY1 - m * newoX1;

            double xDist = (oldX2 - oldX1);
            double xDistDiff = percDiff * xDist / 200;
            double yDist = (oldY2 - oldY1);
            double yDistDiff = percDiff * yDist / 200;

            double newX1 = 0;
            double newY1 = 0;
            double newX2 = 0;
            double newY2 = 0;
            if (m != Double.POSITIVE_INFINITY) {
                newX1 = oldX1 - xDistDiff;
                newY1 = m * newX1 + c;
                newX2 = oldX2 + xDistDiff;
                newY2 = m * newX2 + c;
            } else {
                newX1 = oldX1;
                newY1 = oldY1 - yDistDiff;
                newX2 = oldX2;
                newY2 = oldY2 + yDistDiff;
            }

            afterMove.setAttributeNS(null, "name", afterMove.getAttribute("x1") + ","
                    + afterMove.getAttribute("y1") + ","
                    + afterMove.getAttribute("x2") + ","
                    + afterMove.getAttribute("y2") + "," + name[4]);

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
        } else if (afterMove.getTagName().equals("image")) {
            int x = Integer.parseInt(afterMove.getAttributeNS(null, "x"));
            int y = Integer.parseInt(afterMove.getAttributeNS(null, "y"));

            if (afterMove.getAttribute("id").equals("bImage")) {
                afterMove.setAttributeNS(null, "x", "0");
                afterMove.setAttributeNS(null, "y", "0");
            } else {
                afterMove.setAttributeNS(null, "x", Integer.toString(x + xDiff));
                afterMove.setAttributeNS(null, "y", Integer.toString(y + yDiff));
            }
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

    int level = 0;
    public void display(Node e, int l) {
        NodeList nodeList = e.getChildNodes();
        System.out.println("n : " + e + " | " + nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i);
            display(n, l++);
        }
    }
}
