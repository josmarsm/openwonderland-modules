/**
 * Project Wonderland
 *
 * $URL$
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
 * $Rev$
 * $Date$
 * $Author$
 */
package com.sun.labs.miw.common;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataInt;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;

/**
 *
 * @author Jeff Moguillansky
 */
public class PlaylistCellMessage extends CellMessage {   
    private PlaylistAction action;
    
    /** the list of tracks */
    private List<MIWTrack> tracks;
    private String album;
    
    public PlaylistCellMessage() {
        super();
    }
    
    public PlaylistCellMessage(CellID cellID, PlaylistAction action, 
                               List<MIWTrack> tracks)
    {
        super(cellID);
        this.action = action;
        this.tracks = tracks;
        this.album = new String("");
    }
    
    public PlaylistCellMessage(CellID cellID, PlaylistAction action, 
                               String album) 
    {
        super(cellID);
        this.action = action;
        this.tracks = new ArrayList<MIWTrack>();
        this.album = album;
    }
    
    /**
     * Get the action to take, either NEW to overwrite the playlist or
     * APPEND_FRONT or APPEND_BACK to add to it
     * @return the action to take
     */
    public PlaylistAction getAction() {
        return action;
    }

    /**
     * Get the list of tracks
     * @return the list of tracks
     */
    public List<MIWTrack> getTracks() {
        return tracks;
    }
    
    public String getAlbum() {
        return album;
    }
    
    @Override
    protected void extractMessageImpl(ByteBuffer data) {
        super.extractMessageImpl(data);
    
        action = PlaylistAction.values()[DataInt.value(data)];
        album = DataString.value(data);
        
        int trackCount = DataInt.value(data);
        tracks = new ArrayList<MIWTrack>();
        for (int i = 0; i < trackCount; i++) {
            tracks.add(MIWTrack.value(data));
        }
    }
    
    @Override
    protected void populateDataElements() {
        super.populateDataElements();
        
        dataElements.add(new DataInt(getAction().ordinal()));
        dataElements.add(new DataString(getAlbum()));
        dataElements.add(new DataInt(getTracks().size()));
        
        for (MIWTrack t : getTracks()) {
            t.toDataElements(dataElements);
        }
    }
}
