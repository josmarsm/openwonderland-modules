/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.server;

import org.jdesktop.wonderland.modules.timeline.common.TimelineCellChangeMessage;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;

/**
 *
 * @author drew
 */
public class TimelineServerConfiguration extends TimelineConfiguration {

    private ChannelComponentMO channel;

    public TimelineServerConfiguration(TimelineConfiguration config, ChannelComponentMO channel) {
        this.channel = channel;

        this.setDateRange(config.getDateRange());
        this.setNumSegments(config.getNumSegments());
        this.setPitch(config.getPitch());
        this.setRadsPerSegment(config.getRadsPerSegment());
    }

    @Override
    public void sendUpdatedConfiguration() {
        TimelineCellChangeMessage msg = new TimelineCellChangeMessage();
        msg.setConfig(this);

        // This is a sever-originated message that will go to all clients.
        // I'm assuming that in the case where we get a configuration
        // change from a client we'll handle it differently. 
        channel.sendAll(null, msg);
    }

}
