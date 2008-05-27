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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataElement;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataInt;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;

/**
 * Representation of a single album
 * @author Jeff Moguillansky
 */
public class MIWAlbum implements Serializable, Comparable<MIWAlbum> {
    /** a logger */
    private static final Logger logger =
            Logger.getLogger(MIWAlbum.class.getName());
    
    /** the album id */
    private String albumID;

    /** the artist name */
    private String artist;

    /** the album name */
    private String name;
    
    /** the URL of the artwork */
    private URL artURL;
    
    /** the album tracks */
    private List<MIWTrack> tracks;
    
    public MIWAlbum() {
        this (null);
    }

    public MIWAlbum(String albumID) {
        this (albumID, null, null, null, new ArrayList<MIWTrack>());
    }

    public MIWAlbum(String albumID, String name, String artist, URL artURL,
                    List<MIWTrack> tracks) 
    {
        this.albumID = albumID;
        this.name = name;
        this.artist = artist;
        this.artURL = artURL;
        this.tracks = tracks;
    }
    
    public String getAlbumId() {
        return albumID;
    }

    public void setAlbumID(String albumID) {
        this.albumID = albumID;
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
    
    public URL getArtURL() {
        return artURL;
    }
    
    public void setArtURL(URL artURL) {
        this.artURL = artURL;
    }
    
    public List<MIWTrack> getTracks() {
        return tracks;
    }
    
    public void setTracks(List<MIWTrack> tracks) {
        this.tracks = tracks;
    }
    /**
     * Write this track to a list of data elements
     * @param dataElements a list of data elements
     */
    public void toDataElements(List<DataElement> elements) {
        elements.add(new DataString(getAlbumId()));
        elements.add(new DataString(getName()));
        elements.add(new DataString(getArtist()));
        elements.add(new DataInt(tracks.size()));
        for (MIWTrack t : tracks) t.toDataElements(elements);
    }

    /**
     * Read a track from a ByteBuffer
     * @param data the byte buffer to read from
     * @return a track read from the given buffer
     */
    public static MIWAlbum value(ByteBuffer data) {
        String albumID = DataString.value(data);
        String name = DataString.value(data);
        String artist = DataString.value(data);
        
        URL artURL = null;
        try {
            artURL = new URL(DataString.value(data));
        } catch (MalformedURLException mue) {
            logger.log(Level.WARNING, "Error reading art URL for " + albumID, 
                       mue);
        }
        
        int numTracks = DataInt.value(data);
        List<MIWTrack> tracks = new Vector<MIWTrack>();
        for (int j = 0; j<numTracks; j++) {
            tracks.add(MIWTrack.value(data));
        }
        
        return new MIWAlbum(albumID, name, artist, artURL, tracks);
    }
    
    /**
     * Sort albums by album id
     */
    public int compareTo(MIWAlbum album) {
        return getAlbumId().compareTo(album.getAlbumId());
    }
    
    @Override
    public String toString() {
        return "Album {" + getAlbumId() + "}";
    }
    
    public String fullString() {
        StringBuffer out = new StringBuffer();
        out.append(toString() + "\n");
        out.append("  Name   : " + getName() + "\n");
        out.append("  Artist : " + getArtist() + "\n");
        out.append("  Art URL: " + getArtURL() + "\n");
        out.append("  Tracks : " + getTracks().size() + "\n");
        
        for (MIWTrack track : getTracks()) {
            out.append(track.fullString());
        }
        
        return out.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MIWAlbum)) {
            return false;
        }

        MIWAlbum ot = (MIWAlbum) o;
        if (getAlbumId() == null) {
            return (ot.getAlbumId() == null);
        } else {
            return (getAlbumId().equals(ot.getAlbumId()));
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.albumID != null ? this.albumID.hashCode() : 0;
        return hash;
    }
}
