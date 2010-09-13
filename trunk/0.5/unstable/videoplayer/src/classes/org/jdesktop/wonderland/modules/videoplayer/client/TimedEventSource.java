/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.videoplayer.client;

/**
 * A listenable source of timed events
 *
 * @author nsimpson
 */
public interface TimedEventSource {
    /**
     * Add a listener for time changes
     *
     * @param listener a time listener to be notified of time changes
     */
    public void addTimeListener(TimeListener listener);

    /**
     * Remove a listener for time changes
     *
     * @param listener a time listener to be removed
     */
    public void removeTimeListener(TimeListener listener);
}
