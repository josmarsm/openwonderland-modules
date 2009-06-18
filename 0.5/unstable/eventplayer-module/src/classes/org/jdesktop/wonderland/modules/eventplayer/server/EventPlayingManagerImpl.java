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

/**
 * An implementation of EventPlayingManager, that forwards all methods
 * to a service.
 * @author Bernard Horan
 */
public class EventPlayingManagerImpl implements EventPlayingManager {
    private EventPlayingService service;

    /**
     * Constructor
     * @param service
     */
    public EventPlayingManagerImpl(EventPlayingService service) {
        this.service = service;
    }

    public void replayMessages(String tapeName, MessagesReplayingListener listener) {
        service.replayMessages(tapeName, listener);
    }

    

}
