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
/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.videoplayer.server.cell;

import com.jme.math.Vector2f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.appbase.server.cell.App2DCellMO;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapEventSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapListenerSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.modules.videoplayer.common.VideoPlayerConstants;
import org.jdesktop.wonderland.modules.videoplayer.common.cell.VideoPlayerCellClientState;
import org.jdesktop.wonderland.modules.videoplayer.common.cell.VideoPlayerCellServerState;
import org.jdesktop.wonderland.modules.videoplayer.common.VideoPlayerActions;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A server cell associated with a Video Player.
 *
 * @author nsimpson
 */
@ExperimentalAPI
public class VideoPlayerCellMO extends App2DCellMO implements SharedMapListenerSrv {

    private static final Logger logger = Logger.getLogger(VideoPlayerCellMO.class.getName());
    @UsesCellComponentMO(SharedStateComponentMO.class)
    private ManagedReference<SharedStateComponentMO> sscRef;
    private ManagedReference<SharedMapSrv> statusMapRef;
    // the preferred width
    private int preferredWidth;
    // the preferred height
    private int preferredHeight;
    // whether to decorate the window with a frame
    private boolean decorated;
    // the currently loaded media
    private String mediaURI;
    // the current position (long value as a string)
    private String mediaPosition = "0";
    // the current state of the player (playing, paused, stopped)
    private VideoPlayerActions playerState = VideoPlayerActions.STOP;
    // the time that the player state last changed
    private long stateChangedTime = System.currentTimeMillis();
    // the audio volume
    private float volume;
    // the audio radius
    private float audioRadius;

    public VideoPlayerCellMO() {
        super();
        addComponent(new SharedStateComponentMO(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.videoplayer.client.cell.VideoPlayerCell";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        if (live) {
            // get or create the shared maps we use
            SharedMapSrv statusMap = sscRef.get().get(VideoPlayerConstants.STATUS_MAP);
            statusMap.addSharedMapListener(this);    
            statusMapRef = AppContext.getDataManager().createReference(statusMap);

            updateStatusMap();
        }
    }

    /**
     * Update the status map with the latest values
     */
    protected void updateStatusMap() {
        if (!isLive() || statusMapRef == null) {
            return;
        }

        SharedMapSrv statusMap = statusMapRef.get();

        // put the current status
        statusMap.put(VideoPlayerConstants.MEDIA_URI, SharedString.valueOf(mediaURI));
        statusMap.put(VideoPlayerConstants.MEDIA_POSITION, SharedString.valueOf(mediaPosition));
        statusMap.put(VideoPlayerConstants.PLAYER_STATE, SharedString.valueOf(playerState.name()));
        statusMap.put(VideoPlayerConstants.STATE_CHANGE_TIME, SharedString.valueOf(String.valueOf(System.currentTimeMillis())));
        statusMap.put(VideoPlayerConstants.VOLUME, SharedString.valueOf(String.valueOf(volume)));
        statusMap.put(VideoPlayerConstants.AUDIO_RADIUS, SharedString.valueOf(String.valueOf(audioRadius)));
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public CellClientState getClientState(CellClientState cellClientState,
            WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new VideoPlayerCellClientState(pixelScale);
        }
        ((VideoPlayerCellClientState) cellClientState).setPreferredWidth(preferredWidth);
        ((VideoPlayerCellClientState) cellClientState).setPreferredHeight(preferredHeight);
        ((VideoPlayerCellClientState) cellClientState).setDecorated(decorated);
        ((VideoPlayerCellClientState) cellClientState).setServerTime(System.currentTimeMillis());

        return super.getClientState(cellClientState, clientID, capabilities);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new VideoPlayerCellServerState();
        }
        ((VideoPlayerCellServerState) state).setMediaURI(mediaURI);
        ((VideoPlayerCellServerState) state).setDecorated(decorated);
        ((VideoPlayerCellServerState) state).setPreferredWidth(preferredWidth);
        ((VideoPlayerCellServerState) state).setPreferredHeight(preferredHeight);
        ((VideoPlayerCellServerState) state).setStateChangeTime(stateChangedTime);
        ((VideoPlayerCellServerState) state).setVolume(volume);
        ((VideoPlayerCellServerState) state).setAudioRadius(audioRadius);

        return super.getServerState(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState serverState) {
        super.setServerState(serverState);
        VideoPlayerCellServerState state = (VideoPlayerCellServerState) serverState;

        mediaURI = state.getMediaURI();
        preferredWidth = state.getPreferredWidth();
        preferredHeight = state.getPreferredHeight();
        decorated = state.getDecorated();
        pixelScale = new Vector2f(state.getPixelScaleX(), state.getPixelScaleY());
        stateChangedTime = state.getStateChangeTime();
        volume = state.getVolume();
        audioRadius = state.getAudioRadius();

        updateStatusMap();
    }

    /**
     * {@inheritDoc}
     */
    public boolean propertyChanged(SharedMapEventSrv event) {
        SharedMapSrv map = event.getMap();
        if (map.getName().equals(VideoPlayerConstants.STATUS_MAP)) {
            return handleStatusChange(event.getSenderID(), event.getPropertyName(),
                    event.getOldValue(), event.getNewValue());
        } else {
            logger.warning("unrecognized shared map: " + map.getName());
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    private boolean handleStatusChange(WonderlandClientID sourceID,
            String key, SharedData oldData, SharedData newData) {
        if (key.equals(VideoPlayerConstants.MEDIA_URI)) {
            mediaURI = ((SharedString) newData).getValue();
        } else if (key.equals(VideoPlayerConstants.MEDIA_POSITION)) {
            mediaPosition = ((SharedString) newData).getValue();
        } else if (key.equals(VideoPlayerConstants.PLAYER_STATE)) {
            String statusStr = ((SharedString) newData).getValue();
            playerState = VideoPlayerActions.valueOf(statusStr);
            SharedMapSrv statusMap = sscRef.get().get(VideoPlayerConstants.STATUS_MAP);
            stateChangedTime = System.currentTimeMillis();
            statusMap.put(VideoPlayerConstants.STATE_CHANGE_TIME, SharedString.valueOf(String.valueOf(stateChangedTime)));
        } else if (key.equals(VideoPlayerConstants.VOLUME)) {
            volume = Float.valueOf(((SharedString) newData).getValue());
        } else if (key.equals(VideoPlayerConstants.AUDIO_RADIUS)) {
            audioRadius = Float.valueOf(((SharedString) newData).getValue());
        }

        return true;
    }
}
