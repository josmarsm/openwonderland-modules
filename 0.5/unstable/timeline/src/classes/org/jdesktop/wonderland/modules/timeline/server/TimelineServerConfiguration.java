/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.server;

import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellChangeMessage;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;

/**
 *
 * @author drew
 */
public class TimelineServerConfiguration extends TimelineConfiguration {

    private static final Logger logger =
        Logger.getLogger(TimelineServerConfiguration.class.getName());

    private ChannelComponentMO channel;

    public TimelineServerConfiguration() {
        super();
        this.channel = null;
    }

    public TimelineServerConfiguration(TimelineConfiguration config, ChannelComponentMO channel) {
        super();
        
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
        if(channel!=null)
            channel.sendAll(null, msg);
        else
            logger.warning("Tried to update client configurations, but the channel was null.");
        
    }

}
