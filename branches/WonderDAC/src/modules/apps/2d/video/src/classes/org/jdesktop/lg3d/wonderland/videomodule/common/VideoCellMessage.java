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
package org.jdesktop.lg3d.wonderland.videomodule.common;

import java.nio.ByteBuffer;

import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataDouble;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataFloat;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataInt;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;

/**
 *
 * @author nsimpson
 */
public class VideoCellMessage extends CellMessage {

    public enum Action {

        UNKNOWN,
        REQUEST_COMPLETE,
        SET_SOURCE,
        SET_PTZ,
        PLAY, PAUSE, STOP, REWIND, FAST_FORWARD,
        GET_STATE, SET_STATE,
        NO_ALTER_PERM,   // TW -- Not much of an action, per se.
                         // more of an alert that the client does
                         // not possess alter permissions for the
                         // cell in question.                
        ALTER_PERM,      // Same as above, just the opposite boolean
                         // value.  TW
        PING             // A simple *ping* to elicit a sync message
                         // from a client.  Used when privileges have
                         // changed and the client needs to re-establish
                         // whether or not they can alter the cell.  TW
    };

    public enum RequestStatus {

        REQUEST_OK,
        REQUEST_DENIED
    };

    public enum PlayerState {
        PLAYING,
        PAUSED,
        STOPPED
    };
    
    private String uid;
    private Action action = Action.UNKNOWN;
    private String source;
    private double position;    // nanoseconds (really!)
    private PlayerState state = PlayerState.STOPPED;
    private RequestStatus status = RequestStatus.REQUEST_OK;
    private float pan;
    private float tilt;
    private float zoom;

    public VideoCellMessage() {
        super();
    }

    public VideoCellMessage(CellID cellID) {
        super(cellID);
    }

    public VideoCellMessage(CellID cellID, Action action) {
        super(cellID);
        setAction(action);
    }
    
    public VideoCellMessage(CellID cellID, String uid, Action action) {
        super(cellID);
        setUID(uid);
        setAction(action);
    }

    public VideoCellMessage(CellID cellID, String uid, String video, Action action, double position) {
        super(cellID);
        setUID(uid);
        setSource(video);
        setAction(action);
        setPosition(position);
    }

    public VideoCellMessage(VideoCellMessage vcm) {
        setUID(vcm.getUID());
        setCellID(vcm.getCellID());
        setSource(vcm.getSource());
        setAction(vcm.getAction());
        setPosition(vcm.getPosition());
        setState(vcm.getState());
        setPTZPosition(vcm.getPan(), vcm.getTilt(), vcm.getZoom());
    }

    public VideoCellMessage(Action action) {
        super();
        setAction(action);
    }

    public VideoCellMessage(String source) {
        setSource(source);
    }

    public void setUID(String uid) {
        this.uid = uid;
    }

    public String getUID() {
        return uid;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public double getPosition() {
        return position;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }

    public PlayerState getState() {
        return state;
    }

    public void setPTZPosition(float pan, float tilt, float zoom) {
        this.pan = pan;
        this.tilt = tilt;
        this.zoom = zoom;
    }

    public float getPan() {
        return pan;
    }

    public float getTilt() {
        return tilt;
    }

    public float getZoom() {
        return zoom;
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

    @Override
    public String toString() {
        return "uid: " + getUID() + ", " +
                "action: " + getAction() + ", " +
                "status: " + getRequestStatus() + ", " +
                "source: " + getSource() + ", " +
                "state: " + getState() + ", " +
                "position: " + getPosition() + ", " +
                "pan: " + getPan() + ", " +
                "tilt: " + getTilt() + ", " +
                "zoom: " + getZoom();
    }

    @Override
    protected void extractMessageImpl(ByteBuffer data) {
        super.extractMessageImpl(data);

        action = Action.values()[DataInt.value(data)];  // Move action up to front of list
                                                        // so we can check for it below.
                                                        // TW
        
        if ((action != Action.NO_ALTER_PERM) &&
            (action != Action.ALTER_PERM) &&
            (action != Action.PING)) {  // TW
            uid = DataString.value(data);
            source = DataString.value(data);
            state = PlayerState.values()[DataInt.value(data)];
            position = DataDouble.value(data);
            pan = DataFloat.value(data);
            tilt = DataFloat.value(data);
            zoom = DataFloat.value(data);
        }
        status = RequestStatus.values()[DataInt.value(data)];
    }

    @Override
    protected void populateDataElements() {
        super.populateDataElements();

        dataElements.add(new DataInt(action.ordinal()));  // Move action up to front of list
                                                          // so we can check for it below.
                                                          // TW
        
        if ((action != Action.NO_ALTER_PERM) &&
            (action != Action.ALTER_PERM) &&
            (action != Action.PING)) {  // TW
            dataElements.add(new DataString(uid));
            dataElements.add(new DataString(source));
            dataElements.add(new DataInt(state.ordinal()));
            dataElements.add(new DataDouble(position));
            dataElements.add(new DataFloat(pan));
            dataElements.add(new DataFloat(tilt));
            dataElements.add(new DataFloat(zoom));
        }
        dataElements.add(new DataInt(status.ordinal()));
    }
}
