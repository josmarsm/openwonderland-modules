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


import com.sun.labs.miw.common.AlbumInfoParser;
import com.sun.labs.miw.common.MIWAlbum;
import com.sun.labs.miw.common.MIWTrack;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.service.TransactionProxy;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * This service provides music information in the Darkstar server.
 * <p>
 * The {@link #MusicServiceImpl constructor} supports the following properties:
 * <p>
 *
 * <ul>
 * <li> <i>Key:</i> <code>com.sun.labs.miw.service.musicDatabase</code> <br>
 *	<i>No default &mdash; required</i> <br>
 *	Specifies the path to the music database to use.<p>
 * </ul>
 *
 * @author jkaplan
 */
public class MusicServiceImpl implements MusicService {
    /** the name of the database property */
    private static final String DB_PROP =
        "com.sun.labs.miw.service.musicDatabase";
    
    private Map<String, MIWAlbum> albums = new TreeMap<String, MIWAlbum>();
    
    /**
     * Create a new instance of SITMServiceImpl.
     * @param properties the properties for this service
     * @param systemRegistry the system services
     */
    public MusicServiceImpl(Properties properties,
                            ComponentRegistry systemRegistry) 
    {
        // read the database location property
        String db = properties.getProperty(DB_PROP);
        if (db == null) {
            throw new IllegalArgumentException("Property " + DB_PROP + 
                                               " is required.");
        }
        
        // load the database
        AlbumInfoParser parser = new AlbumInfoParser();
        Collection<MIWAlbum> parsedAlbums;
        
        try {
            URL url = new URL(db);
            parsedAlbums = parser.parse(url);
        } catch (MalformedURLException mue) {
            throw new IllegalArgumentException("Bad database URL: " + db, mue);
        } catch (ParserConfigurationException pce) {
            throw new IllegalStateException("Unable to configure parser", pce);
        } catch (SAXException se) {
            throw new IllegalStateException("SAX Parser error", se);
        } catch (IOException ioe) {
            throw new IllegalStateException("Error reading database " + db, ioe);
        }
        
        for (MIWAlbum album : parsedAlbums) {
            albums.put(album.getName(), album);
        }
    }

    /**
     * @{inheritDoc}
     */
    public MIWAlbum getAlbum(String albumName) {
        return albums.get(albumName);
    }

    /**
     * @{inheritDoc}
     */
    public String getAudioFile(MIWTrack track) {
        return track.getAudioURL().toExternalForm();
    }

    /**
     * @{inheritDoc}
     */
    public List<MIWTrack> suggestTracks(int trackCount) {
        List<MIWTrack> out = new ArrayList<MIWTrack>();
        for (int i = 0; i < trackCount; i++) {
            out.add(randomTrack());
        }
        return out;
    }
    
    /**
     * Get a random track
     * @return the random track
     */
    protected MIWTrack randomTrack() {
        List<MIWAlbum> all = new ArrayList(albums.values());
        MIWAlbum album = all.get((int) (Math.random() * all.size()));
        
        List<MIWTrack> tracks = album.getTracks();
        return tracks.get((int) (Math.random() * tracks.size()));
    }
    
    /**
     * @inheritDoc
     */
    public String getName() {
        return MusicServiceImpl.class.getName();
    }

    /**
     * @inheritDoc
     */
    public void configure(ComponentRegistry reg, TransactionProxy txn) {
        // nothing to do
    }

    /**
     * @inheritDoc
     */
    public boolean shutdown() {
        return true;
    }
}
