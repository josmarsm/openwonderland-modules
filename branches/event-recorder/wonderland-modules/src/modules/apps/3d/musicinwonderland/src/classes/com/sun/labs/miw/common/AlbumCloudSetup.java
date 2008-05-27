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

import java.net.URL;
import java.util.List;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;

/**
 * Cell setup information
 * @author jkaplan
 */
public class AlbumCloudSetup implements CellSetup {
    private String baseURL;
    private List<MIWTrack> playList;
    private URL albumListURL;
    
    public AlbumCloudSetup(String baseURL, List<MIWTrack> playList,
                           URL albumListURL) 
    {
        this.baseURL = baseURL;
        this.playList = playList;
        this.albumListURL = albumListURL;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public URL getAlbumListURL() {
        return albumListURL;
    }
    
    public List<MIWTrack> getPlayList() {
        return playList;
    }
}
