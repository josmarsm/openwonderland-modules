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

import java.util.Calendar;
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
    private static final long MIN_REQUEST_THROTTLE = 2500; // 2.5 seconds
    private long requestThrottle = MIN_REQUEST_THROTTLE;

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

    /**
     * Extend the time it takes to process a camera request to a specified
     * period of time.
     * 
     * Camera requests are made over the network using HTTP. The time it takes
     * to process a request depends on the network round trip time. This will
     * vary depending on the number of network hops between the client making
     * the request and the camera. The throttle delay is intended to make the
     * cost of processing a request a constant, so that clients are better
     * synchronized. In reality, the throttle delay is no guarantee that slow
     * or distant clients will be in sync, but they stand a better chance than
     * if throttling were not used. 
     * @param requestThrottle the desired time (in milliseconds) that a camera
     * request should take
     */
    public void setRequestThrottle(long requestThrottle) {
        logger.fine("camera request throttle set to: " + requestThrottle + "ms");
        this.requestThrottle = requestThrottle;
    }

    /**
     * Get the throttle delay
     * @return the throttle delay in milliseconds
     */
    public long getRequestThrottle() {
        return requestThrottle;
    }

    @Override
    public void run() {
        if (ptz != null) {
            switch (action) {
                case SET_PTZ:
                    Calendar then = Calendar.getInstance();
                    ptz.panTo((int) pan);
                    ptz.tiltTo((int) tilt);
                    ptz.zoomTo((int) zoom);
                    Calendar now = Calendar.getInstance();

                    long duration = now.getTimeInMillis() - then.getTimeInMillis();
                    logger.finest("camera command took: " + duration + "ms " +
                            "sleeping for: " + (requestThrottle - duration) + "ms");

                    if (listener != null) {
                        if (duration < requestThrottle) {
                            // request completed sooner than expected, wait
                            try {
                                Thread.sleep(requestThrottle - duration);
                            } catch (InterruptedException e) {
                                logger.fine("camera action throttle sleep interrupted: " + e);
                            }
                        } else if (duration > requestThrottle) {
                            // the request took longer than the throttle delay
                            // this may cause synchronization problems
                            logger.warning("camera request throttle exceeded by: " +
                                    (duration - requestThrottle) + "ms");
                        }

                        listener.cameraActionComplete(action, pan, tilt, zoom);
                    }
                    break;
            }
        }
    }
}
