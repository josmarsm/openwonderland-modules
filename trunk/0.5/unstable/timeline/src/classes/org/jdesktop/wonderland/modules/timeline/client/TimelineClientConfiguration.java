/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.timeline.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellChangeMessage;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;

/**
 *
 * @author drew
 * @author mabonner
 */
public class TimelineClientConfiguration extends TimelineConfiguration {

    private static final Logger logger =
        Logger.getLogger(TimelineClientConfiguration.class.getName());

    private ChannelComponent channel;

    public TimelineClientConfiguration(TimelineConfiguration config, ChannelComponent channel) {
        super();
        this.channel = channel;
        // TODO matt what does this do?
        // So the issue here is that configurations need to be able to send
        // update messages, but how they do that differs on the client and the
        // server. So we have client/server versions of this object. The twist
        // is that we need a channel to send the message on, and we have no
        // way to get that without forcing its inclusion in the constructor.
        // The other issue is that we can't send Client versions of this object
        // to the server and vice-versa. So when configurations are moving
        // between the client and server, the get converted into generic
        // versions and then when they arrive, I use this constructor to turn
        // them into, eg, a clientconfiguration that includes the right channel
        // object. This is stupid and elaborate but it was the only thing I
        // could figure out at the time that worked. (Drew)
        
        this.setDateRange(config.getDateRange());
        this.setNumSegments(config.getNumSegments());
        this.setPitch(config.getPitch());
        this.setRadsPerSegment(config.getRadsPerSegment());
    }


    public TimelineClientConfiguration() {
        super();
        this.channel = null;
    }

    public void sendUpdatedConfiguration() {
        // If we're on the client, send a single update message to the server.
        TimelineCellChangeMessage msg = new TimelineCellChangeMessage();
        msg.setConfig(new TimelineConfiguration(this));

        if(channel!=null)
            channel.send(msg);
        else
            logger.warning("Tied to update server configurations, but the channel was null.");

    }
}
