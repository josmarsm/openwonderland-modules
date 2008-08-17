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
package com.sun.labs.miw.client.cell;

import com.sun.labs.miw.common.MIWTrack;
import com.sun.labs.miw.common.PlaylistAction;
import java.util.List;
import javax.swing.SwingUtilities;

public class Playlist {
    
    public Playlist() {
        init();
    }
    void init() {
    }

    public void setPlaylist(PlaylistAction action, List<MIWTrack> tracks) {
        switch (action) {
            case NEW:
                this.tracks = tracks;
                break;
            case APPEND_BACK:
                this.tracks.addAll(tracks);
                break;
            case APPEND_FRONT:
                this.tracks.addAll(0,tracks);
                break;
        }
        
        UI.albumCloud.albumQueue.set(this.tracks);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                UI.playlistViewer.updateView();
                UI.albumViewer.refresh();
            }
        });
    }
    
    public void setNowPlaying(MIWTrack track) {
        // find the track in the playist
        int idx = tracks.indexOf(track);
        if (idx != -1) {
            tracks.subList(0, idx).clear();
        }
        
        UI.albumCloud.albumQueue.set(tracks);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                UI.playlistViewer.updateView();
            }
        });
    }
    
    public int indexOf(MIWTrack track) {
        for (int j = 0; j<tracks.size(); j++) {
            MIWTrack t = tracks.get(j);
            if (t.getTrackId().equals(track.getTrackId())) {
                return j;
            }
        }
        return -1;
    }
    public int addTrack(MIWTrack track) {
        tracks.add(track);
        UI.albumCloud.albumQueue.set(tracks);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                UI.playlistViewer.updateView();
            }
        });

        return tracks.size() - 1;
    }
    
    List<MIWTrack> tracks;
    PlaylistViewer playlistView;
}
