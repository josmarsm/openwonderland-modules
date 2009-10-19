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
package org.jdesktop.wonderland.modules.timeline.provider;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineProviderConnectionType;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQuery;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQueryID;
import org.jdesktop.wonderland.modules.timeline.common.provider.messages.ProviderAddQueryRequestMessage;
import org.jdesktop.wonderland.modules.timeline.common.provider.messages.ProviderObjectsMessage;
import org.jdesktop.wonderland.modules.timeline.common.provider.messages.ProviderRemoveQueryRequestMessage;
import org.jdesktop.wonderland.modules.timeline.common.provider.messages.ProviderResetResultMessage;
import org.jdesktop.wonderland.modules.timeline.provider.spi.TimelineProvider;
import org.jdesktop.wonderland.modules.timeline.provider.spi.TimelineProviderContext;

/**
 * A custom connection for sending and receiving timeline provider information.
 * This communicates with the TimelineProviderConnectionHandler on the server
 * to coordinate queries and results between the Wonderland clients and the
 * server.
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineProviderConnection extends BaseConnection {
    private final Map<TimelineQueryID, TimelineProvider> providers =
            new LinkedHashMap<TimelineQueryID, TimelineProvider>();

    public ConnectionType getConnectionType() {
        return TimelineProviderConnectionType.TYPE;
    }

    @Override
    public void handleMessage(Message message) {
        if (message instanceof ProviderAddQueryRequestMessage) {
            handleAddQuery((ProviderAddQueryRequestMessage) message);
        } else if (message instanceof ProviderRemoveQueryRequestMessage) {
            handleRemoveQuery((ProviderRemoveQueryRequestMessage) message);
        }
    }

    private void addResults(TimelineQueryID queryID, Set<DatedObject> objs) {        
        send(new ProviderObjectsMessage(null, queryID,
                                        ProviderObjectsMessage.Action.ADD,
                                        objs));
    }

    private void removeResults(TimelineQueryID queryID, Set<DatedObject> objs) {
        send(new ProviderObjectsMessage(null, queryID,
                                        ProviderObjectsMessage.Action.REMOVE,
                                        objs));
    }

    private void resetResults(TimelineQueryID queryID) {
        send(new ProviderResetResultMessage(null, queryID));
    }

    private void handleAddQuery(ProviderAddQueryRequestMessage message) {
        TimelineQuery query = message.getQuery();

        // create the provider
        TimelineProvider provider = instantiateProvider(query.getQueryClass());

        // initialize it
        provider.initialize(new ProviderContextImpl(query));

        // remember it
        providers.put(query.getQueryID(), provider);
    }

    private void handleRemoveQuery(ProviderRemoveQueryRequestMessage message) {
        // remove the provider from the map
        TimelineProvider provider = providers.remove(message.getQueryID());
        provider.shutdown();
    }

    private TimelineProvider instantiateProvider(String providerClass) {
        try {
            Class<TimelineProvider> clazz = (Class<TimelineProvider>) Class.forName(providerClass);
            return clazz.newInstance();
        } catch (InstantiationException ex) {
            throw new IllegalStateException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Implementation of a context object for a given provider
     */
    private class ProviderContextImpl implements TimelineProviderContext {
        private TimelineQuery query;

        public ProviderContextImpl(TimelineQuery query) {
            this.query = query;
        }

        public TimelineQuery getQuery() {
            return query;
        }

        public void addResults(Set<DatedObject> results) {
            TimelineProviderConnection.this.addResults(query.getQueryID(), results);
        }

        public void removeResults(Set<DatedObject> results) {
            TimelineProviderConnection.this.removeResults(query.getQueryID(), results);
        }

        public void resetResults() {
            TimelineProviderConnection.this.resetResults(query.getQueryID());
        }

    }
}
