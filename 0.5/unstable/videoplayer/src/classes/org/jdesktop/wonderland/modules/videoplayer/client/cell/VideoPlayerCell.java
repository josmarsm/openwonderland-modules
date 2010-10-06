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
package org.jdesktop.wonderland.modules.videoplayer.client.cell;

import com.jme.math.Vector2f;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.client.utils.VideoLibraryLoader;
import org.jdesktop.wonderland.modules.videoplayer.client.VideoPlayerApp;
import org.jdesktop.wonderland.modules.videoplayer.client.VideoPlayerImpl;
import org.jdesktop.wonderland.modules.videoplayer.client.VideoPlayerWindow;
import org.jdesktop.wonderland.modules.videoplayer.common.VideoPlayerConstants;
import org.jdesktop.wonderland.modules.videoplayer.common.VideoPlayerActions;
import org.jdesktop.wonderland.modules.videoplayer.common.cell.VideoPlayerCellClientState;

/**
 * Video Player client cell
 *
 * @author nsimpson
 */
@ExperimentalAPI
public class VideoPlayerCell extends App2DCell implements SharedMapListenerCli {

    private static final Logger logger = Logger.getLogger(VideoPlayerCell.class.getName());
    
    // The (singleton) window created by the video player app
    private VideoPlayerWindow videoPlayerWindow;
    // the video player application
    private VideoPlayerApp videoPlayerApp;
    // shared state
    @UsesCellComponent
    private SharedStateComponent ssc;
    private SharedMapCli statusMap;
    private VideoPlayerCellClientState clientState;

    // difference from server time to our local time
    private long timeDiff;

    /**
     * Create an instance of VideoPlayerCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public VideoPlayerCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * Return whether or not video is available on this platform
     * @return true if video is available or false if not
     */
    public static boolean isVideoAvailable() {
        return VideoPlayerImpl.isVideoAvailable();
    }

    /**
     * Initialize the video player with parameters from the server.
     *
     * @param clientState the client state to initialize the cell with
     */
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        clientState = (VideoPlayerCellClientState) state;

        timeDiff = System.currentTimeMillis() - clientState.getServerTime();
    }

    /**
     * This is called when the status of the cell changes.
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        switch (status) {
            case ACTIVE:
                // the cell is now visible
                if (increasing) {
                    if (this.getApp() == null) {
                        videoPlayerApp = new VideoPlayerApp(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/videoplayer/client/resources/Bundle").getString("VIDEO"), clientState.getPixelScale());
                        setApp(videoPlayerApp);
                    }
                    // tell the app to be displayed in this cell.
                    videoPlayerApp.addDisplayer(this);

                    // set initial position above ground
                    float placementHeight = clientState.getPreferredHeight() + 200;
                    placementHeight *= clientState.getPixelScale().y;
                    setInitialPlacementSize(new Vector2f(0f, placementHeight));

                    // this app has only one window, so it is always top-level
                    try {
                        videoPlayerWindow = new VideoPlayerWindow(this, videoPlayerApp,
                                clientState.getPreferredWidth(), clientState.getPreferredHeight(),
                                true, clientState.getPixelScale());
                        videoPlayerWindow.setDecorated(clientState.getDecorated());

                        videoPlayerApp.setWindow(videoPlayerWindow);
                    } catch (InstantiationException ex) {
                        throw new RuntimeException(ex);
                    }

                    // load the video player's status map
                    videoPlayerWindow.setSSC(ssc);
                    statusMap = ssc.get(VideoPlayerConstants.STATUS_MAP);
                    statusMap.addSharedMapListener(this);

                    // get the current video URI
                    SharedString documentURI = statusMap.get(VideoPlayerConstants.MEDIA_URI,
                            SharedString.class);

                    // load the currently open video
                    // when the video has finished loading, the player will
                    // sync with the current state and position (premature to
                    // do here)
                    handleOpenMedia(null, null, documentURI);

                    // both the app and the user want this window to be visible
                    videoPlayerWindow.setVisibleApp(true);
                    videoPlayerWindow.setVisibleUser(this, true);
                }
                break;
            case DISK:
                if (!increasing) {
                    // The cell is no longer visible
                    if (videoPlayerWindow != null) {
                        // disconnect video player from shared state - we don't
                        // want to stop the player for everyone just because we
                        // went out of range
                        videoPlayerWindow.sync(false);
                        // stop playing the video in preparation for removing the
                        // video player cell
                        videoPlayerWindow.stop();
                        // hide the video player window
                        videoPlayerWindow.setVisibleApp(false);

                        App2D.invokeLater(new Runnable() {

                            public void run() {
                                videoPlayerWindow.cleanup();
                                videoPlayerWindow = null;
                            }
                        });
                    }
                }
                break;
        }
    }

    public void propertyChanged(SharedMapEventCli event) {
        SharedMapCli map = event.getMap();
        if (map.getName().equals(VideoPlayerConstants.STATUS_MAP)) {
            // there's only one map, a map containing the state of the viewer,
            // its key determines what changed:
            //
            // MEDIA_URI: new media has been loaded into this viewer
            // MEDIA_STATE: the state of the media has changed
            // MEDIA_POSITION: the media position has changed
            //
            // newData specifies the new value of the key
            // note that there's only one property change processed at a time

            handleStatusChange(event.getPropertyName(), event.getOldValue(),
                    event.getNewValue());
        } else {
            logger.warning("unrecognized shared map: " + map.getName());
        }
    }

    /**
     * Get the approximate time difference between the client and the server.
     * This is used when guessing where to synchronize with the server, based
     * on the last state change time recorded by the server.
     * @return the approximate time difference
     */
    public long getTimeDiff() {
        return timeDiff;
    }

    private void handleStatusChange(String key, SharedData oldData, SharedData newData) {
        if (key.equals(VideoPlayerConstants.MEDIA_URI)) {
            // a new media file
            handleOpenMedia(key, oldData, newData);
        } else if (key.equals(VideoPlayerConstants.MEDIA_POSITION)) {
            // position changed
            handleMediaPositionChanged(key, oldData, newData);
        } else if (key.equals(VideoPlayerConstants.PLAYER_STATE)) {
            // state changed
            handleMediaStateChanged(key, oldData, newData);
        } else if (key.equals(VideoPlayerConstants.STATE_CHANGE_TIME)) {
            // time of state change - ignored
        } else {
            logger.warning("unhandled status change event: " + key);
        }
    }

    private void handleOpenMedia(String media, SharedData oldData, SharedData newData) {
        if ((newData != null) && videoPlayerWindow.isSynced()) {
            String mediaURI = ((SharedString) newData).getValue();
            logger.fine("handle open media: " + mediaURI);
            videoPlayerWindow.openMedia(mediaURI);
        }
    }

    private void handleMediaPositionChanged(String media, SharedData oldData, SharedData newData) {
        if ((newData != null) && videoPlayerWindow.isSynced()) {
            Double position = Double.valueOf(((SharedString) newData).getValue());
            logger.fine("handle set position: " + position);
            videoPlayerWindow.setPosition(position);
        }
    }

    private void handleMediaStateChanged(String key, SharedData oldData, SharedData newData) {
        if ((newData != null) && videoPlayerWindow.isSynced()) {
            String state = ((SharedString) newData).getValue();
            logger.fine("handle state change: " + state);
            if (state.equals(VideoPlayerActions.PLAY.name())) {
                // play
                videoPlayerWindow.play();
            } else if (state.equals(VideoPlayerActions.PAUSE.name())) {
                // pause
                videoPlayerWindow.pause();
            } else if (state.equals(VideoPlayerActions.STOP.name())) {
                // stop
                videoPlayerWindow.stop();
            } 
        }
    }
}
