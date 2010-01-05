/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.server.provider;

import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineResult;

/**
 * A listener that will be notified of changes to the set of timeline provider
 * Results.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public interface TimelineProviderComponentMOListener {
    /**
     * Notification that a new result was added
     * @param result the result that was added
     */
    public void resultAdded(TimelineResult result);

    /**
     * Notification that a result was removed
     * @param result the result that was removed
     */
    public void resultRemoved(TimelineResult result);
}
