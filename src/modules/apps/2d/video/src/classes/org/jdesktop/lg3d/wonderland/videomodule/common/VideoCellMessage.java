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
        REQUEST_DENIED, REQUEST_COMPLETE,
        SET_SOURCE,
        SET_PTZ,
        PLAY, PAUSE, STOP,
        GET_STATE, SET_STATE,
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
    private float pan;
    private float tilt;
    private float zoom;

    public VideoCellMessage() {
        super();
    }

    public VideoCellMessage(CellID cellID) {
        super(cellID);
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

    @Override
    public String toString() {
        return "uid: " + uid + ", " +
                "source: " + source + ", " +
                "action: " + action + ", " +
                "state: " + state + ", " +
                "position: " + position + ", " +
                "pan: " + pan + ", " +
                "tilt: " + tilt + ", " +
                "zoom: " + zoom;
    }

    @Override
    protected void extractMessageImpl(ByteBuffer data) {
        super.extractMessageImpl(data);

        uid = DataString.value(data);
        source = DataString.value(data);
        action = Action.values()[DataInt.value(data)];
        state = PlayerState.values()[DataInt.value(data)];
        position = DataDouble.value(data);
        pan = DataFloat.value(data);
        tilt = DataFloat.value(data);
        zoom = DataFloat.value(data);
    }

    @Override
    protected void populateDataElements() {
        super.populateDataElements();

        dataElements.add(new DataString(uid));
        dataElements.add(new DataString(source));
        dataElements.add(new DataInt(action.ordinal()));
        dataElements.add(new DataInt(state.ordinal()));
        dataElements.add(new DataDouble(position));
        dataElements.add(new DataFloat(pan));
        dataElements.add(new DataFloat(tilt));
        dataElements.add(new DataFloat(zoom));
    }
}
