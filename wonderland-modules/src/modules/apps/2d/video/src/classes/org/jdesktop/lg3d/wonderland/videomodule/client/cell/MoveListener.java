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

import javax.vecmath.Point3f;

/**
 * A listener for motion events from a LinearMover
 */
public interface MoveListener {
    /**
     * Called when the mover wants to change the position
     * @param position the point in space to move to
     */
    public void move(Point3f position);
    
    public void moveComplete();
}
