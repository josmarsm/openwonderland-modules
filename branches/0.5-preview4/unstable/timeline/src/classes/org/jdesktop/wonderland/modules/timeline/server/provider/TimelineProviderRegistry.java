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
package org.jdesktop.wonderland.modules.timeline.server.provider;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.util.ScalableHashMap;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineProviderConnectionType;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQuery;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQueryID;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineResultListener;
import org.jdesktop.wonderland.modules.timeline.common.provider.messages.ProviderAddQueryRequestMessage;
import org.jdesktop.wonderland.modules.timeline.common.provider.messages.ProviderRemoveQueryRequestMessage;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * The server registration mechanism for timeline providers connected
 * via external clients.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineProviderRegistry {
    private static final String NAME = TimelineProviderRegistry.class.getName();
    private static final String NEXT_ID_BINDING = NAME + ".NextID";
    private static final String REGISTRATION_BINDING = NAME + ".Registration";
    
    /**
     * Get an instance of the TimelineProviderRegistry singleton
     */
    public static TimelineProviderRegistry getInstance() {
        // the registry is stateless, so we can just add a new one
        return new TimelineProviderRegistry();
    }

    // stateless -- nothing to do in the contstructor.  Use getInstance()
    // instead.
    protected TimelineProviderRegistry() {}

    /**
     * Add a query with the given ID and listener to notify of results.
     * @param query the query to add
     * @param listener the listener to notify of changes to this query
     */
    void register(TimelineQuery query, RegistryResultListener listener) {
        // add to our list
        ResultInfo info = new ResultInfo(query, listener);
        getQueries().put(query.getQueryID(), info);

        // notify the connected provider (if any)
        getProviderSender().send(new ProviderAddQueryRequestMessage(query));

        // make sure our max id is up-to-date
        IDHolder holder = getIDHolder();
        if (holder.max.compareTo(query.getQueryID()) <= 0) {
            AppContext.getDataManager().markForUpdate(holder);
            holder.max = query.getQueryID();
        }
    }

    /**
     * Get the ids of all registered queries
     * @return the set of registered ids
     */
    Set<TimelineQueryID> getRegisteredQueries() {
        return getQueries().keySet();
    }

    /**
     * Get a query by id
     * @param id the id of the query to get
     * @return the query with the given id, or null if there is no
     * associated query
     */
    TimelineQuery getQuery(TimelineQueryID id) {
        ResultInfo info = getQueries().get(id);
        if (info == null) {
            return null;
        }

        return info.query;
    }

    /**
     * Get the listener for a given query id
     * @param id the id of the query to get a listener for
     * @return the listener for that query
     */
    RegistryResultListener getListener(TimelineQueryID id) {
        ResultInfo info = getQueries().get(id);
        if (info == null) {
            return null;
        }

        return info.listener;
    }

    /**
     * Remove the query with the given id from the provider
     * @param id the id of the provider to remove
     */
    void unregister(TimelineQueryID id) {
        boolean removed = (getQueries().remove(id) != null);
        if (removed) {
            // notify the connected provider (if any)
            getProviderSender().send(new ProviderRemoveQueryRequestMessage(id));
        }
    }
    
    /**
     * Get the next id to use
     * @return the next unique id to use
     */
    TimelineQueryID nextID() {
        IDHolder holder = getIDHolder();
        AppContext.getDataManager().markForUpdate(holder);

        TimelineQueryID out = holder.max.next();
        holder.max = out;

        return out;
    }

    /**
     * Get the client sender to send to the timeline provider
     * @return the sender to send to the timeline provider
     */
    private WonderlandClientSender getProviderSender() {
        CommsManager cm = WonderlandContext.getCommsManager();
        return cm.getSender(TimelineProviderConnectionType.TYPE);
    }

    /**
     * The registered queries, as a map from id to information about
     * the query.  Create map if necessary.
     * @return the map of queries.
     */
    private Map<TimelineQueryID, ResultInfo> getQueries() {
        Map<TimelineQueryID, ResultInfo> out;

        try {
            out = (Map<TimelineQueryID, ResultInfo>)
                    AppContext.getDataManager().getBinding(REGISTRATION_BINDING);
        } catch (NameNotBoundException nnbe) {
            out = new ScalableHashMap<TimelineQueryID, ResultInfo>();
            AppContext.getDataManager().setBinding(REGISTRATION_BINDING, out);
        }

        return out;
    }

    /**
     * Get the ID holder, creating it if necessary
     * @return the id holder
     */
    private IDHolder getIDHolder() {
        IDHolder out;

        try {
            out = (IDHolder) AppContext.getDataManager().getBinding(NEXT_ID_BINDING);
        } catch (NameNotBoundException nnbe) {
            out = new IDHolder();
            AppContext.getDataManager().setBinding(NEXT_ID_BINDING, out);
        }

        return out;
    }

    /**
     * A listener that is notified of provider events.  This extends the
     * normal result listener to notify when the results have been reset.
     */
    interface RegistryResultListener extends TimelineResultListener {
        /**
         * Add a set of objects.
         */
        public void added(Set<? extends DatedObject> objs);

        /**
         * Remove a set of objects.
         */
        public void removed(Set<? extends DatedObject> objs);

        /**
         * Notification that the results are being reset, and all objects
         * should be removed.
         */
        public void reset();
    }


    /**
     * Hold details of a particular result
     */
    private static final class ResultInfo implements Serializable {
        private TimelineQuery query;
        private RegistryResultListener listener;

        private ResultInfo(TimelineQuery query,
                           RegistryResultListener listener)
        {
            this.query = query;
            this.listener = listener;
        }
    }

    /**
     * Hold the maximum ID we've seen
     */
    private static final class IDHolder implements ManagedObject, Serializable {
        private TimelineQueryID max = new TimelineQueryID(0);
    }
}
