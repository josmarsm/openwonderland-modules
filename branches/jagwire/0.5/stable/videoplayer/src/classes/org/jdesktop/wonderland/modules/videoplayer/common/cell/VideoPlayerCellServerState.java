/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.videoplayer.common.cell;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellServerState;

/**
 * The WFS server state class for VideoPlayerCellMO.
 * 
 * @author nsimpson
 */
@XmlRootElement(name = "videoplayer-cell")
@ServerState
public class VideoPlayerCellServerState extends App2DCellServerState implements Serializable {

    // the URI of the video
    @XmlElement(name = "mediaURI")
    //public String mediaURI = "file:///movies/tronlegacy-tsr1_480p.mov";
    //public String mediaURI = "http://dl.dropbox.com/u/1937997/Video%20Player/tronlegacy-tsr1_480p.mov";
    public String mediaURI = "";
    // the preferred width of the video player (default to 16:9 aspect ratio)
    @XmlElement(name = "preferredWidth")
    public int preferredWidth = 1024;
    // the preferred height of the video player
    @XmlElement(name = "preferredHeight")
    public int preferredHeight = 576;
    // whether to decorate the window with a frame
    @XmlElement(name = "decorated")
    public boolean decorated = true;
    // the time that the player state last changed
    @XmlElement(name = "stateChangedTime")
    public long stateChangedTime = System.currentTimeMillis();
    // the volume level
    @XmlElement(name = "volume")
    public float volume = 1.0f;
    // the audio radius, in meters
    public float audioRadius = 30f;

    public VideoPlayerCellServerState() {
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.videoplayer.server.cell.VideoPlayerCellMO";
    }

    public void setMediaURI(String mediaURI) {
        this.mediaURI = mediaURI;
    }

    @XmlTransient
    public String getMediaURI() {
        return mediaURI;
    }

    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    @XmlTransient
    public int getPreferredWidth() {
        return preferredWidth;
    }

    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    @XmlTransient
    public int getPreferredHeight() {
        return preferredHeight;
    }

    public void setDecorated(boolean decorated) {
        this.decorated = decorated;
    }

    @XmlTransient
    public boolean getDecorated() {
        return decorated;
    }

    public void setStateChangeTime(long stateChangedTime) {
        this.stateChangedTime = stateChangedTime;
    }

    @XmlTransient
    public long getStateChangeTime() {
        return stateChangedTime;
    }

    public void setAudioRadius(float audioRadius) {
        this.audioRadius = audioRadius;
    }

    @XmlTransient
    public float getAudioRadius() {
        return audioRadius;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    @XmlTransient
    public float getVolume() {
        return volume;
    }
}
