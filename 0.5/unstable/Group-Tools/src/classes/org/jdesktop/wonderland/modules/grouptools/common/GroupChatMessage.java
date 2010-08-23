/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.grouptools.common;

import java.io.Serializable;
import java.util.Set;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.MessageID;

/**
 *
 * @author Ryan Babiuch
 * 
 */
public class GroupChatMessage extends Message implements Serializable {
    private Set<String> toGroup; //group to send message to
    private String from; //person message came from
    private String messageBody; //content of message
    private boolean broadcast; //is this a broadcast message or not
    private MessageID key; //

    public GroupChatMessage(Set<String> toGroups, String from, String messageBody, boolean broadcast, MessageID key) {
        super();
        this.toGroup = toGroups;
        this.from = from;

        this.messageBody = messageBody;
        this.broadcast = broadcast;
        this.key = key;

    }
    /**
     *
     * @return the groups to send the message to
     */
    public Set<String> getToGroup() {
        return toGroup;
    }
    /**
     *
     * @return who the message is from
     */
    public String getFrom() {
        return from;
    }
    /**
     *
     * @return content of message
     */
    public String getMessageBody() {
        return messageBody;
    }
    /**
     *
     * @return whether or not this message is to be broadcasted.
     */
    public boolean isBroadcast() {
        return broadcast;
    }

    /**
     *
     * @return the identifier for this message;
     */
    public MessageID getKey() {
        return key;
    }

}
