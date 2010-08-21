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
package org.jdesktop.wonderland.modules.webcamviewer.client;

/**
 * Listener methods for Webcam Viewer's control panel buttons.
 *
 * @author nsimpson
 */
public interface WebcamViewerToolListener {

    /**
     * Toggle the display of the webcam viewer from in-world to on-HUD
     */
    public void toggleHUD();

    /**
     * Connect to a webcam
     */
    public void connectCamera();

    /** 
     * Connect to a specific webcam
     */
    public void connectCamera(String cameraURI, String username, String password);

    /**
     * Play webcam video
     */
    public void play();

    /**
     * Pause webcam video
     */
    public void pause();

    /**
     * Stop playing webcam video
     */
    public void stop();

    /**
     * Toggle synchronized state on/off
     */
    public void sync();
}
