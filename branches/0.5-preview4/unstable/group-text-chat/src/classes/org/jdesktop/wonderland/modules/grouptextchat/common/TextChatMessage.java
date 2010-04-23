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
import org.jdesktop.wonderland.modules.grouptextchat.common.GroupID;

/**
 * A chat message for a specific user. If the user is null or an empty string,
 * then it is meant for all users.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class TextChatMessage extends Message {

    private String textMessage = null;
    private String fromUserName = null;
    private GroupID group = null;

    /** Constructor */
    public TextChatMessage(String msg, String fromUserName, GroupID group) {
        this.textMessage = msg;
        this.fromUserName = fromUserName;
        this.group = group;
    }

    /**
     * Returns the name of the user from which the message came.
     *
     * @return A String user name
     */
    public String getFromUserName() {
        return fromUserName;
    }

    /**
     * Returns the text of the text chat message.
     *
     * @return A String text chat message
     */
    public String getTextMessage() {
        return textMessage;
    }

    /**
     * Returns the recipient of this message.
     * 
     * @return A ChatRecipient object describing who (or which group) this message is for.
     */
    public GroupID getGroup() {
        return this.group;
    }

    public void setGroup(GroupID group) {
        this.group = group;
    }
}