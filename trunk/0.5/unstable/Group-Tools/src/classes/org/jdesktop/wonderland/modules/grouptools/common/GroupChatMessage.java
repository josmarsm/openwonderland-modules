/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
