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

package org.jdesktop.wonderland.modules.grouptextchat.common;

import org.jdesktop.wonderland.common.messages.Message;

/**
 * Message object for notifying clients about their being added to/removed from
 * a chat group.
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class GroupChatMessage extends Message {

    public enum GroupAction {
        JOINED,
        LEFT
    }

    private GroupAction action;
    private GroupID gid;

    public GroupChatMessage(GroupID gid, GroupAction action) {
        this.gid = gid;
        this.action = action;
    }

    /**
     *
     * @return The action this message represents.
     */
    public GroupAction getAction() {
        return action;
    }

    /**
     *
     * @return The group the action is
     */
    public GroupID getGroupID() {
        return gid;
    }

    
}
