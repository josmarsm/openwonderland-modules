/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.grouptools.common;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 *
 * @author ryan
 */
public class UsersInGroupMessage extends ResponseMessage {
    private final Set<BigInteger> users = new HashSet<BigInteger>();

    public UsersInGroupMessage(MessageID messageID, Set<BigInteger> users) {
        super(messageID);
        
        this.users.addAll(users);
    }

    public Set<BigInteger> getUsersInGroupMessage() {
        return users;
    }
}
