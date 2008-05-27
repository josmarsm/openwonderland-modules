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
import org.jdesktop.lg3d.wonderland.darkstar.common.setup.SharedApp2DCellSetup;

/**
 *
 * @author nsimpson
 */
public class TightVNCModuleCellSetup extends SharedApp2DCellSetup {

    private static final Logger logger =
            Logger.getLogger(TightVNCModuleCellSetup.class.getName());
    private static final int DEFAULT_WIDTH = 1024; // XGA resolution
    private static final int DEFAULT_HEIGHT = 768; //
    private static final int DEFAULT_PORT = 5900;
    private String vncServer;
    private int vncPort = DEFAULT_PORT;
    private String username;
    private String password;
    private boolean readOnly = false;
    private int preferredWidth = DEFAULT_WIDTH;
    private int preferredHeight = DEFAULT_HEIGHT;
    private float pixelScale = 1.0f;    // scale factor when mapping from pixels to world units
    
    public TightVNCModuleCellSetup() {
        this(null, null);
    }

    public TightVNCModuleCellSetup(String appName, Matrix4f viewRectMat) {
        super(appName, viewRectMat);
    }

    public void setServer(String vncServer) {
        this.vncServer = vncServer;
    }

    public String getServer() {
        return vncServer;
    }

    public void setPort(int vncPort) {
        this.vncPort = vncPort;
    }

    public int getPort() {
        return vncPort;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
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
    
    public void setPixelScale(float pixelScale) {
        this.pixelScale = pixelScale;
    }
    
    public float getPixelScale() {
        return pixelScale;
    }
}
