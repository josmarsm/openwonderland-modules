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
 */
public class TimelineClientConfiguration extends TimelineConfiguration {

    private static final Logger logger =
        Logger.getLogger(TimelineClientConfiguration.class.getName());

    private ChannelComponent channel;

    public TimelineClientConfiguration(TimelineConfiguration config, ChannelComponent channel) {
        super();
        this.channel = channel;

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
