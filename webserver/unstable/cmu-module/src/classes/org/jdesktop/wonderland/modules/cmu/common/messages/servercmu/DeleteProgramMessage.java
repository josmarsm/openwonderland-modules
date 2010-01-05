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
import org.jdesktop.wonderland.modules.cmu.common.UnloadSceneReason;

/**
 * Message to inform the CMU program manager that a particular program
 * should be destroyed.
 * @author kevin
 */
public class DeleteProgramMessage extends ServerCMUMessage {

    private UnloadSceneReason reason;

    /**
     * Standard constructor.
     * @param cellID The ID of the cell whose program is to be deleted
     * @param reason The reason the program is being deleted
     */
    public DeleteProgramMessage(CellID cellID, UnloadSceneReason reason) {
        super(cellID);
        this.reason = reason;
    }

    /**
     * Get the reason for the deletion of the program.
     * @return Reason for deleting the program
     */
    public UnloadSceneReason getReason() {
        return reason;
    }

    /**
     * Set the reason for deletion of the program.
     * @param reason Reason for deleting the program
     */
    public void setReason(UnloadSceneReason reason) {
        this.reason = reason;
    }
}
