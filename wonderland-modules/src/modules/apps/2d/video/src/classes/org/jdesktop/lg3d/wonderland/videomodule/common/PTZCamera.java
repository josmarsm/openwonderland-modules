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
package org.jdesktop.lg3d.wonderland.videomodule.common;

/**
 *
 * @author nsimpson
 */
public interface PTZCamera extends Camera {

    public void resetCameraPosition();

    public void setPTZPosition(float p, float t, float z);
            
    public float getMinPan();

    public float getMaxPan();

    public float getPan();

    public float getMinTilt();

    public float getMaxTilt();

    public float getTilt();

    public float getMinZoom();

    public float getMaxZoom();

    public float getZoom();

    public float getMinHorizontalFOV();

    public float getMinVerticalFOV();

    public void panTo(float pan);

    public void tiltTo(float tilt);

    public void zoomTo(float zoom);

    public void panBy(float delta);

    public void tiltBy(float delta);

    public void zoomBy(float delta);

    public void center();

    public void zoomInFully();

    public void zoomOutFully();
}
