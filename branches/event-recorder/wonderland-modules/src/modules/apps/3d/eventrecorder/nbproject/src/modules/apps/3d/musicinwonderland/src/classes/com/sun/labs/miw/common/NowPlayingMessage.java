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
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;

/**
 * A message to announce which track is currently playing.  The track is
 * identified only by id, the album details should have been sent by a 
 * previous <code>PlayListMessage</code>
 * @author jkaplan
 */
public class NowPlayingMessage extends Message {
    /** the track id that is currently playing */
    private MIWTrack track;

    public NowPlayingMessage() {
        this (null);
    }

    public NowPlayingMessage(MIWTrack track) {
        this.track = track;
    }
    
    /**
     * Get the track that is currently playing
     * @return the id of the current track
     */
    public MIWTrack getTrack() {
        return track;
    }

    protected void extractMessageImpl(ByteBuffer data) {
        track = MIWTrack.value(data);
    }

    protected void populateDataElements() {
        dataElements.clear();
        track.toDataElements(dataElements);
    }    
}
