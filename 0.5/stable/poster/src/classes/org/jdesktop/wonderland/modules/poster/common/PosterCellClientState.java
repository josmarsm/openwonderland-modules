/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.poster.common;

import com.jme.math.Vector2f;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellClientState;

/**
 * Container for poster client cell client state data.
 *
 * @author Bernard Horan
 */

public class PosterCellClientState extends App2DCellClientState {    
    final public static String SHARED_MAP_KEY = "Poster";
    final public static String TEXT_LABEL_KEY = "text";
    final public static String MODE_LABEL_KEY = "mode";
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;
    
    private int preferredWidth = DEFAULT_WIDTH;
    private int preferredHeight = DEFAULT_HEIGHT;
    
    public PosterCellClientState () {
        this(null);
    }
    
    public PosterCellClientState (Vector2f pixelScale) {
	super(pixelScale);
    }
    
    /*
     * Set the preferred width of the poll app
     * @param preferredWidth the preferred width in pixels
     */
    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }
    
    /*
     * Get the preferred width of the poll app
     * @return the preferred width, in pixels
     */
    public int getPreferredWidth() {
        return preferredWidth;
    }
    
    /*
     * Set the preferred height of the poll app
     * @param preferredHeight the preferred height, in pixels
     */
    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }
    
    /*
     * Get the preferred height of the poll app
     * @return the preferred height, in pixels
     */
    public int getPreferredHeight() {
        return preferredHeight;
    }
}
