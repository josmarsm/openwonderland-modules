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
 * Message sent by a CMUCellMO to a CMU program manager, informing it to
 * create a program from the given asset uri.
 * @author kevin
 */
public class CreateProgramMessage extends Message {

    private static final long serialVersionUID = 1L;
    private String programURI;
    private CellID cellID;

    /**
     * Standard constructor.
     * @param cellID The ID of the cell for which the program is being created
     * @param programURI The URI of the asset representing the program file
     */
    public CreateProgramMessage(CellID cellID, String programURI) {
        super();
        this.setCellID(cellID);
        this.setProgramURI(programURI);
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
     * Get URI of the relevant asset.
     * @return Current asset URI
     */
    public String getProgramURI() {
        return programURI;
    }

    /**
     * Set URI of the relevant asset.
     * @param programURI New asset URI
     */
    public void setProgramURI(String programURI) {
        this.programURI = programURI;
    }

    @Override
    public String toString() {
        return "Create program [Cell:" + getCellID() + "] [Asset:" + getProgramURI() + "]";
    }
}
