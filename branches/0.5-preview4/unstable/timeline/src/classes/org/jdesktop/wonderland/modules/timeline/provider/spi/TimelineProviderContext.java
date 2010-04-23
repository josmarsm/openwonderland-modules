/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.provider.spi;

import java.util.Set;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQuery;

/**
 * An object which is passed into a provider to give it configuration
 * information and let it interact with the rest of the provider system.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public interface TimelineProviderContext {
    /**
     * Get the query that generated this provider
     * @return the provider
     */
    public TimelineQuery getQuery();

    /**
     * Add results to a query
     * @param results the results to add
     */
    public void addResults(Set<DatedObject> results);

    /**
     * Remove results from a query
     * @param results the results to remove
     */
    public void removeResults(Set<DatedObject> results);

    /**
     * Reset all results in the query
     */
    public void resetResults();
}
