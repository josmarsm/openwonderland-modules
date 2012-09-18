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
package org.jdesktop.wonderland.modules.cmu.common.messages.serverclient;

/**
 * Container class for an array containing all server/client message classes.
 * @author kevin
 */
public final class ServerClientMessageTypes {

    /**
     * A list of message types that cells/cellMOs should expect
     * to send and receive when they become active.
     */
    public static final Class[] MESSAGE_TYPES_TO_RECEIVE = {
        ConnectionChangeMessage.class,
        VisibilityChangeMessage.class,
        MouseButtonEventMessage.class,
        PlaybackSpeedChangeMessage.class,
        RestartProgramMessage.class,
        SceneTitleChangeMessage.class,
        EventListMessage.class,
        EventResponseMessage.class,
        AvailableResponsesChangeMessage.class,
    };

    /**
     * Class should never be instantiated
     */
    private ServerClientMessageTypes() {
    }
}
