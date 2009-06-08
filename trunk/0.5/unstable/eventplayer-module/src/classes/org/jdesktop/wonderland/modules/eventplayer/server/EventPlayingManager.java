/**
 * Project Wonderland
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

package org.jdesktop.wonderland.modules.eventplayer.server;

import org.jdesktop.wonderland.common.messages.MessagePacker.ReceivedMessage;
import org.xml.sax.InputSource;

/**
 *
 * @author Bernard Horan
 */
public interface EventPlayingManager {

    public void replayMessages(String tapeName, MessagesReplayingListener listener);

    /**
     * A listener that will be notified of the result of recording a message
     * to c changes file.  Implementations of MessageRecordingListener must
     * be either a ManagedObject or Serializable
     */
    public interface MessagesReplayingListener {

        public void playMessage(ReceivedMessage message);

    }


}
