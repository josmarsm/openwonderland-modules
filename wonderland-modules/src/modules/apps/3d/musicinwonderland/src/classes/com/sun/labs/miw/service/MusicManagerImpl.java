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
 * Implementation of MusicManager.  Just pass everything to the underlying 
 * manager.
 * @author jkaplan
 */
public class MusicManagerImpl implements MusicManager {
    // the underlying service
    private MusicService service;

    public MusicManagerImpl(MusicService service) {
        this.service = service;
    }

    public MIWAlbum getAlbum(String albumName) {
        return service.getAlbum(albumName);
    }

    public String getAudioFile(MIWTrack track) {
        return service.getAudioFile(track);
    }

    public List<MIWTrack> suggestTracks(int trackCount) {
        return service.suggestTracks(trackCount);
    }
}
