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

import com.sun.labs.miw.common.AlbumInfoParser;
import com.sun.labs.miw.common.MIWAlbum;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Read an album collection from a URL;
 * @author jkaplan
 */
public class URLAlbumCollection implements AlbumCollection {
    private URL url;
    private Map<String, MIWAlbum> albums;
    
    public URLAlbumCollection(URL url) throws IOException {
        this.url = url;
        
        // read album data
        this.albums = readAlbums(url);
    }
    
    public Collection<MIWAlbum> getAlbums() {
        return albums.values();
    }
    
    public MIWAlbum getAlbum(String name) {
        return albums.get(name);
    }
    
    protected Map<String, MIWAlbum> readAlbums(URL url) throws IOException {
        Map<String, MIWAlbum> out = new TreeMap<String, MIWAlbum>();
        AlbumInfoParser aip = new AlbumInfoParser();
        Collection<MIWAlbum> parsed;
                
        try {
            parsed = aip.parse(url);
        } catch (Exception ex) {
            IOException ioe = new IOException("Error parsing album info");
            ioe.initCause(ex);
            throw ioe;
        }
        
        for (MIWAlbum album : parsed) {
            out.put(album.getName(), album);
        }
        
        return out;
    }
}