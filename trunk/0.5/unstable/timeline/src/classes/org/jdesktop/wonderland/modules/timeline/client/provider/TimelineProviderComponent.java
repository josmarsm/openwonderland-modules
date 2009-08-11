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

package org.jdesktop.wonderland.modules.timeline.client.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedSet;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineProviderClientState;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQuery;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQueryID;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineResult;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineResultListener;
import org.jdesktop.wonderland.modules.timeline.common.provider.messages.ProviderAddResultMessage;
import org.jdesktop.wonderland.modules.timeline.common.provider.messages.ProviderObjectMessage;
import org.jdesktop.wonderland.modules.timeline.common.provider.messages.ProviderRemoveResultMessage;
import org.jdesktop.wonderland.modules.timeline.common.provider.messages.ProviderResetResultMessage;

/**
 * Component for receiving data from a timeline provider.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineProviderComponent extends CellComponent {
    /** a logger */
    private static Logger logger =
            Logger.getLogger(TimelineProviderComponent.class.getName());

    /** the set of timeline providers */
    private final Map<TimelineQueryID, ResultImpl> results =
            new LinkedHashMap<TimelineQueryID, ResultImpl>();

    /** listeners */
    private final Set<TimelineProviderComponentListener> listeners =
            new CopyOnWriteArraySet<TimelineProviderComponentListener>();

    /** the cell channel */
    @UsesCellComponent
    private ChannelComponent channel;

    /** the message receiver */
    private final MessageReceiver receiver = new MessageReceiver();

    /**
     * Default constructor
     * @param cell the cell this component is attached to
     */
    public TimelineProviderComponent(Cell cell) {
        super(cell);
    }

    /**
     * Get all active results for this cell.
     * @return the set of timeline results
     */
    public Collection<TimelineResult> getResults() {
        return new ArrayList<TimelineResult>(results.values());
    }

    /**
     * Add a listener that will be notified when the set of providers
     * changes.
     * @param listener the listener to add
     */
    public void addComponentListener(TimelineProviderComponentListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener
     * @param listener the listener to remove
     */
    public void removeComponentListener(TimelineProviderComponentListener listener) {
        listeners.remove(listener);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void setClientState(CellComponentClientState state) {
        super.setClientState(state);

        TimelineProviderClientState tpcs = (TimelineProviderClientState) state;
        for (TimelineQuery query : tpcs.getQueries()) {
            DatedSet result = tpcs.getResults(query);

            ResultImpl ri = new ResultImpl(query, result);
            results.put(query.getQueryID(), ri);
        
            fireResultAdded(ri);
        }
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if (status == CellStatus.INACTIVE && increasing) {
            channel.addMessageReceiver(ProviderAddResultMessage.class, receiver);
            channel.addMessageReceiver(ProviderObjectMessage.class, receiver);
            channel.addMessageReceiver(ProviderRemoveResultMessage.class, receiver);
            channel.addMessageReceiver(ProviderResetResultMessage.class, receiver);
        } else if (status == CellStatus.INACTIVE && !increasing) {
            channel.removeMessageReceiver(ProviderAddResultMessage.class);
            channel.removeMessageReceiver(ProviderObjectMessage.class);
            channel.removeMessageReceiver(ProviderRemoveResultMessage.class);
            channel.removeMessageReceiver(ProviderResetResultMessage.class);
        }
    }

    private void fireResultAdded(TimelineResult result) {
        for (TimelineProviderComponentListener listener : listeners) {
            listener.resultAdded(result);
        }
    }

    private void fireResultRemoved(TimelineResult result) {
        for (TimelineProviderComponentListener listener : listeners) {
            listener.resultRemoved(result);
        }
    }

    private void handleAddResult(ProviderAddResultMessage message) {
        ResultImpl ri = new ResultImpl(message.getQuery(), new DatedSet());
        results.put(message.getQuery().getQueryID(), ri);
        fireResultAdded(ri);
    }

    private void handleObject(ProviderObjectMessage message) {
        ResultImpl result = results.get(message.getID());
        switch (message.getAction()) {
            case ADD:
                result.addResult(message.getObject());
                break;
            case REMOVE:
                result.removeResult(message.getObject());
                break;
        }
    }

    private void handleRemoveResult(ProviderRemoveResultMessage message) {
        ResultImpl ri = results.remove(message.getID());
        if (ri != null) {
            fireResultRemoved(ri);
        }
    }

    private void handleResetResult(ProviderResetResultMessage message) {
        // remove all results and notify listeners
        ResultImpl result = results.get(message.getID());
        Iterator<DatedObject> i = result.getResultSet().iterator();
        while (i.hasNext()) {
            result.fireResultRemoved(i.next());
            i.remove();
        }
    }

    /**
     * Message receiver
     */
    private class MessageReceiver implements ComponentMessageReceiver {
        public void messageReceived(CellMessage message) {
            if (message instanceof ProviderAddResultMessage) {
                handleAddResult((ProviderAddResultMessage) message);
            } else if (message instanceof ProviderObjectMessage) {
                handleObject((ProviderObjectMessage) message);
            } else if (message instanceof ProviderRemoveResultMessage) {
                handleRemoveResult((ProviderRemoveResultMessage) message);
            } else if (message instanceof ProviderResetResultMessage) {
                handleResetResult((ProviderResetResultMessage) message);
            }

        }
    }

    /**
     * Implementation of a result object
     */
    private class ResultImpl implements TimelineResult {
        private TimelineQuery query;
        private DatedSet resultSet;

        private final Set<TimelineResultListener> listeners =
                new CopyOnWriteArraySet<TimelineResultListener>();

        ResultImpl(TimelineQuery query, DatedSet resultSet) {
            this.query = query;
            this.resultSet = resultSet;
        }

        public TimelineQuery getQuery() {
                return query;
        }

        public DatedSet getResultSet() {
            return resultSet;
        }

        public void addResultListener(TimelineResultListener listener) {
            listeners.add(listener);
        }

        public void removeResultListener(TimelineResultListener listener) {
            listeners.remove(listener);
        }

        private void addResult(DatedObject obj) {
            resultSet.add(obj);
            fireResultAdded(obj);

        }

        private void removeResult(DatedObject obj) {
            resultSet.remove(obj);
            fireResultRemoved(obj);
        }

        private void fireResultAdded(DatedObject obj) {
            for (TimelineResultListener l : listeners) {
                l.added(obj);
            }
        }

        private void fireResultRemoved(DatedObject obj) {
            for (TimelineResultListener l : listeners) {
                l.removed(obj);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ResultImpl other = (ResultImpl) obj;
            if (this.query != other.query &&
                    (this.query == null || !this.query.equals(other.query)))
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 31 * hash + (this.query != null ? this.query.hashCode() : 0);
            return hash;
        }
    }
}
