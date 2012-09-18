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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.timeline.client.provider.annotation.QueryBuilder;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQuery;

/**
 * Utilities related to the timeline provider
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineProviderUtils {
    /**
     * Get all registered query builders.  The result is a map from
     * the builder's query class to the display name of the builder.  The
     * query class can then be used to get an instance of the builder.
     *
     * @return a map from builder query class to the display name.
     */
    public static Map<String, String> getQueryBuilders() {
        Map<String, String> out = new LinkedHashMap<String, String>();

        // get the classloader
        ScannedClassLoader scl =
                TimelineProviderClientPlugin.getServerSessionManager().getClassloader();

        // find classes annotated with @QueryBuilder
        Iterator<TimelineQueryBuilder> builders =
                scl.getInstances(QueryBuilder.class, TimelineQueryBuilder.class);
        while (builders.hasNext()) {
            TimelineQueryBuilder builder = builders.next();
            out.put(builder.getQueryClass(), builder.getDisplayName());
        }

        return out;
    }

    /**
     * Instantiate a query builder of the given class, and set it up with
     * the given timeline properties.  The setTimelineConfiguration() method
     * of the builder will be called with the given configuration.
     * @param queryClass the queryClass for the builder
     * @param config the configuration
     * @return the instantiated and initialized query builder
     */
    public static TimelineQueryBuilder createBuilder(String queryClass, 
                                             TimelineConfiguration config)
    {
        // get the classloader
        ScannedClassLoader scl = 
                TimelineProviderClientPlugin.getServerSessionManager().getClassloader();

        // find classes annotated with @QueryBuilder
        Iterator<TimelineQueryBuilder> builders =
                scl.getInstances(QueryBuilder.class, TimelineQueryBuilder.class);
        while (builders.hasNext()) {
            TimelineQueryBuilder builder = builders.next();
            if (builder.getQueryClass().equals(queryClass)) {
                builder.setTimelineConfiguration(config);
                return builder;
            }
        }
        
        // no result
        throw new IllegalArgumentException("No builder for " + queryClass);
    }

    /**
     * Create a new query builder for the given query object. The
     * setTimelineConfiguration() and setQuery() methods will be called on
     * the object before it is returned.
     * @param query the query to get a builder for
     * @param config the timeline configuration
     * @return the instantiated and initialized query builder
     */
    public static TimelineQueryBuilder createBuilder(TimelineQuery query,
                                               TimelineConfiguration config)
    {
        TimelineQueryBuilder builder = createBuilder(query.getQueryClass(),
                                                     config);
        builder.setQuery(query);
        return builder;
    }
}
