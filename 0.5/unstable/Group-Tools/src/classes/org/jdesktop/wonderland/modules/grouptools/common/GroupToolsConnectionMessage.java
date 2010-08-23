/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.grouptools.common;

import java.util.List;
import java.util.Map;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * Message sent from server to recently-connected client. Indicates groups to be put
 * in list.
 * 
 * @author Ryan Babiuch
 */
public class GroupToolsConnectionMessage extends Message {

    private Map<String, List<GroupChatMessage>> groupLogs;

    public GroupToolsConnectionMessage(Map groupLogs) {
        this.groupLogs = groupLogs;
    }

    public Map<String, List<GroupChatMessage>> getGroupLogs() {
        return groupLogs;
    }
}
