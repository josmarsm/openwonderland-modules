/**
 * AudioCellMenu.java
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
 */

package com.sun.labs.miw.client.cell;

import org.jdesktop.lg3d.wonderland.darkstar.client.cell.Cell;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.CellMenu;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.CellMenuListener;

import java.util.logging.Logger;

/**
 *
 * @author jp1223
 */
public class BoxCellMenu extends CellMenu implements CellMenuListener {

    private static final Logger logger =
	Logger.getLogger(BoxCellMenu.class.getName());
    
    private static BoxCellMenu BoxCellMenu;

    private AlbumCloudCell currentCell;

    public static BoxCellMenu getInstance() {
	if (BoxCellMenu == null) {
	    BoxCellMenu = new BoxCellMenu();
	}

	return BoxCellMenu;
    }

    private BoxCellMenu() {
	super();

	addCellMenuListener(this);
    }

    public void setActive(Cell cell, String title) {
	currentCell = (AlbumCloudCell) cell;

	super.showVolumeMenu(title);
    }

    public void setInactive() {
	super.setInactive();
    }

    /*
     * CellMenuListener implementation
     */
    public void setVolume(String callId, double volume) {
	logger.warning("volume for " + callId + " changed to " + volume);

	currentCell.setVolume(callId, volume);
    }

}
