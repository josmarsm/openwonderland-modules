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

package org.jdesktop.wonderland.modules.timeline.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
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

    private ManagedReference<ChannelComponentMO> channelRef;

    public TimelineServerConfiguration() {
        super();
        this.channelRef = null;
    }

    public TimelineServerConfiguration(TimelineConfiguration config, ChannelComponentMO channel) {
        super();

        if(channel!=null)
            this.channelRef = AppContext.getDataManager().createReference(channel);

        this.setDateRange(config.getDateRange());
        this.setUnits(config.getUnits());
        this.setPitch(config.getPitch());
        this.setRadsPerSegment(config.getRadsPerSegment());
    }

    public void sendUpdatedConfiguration() {
        TimelineCellChangeMessage msg = new TimelineCellChangeMessage();
        msg.setConfig(new TimelineConfiguration(this));

        // This is a sever-originated message that will go to all clients.
        // I'm assuming that in the case where we get a configuration
        // change from a client we'll handle it differently.
        if(channelRef!=null) {
            channelRef.get().sendAll(null, msg);
        } else {
            logger.warning("Tried to update client configurations, but the channel was null.");
        }
        
    }
}
