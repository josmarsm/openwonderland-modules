/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.marbleous.common.cell.messages;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * Message to start or stop playback of a particular sound file.
 * @author kevin
 */
public class SoundPlaybackMessage extends CellMessage {

    private String callID;
    private String uri;
    private boolean playing;

    /**
     * Standard constructor.
     * @param callID The call ID
     * @param uri The URI of the sound file to play or stop
     * @param playing Whether to start the sound (true) or stop it
     */
    public SoundPlaybackMessage(String callID, String uri, boolean playing) {
        super();
        this.callID = callID;
        this.uri = uri;
        this.playing = playing;
    }

    /**
     * Get the Call ID.
     * @return Call ID
     */
    public String getCallID() {
        return callID;
    }

    /**
     * Whether to start or stop the sound.
     * @return True if the sound should be started, false if stopped
     */
    public boolean shouldPlay() {
        return playing;
    }

    /**
     * Get the URI of the sound file to play or stop.
     * @return Sound file URI
     */
    public String getUri() {
        return uri;
    }
}
