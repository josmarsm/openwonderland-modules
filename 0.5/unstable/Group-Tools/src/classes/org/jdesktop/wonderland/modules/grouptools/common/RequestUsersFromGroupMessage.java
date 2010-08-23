/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.grouptools.common;

import org.jdesktop.wonderland.common.messages.Message;

/**
 *
 * @author Ryan Babiuch
 */
public class RequestUsersFromGroupMessage extends Message {
    public String groupName = null;
    public RequestUsersFromGroupMessage(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

}
