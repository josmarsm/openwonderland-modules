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
package org.jdesktop.wonderland.modules.cmu.common.messages.serverclient;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * Message to forward mouse button events to a CMUCellMO, which can then
 * pass them on to the relevant CMU program.
 * @author kevin
 */
//TODO: Figure out how best to serialize these events
public class MouseButtonEventMessage extends CellMessage {

    private final static long serialVersionUID = 1L;
    //private MouseButtonEvent3D mouseEvent;

    /**
     * Standard constructor.
     * @param cellID ID of the relevant cell
     * @param mouseEvent The mouse button event to forward
     */
    public MouseButtonEventMessage(CellID cellID) {//, MouseButtonEvent3D mouseEvent) {
        super(cellID);
        //setMouseEvent(mouseEvent);
    }

    /**
     * Get the mouse event.
     * @return Mouse event
     *
    public MouseButtonEvent3D getMouseEvent() {
        return mouseEvent;
    }*/

    /**
     * Set the mouse event.
     * @param mouseEvent Mouse event
     *
    public void setMouseEvent(MouseButtonEvent3D mouseEvent) {
        this.mouseEvent = mouseEvent;
    }*/
    
}
