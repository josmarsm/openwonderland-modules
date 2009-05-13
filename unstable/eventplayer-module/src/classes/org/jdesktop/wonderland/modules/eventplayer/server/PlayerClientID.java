/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.eventplayer.server;

import java.math.BigInteger;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author bh37721
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
