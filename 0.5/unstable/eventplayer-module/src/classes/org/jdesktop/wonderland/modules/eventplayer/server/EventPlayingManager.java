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
 * $Revision$
 * $Date$
 * $State$
 */

package org.jdesktop.wonderland.modules.eventplayer.server;

import org.jdesktop.wonderland.common.messages.MessagePacker.ReceivedMessage;

/**
 * Interface for the event playing manager, responsible (as its name suggests)
 * for replaying messages.
 * @author Bernard Horan
 */
public interface EventPlayingManager {

    /**
     * Replay the messages from a named recording
     * @param tapeName the name of the recording
     * @param listener the object that should be notified for callbacks
     */
    public void replayMessages(String tapeName, MessagesReplayingListener listener);

    /**
     * A listener that will be notified of the result of replaying messages
     * from a changes file.  Implementations of MessagesReplayingListener must
     * be either a ManagedObject or Serializable
     */
    public interface MessagesReplayingListener {

        /**
         * All the messages from this recording have been played
         */
        public void allMessagesPlayed();

        /**
         * Replay a message
         * @param message a received message that has been parsed from the recording
         */
        public void playMessage(ReceivedMessage message);

    }


}
