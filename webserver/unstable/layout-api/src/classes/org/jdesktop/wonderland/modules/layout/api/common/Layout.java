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

import java.io.Serializable;
import java.util.List;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A layout manipulates a set of participants by moving them into particular
 * positions as they are added.
 * <p>
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@ExperimentalAPI
public interface Layout extends Serializable {
    /**
     * Set the configuration of this layout. This method may be called any time
     * a property of the configuration changes. It is up to the layout to
     * remain consistent through multiple calls to <code>setConfig()</code>.
     * @param config the configuration for this layout.  The framework will
     * ensure that configurations are compatible with the type of layout that
     * is being configured.
     */
    public void setConfig(LayoutConfig config);

    /**
     * Get the most recently set layout configuration.  This layout
     * configuration should reflect the current state of the layout, so will
     * be updated if the layout is changed via mechanisms other than the
     * <code>setConfig()</code> method.
     * @return the current layout configuration.
     */
    public LayoutConfig getConfig();

    /**
     * Returns a list of layout participants managed by this layout. The list
     * is ordered by the order in which layout participants were added to the
     * layout.
     * @return an ordered list of layout participants.
     */
    public List<LayoutParticipant> getLayoutParticipants();

    /**
     * Set the layout participant given its order. This method is used to insert
     * a layout participant that has been previously added.
     * XXX
     * This is used to recreate the Layout when re-reading from WFS.
     * XXX
     */
    public void setParticipantAt(LayoutParticipant participant, int index);

    /**
     * Add a layout participant. Layout participants are guaranteed to only
     * participate in a single layout at a time.  It is up to the layout to
     * preserve ordering of adds, if desired.
     * <p>
     * It is up to the layout to call <code>setPosition()</code> and
     * <code>setSize()</code> as appropriate when an object is added. The
     * framework will guarantee that the <code>added()</code> method is called.
     * @param participant the participant to add.
     * @return the index of the participant in the list of participants
     */
    public int addParticipant(LayoutParticipant participant);

    /**
     * Remove a participant from the layout.
     * <p>
     * The framework guarantees that the <code>removed()</code> method is called
     * after the participant is removed from the layout.
     * @param participant the participant to remove.
     */
    public void removeParticipant(LayoutParticipant participant);
}
