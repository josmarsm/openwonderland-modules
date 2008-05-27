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
package com.sun.labs.miw.service;

import com.sun.labs.miw.common.MIWAlbum;
import com.sun.labs.miw.common.MIWTrack;
import java.util.List;

/**
 * Provides music information as a Darkstar service.
 *
 * @author jkaplan
 */
public interface MusicManager {

    /** 
     * Get an album by name
     * @param albumName the name of the album to get
     * @return the album with the given name
     */
    public MIWAlbum getAlbum(String albumName);

    /**
     * Get the audio file for the given track
     * @param track the track to get an audio file for
     * @return the audio file for the given track, or null if there
     * is no file for the given track
     */
    public String getAudioFile(MIWTrack track);
  
    /**
     * Suggest tracks for the playlist
     * @param trackCount the number of tracks to sugges
     * @return a list of suggested tracks of the given size
     */
    public List<MIWTrack> suggestTracks(int trackCount);
}
