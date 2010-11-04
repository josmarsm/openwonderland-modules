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
package org.jdesktop.wonderland.modules.videoplayer.client;

/**
 * Defines an interface supported by all video players
 *
 * @author nsimpson
 */
public interface VideoPlayer {

    public enum VideoPlayerState {

        NO_MEDIA, MEDIA_READY, STOPPED, PAUSED, PLAYING
    };

    /**
     * Open video media
     * @param mediaURI the URI of the video media to open
     */
    public void openMedia(String mediaURI);

    /**
     * Close video media
     */
    public void closeMedia();

    /**
     * Gets the URI of the currently loaded media
     * @return the URI of the media
     */
    public String getMedia();

    /**
     * Determine if video player is ready to play media
     * @return true if the video player is ready to play, false otherwise
     */
    public boolean isPlayable();

    /**
     * Play video
     */
    public void play();

    /**
     * Gets whether the media is currently playing
     * @return true if the media is playing, false otherwise
     */
    public boolean isPlaying();

    /**
     * Pause video
     */
    public void pause();

    /**
     * Stop playing video
     */
    public void stop();

    /**
     * Rewind the video
     * @param offset the time in seconds to skip back
     */
    public void rewind(double offset);

    /**
     * Skip forward the video
     * @param offset the time in seconds to skip forward
     */
    public void forward(double offset);

    /**
     * Set the position within the media
     * @param mediaPosition the position in seconds
     */
    public void setPosition(double mediaPosition);

    /**
     * Get the current position
     * @return the current position in seconds
     */
    public double getPosition();

    /**
     * Gets the duration of the media
     * @return the duration of the media in second
     */
    public double getDuration();

    /**
     * Mutes audio
     */
    public void mute();

    /**
     * Unmutes audio
     */
    public void unmute();

    /**
     * Gets the state of the audio
     * @return true if the audio is muted, false otherwise
     */
    public boolean isMuted();

    /**
     * Set the current volume
     * @param volume the volume, normalized to between 0 and 1
     */
    public void setVolume(float volume);

    /**
     * Get the current volume
     * @return the current volume, normalized to between 0 and 1
     */
    public float getVolume();

    /**
     * Gets the state of the player
     * @return the player state
     */
    public VideoPlayerState getState();
}
