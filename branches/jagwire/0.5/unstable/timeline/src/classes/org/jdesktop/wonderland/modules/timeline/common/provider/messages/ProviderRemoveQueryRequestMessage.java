/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.common.provider.messages;

import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQueryID;

/**
 * A message to request removing an existing query
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ProviderRemoveQueryRequestMessage extends Message {
    private TimelineQueryID queryID;

    public ProviderRemoveQueryRequestMessage(TimelineQueryID queryID)
    {
        this.queryID = queryID;
    }

    public TimelineQueryID getQueryID() {
        return queryID;
    }
}
