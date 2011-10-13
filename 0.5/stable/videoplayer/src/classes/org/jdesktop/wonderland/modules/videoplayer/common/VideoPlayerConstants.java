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
package org.jdesktop.wonderland.modules.videoplayer.common;

/**
 * Constants relating to the Video Player state.
 *
 * @author nsimpson
 */
public class VideoPlayerConstants {

    // the name of the map where we store status
    public static final String STATUS_MAP = "videoplayer-status";
    // the key for the URI of the video
    public static final String MEDIA_URI = "media-uri";
    // the key for the state of the video player
    public static final String PLAYER_STATE = "player-state";
    // the key for the current position in the media
    public static final String MEDIA_POSITION = "media-position";
    // the key for the time the state last changed
    public static final String STATE_CHANGE_TIME = "state-change-time";
    // the key for the volume to play audio at
    public static final String VOLUME = "volume";
    // the key for the radius within which audio should be heard
    public static final String AUDIO_RADIUS = "audio-radius";
}
