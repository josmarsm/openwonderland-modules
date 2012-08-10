/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
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
package org.jdesktop.wonderland.modules.mediaboard.client;

import org.jdesktop.wonderland.modules.mediaboard.client.cell.WhiteboardCell;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.Overlay;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.hud.HUDMessage;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import org.jdesktop.wonderland.modules.mediaboard.client.WhiteboardToolManager.WhiteboardColor;
import org.jdesktop.wonderland.modules.mediaboard.client.WhiteboardToolManager.WhiteboardTool;
import org.jdesktop.wonderland.modules.mediaboard.common.cell.WhiteboardCellMessage;
import org.jdesktop.wonderland.modules.mediaboard.common.cell.WhiteboardCellMessage.Action;
import org.jdesktop.wonderland.modules.mediaboard.common.WhiteboardUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * An SVG Whiteboard application
 *
 * @author nsimpson
 * @author jbarratt
 */
@ExperimentalAPI
public class WhiteboardWindow extends Window2D {

    private static final Logger LOGGER =
            Logger.getLogger(WhiteboardWindow.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/mediaboard/client/resources/Bundle");
    private WhiteboardDrawingSurface wbSurface;
    private WhiteboardCell cell;
    private WhiteboardToolManager toolManager;
    private WhiteboardDocument whiteboardDocument;
    private WhiteboardComponent commComponent;
    private AffineTransform scaleTransform;
    private float zoom = 1.0f;
    private boolean synced = true;
    private WhiteboardControlPanel controls;
    protected final Object actionLock = new Object();
    // drawing variables
    private float strokeWeight = 3;
    private Overlay drawingOverlay = new DrawingOverlay();
    private Overlay selectionOverlay = new SelectionOverlay();
    private Overlay movingOverlay = new MovingOverlay();
    private final BasicStroke markerStroke = new BasicStroke(strokeWeight,
            BasicStroke.CAP_SQUARE,
            BasicStroke.JOIN_MITER,
            10,
            new float[]{4, 4}, 0);
    private WhiteboardMouseListener svgMouseListener;
    private WhiteboardSelection selection = null;
    private JSVGCanvas svgCanvas;
    // HUD components
    private HUDComponent controlComponent;
    private HUDComponent messageComponent;
    private DisplayMode displayMode;

    private WhiteboardGVTTreeRendererListener whiteboardGVTTreeRendererListener;
    private WhiteboardUpdateManagerListener whiteboardUpdateManagerListener;

    /**
     * Create a new instance of WhiteboardWindow.
     *
     * @param app The whiteboard app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param topLevel Whether the window is top-level (e.g. is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     * @param commComponent The communications component for communicating with the server.
     */
    public WhiteboardWindow(WhiteboardCell cell, App2D app, int width, int height, boolean topLevel, Vector2f pixelScale,
            final WhiteboardComponent commComponent)
            throws InstantiationException {
        super(app, Type.PRIMARY, width, height, topLevel, pixelScale, new WhiteboardDrawingSurface(width, height));
        LOGGER.info("creating whiteboard with size: " + width + "x" + height);
        this.cell = cell;
        this.commComponent = commComponent;
        setTitle("Mediaboard");
        initCanvas(width, height);
        initHUD();
        setDisplayMode(DisplayMode.WORLD);
        showControls(false);
        wbSurface = (WhiteboardDrawingSurface) getSurface();

        whiteboardDocument = new WhiteboardDocument(this);
        addEventListeners();
    }

    private void initCanvas(int width, int height) {
        svgCanvas = new JSVGCanvas();
        svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
        svgCanvas.setSize(width, height);
        svgCanvas.addSVGDocumentLoaderListener(whiteboardDocument);
        whiteboardGVTTreeRendererListener = new WhiteboardGVTTreeRendererListener(this);
        svgCanvas.addGVTTreeRendererListener(whiteboardGVTTreeRendererListener);
        whiteboardUpdateManagerListener = new WhiteboardUpdateManagerListener((WhiteboardApp) this.getApp());
        svgCanvas.addUpdateManagerListener(whiteboardUpdateManagerListener);
    }

    private void initHUD() {
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        messageComponent = mainHUD.createMessage("");
        messageComponent.setPreferredLocation(Layout.NORTHEAST);
        messageComponent.setDecoratable(false);
        mainHUD.addComponent(messageComponent);
    }

    private void addEventListeners() {
        svgMouseListener = new WhiteboardMouseListener(this, whiteboardDocument);
        addMouseWheelListener(svgMouseListener);
        addMouseMotionListener(svgMouseListener);
        addMouseListener(svgMouseListener);
        addKeyListener(new WhiteboardKeyListener(this));
    }

    private void removeEventListeners() {
        removeMouseWheelListener(svgMouseListener);
        removeMouseMotionListener(svgMouseListener);
        removeMouseListener(svgMouseListener);
        removeKeyListener(new WhiteboardKeyListener(this));
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        setVisibleApp(false);
        showControls(false);
        removeEventListeners();
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        mainHUD.removeComponent(messageComponent);
        mainHUD.removeComponent(controlComponent);
        super.cleanup();

        svgCanvas.removeSVGDocumentLoaderListener(whiteboardDocument);
        svgCanvas.removeGVTTreeRendererListener(whiteboardGVTTreeRendererListener);
        svgCanvas.removeUpdateManagerListener(whiteboardUpdateManagerListener);
        svgCanvas.dispose();

        setDocument(null, false);       // Attempt to clean up document, not sure this is sufficient
    }

    /** 
     * Sets the display mode for the control panel to in-world or on-HUD
     * @param mode the control panel display mode
     */
    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    /**
     * Gets the control panel display mode
     * @return the display mode of the control panel: in-world or on HUD
     */
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void showControls(final boolean visible) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                LOGGER.info("show controls: " + visible);
                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

                if (controlComponent == null) {
                    // create Swing controls
                    controls = new WhiteboardControlPanel(WhiteboardWindow.this);

                    // add event listeners
                    toolManager = new WhiteboardToolManager(WhiteboardWindow.this);
                    controls.addCellMenuListener(toolManager);

                    // create HUD control panel
                    controlComponent = mainHUD.createComponent(controls, cell);
                    controlComponent.setPreferredLocation(Layout.SOUTH);

                    // add HUD control panel to HUD
                    mainHUD.addComponent(controlComponent);
                }

                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        // change visibility of controls
                        if (getDisplayMode() == DisplayMode.HUD) {
                            if (controlComponent.isWorldVisible()) {
                                controlComponent.setWorldVisible(false);
                            }
                            controlComponent.setVisible(visible);
                        } else {
                            controlComponent.setWorldLocation(new Vector3f(0.0f, 2.7f, 0.1f));
                            if (controlComponent.isVisible()) {
                                controlComponent.setVisible(false);
                            }
                            controlComponent.setWorldVisible(visible); // show world view
                        }

                        updateMenu();
                    }
                });
            }
        });

    }

    public boolean showingControls() {
        return ((controlComponent != null) && (controlComponent.isVisible() || controlComponent.isWorldVisible()));
    }

    public void movingMarker(MouseEvent e) {
        // Remove previous overlay painting
        addToDisplay(movingOverlay);
        svgMouseListener.setCurrentPoint(e.getPoint());
        addToDisplay(movingOverlay);
    }

    protected class MovingOverlay implements Overlay {

        public void paint(Graphics g) {
            Point currentPoint = svgMouseListener.getCurrentPoint();
            Point pressedPoint = svgMouseListener.getPressedPoint();
            if (currentPoint != null && selection != null) {
                Graphics2D g2d = (Graphics2D) g;

                g2d.setXORMode(Color.WHITE);
                g2d.setColor(Color.GRAY);
                g2d.setStroke(markerStroke);

                Shape s = selection.getSelectedShape();
                int xDiff = (int) (currentPoint.getX() - pressedPoint.getX());
                int yDiff = (int) (currentPoint.getY() - pressedPoint.getY());

                Shape newShape = null;

                if (s instanceof Line2D) {
                    Line2D line = (Line2D) s;
                    newShape = new Line2D.Double(line.getX1() + xDiff, line.getY1() + yDiff,
                            line.getX2() + xDiff, line.getY2() + yDiff);
                } else if (s instanceof Rectangle2D) {
                    Rectangle2D rectangle = (Rectangle2D) s;
                    newShape = new Rectangle2D.Double(rectangle.getX() + xDiff, rectangle.getY() + yDiff,
                            rectangle.getWidth(), rectangle.getHeight());
                } else if (s instanceof Ellipse2D) {
                    Ellipse2D ellipse = (Ellipse2D) s;
                    newShape = new Ellipse2D.Double(ellipse.getX() + xDiff, ellipse.getY() + yDiff,
                            ellipse.getWidth(), ellipse.getHeight());
                }

                g2d.draw(newShape);
            }
        }
    }

    public void drawingMarker(Point aPoint) {
        // Remove previous overlay painting
        addToDisplay(drawingOverlay);
        svgMouseListener.setCurrentPoint(aPoint);
        addToDisplay(drawingOverlay);
    }

    protected class DrawingOverlay implements Overlay {

        public void paint(Graphics g) {
            Point currentPoint = svgMouseListener.getCurrentPoint();
            Point pressedPoint = svgMouseListener.getPressedPoint();
            if (currentPoint != null) {
                Graphics2D g2d = (Graphics2D) g;

                g2d.setXORMode(Color.WHITE);
                g2d.setColor(Color.GRAY);
                g2d.setStroke(markerStroke);
                WhiteboardTool currentTool = toolManager.getTool();
                if (currentTool == WhiteboardTool.LINE) {
                    LOGGER.fine("drawing line: " + pressedPoint.getX() + ", " + pressedPoint.getY() +
                            " to " + currentPoint.getX() + ", " + currentPoint.getY());
                    g2d.drawLine((int) pressedPoint.getX(), (int) pressedPoint.getY(),
                            (int) currentPoint.getX(), (int) currentPoint.getY());
                } else {
                    Rectangle r = WhiteboardUtils.constructRectObject(pressedPoint, currentPoint);
                    if (currentTool == WhiteboardTool.RECT) {
                        LOGGER.fine("drawing rectangle: " + r);
                        g2d.draw(r);
                    } else if (currentTool == WhiteboardTool.ELLIPSE) {
                        g2d.drawOval((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
                    }
                }
            }
        }
    }

    /**
     * Set the size of the application
     * @param width the width of the application
     * @param height the height of the application
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        svgCanvas.setSize(width, height);
    }

    /**
     * Show a status message in the HUD
     * @param message the string to display in the message
     */
    public void showHUDMessage(String message) {
        showHUDMessage(message, 1000);
    }

    /**
     * Show a status message in the HUD and remove it after a timeout
     * @param message the string to display in the message
     * @param timeout the period in milliseconds to display the message for
     */
    public void showHUDMessage(final String message, final int timeout) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                LOGGER.info(this.getClass().getName() + ": " + message);
                ((HUDMessage) messageComponent).setMessage(message);
                messageComponent.setVisible(true);
                messageComponent.setVisible(false, timeout);
            }
        });
    }

    /**
     * Hide the HUD message
     * @param immediately if true, remove the message now, otherwise slide it
     * off the screen first
     */
    public void hideHUDMessage(boolean immediately) {
        if (messageComponent.isVisible()) {
            messageComponent.setVisible(false);
        }
    }

    public boolean isSynced() {
        return synced;
    }

    /**
     * Resynchronize the state of the cell.
     *
     * A resync is necessary when the cell transitions from INACTIVE to
     * ACTIVE cell state, where the cell may have missed state synchronization
     * messages while in the INACTIVE state.
     *
     * Resynchronization is only performed if the cell is currently synced.
     * To sync an unsynced cell, call sync(true) instead.
     */
    public void resync() {
        if (isSynced()) {
            synced = false;
            sync(true);
        }
    }

    public void sync(boolean syncing) {
        if ((syncing == false) && (synced == true)) {
            synced = false;
            LOGGER.info("whiteboard: unsynced");
            showHUDMessage(BUNDLE.getString("Unsynced"), 3000);
            updateMenu();
        } else if ((syncing == true) && (synced == false)) {
            synced = true;
            LOGGER.info("whiteboard: requesting sync with shared state");
            showHUDMessage(BUNDLE.getString("Syncing..."), 3000);
            updateMenu();
            sendRequest(Action.GET_STATE, null, null, null, null);
        }
    }

    protected class SelectionOverlay implements Overlay {

        public void paint(Graphics g) {
            if (selection != null) {
                Graphics2D g2d = (Graphics2D) g;

                //g2d.setXORMode(Color.WHITE);

                List<Shape> selectionPoints = selection.getSelectionPoints();
                Iterator<Shape> it = selectionPoints.iterator();

                while (it.hasNext()) {
                    Shape s = it.next();

//                    g2d.setColor(new Color(0, 0, 1, 0.4f));
//                    g2d.fill(s);
                    g2d.setColor(Color.BLACK);
                    g2d.draw(s);
                }
            }
        }
    }

    public void singleSelection(Point p) {
        BridgeContext bc = svgCanvas.getUpdateManager().getBridgeContext();
        Element parent = svgCanvas.getSVGDocument().getDocumentElement();
        GraphicsNode gNode = bc.getGraphicsNode(parent);
        //setting the selection mode for the parent and its children
        gNode.setPointerEventType(GraphicsNode.VISIBLE);

        NodeList childNodes = parent.getChildNodes();
        Node aNode = null;
        GraphicsNode aGraphicsNode = null;
        boolean selectionMade = false;

        for (int i = 0; i < childNodes.getLength(); i++) {
            aNode = childNodes.item(i);

            if (aNode != null && aNode instanceof Element) {
                aGraphicsNode = bc.getGraphicsNode(aNode);

                if (aGraphicsNode.contains(p)) {
                    selection = new WhiteboardSelection((Element) aNode, aGraphicsNode.getBounds());
                    selectionMade = true;
                }
            }
        }

        if (selectionMade) {
            if (selection != null) {
                repaint();
            }
            addToDisplay(selectionOverlay);
        } else if (!selectionMade && selection != null) {
            repaint();
            selection = null;
        }
    }

    private void addToDisplay(final Overlay overlay) {
        if (wbSurface != null) {
            wbSurface.addToDisplay(overlay);
        }
    }

    /**
     * Return the client id of this window's cell.
     */
    public BigInteger getClientID(App2D app) {
        return cell.getClientID();
    }

    /**
     * Return the ID of this window's cell.
     */
    public CellID getCellID(App2D app) {
        return cell.getCellID();
    }

    /**
     * Return the ID of this window's cell.
     */
    public String getCellUID(App2D app) {
        return cell.getUID();
    }

    /**
     * Return the cell
     * @return the cell associated with this window
     */
    public WhiteboardCell getCell() {
        return cell;
    }

    protected void sendRequest(Action action, String xmlString,
            String docURI, Point position, Float zoom) {

        WhiteboardCellMessage msg = new WhiteboardCellMessage(getClientID(app), getCellID(app),
                getCellUID(app), action, xmlString, docURI, position, zoom);

        // send request to server
        LOGGER.fine("whiteboard: sending request: " + msg);
        commComponent.sendMessage(msg);
    }

    /**
     * Retries an SVG action request
     * @param action the action to retry
     * @param xmlString the xml string that contains the document or element
     * @param docURI the URI for the document
     * @param position the image scroll position
     * @param zoom the zoom amount
     */
    protected void retryRequest(Action action, String xmlString, String docURI, Point position, Float zoom) {
        LOGGER.fine("whiteboard: creating retry thread for: " + action + ", " + xmlString + ", " + position);
        new ActionScheduler(action, xmlString, docURI, position, zoom).start();
    }

    protected class ActionScheduler extends Thread {

        private Action action;
        private String xmlString;
        private String docURI;
        private Point position;
        private Float zoom;

        public ActionScheduler(Action action, String xmlString, String docURI, Point position, Float zoom) {
            this.action = action;
            this.xmlString = xmlString;
            this.docURI = docURI;
            this.position = position;
            this.zoom = zoom;
        }

        @Override
        public void run() {
            // wait for a retry window
            synchronized (actionLock) {
                try {
                    LOGGER.fine("whiteboard: waiting for retry window");
                    actionLock.wait();
                } catch (Exception e) {
                    LOGGER.fine("whiteboard: exception waiting for retry: " + e);
                }
            }
            // retry this request
            LOGGER.info("whiteboard: now retrying: " + action + ", " + xmlString + ", " + position + ", " + zoom);
            sendRequest(action, xmlString, docURI, position, zoom);
        }
    }

    /**
     * Set the zoom
     * @param zoom the zoom factor
     * @param notify whether to notify other clients
     */
    public void setZoom(Float zoom, boolean notify) {
        if ((notify == true) && isSynced()) {
            sendRequest(Action.SET_ZOOM, null, null, null, zoom);
        } else {
            this.zoom = zoom;
            scaleTransform =
                    AffineTransform.getScaleInstance(zoom, zoom);

            AffineTransform current = svgCanvas.getRenderingTransform();

            Dimension dim = svgCanvas.getSize();
            int zx = dim.width / 2;
            int zy = dim.height / 2;
            AffineTransform t = AffineTransform.getTranslateInstance(zx, zy);
            t.concatenate(scaleTransform);
            t.translate(-zx, -zy);
            t.concatenate(current);
            svgCanvas.setRenderingTransform(t);
        }
    }

    /**
     * Set the view position
     * @param position the desired position
     */
    public void setViewPosition(Point position) {
        // REMIND: not implemented
    }

    public Point getViewPosition() {
        // REMIND: not implemented
        return null;
    }

    protected void updateMenu() {
      /*  controls.setSynced(isSynced());

        if (toolManager.isFilled()) {
            controls.setFillMode();
        } else {
            controls.setDrawMode();
        }
        controls.setOnHUD(!toolManager.isOnHUD());
       * 
       */
    }

    /**
     * Paint contents of window
     * @param g the graphics context on which to paint
     */
    protected void paint(Graphics2D g) {
//        logger.finest("whiteboard: paint");
        if (svgCanvas != null) {
            svgCanvas.paint(g);
        }
    }

    /**
     * Repaint the canvas
     */
    public void repaintCanvas() {
        if ((svgCanvas != null) && (wbSurface != null)) {
            svgCanvas.paint(wbSurface.getGraphics());
            wbSurface.repaint();
        }
    }

    /**
     * @return the currentColor
     */
    public Color getCurrentColor() {
        return toolManager.getColor();
    }

    /**
     * @return the markerStroke
     */
    public BasicStroke getMarkerStroke() {
        return markerStroke;
    }

    /**
     * @return the strokeWeight
     */
    public float getStrokeWeight() {
        return strokeWeight;
    }

    /**
     * @return the pressedPoint
     */
    public Point getPressedPoint() {
        return svgMouseListener.getPressedPoint();
    }

    /**
     * @return the currentPoint
     */
    public Point getCurrentPoint() {
        return svgMouseListener.getCurrentPoint();
    }

    /**
     * Set the SVG document
     * @param document the SVG document XML
     * @param notify whether to notify other clients
     */
    public void setDocument(SVGDocument document, boolean notify) {
        whiteboardDocument.setSVGDocument(document);
        svgCanvas.setDocument(document);
    }

    /**
     * Get the SVG document
     * @return the SVG Document object
     */
    public SVGDocument getDocument() {
        return whiteboardDocument.getSVGDocument();
    }

    public WhiteboardDocument getWhiteboardDocument() {
        return whiteboardDocument;

    }

    /**
     * Loads an SVG document
     * @param uri the URI of the SVG document to load
     */
    public void openDocument(String uri, boolean notify) {
        whiteboardDocument.openDocument(uri, notify);
    }

    public void newDocument(boolean notify) {
        SVGDocument document = (SVGDocument) WhiteboardUtils.newDocument();
        svgCanvas.setDocument(document);
        whiteboardDocument.setSVGDocument(document);
        if (isSynced() && (notify == true)) {
            // notify
            sendRequest(Action.NEW_DOCUMENT, null, null, null, null);
        }
    }

    public void showSVGDialog() {
        whiteboardDocument.showSVGDialog();
    }

    public void selectTool(WhiteboardTool tool) {
        controls.selectTool(tool);
    }

    public void deselectTool(WhiteboardTool tool) {
        controls.deselectTool(tool);
    }

    public void selectColor(WhiteboardColor color) {
        controls.selectColor(color);
    }

    public void deselectColor(WhiteboardColor color) {
        controls.deselectColor(color);
    }

    public WhiteboardTool getCurrentTool() {
        return toolManager.getTool();
    }

    public WhiteboardSelection getSelection() {
        return selection;
    }

    /**
     * @return the toolManager
     */
    public WhiteboardToolManager getToolManager() {
        return toolManager;
    }

    /**
     * @param selection the selection to set
     */
    public void setSelection(WhiteboardSelection selection) {
        this.selection = selection;
    }

    /**
     * Add an Element to an SVG document
     * @param e the element to add
     * @param notify whether to notify other clients
     */
    public void addElement(final Element e, boolean notify) {
        addNewElement(e, notify);
    }

    /**
     * Add a new Element to an SVG document
     * @param e the element to add
     * @param notify whether to notify other clients
     */
    public void addNewElement(final Element e, boolean notify) {
        svgCanvas.getUpdateManager().getUpdateRunnableQueue().
                invokeLater(new Runnable() {

            public void run() {
                // Attach the element to the root 'svg' element.
                whiteboardDocument.appendChild(e);
            }
        });

        if (isSynced() && (notify == true)) {
            // notify
            sendRequest(Action.ADD_ELEMENT, WhiteboardUtils.elementToXMLString(e), null, null, null);
        }
    }

    /**
     * Move an Element in an SVG document
     * @param e the element to move
     * @param notify whether to notify other clients
     */
    public Element moveElement(Element toMove) {
        return whiteboardDocument.moveElement(toMove);
    }

    /**
     * Move an Element in an SVG document by a delta
     * @param e the element to move
     * @param xDiff the x-axis delta
     * @param yDiff the y-axis delta
     */
    public Element moveElement(Element toMove, int xDiff, int yDiff) {
        return whiteboardDocument.moveElement(toMove, xDiff, yDiff);
    }

    /**
     * Import an Element into an SVG document
     * @param e the element to import
     * @param notify whether to notify other clients
     */
    public Element importElement(final Element e, boolean notify) {
        return (Element) whiteboardDocument.importNode(e, true);
    }

    /**
     * Remove an Element from an SVG document
     * @param e the element to remove
     * @param notify whether to notify other clients
     */
    public void removeElement(final Element toRemove, boolean notify) {
        svgCanvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(new Runnable() {

            public void run() {
                // Remove the element from the document
                Element rem = whiteboardDocument.getElementById(toRemove.getAttributeNS(null, "id"));
                if (rem != null) {//In case this client has already removed it
                    whiteboardDocument.removeChild(rem);
                }

            }
        });

        if (isSynced() && (notify == true)) {
            // notify
            sendRequest(Action.REMOVE_ELEMENT, WhiteboardUtils.elementToXMLString(toRemove), null, null, null);
        }
    }

    /**
     * Update an Element in an SVG document
     * @param e the element to update
     * @param notify whether to notify other clients
     */
    public void updateElement(final Element afterMove, boolean notify) {
        svgCanvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(new Runnable() {

            public void run() {
                // get element by id for use when server provides an element's new state
                whiteboardDocument.replaceChild(afterMove, whiteboardDocument.getElementById(afterMove.getAttributeNS(null, "id")));
            }
        });

        if (isSynced() && (notify == true)) {
            // notify
            sendRequest(Action.UPDATE_ELEMENT, WhiteboardUtils.elementToXMLString(afterMove), null, null, null);
        }
    }
}
