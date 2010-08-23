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
