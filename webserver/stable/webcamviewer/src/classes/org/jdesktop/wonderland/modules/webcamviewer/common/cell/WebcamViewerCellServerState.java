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
package org.jdesktop.wonderland.modules.webcamviewer.common.cell;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellServerState;

/**
 * The WFS server state class for WebcamViewerCellMO.
 * 
 * @author nsimpson
 */
@XmlRootElement(name = "webcamviewer-cell")
@ServerState
public class WebcamViewerCellServerState extends App2DCellServerState implements Serializable {

    // the URI of the webcam
    @XmlElement(name = "cameraURI")
    // construction cam:
    public String cameraURI = "http://construction.muhlenberg.edu/axis-cgi/mjpg/video.cgi?showlength=1";
    // fish cam:
    //public String cameraURI = "http://210.236.173.198/axis-cgi/mjpg/video.cgi?camera=&resolution=640x480";
    // traffic cam:
    //public String cameraURI = "http://trafico.sctfe.es/axis-cgi/mjpg/video.cgi?motion=0&camera=2";
    // the preferred width of the webcam viewer (default to 4:3 aspect ratio)
    @XmlElement(name = "preferredWidth")
    public int preferredWidth = 640;
    // the preferred height of the webcam viewer
    @XmlElement(name = "preferredHeight")
    public int preferredHeight = 480;
    // whether to decorate the window with a frame
    @XmlElement(name = "decorated")
    public boolean decorated = true;

    public WebcamViewerCellServerState() {
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.webcamviewer.server.cell.WebcamViewerCellMO";
    }

    public void setCameraURI(String cameraURI) {
        this.cameraURI = cameraURI;
    }

    @XmlTransient
    public String getCameraURI() {
        return cameraURI;
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
