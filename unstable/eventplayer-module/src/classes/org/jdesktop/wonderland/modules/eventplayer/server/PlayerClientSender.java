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
package org.jdesktop.wonderland.modules.eventplayer.server;

import com.sun.sgs.app.Channel;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * A simulated WonderlandClientSender that participates in replaying messages
 * @author Bernard Horan
 */
public class PlayerClientSender implements WonderlandClientSender, Serializable {
    private static final Logger logger = Logger.getLogger(PlayerClientSender.class.getName());

    private static long COUNTER = 1;
    private BigInteger id;

    PlayerClientSender() {
        id = BigInteger.valueOf(COUNTER);
        COUNTER++;
    }

    public ConnectionType getClientType() {
        return PlayerConnectionType.CLIENT_TYPE;
    }

    public Set<WonderlandClientID> getClients() {
        return new HashSet<WonderlandClientID>();
    }

    public boolean hasSessions() {
        return false;
    }

    public void send(Message message) {
        logger.info("message: " + message);
    }

    public void send(WonderlandClientID clientID, Message message) {
        logger.info("clientID: " + clientID + " message: " + message);
    }

    public void send(Set<WonderlandClientID> sessions, Message message) {
        logger.info("sessions: " + sessions + " message: " + message);
    }

    public void send(Channel channel, Message message) {
        logger.info("channel: " + channel + " message: " + message);
    }

    public static class PlayerConnectionType extends ConnectionType {

        /** the client type for the null client */
        public static final ConnectionType CLIENT_TYPE =
                new PlayerConnectionType("__NullClient");

        private PlayerConnectionType(String type) {
            super(type);
        }
    }
}
