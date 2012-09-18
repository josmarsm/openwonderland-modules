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

/**
 * Listener methods for Video Player's control panel buttons.
 *
 * @author nsimpson
 */
public interface VideoPlayerToolActionListener {

    /**
     * Toggle the display of the video player from in-world to on-HUD
     */
    public void toggleHUDAction();

    /**
     * Open video media action
     */
    public void openMediaAction();

    /**
     * Play video action
     */
    public void playAction();

    /**
     * Pause video action
     */
    public void pauseAction();

    /**
     * Stop video action
     */
    public void stopAction();

    /**
     * Rewind video action
     */
    public void rewindAction();

    /**
     * Skip forward action
     */
    public void forwardAction();

    /**
     * Absolute position action
     */
    public void setPositionAction(double position);

    /**
     * Toggle synchronized state action
     */
    public void syncAction();

    /**
     * Change local volume action
     */
    public void volumeAction();
}
