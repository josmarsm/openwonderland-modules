/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.lg3d.wonderland.videomodule.client.cell;

import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage.Action;

/**
 *
 * @author nsimpson
 */
public interface CameraListener {
    public void cameraActionComplete(Action action, float pan, float tilt, float zoom);
}
