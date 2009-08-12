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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * Server state for timeline provider cell component
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@XmlRootElement(name="timeline-provider-component")
@ServerState
public class TimelineProviderServerState extends CellComponentServerState {
    /** the set of queries */
    private Set<TimelineQuery> queries = new LinkedHashSet<TimelineQuery>();

    /** Default constructor */
    public TimelineProviderServerState() {
    }

    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.timeline.server.provider.TimelineProviderComponentMO";
    }

    @XmlElement
    @XmlJavaTypeAdapter(QuerySetAdapter.class)
    public Set<TimelineQuery> getQueries() {
        return queries;
    }

    public void setQueries(Set<TimelineQuery> queries) {
        this.queries = queries;
    }

    private static final class QuerySetAdapter
            extends XmlAdapter<TimelineQuery[], Set<TimelineQuery>>
    {

        @Override
        public Set<TimelineQuery> unmarshal(TimelineQuery[] v) throws Exception {
            return new LinkedHashSet<TimelineQuery>(Arrays.asList(v));
        }

        @Override
        public TimelineQuery[] marshal(Set<TimelineQuery> v) throws Exception {
            return v.toArray(new TimelineQuery[v.size()]);
        }
    }
}
