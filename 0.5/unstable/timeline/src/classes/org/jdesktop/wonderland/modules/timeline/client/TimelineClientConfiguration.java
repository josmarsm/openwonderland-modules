/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.client;

import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellChangeMessage;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;

/**
 *
 * @author drew
 */
public class TimelineClientConfiguration extends TimelineConfiguration {

    private ChannelComponent channel;

    public TimelineClientConfiguration(TimelineConfiguration config, ChannelComponent channel) {
        this.channel = channel;

        this.setDateRange(config.getDateRange());
        this.setNumSegments(config.getNumSegments());
        this.setPitch(config.getPitch());
        this.setRadsPerSegment(config.getRadsPerSegment());
    }

    public void sendUpdatedConfiguration() {
        // If we're on the client, send a single update message to the server.
        TimelineCellChangeMessage msg = new TimelineCellChangeMessage();
        msg.setConfig(this);
        channel.send(msg);
    }
}
