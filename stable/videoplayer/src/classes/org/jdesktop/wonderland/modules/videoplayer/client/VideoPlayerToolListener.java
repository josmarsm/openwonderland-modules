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

/**
 * Listener methods for video player's control panel buttons
 * @author nsimpson
 */
public interface VideoPlayerToolListener {

    /**
     * Toggle the display of the video player from in-world to on-HUD
     */
    public void toggleHUD();

    /**
     * Open a new media source
     */
    public void openMedia();

    /**
     * Rewind the media
     */
    public void rewind();

    /**
     * Play the media
     */
    public void play();

    /**
     * Pause the media
     */
    public void pause();

    /**
     * Stop playing the media
     */
    public void stop();

    /**
     * Fast forward the media
     */
    public void fastForward();

    /**
     * Synchronize with the shared state
     */
    public void sync();

    /**
     * Unsynchronize from the shared state
     */
    public void unsync();
}
