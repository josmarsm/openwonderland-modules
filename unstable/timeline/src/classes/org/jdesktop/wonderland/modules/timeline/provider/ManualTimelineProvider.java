/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.provider;

import org.jdesktop.wonderland.modules.timeline.provider.spi.TimelineProvider;
import org.jdesktop.wonderland.modules.timeline.provider.spi.TimelineProviderContext;

/**
 * The manual timeline provider doesn't do anything in the provider process,
 * it accepts user additions and removals from the cell MO
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ManualTimelineProvider implements TimelineProvider {
    public void initialize(TimelineProviderContext context) {
        // do nothing
    }

    public void shutdown() {
        // do nothing
    }
}
