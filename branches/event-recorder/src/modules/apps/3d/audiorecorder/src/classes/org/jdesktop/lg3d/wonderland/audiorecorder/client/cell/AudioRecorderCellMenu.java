/**
 * Project Looking Glass
 *
 * $RCSfile: AudioRecorderCellMenu.java,v $
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
 * $State: Exp $
 * $Id$
 */

package org.jdesktop.lg3d.wonderland.audiorecorder.client.cell;

import org.jdesktop.lg3d.wonderland.darkstar.client.cell.Cell;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.CellMenu;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.CellMenuListener;


/**
 * Menu for the AudioRecorderCell. 
 * @author Joe Provino
 */
public class AudioRecorderCellMenu extends CellMenu implements CellMenuListener {

    private static AudioRecorderCellMenu audioRecorderCellMenu;

    private AudioRecorderCell currentCell;

    public static AudioRecorderCellMenu getInstance() {
	if (audioRecorderCellMenu == null) {
	    audioRecorderCellMenu = new AudioRecorderCellMenu();
	}

	return audioRecorderCellMenu;
    }

    private AudioRecorderCellMenu() {
	super();

	addCellMenuListener(this);
    }

    public void setActive(Cell cell, String title) {
	currentCell = (AudioRecorderCell) cell;

	super.showVolumeMenu(title);
    }


    public void setVolume(String callId, double volume) {
	currentCell.setVolume(callId, volume);
    }

    

}
