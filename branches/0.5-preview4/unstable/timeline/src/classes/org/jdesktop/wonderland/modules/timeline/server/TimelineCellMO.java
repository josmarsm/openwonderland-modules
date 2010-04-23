/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
import java.io.Serializable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellChangeMessage;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellClientState;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellServerState;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedSet;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineResult;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineResultListener;
import org.jdesktop.wonderland.modules.timeline.server.audio.TimelineAudioComponentMO;
import org.jdesktop.wonderland.modules.timeline.server.layout.TimelineLayoutComponentMO;
import org.jdesktop.wonderland.modules.timeline.server.provider.TimelineProviderComponentMO;
import org.jdesktop.wonderland.modules.timeline.server.provider.TimelineProviderComponentMOListener;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.DependsOnCellComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * 
 */
@DependsOnCellComponentMO(TimelineLayoutComponentMO.class)
public class TimelineCellMO extends CellMO {

    private static final Logger logger =
        Logger.getLogger(TimelineCellMO.class.getName());

    private TimelineConfiguration config;

    private DatedSet segments = new DatedSet();

    private final ProviderListener providerListener;
    private final TimelineChildCellCreator cellCreator;

    @UsesCellComponentMO(TimelineProviderComponentMO.class)
    private ManagedReference<TimelineProviderComponentMO> providerRef;

    @UsesCellComponentMO(TimelineAudioComponentMO.class)
    private ManagedReference<TimelineAudioComponentMO> audioRef;

    public TimelineCellMO() {
        super();

        cellCreator = new TimelineChildCellCreatorImpl();
        cellCreator.setLive(this);
        providerListener = new ProviderListener(this, cellCreator);
    }

    public String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.timeline.client.TimelineCell";
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);

        this.setConfiguration(((TimelineCellServerState)state).getConfig());
        logger.info("generating segments");
        this.segments = config.generateSegments();
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new TimelineCellServerState();
        }

        ((TimelineCellServerState)state).setConfig(new TimelineConfiguration(config));

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
            providerRef.get().addComponentMOListener(providerListener);

            // notify the listener of any existing results
            for (TimelineResult result : providerRef.get().getResults()) {
                providerListener.resultAdded(result);
            }
        } else {
            channel.removeMessageReceiver(TimelineCellChangeMessage.class);
            providerRef.get().removeComponentMOListener(providerListener);
        }
    }

    public void setConfiguration(TimelineConfiguration config) {
        this.config = new TimelineServerConfiguration(config, getComponent(ChannelComponentMO.class));
    }

    public TimelineConfiguration getConfiguration() {
        return this.config;
    }

    public TimelineAudioComponentMO getAudio() {

        if(this.audioRef.get()==null) {
            TimelineAudioComponentMO audio = new TimelineAudioComponentMO(this);
            this.addComponent(audio, TimelineAudioComponentMO.class);
            this.audioRef = AppContext.getDataManager().createReference(audio);
        }

        return this.audioRef.get();
    }

    public DatedSet getSegments() {
        return segments;
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

    private static class ProviderListener
            implements TimelineProviderComponentMOListener,
                       TimelineResultListener, Serializable
    {
        private ManagedReference<TimelineCellMO> cellRef;
        private TimelineChildCellCreator cellCreator;

        public ProviderListener(TimelineCellMO cell,
                                TimelineChildCellCreator cellCreator)
        {
            this.cellRef = AppContext.getDataManager().createReference(cell);
            this.cellCreator = cellCreator;
        }

        public void resultAdded(TimelineResult result) {
            logger.warning("Result added: " + result);

            result.addResultListener(this);
            for (DatedObject datedObj : result.getResultSet()) {
                added(datedObj);
            }
        }

        public void added(DatedObject obj) {
            logger.warning("Object added: " + obj);

            cellCreator.createCell(obj);
        }

        public void resultRemoved(TimelineResult result) {
            for (DatedObject datedObj : result.getResultSet()) {
                removed(datedObj);
            }
        }

        public void removed(DatedObject obj) {
            cellCreator.cleanupCell(obj);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ProviderListener other = (ProviderListener) obj;
            if (this.cellRef != other.cellRef &&
                (this.cellRef == null || !this.cellRef.equals(other.cellRef)))
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 47 * hash + (this.cellRef != null ? this.cellRef.hashCode() : 0);
            return hash;
        }
    }
}
