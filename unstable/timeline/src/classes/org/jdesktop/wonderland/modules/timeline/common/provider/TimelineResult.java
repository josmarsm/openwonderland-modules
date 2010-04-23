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
    public DatedSet<? extends DatedObject> getResultSet();

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
