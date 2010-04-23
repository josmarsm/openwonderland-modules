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
package org.jdesktop.wonderland.modules.cmu.common;

import java.util.ArrayList;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.cmu.common.events.EventResponseList;
import org.jdesktop.wonderland.modules.cmu.common.events.responses.CMUResponseFunction;

/**
 * Client state for the CMU class; contains connection information
 * as well as properties about the CMU program which can be displayed.
 * @author kevin
 */
public class CMUCellClientState extends CellClientState {

    private boolean serverAndPortInitialized = false;
    private boolean playing;
    private float playbackSpeed;
    private boolean groundPlaneShowing;
    private String server;
    private int port;
    private String sceneTitle;
    private EventResponseList eventList;
    private ArrayList<CMUResponseFunction> allowedResponses;

    /**
     * Get the title of the scene.
     * @return Title of the scene
     */
    public synchronized String getSceneTitle() {
        return sceneTitle;
    }

    /**
     * Set the title of the scene.
     * @param sceneTitle Title of the scene
     */
    public synchronized void setSceneTitle(String sceneTitle) {
        this.sceneTitle = sceneTitle;
    }

    /**
     * Get the list of Wonderland events which this scene should respond to.
     * @return List of Wonderland events with responses
     */
    public synchronized EventResponseList getEventList() {
        return eventList;
    }

    /**
     * Set the list of Wonderland events which this scene should respond to.
     * @param eventList List of Wonderland events with responses
     */
    public synchronized void setEventList(EventResponseList eventList) {
        this.eventList = eventList;
    }

    public ArrayList<CMUResponseFunction> getAllowedResponses() {
        return allowedResponses;
    }

    public void setAllowedResponses(ArrayList<CMUResponseFunction> allowedResponses) {
        this.allowedResponses = allowedResponses;
    }

    /**
     * Set the server and port on which the relevant socket is running,
     * and store that these have been set.  Note that we can only
     * set both values at once.
     * @param server The host address to connect to
     * @param port The port on which to connect
     */
    public synchronized void setServerAndPort(String server, int port) {
        this.serverAndPortInitialized = true;
        this.server = server;
        this.port = port;
    }

    /**
     * Get the server on which the relevant socket is running.
     * @return The host address to connect to
     */
    public synchronized String getServer() {
        return server;
    }

    /**
     * Get the port to which the relevant socket is listening.
     * @return The port to connect to
     */
    public synchronized int getPort() {
        return port;
    }

    /**
     * Get the current playback speed of the CMU program.
     * @return Current playback speed
     */
    public synchronized float getPlaybackSpeed() {
        return playbackSpeed;
    }

    /**
     * Set the current playback speed of the CMU program.
     * @param playbackSpeed New playback speed
     */
    public synchronized void setPlaybackSpeed(float playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
    }

    /**
     * Check whether the server and port of this client state are valid to
     * use.  Their values should not be treated as valid connection information
     * unless this returns true.
     * @return True if connection information has been provided to this state
     */
    public synchronized boolean isServerAndPortInitialized() {
        return serverAndPortInitialized;
    }

    /**
     * Get the play/pause state of the scene.
     * @return True if playing, false if paused
     */
    public synchronized boolean isPlaying() {
        return playing;
    }

    /**
     * Set the play/pause state of the scene.
     * @param playing True if playing, false if paused
     */
    public synchronized void setPlaying(boolean playing) {
        this.playing = playing;
    }

    /**
     * Find out whether the ground plane is showing in this cell.
     * @return Whether the ground plane is showing
     */
    public synchronized boolean isGroundPlaneShowing() {
        return groundPlaneShowing;
    }

    /**
     * Set the ground plane visibility for this cell.
     * @param groundPlaneShowing Whether the ground plane is showing
     */
    public synchronized void setGroundPlaneShowing(boolean groundPlaneShowing) {
        this.groundPlaneShowing = groundPlaneShowing;
    }
}
