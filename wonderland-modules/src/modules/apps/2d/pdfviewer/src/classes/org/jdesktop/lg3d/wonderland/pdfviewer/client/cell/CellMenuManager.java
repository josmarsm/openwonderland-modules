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
package org.jdesktop.lg3d.wonderland.pdfviewer.client.cell;

import org.jdesktop.lg3d.wonderland.darkstar.client.cell.Cell;
import java.util.logging.Logger;

/**
 *
 * @author jh215363
 */
public class CellMenuManager {

    private static final Logger logger =
	Logger.getLogger(CellMenuManager.class.getName());
    
    private static CellMenuManager cellMenuManager;

    private static CellMenu currentCellMenu;

    private String title;

    public static synchronized CellMenuManager getInstance() {
        if (cellMenuManager == null) {
            cellMenuManager = new CellMenuManager();
	}
        
        return cellMenuManager;
    }
    
    private CellMenuManager() {
    }

    public void showMenu(Cell cell, CellMenu cellMenu, String title) {
	this.title = title;

	if (currentCellMenu != cellMenu) {
	    hideMenu();	// hide existing menu
	}

	currentCellMenu = cellMenu;
	currentCellMenu.setActive(cell, title, true);
    }

    public void hideMenu() {
	if (currentCellMenu != null) {
	    currentCellMenu.setInactive();
	}
    }
}
