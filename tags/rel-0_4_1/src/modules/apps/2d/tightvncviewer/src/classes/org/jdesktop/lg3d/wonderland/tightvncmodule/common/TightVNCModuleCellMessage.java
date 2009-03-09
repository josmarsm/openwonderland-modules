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

import java.nio.ByteBuffer;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataInt;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;

/**
 *
 * @author nsimpson
 */
public class TightVNCModuleCellMessage extends CellMessage {

    private static final int DEFAULT_VNC_PORT = 5900;

    public enum Action {

        UNKNOWN,
        REQUEST_COMPLETE,
        OPEN_SESSION,
        CLOSE_SESSION,
        GET_STATE, SET_STATE
    };

    public enum RequestStatus {

        REQUEST_OK,
        REQUEST_DENIED
    };
    private String uid;
    private Action action = Action.UNKNOWN;
    private String vncServer;
    private int vncPort = DEFAULT_VNC_PORT;
    private String vncUsername;
    private String vncPassword;
    private RequestStatus status = RequestStatus.REQUEST_OK;

    public TightVNCModuleCellMessage() {
        super();
    }

    public TightVNCModuleCellMessage(Action action) {
        this(action, null, 0, null, null);
    }

    public TightVNCModuleCellMessage(Action action, String vncServer, int vncPort, String vncUsername, String vncPassword) {
        super();
        this.action = action;
        this.vncServer = vncServer;
        this.vncPort = vncPort;
        this.vncUsername = vncUsername;
        this.vncPassword = vncPassword;
    }

    public TightVNCModuleCellMessage(CellID cellID, Action action) {
        super(cellID);
        setAction(action);
    }

    public TightVNCModuleCellMessage(CellID cellID, String uid, Action action, String vncServer, int vncPort, String vncUsername, String vncPassword) {
        super(cellID);
        setUID(uid);
        this.action = action;
        this.vncServer = vncServer;
        this.vncPort = vncPort;
        this.vncUsername = vncUsername;
        this.vncPassword = vncPassword;
    }

    public TightVNCModuleCellMessage(TightVNCModuleCellMessage msg) {
        this.setAction(msg.getAction());
        this.setUID(msg.getUID());
        setCellID(msg.getCellID());
        this.setServer(msg.getServer());
        this.setPort(msg.getPort());
        this.setUsername(msg.getUsername());
        this.setPassword(msg.getPassword());
    }

    public void setUID(String uid) {
        this.uid = uid;
    }

    public String getUID() {
        return uid;
    }

    /**
     * Set the action
     * @param action the action
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * Get the action
     * @return the action
     */
    public Action getAction() {
        return action;
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

    /**
     * Set the status of this request
     * @param status the status of the request
     */
    public void setRequestStatus(RequestStatus status) {
        this.status = status;
    }

    /**
     * Get the status of this request
     * @return the request status
     */
    public RequestStatus getRequestStatus() {
        return status;
    }

    /** 
     * Get a string representation of the VNC cell message
     * @return a the cell message as as String
     */
    @Override
    public String toString() {
        return "uid: " + getUID() + ", " +
                "action: " + getAction() + ", " +
                "status: " + getRequestStatus() + ", " +
                "server:" + getServer() + ", " +
                "port: " + getPort() + ", " +
                "user: " + getUsername();
    }

    /**
     * Extract the message from binary data
     */
    @Override
    protected void extractMessageImpl(ByteBuffer data) {
        super.extractMessageImpl(data);

        uid = DataString.value(data);
        action = Action.values()[DataInt.value(data)];
        vncServer = DataString.value(data);
        vncPort = DataInt.value(data);
        vncUsername = DataString.value(data);
        vncPassword = DataString.value(data);
        status = RequestStatus.values()[DataInt.value(data)];

    }

    /**
     * Create a binary version of the message
     */
    @Override
    protected void populateDataElements() {
        super.populateDataElements();

        dataElements.add(new DataString(uid));
        dataElements.add(new DataInt(action.ordinal()));
        dataElements.add(new DataString(vncServer));
        dataElements.add(new DataInt(vncPort));
        dataElements.add(new DataString(vncUsername));
        dataElements.add(new DataString(vncPassword));
        dataElements.add(new DataInt(status.ordinal()));
    }
}
