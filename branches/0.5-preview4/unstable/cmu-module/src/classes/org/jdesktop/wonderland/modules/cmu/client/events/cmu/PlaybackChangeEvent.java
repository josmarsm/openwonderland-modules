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
package org.jdesktop.wonderland.modules.cmu.client.events.cmu;

import org.jdesktop.wonderland.modules.cmu.client.CMUCell;

/**
 * Event to represent a change in playback speed for a scene.  The event
 * encompasses both the speed which has been chosen and the play/pause
 * state of the scene.
 * @author kevin
 */
public class PlaybackChangeEvent extends CMUChangeEvent {

    private float playbackSpeed;
    private boolean playing;
    
    /**
     * Standard constructor.
     * @param cell The CMU cell on which the playback change has occurred
     * @param playbackSpeed The speed at which the scene is playing
     * @param isPlaying Whether the scene is playing (true) or paused (false)
     */
    public PlaybackChangeEvent(CMUCell cell, float playbackSpeed, boolean isPlaying) {
        super(cell);
        setPlaybackSpeed(playbackSpeed);
        setPlaying(isPlaying);
    }

    /**
     * Get the playback speed represented by this event.
     * @return Playback speed for this event
     */
    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    /**
     * Set the playback speed for this event.
     * @param playbackSpeed New playback speed
     */
    public void setPlaybackSpeed(float playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
    }

    /**
     * Get the play/pause state represented by this event.
     * @return True if playing, false if paused
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * Set the play/pause state for this event.
     * @param playing True if playing, false if paused
     */
    public void setPlaying(boolean playing) {
        this.playing = playing;
    }
}
