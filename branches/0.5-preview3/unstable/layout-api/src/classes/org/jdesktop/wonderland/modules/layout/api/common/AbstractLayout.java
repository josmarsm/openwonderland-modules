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
package org.jdesktop.wonderland.modules.layout.api.common;

import java.util.LinkedList;
import java.util.List;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * An abstract base class that implements the basics of the Layout interface.
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@ExperimentalAPI
public abstract class AbstractLayout implements Layout {

    // The current configuration for the layout
    private LayoutConfig layoutConfig = null;

    // An ordered list of partipants, in the order in which they were added.
    private List<LayoutParticipant> layoutParticipantList =
            new LinkedList<LayoutParticipant>();

    /**
     * Returns the index of the given layout participant in the list, or -1
     * if the participant is not in the layout.
     *
     * @param participant The layout participant
     * @return The position in the list of the participant
     */
    public int getIndexOfParticipant(LayoutParticipant participant) {
        synchronized (layoutParticipantList) {
            return layoutParticipantList.indexOf(participant);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setConfig(LayoutConfig config) {
        layoutConfig = config;
    }

    /**
     * {@inheritDoc}
     */
    public LayoutConfig getConfig() {
        return layoutConfig;
    }

    /**
     * {@inheritDoc}
     */
    public List<LayoutParticipant> getLayoutParticipants() {
        // Return a copy of the list, so that the invoker can modify it as it
        // pleases
        return new LinkedList<LayoutParticipant>(layoutParticipantList);
    }

    /**
     * {@inheritDoc}
     */
    public void setParticipantAt(LayoutParticipant participant, int index) {
        // Add the participant, synchronizing around the list. We do not
        // notify the layour participant here
        // XXXX
        synchronized (layoutParticipantList) {
            layoutParticipantList.add(index, participant);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int addParticipant(LayoutParticipant participant) {
        // Add the participant, synchronizing around the list. Also notify
        // the layout participant that is has been added. Should the notify
        // happen AFTER the layout has actually happened though?
        synchronized (layoutParticipantList) {
            layoutParticipantList.add(participant);
            participant.added(this);
            return layoutParticipantList.indexOf(participant);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeParticipant(LayoutParticipant participant) {
        // Remove the participant, synchronizing around the list. Also notify
        // the layout participant that is has been removed.
        synchronized (layoutParticipantList) {
            layoutParticipantList.remove(participant);
            participant.removed(this);
        }
    }
}
