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

package org.jdesktop.wonderland.modules.timeline.common.provider;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 * Client state for timeline provider component
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineProviderClientState extends CellComponentClientState {
    /** the results */
    private final Map<TimelineQuery, DatedSet> results = 
            new LinkedHashMap<TimelineQuery, DatedSet>();

    /** Default constructor */
    public TimelineProviderClientState() {
    }

    /**
     * Add a result
     * @param query the query that generated the result
     * @param results the set of results
     */
    public void addResult(TimelineQuery query, DatedSet resultSet) {
        results.put(query, resultSet);
    }

    /**
     * Get all the queries in the result set
     * @return all queries
     */
    public Set<TimelineQuery> getQueries() {
        return results.keySet();
    }

    /**
     * Get the result set that maps to the given query
     * @return the results for the given query
     */
    public DatedSet getResults(TimelineQuery query) {
        return results.get(query);
    }
}
