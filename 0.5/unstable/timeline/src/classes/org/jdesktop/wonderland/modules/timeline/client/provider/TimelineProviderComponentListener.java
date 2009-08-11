/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.client.provider;

/**
 * A listener that will be notified of changes to the set of timeline provider
 * Results.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public interface TimelineProviderComponentListener {
    /**
     * Notification that a new result was added
     * @param result the result that was added
     */
    public void resultAdded(TimelineProviderResult result);

    /**
     * Notification that a result was removed
     * @param result the result that was removed
     */
    public void resultRemoved(TimelineProviderResult result);
}
