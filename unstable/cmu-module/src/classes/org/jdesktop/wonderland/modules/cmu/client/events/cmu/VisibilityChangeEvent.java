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
package org.jdesktop.wonderland.modules.cmu.client.events.cmu;

import org.jdesktop.wonderland.modules.cmu.client.CMUCell;
import org.jdesktop.wonderland.modules.cmu.common.VisualType;

/**
 * Event representing a change in visibility for a particular visual node type.
 * We allow the visibility of nodes to be changed on a type-by-type basis to,
 * for example, allow the ground plane of a scene to be switched on and off.
 * @author kevin
 */
public class VisibilityChangeEvent extends CMUChangeEvent {

    private VisualType visualType;
    private boolean showing;

    /**
     * Standard constructor.
     * @param cell The cell on which visibility has changed
     * @param visualType The type of visual whose visibility is changing
     * @param showing The new visibility for that visual
     */
    public VisibilityChangeEvent(CMUCell cell, VisualType visualType, boolean showing) {
        super(cell);
        setVisualType(visualType);
        setShowing(showing);
    }

    /**
     * Get the visibility represented by this event.
     * @return Whether visuals of this event's type are showing
     */
    public boolean isShowing() {
        return showing;
    }

    /**
     * Set the visibility for this event
     * @param showing Whether visuals of this event's type are showing
     */
    public void setShowing(boolean showing) {
        this.showing = showing;
    }

    /**
     * Get the type of visual represented by this event.
     * @return The type of visual whose visibility is changing
     */
    public VisualType getVisualType() {
        return visualType;
    }

    /**
     * Set the type of visual represented by this event.
     * @param visualType The type of visual whose visibility is changing
     */
    public void setVisualType(VisualType visualType) {
        this.visualType = visualType;
    }
}