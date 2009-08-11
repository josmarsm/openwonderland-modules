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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedSet;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineResult;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineResultListener;

/**
 * Component for receiving data from a timeline provider.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineProviderComponent extends CellComponent {
    /** a logger */
    private static Logger logger =
            Logger.getLogger(TimelineProviderComponent.class.getName());

    /** the set of timeline providers */
    private final Set<TimelineResult> results =
            new LinkedHashSet<TimelineResult>();

    /** listeners */
    private final Set<TimelineProviderComponentListener> listeners =
            new CopyOnWriteArraySet<TimelineProviderComponentListener>();

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
    public Set<TimelineResult> getResults() {
        return Collections.unmodifiableSet(results);
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
     * Implementation of a result object
     */
    private class ResultImpl implements TimelineResult {
        private DatedSet resultSet;
        private final Set<TimelineResultListener> listeners =
                new CopyOnWriteArraySet<TimelineResultListener>();

        ResultImpl(DatedSet resultSet) {
            this.resultSet = resultSet;
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

        public void addResult(DatedObject obj) {
            resultSet.add(obj);
            fireResultAdded(obj);

        }

        public void removeResult(DatedObject obj) {
            resultSet.remove(obj);
            fireResultRemoved(obj);
        }

        protected void fireResultAdded(DatedObject obj) {
            for (TimelineResultListener l : listeners) {
                l.added(obj);
            }
        }

        protected void fireResultRemoved(DatedObject obj) {
            for (TimelineResultListener l : listeners) {
                l.removed(obj);
            }
        }
    }
}