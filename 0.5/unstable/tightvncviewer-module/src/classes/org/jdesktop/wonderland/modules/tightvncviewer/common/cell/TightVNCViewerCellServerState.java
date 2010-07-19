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
package org.jdesktop.wonderland.modules.tightvncviewer.common.cell;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellServerState;

/**
 * The WFS server state class for TightVNCViewerCellMO.
 * 
 * @author nsimpson
 */
@XmlRootElement(name = "tightvncviewer-cell")
@ServerState
public class TightVNCViewerCellServerState extends App2DCellServerState implements Serializable {

    // the address of the VNC server
    @XmlElement(name = "vncServer")
    public String vncServer = "";
    // the VNC port number of the VNC server
    @XmlElement(name = "vncPort")
    public int vncPort = 5900;
    // the VNC username
    @XmlElement(name = "vncUsername")
    public String vncUsername = "";
    // the VNC password
    @XmlElement(name = "vncPassword")
    public String vncPassword = "";
    // the preferred width of the VNC viewer (default to 4:3 aspect ratio)
    @XmlElement(name = "preferredWidth")
    public int preferredWidth = 1024;
    // the preferred height of the VNC viewer
    @XmlElement(name = "preferredHeight")
    public int preferredHeight = 768;
    // whether to decorate the window with a frame
    @XmlElement(name = "decorated")
    public boolean decorated = true;

    public TightVNCViewerCellServerState() {
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.tightvncviewer.server.cell.TightVNCViewerCellMO";
    }

    public void setVNCServer(String vncServer) {
        this.vncServer = vncServer;
    }

    @XmlTransient
    public String getVNCServer() {
        return vncServer;
    }

    public void setVNCPort(int vncPort) {
        this.vncPort = vncPort;
    }

    @XmlTransient
    public int getVNCPort() {
        return vncPort;
    }

    public void setVNCUsername(String vncUsername) {
        this.vncUsername = vncUsername;
    }

    @XmlTransient
    public String getVNCUsername() {
        return vncUsername;
    }

    public void setVNCPassword(String vncPassword) {
        this.vncPassword = vncPassword;
    }

    @XmlTransient
    public String getVNCPassword() {
        return vncPassword;
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
