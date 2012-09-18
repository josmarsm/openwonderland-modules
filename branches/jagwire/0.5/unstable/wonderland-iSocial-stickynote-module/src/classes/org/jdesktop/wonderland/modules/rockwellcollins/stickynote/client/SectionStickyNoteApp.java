/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */

/**
 *
 * @author xiuzhen
 */


package org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client;

import org.jdesktop.wonderland.modules.appbase.client.App2D;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.ControlArbSingle;


@ExperimentalAPI
public class SectionStickyNoteApp extends App2D {

    /**
     * Create a new instance of StickyNoteApp. This in turn creates
     * and makes visible the single window used by the app.
     *
     * @param name The name of the app.
     * @param pixelScale The horizontal and vertical pixel sizes (in world meters per pixel).
     */
    public SectionStickyNoteApp(String name, Vector2f pixelScale) {
        super(name, new ControlArbSingle() {
        }, pixelScale);
        controlArb.setApp(this);
    }
}
