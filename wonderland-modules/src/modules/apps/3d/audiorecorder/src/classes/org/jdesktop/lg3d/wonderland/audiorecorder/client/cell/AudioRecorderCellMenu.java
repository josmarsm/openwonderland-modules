/*
 * AudioRecorderCellMenu.java
 * 
 * Created on Dec 12, 2007, 7:05:32 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.lg3d.wonderland.audiorecorder.client.cell;

import org.jdesktop.lg3d.wonderland.darkstar.client.cell.Cell;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.CellMenu;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.CellMenuListener;

import java.util.logging.Logger;

/**
 *
 * @author jp1223
 */
public class AudioRecorderCellMenu extends CellMenu implements CellMenuListener {

    private static final Logger logger =
	Logger.getLogger(AudioRecorderCellMenu.class.getName());
    
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

    public void setInactive() {
	super.setInactive();
    }

    public void setVolume(String callId, double volume) {
	currentCell.setVolume(callId, volume);
    }

    public void volumeChanged(String callId, double volume) {
	super.volumeChanged(callId, volume);
    }

}
