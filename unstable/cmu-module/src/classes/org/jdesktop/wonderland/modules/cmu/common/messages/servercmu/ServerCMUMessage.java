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
package org.jdesktop.wonderland.modules.cmu.common.messages.servercmu;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * Abstract class to represent a message from the server to the CMU
 * program manager regarding a particular program.  The relevant program
 * can be found based on the cell ID with which it is associated.
 * @author kevin
 */
public abstract class ServerCMUMessage extends Message {

    private CellID cellID;

    /**
     * Standard constructor.
     * @param cellID ID for the cell whose program this message applies to
     */
    public ServerCMUMessage(CellID cellID) {
        setCellID(cellID);
    }

    /**
     * Get ID of the relevant cell.
     * @return Current cell ID
     */
    public CellID getCellID() {
        return cellID;
    }

    /**
     * Set ID of the relevant cell.
     * @param cellID New cell ID
     */
    public void setCellID(CellID cellID) {
        this.cellID = cellID;
    }

    /**
     * Get a String representation of the message, with debug info.
     * @return String representation of the message
     */
    @Override
    public String toString() {
        return getClass().getName() + "[cellID=" + cellID + "]";
    }
}
