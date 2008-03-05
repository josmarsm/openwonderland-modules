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
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataInt;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataString;

/**
 *
 * @author nsimpson
 */
public class VideoModuleCellMessage extends CellMessage {
    private String videoURL;
    
    public enum Action {
        NULL,
        PAN_LEFT, PAN_RIGHT, PAN_CENTER,
        TILT_UP, TILT_DOWN, TILT_CENTER,
        ZOOM_IN, ZOOM_IN_FULLY, ZOOM_OUT, ZOOM_OUT_FULLY,
        CENTER,
        PLAY, PAUSE, STOP, SET_SOURCE,
        GET_POSITION, SET_POSITION, REPORT_POSITION,
        GET_STATUS, STATUS
    };
    
    public enum PlayerState {
        PLAYING,
        PAUSED,
        STOPPED
    };
    
    private Action action = Action.NULL;
    private PlayerState state = PlayerState.STOPPED;
    private double position;    // nanoseconds (really!)
    
    public VideoModuleCellMessage() {
        super();
    }
    
    public VideoModuleCellMessage(CellID cellID) {
        super(cellID);
    }
    
    public VideoModuleCellMessage(CellID cellID, Action action) {
        super(cellID);
        setAction(action);
    }

    public VideoModuleCellMessage(CellID cellID, String video, Action action, double position) {
        super(cellID);
        setVideoURL(video);
        setAction(action);
        setPosition(position);
    }
    
    public VideoModuleCellMessage(Action action) {
        super();
        setAction(action);
    }
    
    public VideoModuleCellMessage(String videoURL) {
        setVideoURL(videoURL);
    }
    
    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }
    
    public String getVideoURL() {
        return videoURL;
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
    
    @Override
    public String toString() {
        return "video: " + videoURL + ", " +
               "action: " + action + ", " +
               "position: " + position;
    }
    
    @Override
    protected void extractMessageImpl(ByteBuffer data) {
        super.extractMessageImpl(data);
        
        videoURL = DataString.value(data);
        action = Action.values()[DataInt.value(data)];
        state = PlayerState.values()[DataInt.value(data)];
        position = DataDouble.value(data);
    }
    
    @Override
    protected void populateDataElements() {
        super.populateDataElements();
        
        dataElements.add(new DataString(videoURL));
        dataElements.add(new DataInt(action.ordinal()));
        dataElements.add(new DataInt(state.ordinal()));
        dataElements.add(new DataDouble(position));
    }
}
