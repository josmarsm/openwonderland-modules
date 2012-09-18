/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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
package uk.ac.essex.wonderland.modules.tightvnccontrol.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Record to represent a Tight VNC Viewer
 * @author Bernard Horan
 */
@XmlRootElement
public class TightVNCRecord implements Serializable {

    @XmlElement
    private int cellID;
    @XmlElement
    private String cellName;
    private String vncServer;
    private String vncUsername;
    private String vncPassword;
    private int vncPort;
    private List<TightVNCAction> actions;

    public TightVNCRecord(int cellID, String cellName, String vncServer, int vncPort, String vncUsername, String vncPassword) {
        this.cellID = cellID;
        this.cellName = cellName;
        this.vncServer = vncServer;
        this.vncPort = vncPort;
        this.vncUsername = vncUsername;
        this.vncPassword = vncPassword;
        this.actions = new ArrayList<TightVNCAction>();
    }

    public TightVNCRecord() {
        
    }    

    public int getCellID() {
        return cellID;
    }

    public String getCellName() {
        return cellName;
    }

    public void addAction(TightVNCAction action) {
        actions.add(action);
    }

    public List<TightVNCAction> getActions() {
        return actions;
    }

    public String getVncServer() {
        return vncServer;
    }

    public int getVncPort() {
        return vncPort;
    }

    public String getVncUsername() {
        return vncUsername;
    }

    public String getVncPassword() {
        return vncPassword;
    }

    public String getVncObscuredPassword() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < vncPassword.length(); i++) {
            builder.append('*');
        }
        return builder.toString();
    }

    /**
     * Test equality of tightvnc records
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof TightVNCRecord) {
            return cellID == ((TightVNCRecord)o).cellID;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + this.cellID;
        return hash;
    }

    public void setVncServer(String vncServer) {
        this.vncServer = vncServer;
    }

    public void setVncPort(int vncPort) {
        this.vncPort = vncPort;
    }

    public void setVncUsername(String vncUsername) {
        this.vncUsername = vncUsername;
    }

    public void setVncPassword(String vncPassword) {
        this.vncPassword = vncPassword;
    }
}
