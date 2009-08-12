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

package org.jdesktop.wonderland.modules.timeline.server.provider;

import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQueryID;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.util.ScalableHashMap;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedSet;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineProviderClientState;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineProviderServerState;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQuery;
import org.jdesktop.wonderland.modules.timeline.common.provider.messages.ProviderObjectsMessage;
import org.jdesktop.wonderland.modules.timeline.common.provider.messages.ProviderResetResultMessage;
import org.jdesktop.wonderland.modules.timeline.server.provider.TimelineProviderRegistry.RegistryResultListener;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * The server side of the timeline provider
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineProviderComponentMO extends CellComponentMO {
    /** the results of active queries */
    private ManagedReference<Map<TimelineQueryID, TimelineResultHolder>> resultsRef;

    /** the channel component */
    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelRef;

    public TimelineProviderComponentMO(CellMO cell) {
        super(cell);

        // initialize the results map
        DataManager dm = AppContext.getDataManager();
        Map<TimelineQueryID, TimelineResultHolder> results =
                new ScalableHashMap<TimelineQueryID, TimelineResultHolder>();
        resultsRef = dm.createReference(results);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.timeline.client.provider.TimelineProviderComponent";
    }

    @Override
    public CellComponentClientState getClientState(CellComponentClientState state,
                                                   WonderlandClientID clientID,
                                                   ClientCapabilities capabilities)
    {
        if (state == null) {
            state = new TimelineProviderClientState();
        }

        TimelineProviderClientState tpcs = (TimelineProviderClientState) state;
        for (TimelineResultHolder result : resultsRef.get().values()) {
            tpcs.addResult(result.getQuery(), result.getResults());
        }

        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if (state == null) {
            state = new TimelineProviderServerState();
        }

        TimelineProviderServerState tpss = (TimelineProviderServerState) state;
        Set<TimelineQuery> queries = tpss.getQueries();
        for (TimelineResultHolder result : resultsRef.get().values()) {
            queries.add(result.getQuery());
        }

        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);

        TimelineProviderServerState tpss = (TimelineProviderServerState) state;
        for (TimelineQuery query : tpss.getQueries()) {
            addQuery(query);
        }
    }

    /**
     * Add a new query to the system.  This will create all the relevant
     * data necessary, and also register the new query with the provider
     * registry.
     * @param query the query to add
     * @return the ID assigned to the newly-added query
     */
    public TimelineQueryID addQuery(TimelineQuery query) {
        TimelineProviderRegistry reg = TimelineProviderRegistry.getInstance();

        // assign an id if necessary
        if (query.getQueryID() == null) {
            query.setQueryID(reg.nextID());
        }

        // create the result data structure
        TimelineResultHolder holder = new TimelineResultHolder(cellRef.get(),
                                                               query);

        // add it to our record
        resultsRef.get().put(query.getQueryID(), holder);

        // register with the registry
        ResultListener listener = new ResultListener(query.getQueryID(),
                                                     resultsRef.get());
        reg.register(query, listener);

        // return the new id
        return query.getQueryID();
    }

    /**
     * Remove a query from the system.  This will remove the data and
     * the provider registry.
     * @param id the id of the query to remove
     * @return true of the id existed and was removed, or false if not
     */
    public boolean removeQuery(TimelineQueryID id) {
        boolean removed = (resultsRef.get().remove(id) != null);
        if (removed) {
            // unregister with the registry
            TimelineProviderRegistry.getInstance().unregister(id);
        }

        return removed;
    }

    /**
     * A listener associated with a particular result set
     */
    private static class ResultListener 
            implements RegistryResultListener, Serializable 
    {
        private ManagedReference<Map<TimelineQueryID, TimelineResultHolder>> resultsRef;
        private TimelineQueryID id;
        
        public ResultListener(TimelineQueryID id,
                              Map<TimelineQueryID, TimelineResultHolder> results) 
        {
            this.id = id;
            
            resultsRef = AppContext.getDataManager().createReference(results);
        }
    
        public void added(DatedObject obj) {
            added(Collections.singleton(obj));
        }

        public void added(Set<DatedObject> objs) {
            TimelineResultHolder holder = resultsRef.get().get(id);
            holder.addResults(objs);
        }

        public void removed(DatedObject obj) {
            removed(Collections.singleton(obj));
        }

        public void removed(Set<DatedObject> objs) {
            TimelineResultHolder holder = resultsRef.get().get(id);
            holder.removeResults(objs);
        }

        public void reset() {
            TimelineResultHolder holder = resultsRef.get().get(id);
            holder.resetResults();
        }
    }

    /**
     * A holder class to handle all the date associated with a particular
     * query.
     */
    private static class TimelineResultHolder implements Serializable {
        private TimelineQuery query;
        private DatedSet results;

        private CellID cellID;
        private ManagedReference<CellMO> cellRef;
        private ManagedReference<ChannelComponentMO> channelRef;

        public TimelineResultHolder(CellMO cell, TimelineQuery query)
        {
            this.cellID = cell.getCellID();
            this.cellRef = AppContext.getDataManager().createReference(cell);
            this.query = query;
        }

        public TimelineQuery getQuery() {
            return query;
        }

        public DatedSet getResults() {
            return results;
        }

        private void addResults(Set<DatedObject> objs) {
            results.addAll(objs);

            // send a message to all clients
            send(new ProviderObjectsMessage(cellID, query.getQueryID(),
                                            ProviderObjectsMessage.Action.ADD,
                                            objs));
        }

        private void removeResults(Set<DatedObject> objs) {
            results.removeAll(objs);
            
            // send a message to all clients
            // send a message to all clients
            send(new ProviderObjectsMessage(cellID, query.getQueryID(),
                                            ProviderObjectsMessage.Action.REMOVE,
                                            objs));
        }

        private void resetResults() {
            results.clear();
            
            // send a message to all clients
            send(new ProviderResetResultMessage(cellID, query.getQueryID()));
        }

        /**
         * Send a message to the cell channel
         * @param message the message to send
         */
        private void send(CellMessage message) {
           ChannelComponentMO channel = getCellChannel();
           if (channel != null) {
               channel.sendAll(null, message);
           }
        }

        /**
         * Get the cell's channel
         * @return the cell's channel, or null if the cell is not live
         */
        private ChannelComponentMO getCellChannel() {
            if (channelRef != null) {
                return channelRef.get();
            }

            // the channel is not set -- get it from the cell
            CellMO cell = cellRef.get();
            ChannelComponentMO cc = cell.getComponent(ChannelComponentMO.class);
            if (cc == null) {
                return null;
            }

            // remember the reference for next time
            channelRef = AppContext.getDataManager().createReference(cc);
            return cc;
        }
    }
}
