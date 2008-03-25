/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.lg3d.wonderland.videomodule.client.cell;

import java.util.logging.Logger;
import org.jdesktop.lg3d.wonderland.videomodule.common.PTZCamera;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage.Action;

/**
 *
 * @author nsimpson
 */
public class CameraTask extends Thread {

    private static final Logger logger =
            Logger.getLogger(CameraTask.class.getName());
    private PTZCamera ptz;
    private Action action;
    private float pan;
    private float tilt;
    private float zoom;
    private CameraListener listener;

    public CameraTask(PTZCamera ptz, Action action, float pan, float tilt, float zoom) {
        this(ptz, action, pan, tilt, zoom, null);
    }

    public CameraTask(PTZCamera ptz, Action action, float pan, float tilt, float zoom,
            CameraListener listener) {
        this.ptz = ptz;
        this.action = action;
        this.pan = pan;
        this.tilt = tilt;
        this.zoom = zoom;
        this.listener = listener;
    }

    @Override
    public void run() {
        if (ptz != null) {
            switch (action) {
                case SET_PTZ:
                    ptz.panTo((int) pan);
                    ptz.tiltTo((int) tilt);
                    ptz.zoomTo((int) zoom);
                    if (listener != null) {
                        listener.cameraActionComplete(action, pan, tilt, zoom);
                    }
                    break;
            }
        }
    }
}
