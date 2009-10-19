/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
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
 * The WFS server state class for VideoPlayerCellMO
 * 
 * @author nsimpson
 */
@XmlRootElement(name = "videoplayer-cell")
@ServerState
public class VideoPlayerCellServerState extends App2DCellServerState implements Serializable {

    // the URI of the media
    @XmlElement(name = "mediaURI")
    public String mediaURI = "http://movies.apple.com/movies/fox/avatar/avatar2009aug0820a-tsr_h.640.mov";
    // the position within the media
    @XmlElement(name = "mediaPosition")
    public double mediaPosition = 5.0;
    // the preferred width of the video player (default to 4:3 aspect ratio)
    @XmlElement(name = "preferredWidth")
    public int preferredWidth = 640;
    // the preferred height of the video player
    @XmlElement(name = "preferredHeight")
    public int preferredHeight = 480;
    // whether to decorate the window with a frame
    @XmlElement(name = "decorated")
    public boolean decorated = true;

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

    public void setMediaPosition(double mediaPosition) {
        this.mediaPosition = mediaPosition;
    }

    @XmlTransient
    public double getMediaPosition() {
        return mediaPosition;
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
}
