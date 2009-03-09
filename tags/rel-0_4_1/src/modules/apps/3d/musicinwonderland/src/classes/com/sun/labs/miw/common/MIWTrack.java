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

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataElement;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataInt;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;

/**
 * Representation of a single track
 * @author jkaplan
 */
public class MIWTrack implements Serializable {
    /** logger */
    private static final Logger logger =
            Logger.getLogger(MIWTrack.class.getName());
    
    /** the track id */
    private String trackId;

    /** the artist name */
    private String artist;

    /** the track name */
    private String name;

    /** the album name */
    private String album;

    /** the audio data */
    private URL audioURL;
    
    /** the track length */
    private int length;
    
    public MIWTrack() {
        this (null);
    }

    public MIWTrack(String trackId) {
        this (trackId, null, null, null, null, 0);
    }
    
    public MIWTrack(String trackId, String name, String artist, String album,
                    URL audioURL, int length) 
    {
        this.trackId = trackId;
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.audioURL = audioURL;
        this.length = length;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
    
    public URL getAudioURL() {
        return audioURL;
    }
    
    public void setAudioURL(URL audioURL) {
        this.audioURL = audioURL;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
    /**
     * Write this track to a list of data elements
     * @param dataElements a list of data elements
     */
    public void toDataElements(List<DataElement> elements) {
        elements.add(new DataString(getTrackId()));
        elements.add(new DataString(getName()));
        elements.add(new DataString(getArtist()));
        elements.add(new DataString(getAlbum()));
        elements.add(new DataString(getAudioURL().toExternalForm()));
        elements.add(new DataInt(getLength()));
    }

    /**
     * Read a track from a ByteBuffer
     * @param data the byte buffer to read from
     * @return a track read from the given buffer
     */
    public static MIWTrack value(ByteBuffer data) {
        String trackId = DataString.value(data);
        String name = DataString.value(data);
        String artist = DataString.value(data);
        String album = DataString.value(data);
        
        URL audioURL = null;
        try {
            audioURL = new URL(DataString.value(data));
        } catch (MalformedURLException mue) {
            logger.log(Level.WARNING, "Error reading audio URL for " +
                       trackId, mue);
        }
        
        int length = DataInt.value(data);
        
        return new MIWTrack(trackId, name, artist, album, audioURL, length);
    }
    
    @Override
    public String toString() {
        return "Track {" + getTrackId() + "}";
    }
    
    public String fullString() {
        StringBuffer out = new StringBuffer();
        out.append(toString() + "\n");
        out.append("  Name     : " + getName() + "\n");
        out.append("  Length   : " + getLength() + "\n");
        out.append("  Audio URL: " + getAudioURL() + "\n");
        return out.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MIWTrack)) {
            return false;
        }

        MIWTrack ot = (MIWTrack) o;
        if (getTrackId() == null) {
            return (ot.getTrackId() == null);
        } else {
            return (getTrackId().equals(ot.getTrackId()));
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.trackId != null ? this.trackId.hashCode() : 0;
        return hash;
    }
}
