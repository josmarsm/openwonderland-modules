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

package org.jdesktop.wonderland.modules.cmu.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.Message;

/**
 *
 * @author kevin
 */
public class CreateProgramMessage extends Message {

    private String programURI;
    private CellID cellID;

    public CreateProgramMessage(CellID cellID, String programURI) {
        super();
        this.setCellID(cellID);
        this.setProgramURI(programURI);
    }

    public CellID getCellID() {
        return cellID;
    }

    public void setCellID(CellID cellID) {
        this.cellID = cellID;
    }

    public String getProgramURI() {
        return programURI;
    }

    public void setProgramURI(String programURI) {
        this.programURI = programURI;
    }

}
