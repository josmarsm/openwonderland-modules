/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
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

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.Overlay;
import org.apache.batik.util.RunnableQueue;
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
import org.jdesktop.wonderland.modules.whiteboard.client.WhiteboardToolManager.WhiteboardColor;
import org.jdesktop.wonderland.modules.whiteboard.client.WhiteboardToolManager.WhiteboardTool;
import org.jdesktop.wonderland.modules.whiteboard.client.cell.WhiteboardCell;
import org.jdesktop.wonderland.modules.whiteboard.common.WhiteboardUtils;
import org.jdesktop.wonderland.modules.whiteboard.common.cell.WhiteboardCellMessage;
import org.jdesktop.wonderland.modules.whiteboard.common.cell.WhiteboardCellMessage.Action;
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
 * @author Abhishek Upadhyay
 */
@ExperimentalAPI
public class WhiteboardWindow extends Window2D {

    private static final Logger LOGGER
            = Logger.getLogger(WhiteboardWindow.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/whiteboard/client/resources/Bundle");
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
    private Set<WhiteboardSelection> selections = null;
    private JSVGCanvas svgCanvas;
    // HUD components
    private HUDComponent controlComponent;
    private HUDComponent messageComponent;
    private DisplayMode displayMode;
    private WhiteboardGVTTreeRendererListener whiteboardGVTTreeRendererListener;
    private WhiteboardUpdateManagerListener whiteboardUpdateManagerListener;
    private Element currentTextElement = null;
    private boolean newText = false;
    private boolean textExist = false;
    private int initialCursorPos = 0;
    private RunnableQueue queue = null;

    private boolean shiftPressed = false;

    private SpinnerChangeListener spinnerChangeListener = new SpinnerChangeListener();

    /**
     * Create a new instance of WhiteboardWindow.
     *
     * @param app The whiteboard app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param topLevel Whether the window is top-level (e.g. is decorated) with
     * a frame.
     * @param pixelScale The size of the window pixels.
     * @param commComponent The communications component for communicating with
     * the server.
     */
    public WhiteboardWindow(WhiteboardCell cell, App2D app, int width, int height, boolean topLevel, Vector2f pixelScale,
            final WhiteboardComponent commComponent)
            throws InstantiationException {
        super(app, Type.PRIMARY, width, height, topLevel, pixelScale, new WhiteboardDrawingSurface(width, height));
        LOGGER.info("creating whiteboard with size: " + width + "x" + height);
        this.cell = cell;
        this.commComponent = commComponent;
        setTitle(BUNDLE.getString("Whiteboard"));
        initCanvas(width, height);
        setDecorated(false);
        initHUD();
        setDisplayMode(DisplayMode.HUD);
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
     *
     * @param mode the control panel display mode
     */
    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    /**
     * Gets the control panel display mode
     *
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
                            controlComponent.setWorldLocation(new Vector3f(0.0f, -3.7f, 0.1f));
                            if (controlComponent.isVisible()) {
                                controlComponent.setVisible(false);
                            }
                            controlComponent.setWorldVisible(visible); // show world view
                        }

                        updateMenu();
                    }
                });
                if (!visible) {
                    removeCursor();
                }
            }
        });
    }

    public boolean showingControls() {
        return ((controlComponent != null) && (controlComponent.isVisible() || controlComponent.isWorldVisible()));
    }

    /**
     * Set the size of the application
     *
     * @param width the width of the application
     * @param height the height of the application
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        svgCanvas.setSize(width, height);
    }

    /**
     * Set the size of the window not of the svg canvas
     *
     * @param width the width of the application
     * @param height the height of the application
     */
    public void setSizeOfWindow(int width, int height) {
        super.setSize(width, height);
    }

    /**
     * Show a status message in the HUD
     *
     * @param message the string to display in the message
     */
    public void showHUDMessage(String message) {
        showHUDMessage(message, 1000);
    }

    /**
     * Show a status message in the HUD and remove it after a timeout
     *
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
     *
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
     * A resync is necessary when the cell transitions from INACTIVE to ACTIVE
     * cell state, where the cell may have missed state synchronization messages
     * while in the INACTIVE state.
     *
     * Resynchronization is only performed if the cell is currently synced. To
     * sync an unsynced cell, call sync(true) instead.
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
     *
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
     *
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
     *
     * @param zoom the zoom factor
     * @param notify whether to notify other clients
     */
    public void setZoom(Float zoom, boolean notify) {
        if ((notify == true) && isSynced()) {
            sendRequest(Action.SET_ZOOM, null, null, null, zoom);
        } else {
            this.zoom = zoom;
            scaleTransform
                    = AffineTransform.getScaleInstance(zoom, zoom);

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
     *
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
        //controls.setSynced(isSynced());

        //controls.setOnHUD(!toolManager.isOnHUD());
    }

    /**
     * Paint contents of window
     *
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
     *
     * @param document the SVG document XML
     * @param notify whether to notify other clients
     */
    public void setDocument(SVGDocument document, boolean notify) {
        whiteboardDocument.setSVGDocument(document);
        svgCanvas.setDocument(document);
    }

    /**
     * Get the SVG document
     *
     * @return the SVG Document object
     */
    public SVGDocument getDocument() {
        return whiteboardDocument.getSVGDocument();
    }

    /**
     * Loads an SVG document
     *
     * @param uri the URI of the SVG document to load
     */
    public void openDocument(String uri, boolean notify) {
        whiteboardDocument.openDocument(uri, notify);
    }

    public void newDocument(boolean notify) {
        SVGDocument document = (SVGDocument) WhiteboardUtils.newDocument();
        svgCanvas.setDocument(document);
        whiteboardDocument.setSVGDocument(document);
        setSize(800, 600);
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

    public void movingMarker(MouseEvent e) {
        // Remove previous overlay painting
        refreshSelection();
        addToDisplay(movingOverlay);
        svgMouseListener.setCurrentPoint(e.getPoint());
        addToDisplay(movingOverlay);
    }

    protected class MovingOverlay implements Overlay {

        public void paint(Graphics g) {
            Point currentPoint = svgMouseListener.getCurrentPoint();
            Point pressedPoint = svgMouseListener.getPressedPoint();
            if (currentPoint != null && selections != null) {
                for (WhiteboardSelection selection : selections) {
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
                    LOGGER.fine("drawing line: " + pressedPoint.getX() + ", " + pressedPoint.getY()
                            + " to " + currentPoint.getX() + ", " + currentPoint.getY());
                    g2d.drawLine((int) pressedPoint.getX(), (int) pressedPoint.getY(),
                            (int) currentPoint.getX(), (int) currentPoint.getY());
                } else {
                    Rectangle r = WhiteboardUtils.constructRectObject(pressedPoint, currentPoint);
                    if (currentTool == WhiteboardTool.RECT || currentTool == WhiteboardTool.RECT_FILL) {
                        LOGGER.fine("drawing rectangle: " + r);
                        g2d.draw(r);
                    } else if (currentTool == WhiteboardTool.ELLIPSE || currentTool == WhiteboardTool.ELLIPSE_FILL) {
                        g2d.drawOval((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
                    }
                }
            }
        }
    }

    protected class SelectionOverlay implements Overlay {

        public void paint(Graphics g) {
            if (selections != null) {
                for (WhiteboardSelection selection : selections) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setPaintMode();
                    //g2d.setXORMode(Color.WHITE);

                    List<Shape> selectionPoints = selection.getSelectionPoints();
                    Iterator<Shape> it = selectionPoints.iterator();
                    while (it.hasNext()) {
                        Shape s = it.next();
                        g2d.setColor(Color.WHITE);
                        g2d.fill(s);
                        g2d.setColor(Color.BLACK);
                        g2d.draw(s);
                    }
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

                if (aGraphicsNode!=null && ((Element) aNode).getAttribute("id")!=null && 
                        aGraphicsNode.contains(p) && !((Element) aNode).getAttribute("id").equals("bImage")) {
                    selection = new WhiteboardSelection((Element) aNode, aGraphicsNode.getBounds());
                    selectionMade = true;
                }
            }
        }
        if (selectionMade && getCurrentTool() == WhiteboardTool.SELECTOR) {
            boolean select = false;
            if (selection != null) {
                if (selections != null) {
                    for (WhiteboardSelection sel : selections) {
                        if (sel.getSelectedElement().getAttribute("id")
                                .equals(selection.getSelectedElement().getAttribute("id"))) {
                            select = true;
                        }
                    }
                }
                if (select && !isShiftPressed() && selections != null && selections.size() > 1) {
                    //do nothing
                } else {
                    repaintCanvas();
                    if (selections == null) {
                        selections = new HashSet<WhiteboardSelection>();
                    }
                    Set<WhiteboardSelection> newSelection = new HashSet<WhiteboardSelection>(selections);
                    selections.clear();
                    boolean add = true;
                    if (isShiftPressed()) {
                        for (WhiteboardSelection s : newSelection) {
                            if (s.getSelectedElement().getAttribute("id").equals(selection.getSelectedElement().getAttribute("id"))) {
                                add = false;
                            } else {
                                selections.add(s);
                            }
                        }
                    }
                    if (add) {
                        selections.add(selection);
                    }
                }
            }
            addToDisplay(selectionOverlay);
            populateSpinner();
        } else if (!selectionMade && selection != null) {
            clearSelections();
            setSelection(null);
            repaintCanvas();
            repaint();
        }
    }

    int first = 0;
    private void populateSpinner() {
        Element e = selection.getSelectedElement();
        int ow = 0;
        int cw = 0;
        double perc = 0;
        if (e.getTagName().equals("rect") || e.getTagName().equals("image")) {
            String[] size = e.getAttribute("name").split("x");
            ow = (int) Double.parseDouble(size[0]);
            cw = Integer.parseInt(e.getAttribute("width"));
            perc = ((double) (cw * 100) / (double) ow);
        } else if (e.getTagName().equals("ellipse")) {
            String[] size = e.getAttribute("name").split("x");
            ow = (int) Double.parseDouble(size[0]);
            cw = (int) Double.parseDouble(e.getAttribute("rx"));
            perc = ((double) (cw * 100) / (double) ow);
        } else if (e.getTagName().equals("text")) {
            String[] size = e.getAttribute("name").split("px");
            ow = (int) Double.parseDouble(size[0]);
            cw = (int) Double.parseDouble(e.getAttribute("font-size").split("px")[0]);
            perc = ((double) (cw * 100) / (double) ow);
        } else if (e.getTagName().equals("line")) {
            String[] size = e.getAttribute("name").split(",");
            perc = 100 + Integer.parseInt(size[4]);
        }
        
        int percInt = (int)perc;
        double percFraction = perc - percInt;
        
        if(percFraction>=0.5) {
            percInt = percInt+1;
        }
        first = 0;
        controls.getResizeSpinner().setValue(percInt);
        controls.getResizeSpinner().addChangeListener(spinnerChangeListener);
    }

    private class SpinnerChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            try {
                if(first==0) {
                    first = 1;
                    return;
                }
                Element ele = selection.getSelectedElement();
                int value = Integer.parseInt(controls.getResizeSpinner().getValue().toString());
                
                int diff = value - 100;
                Element nEle = (Element) importElement(whiteboardDocument.getElementById(selection
                        .getSelectedElement().getAttribute("id")), true).cloneNode(true);
                //update element attributes based on the tagname
                if (ele.getTagName().equals("rect") || ele.getTagName().equals("image")) {
                    String[] size = ele.getAttribute("name").split("x");
                    int ow = Integer.parseInt(size[0]);
                    int oh = Integer.parseInt(size[1]);
                    int newWidth = ow + ((ow * diff) / 100);
                    int newHeight = oh + ((oh * diff) / 100);
                    nEle.setAttribute("width", String.valueOf(newWidth));
                    nEle.setAttribute("height", String.valueOf(newHeight));
                } else if (ele.getTagName().equals("ellipse")) {
                    String[] size = ele.getAttribute("name").split("x");
                    int orx = (int) Double.parseDouble(size[0]);
                    int ory = (int) Double.parseDouble(size[1]);
                    int newRx = orx + ((orx * diff) / 100);
                    int newRy = ory + ((ory * diff) / 100);
                    nEle.setAttribute("rx", String.valueOf(newRx));
                    nEle.setAttribute("ry", String.valueOf(newRy));
                } else if (ele.getTagName().equals("text")) {
                    String size = ele.getAttribute("name");
                    int ofs = (int) Double.parseDouble(size);
                    int newfs = ofs + ((ofs * diff) / 100);
                    nEle.setAttribute("font-size", String.valueOf(newfs) + "px");
                } else if (ele.getTagName().equals("line")) {
                    String[] size = ele.getAttribute("name").split(",");
                    double oldX1 = Double.parseDouble(size[0]);
                    double oldY1 = Double.parseDouble(size[1]);
                    double oldX2 = Double.parseDouble(size[2]);
                    double oldY2 = Double.parseDouble(size[3]);

                    double m = ((oldY2 - oldY1) / (oldX2 - oldX1));
                    double c = oldY1 - m * oldX1;
                    double xDist = (oldX2 - oldX1);
                    double xDistDiff = diff * xDist / 200;
                    double yDist = (oldY2 - oldY1);
                    double yDistDiff = diff * yDist / 200;
                    
                    double newX1 = 0;
                    double newY1 = 0;
                    double newX2 = 0;
                    double newY2 = 0;
                    
                    if (m != Double.POSITIVE_INFINITY && m != Double.NEGATIVE_INFINITY) {
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

                    String[] name = nEle.getAttribute("name").split(",");
                    name[4] = diff + "";
                    String newName = name[0] + "," + name[1] + "," + name[2] + "," + name[3] + "," + name[4] + ",";

                    nEle.setAttribute("name", newName);
                    nEle.setAttribute("x1", String.valueOf((int) newX1));
                    nEle.setAttribute("y1", String.valueOf((int) newY1));
                    nEle.setAttribute("x2", String.valueOf((int) newX2));
                    nEle.setAttribute("y2", String.valueOf((int) newY2));
                }
                updateElement(nEle, true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addToDisplay(final Overlay overlay) {
        if (wbSurface != null) {
            wbSurface.addToDisplay(overlay);
        }
    }

    public void refreshSelection() {
        if (selection != null) {
            selection.refreshSelection(whiteboardDocument, svgCanvas.getUpdateManager().getBridgeContext());
        }
        if (selections != null) {
            for (WhiteboardSelection sel : selections) {
                sel.refreshSelection(whiteboardDocument, svgCanvas.getUpdateManager().getBridgeContext());
            }
        }
    }

    public void selectElements() {
        if (selections != null) {
            for (WhiteboardSelection sel : selections) {
                selection = sel;
                addToDisplay(selectionOverlay);
            }
        }
    }

    public Element getSelectedTextElement(Point p) {
        BridgeContext bc = svgCanvas.getUpdateManager().getBridgeContext();
        Element parent = svgCanvas.getSVGDocument().getDocumentElement();
        GraphicsNode gNode = bc.getGraphicsNode(parent);
        //setting the selection mode for the parent and its children
        if (gNode != null) {
            gNode.setPointerEventType(GraphicsNode.VISIBLE);

            NodeList childNodes = parent.getChildNodes();
            Node aNode = null;
            GraphicsNode aGraphicsNode = null;

            for (int i = 0; i < childNodes.getLength(); i++) {
                aNode = childNodes.item(i);

                if (aNode != null && aNode instanceof Element) {
                    Element e = (Element) aNode;
                    aGraphicsNode = bc.getGraphicsNode(aNode);

                    if (aGraphicsNode != null && aGraphicsNode.contains(p) && e.getTagName().equals("text")) {
                        return e;
                    }
                }
            }
        }
        return null;
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
     *
     * @param e the element to add
     * @param notify whether to notify other clients
     */
    public void addElement(final Element e, boolean notify) {
        addNewElement(e, notify);
    }

    /**
     * Add background image to an SVG document
     *
     * @param e the element to add
     * @param notify whether to notify other clients
     */
    public void addBackgroundImage(final Element e, boolean notify, boolean resize) {
        e.setAttribute("opacity", "0");
        if (resize) {
            setSizeOfWindow(Integer.parseInt(e.getAttribute("width")), Integer.parseInt(e.getAttribute("height")));
        }
        svgCanvas.getUpdateManager().getUpdateRunnableQueue().
                invokeLater(new Runnable() {
                    public void run() {
                        // Attach the element to the root 'svg' element.
                        whiteboardDocument.appendChild(e);
                    }
                });

        if (isSynced() && (notify == true)) {
            // notify
            sendRequest(Action.ADD_BACKGROUND_IMAGE, WhiteboardUtils.elementToXMLString(e), null, null, null);
        }
    }

//    public void addBackgroundImage(final Element e, boolean notify) {
//        addBackgroundImage(e,notify,true);
//    }
    /**
     * Update background image in an SVG document
     *
     * @param e the element to update
     * @param notify whether to notify other clients
     */
    public void updateBackgroundImage(final Element afterMove, final boolean notify) {
        //remove extra bImage elemet
        afterMove.setAttribute("opacity", "1");
        List<Element> bImgEles = new ArrayList<Element>();
        NodeList nodes = getDocument().getDocumentElement().getChildNodes();
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node instanceof Element && ((Element) node).getAttribute("id").equals("bImage")) {
                    bImgEles.add((Element) node);
                }
            }
        }
        if (bImgEles.size() > 1) {
            for (int i = 1; i < bImgEles.size(); i++) {
                removeElement(bImgEles.get(i), false);
            }
        }
        int oldW = getWidth();
        int newW = Integer.parseInt(afterMove.getAttribute("width"));
        int oldH = getHeight();
        int newH = Integer.parseInt(afterMove.getAttribute("height"));
        setSizeOfWindow(Integer.parseInt(afterMove.getAttribute("width")), Integer.parseInt(afterMove.getAttribute("height")));
        svgCanvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(new Runnable() {
            public void run() {
                // get element by id for use when server provides an element's new state
                whiteboardDocument.replaceChild(afterMove, whiteboardDocument.getElementById(afterMove.getAttributeNS(null, "id")));
            }
        });
        scaleContents(oldW, newW, oldH, newH);
        if (isSynced() && (notify == true)) {
            // notify
            sendRequest(Action.UPDATE_BACKGROUND_IMAGE, WhiteboardUtils.elementToXMLString(afterMove), null, null, null);
        }
    }

    private void scaleContents(final int oldW, final int width, final int oldH, final int height) {
        new Thread(new Runnable() {
            public void run() {
                double sfW = (double) width / (double) oldW;
                double sfH = (double) height / (double) oldH;
                NodeList nodes = getDocument().getDocumentElement().getChildNodes();
                if (nodes != null) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Node node = nodes.item(i);
                        if (node instanceof Element) {
                            Element e = (Element) node.cloneNode(true);
                            if (e.getTagName().equals("text")) {
                                int oldEW = Integer.parseInt(e.getAttribute("font-size").split("px")[0]);
                                int oldEX = Integer.parseInt(e.getAttribute("x"));
                                int oldEY = Integer.parseInt(e.getAttribute("y"));
                                int newEW = (int) ((double) sfW * (double) oldEW);
                                int newEX = (int) ((double) sfW * (double) oldEX);
                                int newEY = (int) ((double) sfH * (double) oldEY);

                                e.setAttribute("font-size", String.valueOf(newEW));
                                e.setAttribute("x", String.valueOf(newEX));
                                e.setAttribute("y", String.valueOf(newEY));
                                updateElement(e, true);
                            } else if (e.getTagName().equals("rect")) {
                                int oldEW = Integer.parseInt(e.getAttribute("width"));
                                int oldEH = Integer.parseInt(e.getAttribute("height"));
                                double sfE = (double) oldEW / (double) oldEH;
                                int oldEX = Integer.parseInt(e.getAttribute("x"));
                                int oldEY = Integer.parseInt(e.getAttribute("y"));

                                int newEX = (int) ((double) sfW * (double) oldEX);
                                int newEY = (int) ((double) sfH * (double) oldEY);

                                int newEW = 0;
                                int newEH = 0;

                                if (width <= oldW && height <= oldH) {
                                    if ((oldW - width) > (oldH - height)) {
                                        newEW = (int) ((double) sfW * (double) oldEW);
                                        newEH = (int) ((double) newEW / (double) sfE);
                                    } else if ((oldW - width) <= (oldH - height)) {
                                        newEH = (int) ((double) sfH * (double) oldEH);
                                        newEW = (int) ((double) newEH * (double) sfE);
                                    }
                                } else if (width >= oldW && height >= oldH) {
                                    if ((width - oldW) > (height - oldH)) {
                                        newEW = (int) ((double) sfW * (double) oldEW);
                                        newEH = (int) ((double) newEW / (double) sfE);
                                    } else if ((width - oldW) <= (height - oldH)) {
                                        newEH = (int) ((double) sfH * (double) oldEH);
                                        newEW = (int) ((double) newEH * (double) sfE);
                                    }
                                } else if (width >= oldW && height <= oldH) {
                                    newEH = (int) ((double) sfH * (double) oldEH);
                                    newEW = (int) ((double) newEH * (double) sfE);
                                } else if (width <= oldW && height >= oldH) {
                                    newEW = (int) ((double) sfW * (double) oldEW);
                                    newEH = (int) ((double) newEW / (double) sfE);
                                }

                                e.setAttribute("width", String.valueOf(newEW));
                                e.setAttribute("height", String.valueOf(newEH));
                                e.setAttribute("x", String.valueOf(newEX));
                                e.setAttribute("y", String.valueOf(newEY));
                                updateElement(e, true);
                            } else if (e.getTagName().equals("line")) {
                                int oldEX1 = Integer.parseInt(e.getAttribute("x1"));
                                int oldEX2 = Integer.parseInt(e.getAttribute("x2"));
                                int oldEY1 = Integer.parseInt(e.getAttribute("y1"));
                                int oldEY2 = Integer.parseInt(e.getAttribute("y2"));
                                int newEX1 = (int) ((double) sfW * (double) oldEX1);
                                int newEX2 = (int) ((double) sfW * (double) oldEX2);
                                int newEY1 = (int) ((double) sfH * (double) oldEY1);
                                int newEY2 = (int) ((double) sfH * (double) oldEY2);

                                e.setAttribute("x1", String.valueOf(newEX1));
                                e.setAttribute("x2", String.valueOf(newEX2));
                                e.setAttribute("y1", String.valueOf(newEY1));
                                e.setAttribute("y2", String.valueOf(newEY2));

                                String[] name = e.getAttribute("name").split(",");
                                e.setAttributeNS(null, "name", e.getAttribute("x1") + ","
                                        + e.getAttribute("y1") + ","
                                        + e.getAttribute("x2") + ","
                                        + e.getAttribute("y2") + "," + name[4]);

                                updateElement(e, true);
                            } else if (e.getTagName().equals("ellipse")) {
                                float oldRX = Float.parseFloat(e.getAttribute("rx"));
                                float oldRY = Float.parseFloat(e.getAttribute("ry"));
                                float oldCX = Float.parseFloat(e.getAttribute("cx"));
                                float oldCY = Float.parseFloat(e.getAttribute("cy"));
                                float sfE = oldRX / oldRY;

                                int newRX = (int) ((double) sfW * (double) oldRX);
                                int newRY = (int) ((double) newRX / (double) sfE);
                                int newCX = (int) ((double) sfW * (double) oldCX);
                                int newCY = (int) ((double) sfH * (double) oldCY);

                                if (width <= oldW && height <= oldH) {
                                    if ((oldW - width) > (oldH - height)) {
                                        newRX = (int) ((double) sfW * (double) oldRX);
                                        newRY = (int) ((double) newRX / (double) sfE);
                                    } else if ((oldW - width) <= (oldH - height)) {
                                        newRY = (int) ((double) sfH * (double) oldRY);
                                        newRX = (int) ((double) newRY * (double) sfE);
                                    }
                                } else if (width >= oldW && height >= oldH) {
                                    if ((width - oldW) > (height - oldH)) {
                                        newRX = (int) ((double) sfW * (double) oldRX);
                                        newRY = (int) ((double) newRX / (double) sfE);
                                    } else if ((width - oldW) <= (height - oldH)) {
                                        newRY = (int) ((double) sfH * (double) oldRY);
                                        newRX = (int) ((double) newRY * (double) sfE);
                                    }
                                } else if (width >= oldW && height <= oldH) {
                                    newRY = (int) ((double) sfH * (double) oldRY);
                                    newRX = (int) ((double) newRY * (double) sfE);
                                } else if (width <= oldW && height >= oldH) {
                                    newRX = (int) ((double) sfW * (double) oldRX);
                                    newRY = (int) ((double) newRX / (double) sfE);
                                }

                                e.setAttribute("rx", String.valueOf(newRX));
                                e.setAttribute("ry", String.valueOf(newRY));
                                e.setAttribute("cx", String.valueOf(newCX));
                                e.setAttribute("cy", String.valueOf(newCY));
                                updateElement(e, true);
                            } else if (e.getTagName().equals("image") && !e.getAttribute("id").equals("bImage")) {
                                int oldEX = Integer.parseInt(e.getAttribute("x"));
                                int oldEY = Integer.parseInt(e.getAttribute("y"));
                                int newEX = (int) ((double) sfW * (double) oldEX);
                                int newEY = (int) ((double) sfH * (double) oldEY);
                                e.setAttribute("x", String.valueOf(newEX));
                                e.setAttribute("y", String.valueOf(newEY));
                                updateElement(e, true);
                            }
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * Add a new Element to an SVG document
     *
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
     * Add a new text Element to an SVG document
     *
     * @param e the element to add
     * @param notify whether to notify other clients
     */
    public void addNewTextElement(final Element e, boolean notify) {
        svgCanvas.getUpdateManager().getUpdateRunnableQueue().
                invokeLater(new Runnable() {
                    public void run() {
                        // Attach the element to the root 'svg' element.
                        whiteboardDocument.appendChild(e);
                    }
                });

        if (isSynced() && (notify == true)) {
            // notify
            sendRequest(Action.ADD_TEXT_ELEMENT, WhiteboardUtils.elementToXMLString(e), null, null, null);
        }
    }

    /**
     * Add a new text Element to an SVG document to all other clients not for
     * this
     *
     * @param e the element to add
     * @param notify whether to notify other clients
     */
    public void addNewTextElement(final Element e) {
        sendRequest(Action.ADD_TEXT_ELEMENT, WhiteboardUtils.elementToXMLString(e), null, null, null);
    }

    /**
     * Move an Element in an SVG document
     *
     * @param e the element to move
     * @param notify whether to notify other clients
     */
    public Element moveElement(Element toMove) {
        return whiteboardDocument.moveElement(toMove);
    }

    /**
     * Move an Element in an SVG document by a delta
     *
     * @param e the element to move
     * @param xDiff the x-axis delta
     * @param yDiff the y-axis delta
     */
    public Element moveElement(Element toMove, int xDiff, int yDiff) {
        return whiteboardDocument.moveElement(toMove, xDiff, yDiff);
    }

    /**
     * Import an Element into an SVG document
     *
     * @param e the element to import
     * @param notify whether to notify other clients
     */
    public Element importElement(final Element e, boolean notify) {
        return (Element) whiteboardDocument.importNode(e, true);
    }

    /**
     * Remove an Element from an SVG document
     *
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
     *
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

    /**
     * Update a text Element in an SVG document
     *
     * @param e the element to update
     * @param notify whether to notify other clients
     */
    public void updateTextElement(final Element afterMove, boolean notify) {
        svgCanvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(new Runnable() {
            public void run() {
                // get element by id for use when server provides an element's new state
                whiteboardDocument.replaceChild(afterMove, whiteboardDocument.getElementById(afterMove.getAttributeNS(null, "id")));
            }
        });

        if (isSynced() && (notify == true)) {
            // notify
            sendRequest(Action.UPDATE_TEXT_ELEMENT, WhiteboardUtils.elementToXMLString(afterMove), null, null, null);
        }
    }

    public void updateTextElement(final Element afterMove) {
        sendRequest(Action.UPDATE_TEXT_ELEMENT, WhiteboardUtils.elementToXMLString(afterMove), null, null, null);
    }

    /*
     * remove text cursor from the whiteboard
     */
    public void removeCursor() {
        if (getDocument() != null) {
            Element ce = getDocument().getElementById("cursor");
            Element ce1 = getDocument().getElementById("cursor1");
            if (ce != null) {
                removeElement(ce, true);
                removeElement(ce1, true);
                emptyRunnableQueue();
            }
        }
    }

    /*
     * update text element with the selected font
     */
    public void updateSelectedTextElements(String fontName, String fontSize, String color, String style, String weight) {

        if (selections != null) {
            for (WhiteboardSelection sel : selections) {

                Element e = sel.getSelectedElement();
                if (e != null && e.getTagName().equals("text")) {
                    e = (Element) e.cloneNode(true);
                    e.setAttributeNS(null, "font-size", fontSize);
                    e.setAttributeNS(null, "font-family", fontName);
                    e.setAttributeNS(null, "font-style", style);
                    e.setAttributeNS(null, "font-weight", weight);
                    //e.setAttributeNS(null, "fill", color);
                    if (e.getAttribute("text-decoration").equals("underline")) {
                        updateElement(e, false);
                    } else {
                        updateElement(e, true);
                    }
                    setSelection(null);
                }
            }
        }
    }

    public void updateSelectedTextElements() {
        updateSelectedTextElements(getToolManager().getFontName(), getToolManager().getFontSize() + "px", getToolManager().getFontColor(), getToolManager().getFontStyle(), getToolManager().getFontWeight());
    }

    public void updateElementsColor() {
        if (selections != null) {
            for (WhiteboardSelection sel : selections) {
                Element e = sel.getSelectedElement();
                e = (Element) e.cloneNode(true);
                String tagName = e.getTagName();

                if (tagName.equals("line")) {
                    e.setAttributeNS(null, "stroke", getToolManager().getGlobalColor());
                } else if (tagName.equals("rect")) {
                    e.setAttributeNS(null, "stroke", getToolManager().getGlobalColor());
                    e.setAttributeNS(null, "fill", getToolManager().getGlobalColor());
                } else if (tagName.equals("ellipse")) {
                    e.setAttributeNS(null, "stroke", getToolManager().getGlobalColor());
                    e.setAttributeNS(null, "fill", getToolManager().getGlobalColor());
                } else if (tagName.equals("text")) {
                    e.setAttributeNS(null, "fill", getToolManager().getFontColor());
                }
                updateElement(e, true);
            }
            //setSelection(null);
            //selections.clear();
        }
    }

    public WhiteboardMouseListener getSvgMouseListener() {
        return svgMouseListener;
    }

    public Element getCurrentTextElement() {
        return currentTextElement;
    }

    public void setCurrentTextElement(Element currentTextElement) {
        this.currentTextElement = currentTextElement;
    }

    public RunnableQueue getRunnableQueue() {
        if (queue == null) {
            queue = getBridgeContext().getUpdateManager().getUpdateRunnableQueue();
        }
        return queue;
    }

    public void emptyRunnableQueue() {
        queue = null;
    }

    public BridgeContext getBridgeContext() {
        return svgCanvas.getUpdateManager().getBridgeContext();
    }

    public boolean isNewText() {
        return newText;
    }

    public void setNewText(boolean newText) {
        this.newText = newText;
    }

    public int getInitialCursorPos() {
        return initialCursorPos;
    }

    public void setInitialCursorPos(int initialCursorPos) {
        this.initialCursorPos = initialCursorPos;
    }

    public boolean isTextExist() {
        return textExist;
    }

    public void setTextExist(boolean textExist) {
        this.textExist = textExist;
    }

    public Set<WhiteboardSelection> getSelections() {
        return selections;
    }

    public void setSelections(Set<WhiteboardSelection> selections) {
        this.selections = selections;
    }

    public void clearSelections() {
        if (selections != null) {
            selections.clear();
        }
    }

    public void setShiftPressed(boolean pressed) {
        this.shiftPressed = pressed;
    }

    public boolean isShiftPressed() {
        return shiftPressed;
    }
}
