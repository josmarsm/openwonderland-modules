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
package org.jdesktop.wonderland.modules.telepointer.common.cell.messages;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.messages.*;

/**
 *
 * @author paulby
 */
public class TelePointerMessage extends CellMessage {

    private Vector3f pointerPosition;
    private Vector3f pointerDirection;
    private int mouseEventID;       // AWT mouse event ID;

    public TelePointerMessage() {
    }

    /**
     * @return the pointerPosition
     */
    public Vector3f getPointerPosition() {
        return pointerPosition;
    }

    /**
     * @return the pointerDirection
     */
    public Vector3f getPointerDirection() {
        return pointerDirection;
    }

    /**
     * @param pointerPosition the pointerPosition to set
     */
    public void setPointerPosition(Vector3f pointerPosition) {
        this.pointerPosition = pointerPosition;
    }

    /**
     * @param pointerDirection the pointerDirection to set
     */
    public void setPointerDirection(Vector3f pointerDirection) {
        this.pointerDirection = pointerDirection;
    }

    /**
     * @return the mouseEventID
     */
    public int getMouseEventID() {
        return mouseEventID;
    }

    /**
     * @param mouseEventID the mouseEventID to set
     */
    public void setMouseEventID(int mouseEventID) {
        this.mouseEventID = mouseEventID;
    }
}
