/**
 * Project Looking Glass
 * 
 * $RCSfile: SampleModuleCellSetup.java,v $
 * 
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 * 
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 * 
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 * 
 * $Revision: 1.2 $
 * $Date: 2008/01/23 18:48:44 $
 * $State: Exp $ 
 */
package org.jdesktop.lg3d.wonderland.samplemodule.common;

import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;

/**
 *
 * @author jkaplan
 */
public class SampleModuleCellSetup implements CellSetup {

    private String selectionID;

    public String getSelectionID() {
        return selectionID;
    }

    public void setSelectionID(String selectionID) {
        this.selectionID = selectionID;
    }
}
