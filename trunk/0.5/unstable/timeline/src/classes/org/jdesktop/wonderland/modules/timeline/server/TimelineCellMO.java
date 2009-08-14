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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.awt.LayoutManager;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellChangeMessage;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellClientState;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellServerState;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;
import org.jdesktop.wonderland.modules.timeline.common.TimelineSegment;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedSet;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;
import org.jdesktop.wonderland.modules.timeline.server.layout.BaseLayout;
import org.jdesktop.wonderland.modules.timeline.server.provider.TimelineProviderComponentMO;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.DependsOnCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * 
 */
@DependsOnCellComponentMO(TimelineProviderComponentMO.class)
public class TimelineCellMO extends CellMO {

    private static final Logger logger =
        Logger.getLogger(TimelineCellMO.class.getName());

    private TimelineConfiguration config;

    private DatedSet segments = new DatedSet();

    private BaseLayout layout;

    public TimelineCellMO() {
        super();
    }

    public String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.timeline.client.TimelineCell";
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);

        this.setConfiguration(((TimelineCellServerState)state).getConfig());
        logger.info("generating segments");
//        Date futureDate = new Date();
//        futureDate = new Date(futureDate.getTime() - 2000000000);
//        config.setDateRange(new TimelineDate(futureDate, new Date()));
        this.segments = config.generateSegments();
        if(layout==null)
            layout = new BaseLayout(this);
        else
            layout.doLayout(true);
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new TimelineCellServerState();
        }

        ((TimelineCellServerState)state).setConfig(config);


        return super.getServerState(state);
    }


    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new TimelineCellClientState();
        }

        ((TimelineCellClientState)cellClientState).setConfig(new TimelineConfiguration(config));
        
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if(live) {

            channel.addMessageReceiver(TimelineCellChangeMessage.class, 
		(ChannelComponentMO.ComponentMessageReceiver)new TimelineCellMessageReceiver(this));

        } else {
            channel.removeMessageReceiver(TimelineCellChangeMessage.class);
        }
    }

    public void setConfiguration(TimelineConfiguration config) {
        this.config = new TimelineServerConfiguration(config, getComponent(ChannelComponentMO.class));
    }

    public TimelineConfiguration getConfiguration() {
        return this.config;
    }

    private static class TimelineCellMessageReceiver extends AbstractComponentMessageReceiver {

        public TimelineCellMessageReceiver(TimelineCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, 
		CellMessage message) {

            TimelineCellMO cellMO = (TimelineCellMO)getCell();

            TimelineCellChangeMessage msg = (TimelineCellChangeMessage)message;
            cellMO.setConfiguration(msg.getConfig());

            // Send updates to all other clients.
            Set<WonderlandClientID> otherClients = sender.getClients();
            otherClients.remove(clientID);
            sender.send(otherClients, msg);
        }

    }

    public DatedSet getSegments() {
        return segments;
    }

//    private void generateSegments() {
//        // based on the timeline configuration, generate a set of shell segments
//
//        float radius = 10.0f;
//
//        long dateIncrement = config.getDateRange().getRange() / config.getNumSegments();
//        long curDate = config.getDateRange().getMinimum().getTime();
//
//        logger.info("numSegmentsToGenerate: " + config.getNumSegments());
//        for(int i=0; i<config.getNumSegments(); i++) {
//            TimelineDate d = new TimelineDate(new Date(curDate), new Date(curDate + dateIncrement));
//
//            // now figure out the segment transform.
//            // all we care about is the translation for now, will worry about
//            // rotations later (and I think Matt is doing that math, so I'll
//            // just throw it in when he's worked it all out)
//
//            // starting at a theta of zero, move our way up.
//            Vector3f pos = new Vector3f(((float)(radius * Math.sin(i*config.getRadsPerSegment()))), i*config.getHeight()/config.getNumSegments(),(float) ((float)radius * Math.cos(i*config.getRadsPerSegment())));
//
//            TimelineSegment seg = new TimelineSegment(d);
//
//            seg.setTransform(new CellTransform(new Quaternion(), pos));
//
//            logger.info("Added segment: " + seg);
//            segments.add(seg);
//
//            curDate+=dateIncrement;
//        }
//    }
}
