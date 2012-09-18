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
package org.jdesktop.wonderland.modules.cmu.client.ui.events;

import javax.swing.JPanel;
import org.jdesktop.wonderland.modules.cmu.common.events.WonderlandEvent;
import org.jdesktop.wonderland.modules.cmu.common.events.responses.CMUResponseFunction;

/**
 * Base class for a a panel containing options for a particular event type.
 * @author kevin
 */
public abstract class EventSettingsPanel<EventType extends WonderlandEvent> extends JPanel {

    /**
     * Get the event associated with these settings.  Should return null if
     * no valid event can be constructed.
     * @return Event for these settings, or null if the settings are invalid
     */
    public abstract EventType getEvent();

    /**
     * Update the fields of this panel to represent the given event (e.g.
     * so that if getEvent() were called, it would return an event equivalent
     * to the one provided).
     * @param event Event to represent
     */
    public abstract void setEvent(EventType event);

    public abstract Class getEventClass();

    public abstract boolean allowsResponse(CMUResponseFunction response);
}
