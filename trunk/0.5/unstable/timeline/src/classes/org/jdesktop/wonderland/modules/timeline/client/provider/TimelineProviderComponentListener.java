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

import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineResult;

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
    public void resultAdded(TimelineResult result);

    /**
     * Notification that a result was removed
     * @param result the result that was removed
     */
    public void resultRemoved(TimelineResult result);
}
