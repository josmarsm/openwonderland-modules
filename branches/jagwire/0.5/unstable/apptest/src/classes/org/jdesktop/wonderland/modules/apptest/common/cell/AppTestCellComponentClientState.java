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
package org.jdesktop.wonderland.modules.apptest.common.cell;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 * Client state for the app test cell component.
 *
 * @author deronj
 */
public class AppTestCellComponentClientState extends CellComponentClientState {

    private CellID appTestCellID;
    private String displayName;

    /** Default constructor */
    public AppTestCellComponentClientState() {
    }

    public void setAppTestCellID (CellID appTestCellID) {
        this.appTestCellID = appTestCellID;
    }

    public CellID getAppTestCellID () {
        return appTestCellID;
    }

    public void setDisplayName (String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName () {
        return displayName;
    }
}
