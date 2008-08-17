/**
 * Project Looking Glass
 *
 * $RCSfile$
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
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.lg3d.wonderland.tightvncmodule.server.cell;

import com.sun.sgs.app.ManagedObject;
import java.io.Serializable;
import java.util.Calendar;
import java.util.logging.Logger;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.TightVNCModuleCellSetup;

/**
 *
 * @author nsimpson
 */
public class TightVNCModuleStateMO implements Serializable, ManagedObject {

    private static final Logger logger =
            Logger.getLogger(TightVNCModuleStateMO.class.getName());
    private TightVNCModuleCellSetup setup;
    private String id;
    private Calendar controlOwnedDate = null;

    public TightVNCModuleStateMO(TightVNCModuleCellSetup setup) {
        this.setup = setup;
    }

    public void setControllingCell(String id) {
        this.id = id;
        if (id != null) {
            controlOwnedDate = Calendar.getInstance();
        } else {
            controlOwnedDate = null;
        }
    }

    public String getControllingCell() {
        return id;
    }

    public TightVNCModuleCellSetup getCellSetup() {
        return setup;
    }

    /**
     * Set the hostname of the VNC server
     * @param vncServer the hostname of the VNC server
     */
    public void setServer(String vncServer) {
        setup.setServer(vncServer);
    }

    /**
     * Get the hostname of the VNC server
     * @return the hostname of the VNC server
     */
    public String getServer() {
        return setup.getServer();
    }

    /**
     * Set the VNC server's port number 
     * @param vncPort the VNC server's port number
     */
    public void setPort(int vncPort) {
        setup.setPort(vncPort);
    }

    /**
     * Get the VNC server's port numer
     * @return the port number of the VNC server
     */
    public int getPort() {
        return setup.getPort();
    }

    /**
     * Set the VNC username 
     * @param vncUsername the name of the user establishing the VNC session
     */
    public void setUsername(String vncUsername) {
        setup.setUsername(vncUsername);
    }

    /**
     * Get the VNC username
     * @return the name of the user for establishing the VNC session
     */
    public String getUsername() {
        return setup.getUsername();
    }

    /**
     * Set the VNC password 
     * @param vncPassword the password of the user establishing the VNC session
     */
    public void setPassword(String vncPassword) {
        setup.setPassword(vncPassword);
    }

    /**
     * Get the VNC password
     * @return the password of the user establishing the VNC session
     */
    public String getPassword() {
        return setup.getPassword();
    }

    /*
     * Set the preferred width
     * @param preferredWidth the preferred width in pixels
     */
    public void setPreferredWidth(int preferredWidth) {
        setup.setPreferredWidth(preferredWidth);
    }

    /*
     * Get the preferred width
     * @return the preferred width, in pixels
     */
    public int getPreferredWidth() {
        return setup.getPreferredWidth();
    }

    /*
     * Set the preferred height
     * @param preferredHeight the preferred height, in pixels
     */
    public void setPreferredHeight(int preferredHeight) {
        setup.setPreferredHeight(preferredHeight);
    }

    /*
     * Get the preferred height
     * @return the preferred height, in pixels
     */
    public int getPreferredHeight() {
        return setup.getPreferredHeight();
    }

    public long getControlOwnedDuration() {
        long ownedDuration = 0;

        if (controlOwnedDate != null) {
            Calendar now = Calendar.getInstance();
            ownedDuration = now.getTimeInMillis() - controlOwnedDate.getTimeInMillis();
        }

        return ownedDuration;
    }
}
