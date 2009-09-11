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
package org.jdesktop.wonderland.modules.videoplayer.client;

import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;

/**
 * Class to manage the selected tool
 *
 * @author nsimpson
 */
public class VideoPlayerToolManager implements VideoPlayerToolListener {

    private boolean hudState = false;
    private HUDComponent component;
    private VideoPlayerWindow videoPlayerWindow;

    VideoPlayerToolManager(VideoPlayerWindow pdfViewerWindow) {
        this.videoPlayerWindow = pdfViewerWindow;
    }

    // VideoPlayerToolListener methods
    /**
     * Toggle the display of the video player from in-world to on-HUD
     */
    public void toggleHUD() {
        if (videoPlayerWindow.getDisplayMode().equals(DisplayMode.HUD)) {
            videoPlayerWindow.setDisplayMode(DisplayMode.WORLD);
        } else {
            videoPlayerWindow.setDisplayMode(DisplayMode.HUD);
        }
        videoPlayerWindow.showControls(true);
    }

    /**
     * Open a new media source
     */
    public void openMedia() {
        videoPlayerWindow.openMedia();
    }

    public void openDocument(String mediaURI) {
        videoPlayerWindow.openMedia(mediaURI);
    }

    /**
     * Rewind the media
     */
    public void rewind() {
        videoPlayerWindow.rewind();
    }

    /**
     * Play the media
     */
    public void play() {
        videoPlayerWindow.play();
    }

    /**
     * Pause the media
     */
    public void pause() {
        videoPlayerWindow.pause();
    }

    /**
     * Stop playing the media
     */
    public void stop() {
        videoPlayerWindow.stop();
    }

    /**
     * Fast forward the media
     */
    public void fastForward() {
        videoPlayerWindow.fastForward();
    }

    /**
     * Synchronize with the shared state
     */
    public void sync() {
        videoPlayerWindow.sync(!videoPlayerWindow.isSynced());
    }

    /**
     * Unsynchronize from the shared state
     */
    public void unsync() {
        videoPlayerWindow.sync(!videoPlayerWindow.isSynced());
    }

    public boolean isOnHUD() {
        return (videoPlayerWindow.getDisplayMode().equals(DisplayMode.HUD));
    }
}
