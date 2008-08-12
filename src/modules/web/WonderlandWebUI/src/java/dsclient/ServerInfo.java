/**
 * Project Looking Glass
 *
 * $RCSfile: ServerInfo.java,v $
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
 * $Revision: 1.2 $
 * $Date: 2007/10/26 00:41:44 $
 * $State: Exp $
 */
package dsclient;


/**
  * Properties for each server the ManagerUI knows about
  */
public class ServerInfo {
    private String serverName = null;
    private String port = null;
    private String username="ServerManager";
    private char[] password = null;
    private boolean passwordPersisted = false;

    public ServerInfo() {
    }

    public ServerInfo(String serverName, String port) {
        this.serverName = serverName;
        this.port = port;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ServerInfo))
            return false;

        ServerInfo si = (ServerInfo)o;

        if (si.serverName.equals(serverName) && si.port.equals(port))
            return true;

        return false;
    }

    /**
      * Get the management account username for this server
      */
    public String getUsername() {
        return username;
    }

    /**
      * Set the management account username for this server
      */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
      * Get the management account password for this server, null if no password
      * is available
      */
    public char[] getPassword() {
        return password;
    }

    /**
      * Set the password for the management account
      */
    public void setPassword(char[] password) {
        this.password = password;
    }

    /**
     * Returns true if the password should be persisted between runs
     * 
     * @return
     */
    public boolean isPasswordPersisted() {
        return passwordPersisted;
    }

    public void setPasswordPersisted(boolean rememberPassword) {
        this.passwordPersisted = rememberPassword;
    }
}