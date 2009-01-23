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

import org.w3c.dom.Element;
import org.jdesktop.wonderland.modules.appbase.client.AppType;
import org.jdesktop.wonderland.modules.appbase.client.AppGraphics2D;
import org.jdesktop.wonderland.modules.appbase.client.ControlArbMulti;
import com.jme.math.Vector2f;
import java.awt.Point;
import org.w3c.dom.svg.SVGDocument;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 *
 * A 2D SVG whiteboard application
 *
 * @author paulby
 * @author deronj
 * @author nsimpson
 */
@ExperimentalAPI
public class WhiteboardApp extends AppGraphics2D {

    /** The single whiteboardWindow created by this app */
    private WhiteboardWindow whiteboardWindow;

    /**
     * Create a new instance of WhiteboardApp. This in turn creates
     * and makes visible the single whiteboardWindow used by the app.
     *
     * @param appType The type of app (should be WhiteboardAppType).
     * @param width The width (in pixels) of the whiteboard whiteboardWindow.
     * @param height The height (in pixels) of the whiteboard whiteboardWindow.
     * @param pixelScale The horizontal and vertical pixel sizes
     * (in world meters per pixel).
     * @param commComponent The communications component for communicating with the server.
     */
    public WhiteboardApp(AppType appType, int width, int height, Vector2f pixelScale,
            WhiteboardComponent commComponent) {

        // configWorld can be null because the server cell is already configured
        super(appType, new ControlArbMulti(), pixelScale);
        controlArb.setApp(this);

        // This app has only one whiteboardWindow, so it is always top-level
        try {
            whiteboardWindow = new WhiteboardWindow(this, width, height, true, pixelScale, commComponent);
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Load an SVG document
     * @param uri the URI of the SVG document to load
     * @param notify whether to notify other clients
     */
    public void openDocument(String uri, boolean notify) {
        whiteboardWindow.openDocument(uri, notify);
    }

    /**
     * Create a new SVG document
     * @param notify
     */
    public void newDocument(boolean notify) {
        whiteboardWindow.newDocument(notify);
    }

    /**
     * Set the SVG document
     * @param document the SVG document XML
     * @param notify whether to notify other clients
     */
    public void setDocument(SVGDocument document, boolean notify) {
        whiteboardWindow.setDocument(document, notify);
    }

    /**
     * Import an Element into an SVG document
     * @param e the element to import
     * @param notify whether to notify other clients
     */
    public Element importElement(final Element e, boolean notify) {
        return (Element) whiteboardWindow.importElement(e, notify);
    }

    /**
     * Add an Element to an SVG document
     * @param e the element to add
     * @param notify whether to notify other clients
     */
    public void addElement(Element e, boolean notify) {
        Element adding = importElement(e, notify);
        if (adding != null) {
            whiteboardWindow.addElement(adding, notify);
        }
    }

    /**
     * Add a new Element to an SVG document
     * @param e the element to add
     * @param notify whether to notify other clients
     */
    public void addNewElement(Element e, boolean notify) {
        whiteboardWindow.addNewElement(e, notify);
    }

    /**
     * Remove an Element from an SVG document
     * @param e the element to remove
     * @param notify whether to notify other clients
     */
    public void removeElement(Element e, boolean notify) {
        Element removing = importElement(e, notify);
        if (removing != null) {
            whiteboardWindow.removeElement(removing, notify);
        }
    }

    /**
     * Update an Element in an SVG document
     * @param e the element to update
     * @param notify whether to notify other clients
     */
    public void updateElement(Element e, boolean notify) {
        Element updating = importElement(e, notify);
        if (updating != null) {
            whiteboardWindow.updateElement(updating, notify);
        }
    }

    /**
     * Set the view position
     * @param position the desired position
     */
    public void setViewPosition(Point position) {
        whiteboardWindow.setViewPosition(position);
    }

    /**
     * Set the zoom
     * @param zoom the zoom factor
     * @param notify whether to notify other clients
     */
    public void setZoom(Float zoom, boolean notify) {
        whiteboardWindow.setZoom(zoom, notify);
    }

    /** 
     * Clean up resources.
     */
    @Override
    public void cleanup() {
        super.cleanup();
        if (whiteboardWindow != null) {
            whiteboardWindow.setVisible(false);
            whiteboardWindow.cleanup();
            whiteboardWindow = null;
        }
    }

    /**
     * Returns the app's whiteboardWindow.
     */
    public WhiteboardWindow getWindow() {
        return whiteboardWindow;
    }

    /**
     * Change the visibility of the app.
     *
     * @param visible Whether the application is visible.
     */
    public void setVisible(boolean visible) {
        whiteboardWindow.setVisible(visible);
    }

    /**
     * Repaint the canvas
     */
    public void repaintCanvas() {
        whiteboardWindow.repaintCanvas();
    }
}
