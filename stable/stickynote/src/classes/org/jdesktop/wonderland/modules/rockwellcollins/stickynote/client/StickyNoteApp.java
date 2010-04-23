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
package org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client;

import org.jdesktop.wonderland.modules.appbase.client.App2D;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.ControlArbSingle;

/**
 *
 * The Wonderland application for sticky notes.
 * This app is derived from Swing Test originally from
 * @author deronj.
 * @author Ryan (mymegabyte)
 */
@ExperimentalAPI
public class StickyNoteApp extends App2D {

    /**
     * Create a new instance of StickyNoteApp. This in turn creates
     * and makes visible the single window used by the app.
     *
     * @param name The name of the app.
     * @param pixelScale The horizontal and vertical pixel sizes (in world meters per pixel).
     */
    public StickyNoteApp(String name, Vector2f pixelScale) {
        super(name, new ControlArbSingle() {
        }, pixelScale);
        controlArb.setApp(this);
    }
}
