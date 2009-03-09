/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.lg3d.wonderland.tightvncmodule.common;

import java.util.logging.Logger;
import javax.vecmath.Matrix4f;
import org.jdesktop.lg3d.wonderland.darkstar.common.setup.SharedApp2DImageCellSetup;
import org.jdesktop.lg3d.wonderland.tightvncmodule.client.cell.TightVNCModuleApp;

/**
 *
 * @author nsimpson
 */
public class TightVNCModuleCellSetup extends SharedApp2DImageCellSetup {

    private static final Logger logger =
            Logger.getLogger(TightVNCModuleCellSetup.class.getName());
    private static final int DEFAULT_WIDTH = 1024; // XGA resolution
    private static final int DEFAULT_HEIGHT = 768; //
    private static final int DEFAULT_PORT = 5900;
    private long controlTimeout = 90 * 1000;    // how long a client can retain control (ms)
    private String vncServer;
    private int vncPort = DEFAULT_PORT;
    private String vncUsername;
    private String vncPassword;
    private boolean readOnly = false;
    private int preferredWidth = DEFAULT_WIDTH;
    private int preferredHeight = DEFAULT_HEIGHT;
    private boolean decorated = true;   // show window decorations
    private float pixelScale = 1.0f;    // scale factor when mapping from pixels to world units

    public TightVNCModuleCellSetup() {
        this(null, null, null);
    }

    public TightVNCModuleCellSetup(String appName, Matrix4f viewRectMat, String clientClassName) {
        super(appName, viewRectMat, clientClassName);
    }

    /**
     * Return the classname of the AppWindowGraphics2DApp subclass
     */
    @Override
    public String getClientClassname() {
        return TightVNCModuleApp.class.getName();
    }

    /**
     * Set the hostname of the VNC server
     * @param vncServer the hostname of the VNC server
     */
    public void setServer(String vncServer) {
        this.vncServer = vncServer;
    }

    /**
     * Get the hostname of the VNC server
     * @return the hostname of the VNC server
     */
    public String getServer() {
        return vncServer;
    }

    /**
     * Set the VNC server's port number 
     * @param vncPort the VNC server's port number
     */
    public void setPort(int vncPort) {
        this.vncPort = vncPort;
    }

    /**
     * Get the VNC server's port numer
     * @return the port number of the VNC server
     */
    public int getPort() {
        return vncPort;
    }

    /**
     * Set the VNC username 
     * @param vncUsername the name of the user establishing the VNC session
     */
    public void setUsername(String vncUsername) {
        this.vncUsername = vncUsername;
    }

    /**
     * Get the VNC username
     * @return the name of the user for establishing the VNC session
     */
    public String getUsername() {
        return vncUsername;
    }

    /**
     * Set the VNC password 
     * @param vncPassword the password of the user establishing the VNC session
     */
    public void setPassword(String vncPassword) {
        this.vncPassword = vncPassword;
    }

    /**
     * Get the VNC password
     * @return the password of the user establishing the VNC session
     */
    public String getPassword() {
        return vncPassword;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean getReadOnly() {
        return readOnly;
    }

    /*
     * Set the preferred width
     * @param preferredWidth the preferred width in pixels
     */
    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    /*
     * Get the preferred width
     * @return the preferred width, in pixels
     */
    public int getPreferredWidth() {
        return preferredWidth;
    }

    /*
     * Set the preferred height
     * @param preferredHeight the preferred height, in pixels
     */
    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    /*
     * Get the preferred height
     * @return the preferred height, in pixels
     */
    public int getPreferredHeight() {
        return preferredHeight;
    }

    /** 
     * Set the window decoration status
     * @param decorated whether to show or hide the window decorations
     */
    public void setDecorated(boolean decorated) {
        this.decorated = decorated;
    }

    /**
     * Get the window decoration status
     * @return true if the window decorations are enabled, false otherwise
     */
    public boolean getDecorated() {
        return decorated;
    }

    public void setPixelScale(float pixelScale) {
        this.pixelScale = pixelScale;
    }

    public float getPixelScale() {
        return pixelScale;
    }

    /**
     * Set the timeout for client requests
     * Clients that take longer than this time to process a request
     * will lose control
     * @param controlTimeout maximum time in milliseconds that a client can
     * retain control
     */
    public void setControlTimeout(long controlTimeout) {
        this.controlTimeout = controlTimeout;
    }

    /**
     * Get the control timeout (the length of time a client can have 
     * control before the server takes control away from the client)
     * @return the control timeout in milliseconds
     */
    public long getControlTimeout() {
        return controlTimeout;
    }
}
