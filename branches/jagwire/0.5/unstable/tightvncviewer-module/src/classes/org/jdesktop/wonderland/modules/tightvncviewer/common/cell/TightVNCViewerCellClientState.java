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
package org.jdesktop.wonderland.modules.tightvncviewer.common.cell;

import java.util.logging.Logger;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellClientState;

/**
 * Container for VNC viewer client state data.
 *
 * @author nsimpson
 */
@ExperimentalAPI
public class TightVNCViewerCellClientState extends App2DCellClientState {

    private static final Logger logger = Logger.getLogger(TightVNCViewerCellClientState.class.getName());
    private int preferredWidth;
    private int preferredHeight;
    private boolean decorated;           // show window decorations?
    private String checksum;

    public TightVNCViewerCellClientState() {
        this(null);
    }

    public TightVNCViewerCellClientState(Vector2f pixelScale) {
        super(pixelScale);
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
     * Set the checksum
     * @param checksum the checksum
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /*
     * Get the checksum
     * @return the checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Returns a string representation of this class.
     *
     * @return The client state information as a string.
     */
    @Override
    public String toString() {
        return super.toString() + " [TightVNCViewerCellClientState]: "
                + "preferredWidth=" + preferredWidth + ","
                + "preferredHeight=" + preferredHeight + ","
                + "decorated = " + decorated;
    }
}
