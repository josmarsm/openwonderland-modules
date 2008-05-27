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
package com.sun.labs.miw.server.cell;

import com.sun.labs.miw.common.AlbumCloudSetup;
import java.net.URL;
import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;

/**
 *
 * @author jkaplan
 */
public class AlbumCloudCellGLOSetup extends BasicCellGLOSetup<AlbumCloudSetup> {
    private boolean playAudio = false;
    private URL artPath;
    private int minPlaylistSize = 7;
    private int maxPlaylistSize = 21;
    private float audioVolume = 0.2f;
    
    public AlbumCloudCellGLOSetup() {
        this (null, null, null);
    }
    
    public AlbumCloudCellGLOSetup(Bounds bounds, Matrix4d origin,
                                  AlbumCloudSetup setup) 
    {
        super (bounds, origin, AlbumCloudCellGLO.class.getName(), setup);
    }
    
    public boolean getPlayAudio() {
        return playAudio;
    }
    
    public void setPlayAudio(boolean playAudio) {
        this.playAudio = playAudio;
    }

    public URL getAlbumListURL() {
        return artPath;
    }

    public void setAlbumListURL(URL artPath) {
        this.artPath = artPath;
    }

    public int getMinPlaylistSize() {
        return minPlaylistSize;
    }

    public void setMinPlaylistSize(int minPlaylistSize) {
        this.minPlaylistSize = minPlaylistSize;
    }

    public int getMaxPlaylistSize() {
        return maxPlaylistSize;
    }

    public void setMaxPlaylistSize(int maxPlaylistSize) {
        this.maxPlaylistSize = maxPlaylistSize;
    }
    
    public float getAudioVolume() {
        return audioVolume;
    }
    
    public void setAudioVolume(float audioVolume) {
        this.audioVolume = audioVolume;
    }
}
