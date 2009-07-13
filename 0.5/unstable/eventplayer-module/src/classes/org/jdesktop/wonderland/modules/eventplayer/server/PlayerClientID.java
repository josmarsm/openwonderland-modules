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

import java.math.BigInteger;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A simulated WonderlandClientID that participates in replaying messages
 * @author Bernard Horan
 */
public class PlayerClientID extends WonderlandClientID {
    private static long COUNTER = Long.MAX_VALUE;
    private BigInteger id;

    PlayerClientID() {
        id = BigInteger.valueOf(COUNTER);
        COUNTER--;
    }

    /**
     * Get the unique ID of this client.
     * @return a unique ID for this client
     */
    @Override
    public BigInteger getID() {
        return id;
    }

    /**
     * Compare client IDs based on the id object.
     * @param obj the other object
     * @return true if <code>obj</code> is a PlayerClientID with the same
     * id
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlayerClientID other = (PlayerClientID) obj;
        if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    /**
     * Generate a hash code based on the id
     * @return a hashcode based on the id
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
