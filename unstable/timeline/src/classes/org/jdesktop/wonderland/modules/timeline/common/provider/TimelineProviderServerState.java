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

package org.jdesktop.wonderland.modules.timeline.common.provider;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
    private List<TimelineQuery> queries = new LinkedList<TimelineQuery>();

    /** Default constructor */
    public TimelineProviderServerState() {
    }

    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.timeline.server.provider.TimelineProviderComponentMO";
    }

    @XmlElement
    @XmlJavaTypeAdapter(QueryListAdapter.class)
    public List<TimelineQuery> getQueries() {
        return queries;
    }

    public void setQueries(List<TimelineQuery> queries) {
        this.queries = queries;
    }

    private static final class QueryListAdapter
            extends XmlAdapter<TimelineQuery[], List<TimelineQuery>>
    {

        @Override
        public List<TimelineQuery> unmarshal(TimelineQuery[] v) throws Exception {
            return new LinkedList<TimelineQuery>(Arrays.asList(v));
        }

        @Override
        public TimelineQuery[] marshal(List<TimelineQuery> v) throws Exception {
            return v.toArray(new TimelineQuery[v.size()]);
        }
    }
}
