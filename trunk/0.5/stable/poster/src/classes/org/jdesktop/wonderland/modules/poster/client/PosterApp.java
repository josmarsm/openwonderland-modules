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
package org.jdesktop.wonderland.modules.poster.client;

import org.jdesktop.wonderland.modules.appbase.client.App2D;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.ControlArbNone;

/**
 * Class to represent the poster app. 
 * @author Bernard Horan
 */
public class PosterApp extends App2D {

    /**
     * Create a new instance of PosterApp. This in turn creates
     * and makes visible the single window used by the app.
     *
     * @param name The name of the app.
     * @param pixelScale The horizontal and vertical pixel sizes (in world metres per pixel).
     */
    public PosterApp(String name, Vector2f pixelScale) {
        // default is no ability to take control
        this (name, new ControlArbNone(), pixelScale);
    }
    
    /**
     * Create a new instance of PosterApp. This in turn creates
     * and makes visible the single window used by the app.
     *
     * @param name The name of the app.
     * @param control arb the control arbiter to use for the poster.
     * @param pixelScale The horizontal and vertical pixel sizes (in world metres per pixel).
     */
    protected PosterApp(String name, ControlArb controlArb, Vector2f pixelScale) {
        super (name, controlArb, pixelScale);
        controlArb.setApp(this);
    }
}
