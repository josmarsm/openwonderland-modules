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
package org.jdesktop.wonderland.modules.videoplayer.client;

import org.jdesktop.wonderland.modules.appbase.client.App2D;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb.ControlChangeListener;
import org.jdesktop.wonderland.modules.appbase.client.ControlArbMulti;

/**
 * A Video Player application.
 *
 * @author nsimpson
 */
@ExperimentalAPI
public class VideoPlayerApp extends App2D implements ControlChangeListener {

    private VideoPlayerWindow videoPlayerWindow;

    /**
     * Create a new instance of VideoPlayerApp. This in turn creates
     * and makes visible the single window used by the app.
     *
     * @param name the name of the app.
     * @param pixelScale the horizontal and vertical pixel sizes
     * (in world meters per pixel).
     */
    public VideoPlayerApp(String name, Vector2f pixelScale) {
        super(name, new ControlArbMulti(), pixelScale);
        controlArb.setApp(this);
        controlArb.addListener(this);
    }

    /**
     * Set the app's window
     * @param videoPlayerWindow the video player window
     */
    public void setWindow(VideoPlayerWindow videoPlayerWindow) {
        this.videoPlayerWindow = videoPlayerWindow;
    }

    /**
     * Returns the app's window
     * @return the video player app window
     */
    public VideoPlayerWindow getWindow() {
        return videoPlayerWindow;
    }

    /**
     * The state of a control arb you are subscribed to may have changed.
     * The state of whether this user has control or the current set of
     * controlling users may have changed.
     *
     * @param controlArb The control arb that changed.
     */
    public void updateControl(ControlArb controlArb) {
        videoPlayerWindow.showControls(controlArb.hasControl());
    }
}
