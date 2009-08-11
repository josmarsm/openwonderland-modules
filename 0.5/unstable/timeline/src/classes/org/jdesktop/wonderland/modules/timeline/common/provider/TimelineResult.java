/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.common.provider;

/**
 * The client for a particular timeline provider.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public interface TimelineResult {
    /**
     * Get the query that generated this result
     * @return the query that generated this result
     */
    public TimelineQuery getQuery();

    /**
     * Get the set of all objects exposed by this result.
     * @return all objects in this result
     */
    public DatedSet getResultSet();

    /**
     * Add a listener for objects being added or removed
     * @param listener the listener to add
     */
    public void addResultListener(TimelineResultListener listener);

    /**
     * Remove a listener for objects being added or removed
     * @param listener the listener to remove
     */
    public void removeResultListener(TimelineResultListener listener);
}
