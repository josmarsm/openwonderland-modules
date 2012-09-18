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
package org.jdesktop.wonderland.modules.whiteboard.common.cell;

import java.util.logging.Logger;
import com.jme.math.Vector2f;
import java.awt.Point;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellClientState;

/**
 * Container for whiteboard cell data
 *
 * @author nsimpson,deronj
 */
@ExperimentalAPI
public class WhiteboardSVGCellClientState extends App2DCellClientState {

    private static final Logger logger = Logger.getLogger(WhiteboardSVGCellClientState.class.getName());
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private int preferredWidth = DEFAULT_WIDTH;
    private int preferredHeight = DEFAULT_HEIGHT;
    private boolean decorated = true;           // show window decorations
    private Point position = new Point(0, 0);   // image pan/scroll position
    private float zoom = 1.0f;
    private String checksum;

    public WhiteboardSVGCellClientState() {
        this(null);
    }

    public WhiteboardSVGCellClientState(Vector2f pixelScale) {
        super(pixelScale);
    }

    /*
     * Set the image position
     * @param position the scroll position in x and y coordinates
     */
    public void setPosition(Point position) {
        this.position = position;
    }

    /*
     * Get the image position
     * @return the scroll position of the image
     */
    public Point getPosition() {
        return position;
    }

    /**
     * Set the zoom factor
     * @param zoom the zoom factor
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    /**
     * Get the zoom factor
     * @return the zoom factor
     */
    public float getZoom() {
        return zoom;
    }

    /*
     * Set the preferred width
     * @param preferredWidth the preferred width in pixels
     */
    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    /*
     * Get the preferred width
     * @return the preferred width, in pixels
     */
    public int getPreferredWidth() {
        return preferredWidth;
    }

    /*
     * Set the preferred height
     * @param preferredHeight the preferred height, in pixels
     */
    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    /*
     * Get the preferred height
     * @return the preferred height, in pixels
     */
    public int getPreferredHeight() {
        return preferredHeight;
    }

    /**
     * Set the window decoration status
     * @param decorated whether to show or hide the window decorations
     */
    public void setDecorated(boolean decorated) {
        this.decorated = decorated;
    }

    /**
     * Get the window decoration status
     * @return true if the window decorations are enabled, false otherwise
     */
    public boolean getDecorated() {
        return decorated;
    }

    /*
     * Get the checksum
     * @return the checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /*
     * Set the checksum
     * @param checksum the checksum
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public void copyLocal(WhiteboardSVGCellClientState state) {
        setPreferredWidth(state.getPreferredWidth());
        setPreferredHeight(state.getPreferredHeight());
        setPosition(state.getPosition());
        setDecorated(state.getDecorated());
        setZoom(state.getZoom());
    }

    /**
     * Returns a string representation of this class.
     *
     * @return The client state information as a string.
     */
    @Override
    public String toString() {
        return super.toString() + " [WhiteboardSVGCellClientState]: " +
                "position = " + position +
                "preferredWidth=" + preferredWidth + "," +
                "preferredHeight=" + preferredHeight + "," +
                "zoom = " + zoom + ", " +
                "decorated = " + decorated;
    }
}
