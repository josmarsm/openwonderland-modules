/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.client.provider;

import org.jdesktop.wonderland.modules.timeline.common.provider.DatedSet;

/**
 * The client for a particular timeline provider.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public interface TimelineProviderResult {
    /**
     * Get the set of all objects exposed by this result.
     * @return all objects in this result
     */
    public DatedSet getResultSet();

    /**
     * Add a listener for objects being added or removed
     * @param listener the listener to add
     */
    public void addResultListener(TimelineProviderResultListener listener);

    /**
     * Remove a listener for objects being added or removed
     * @param listener the listener to remove
     */
    public void removeResultListener(TimelineProviderResultListener listener);
}
