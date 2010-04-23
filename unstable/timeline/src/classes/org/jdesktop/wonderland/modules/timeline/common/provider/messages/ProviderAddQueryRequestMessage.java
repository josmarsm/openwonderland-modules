/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.common.provider.messages;

import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQuery;

/**
 * A message to request adding a new query
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ProviderAddQueryRequestMessage extends Message {
    private TimelineQuery query;

    public ProviderAddQueryRequestMessage(TimelineQuery query)
    {
        this.query = query;
    }

    public TimelineQuery getQuery() {
        return query;
    }
}
