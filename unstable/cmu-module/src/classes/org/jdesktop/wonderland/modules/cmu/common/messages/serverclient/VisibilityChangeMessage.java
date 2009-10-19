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
package org.jdesktop.wonderland.modules.cmu.common.messages.serverclient;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.cmu.common.VisualType;

/**
 * Message to notify the server/clients that the visibility of a particular
 * category of visual has changed.  Used to e.g. turn ground plane visibility
 * on and off.
 * @author kevin
 */
public class VisibilityChangeMessage extends CellMessage {

    private VisualType type;
    private boolean showing;

    /**
     * Standard constructor.
     * @param type The category of visual whose visibility has changed
     * @param showing The visibility of visuals in the provided category
     */
    public VisibilityChangeMessage(VisualType type, boolean showing) {
        super();
        setShowing(showing);
        setType(type);
    }

    /**
     * Find out whether visuals of this message's type are showing.
     * @return Visibility of visuals of this message's type
     */
    public boolean isShowing() {
        return showing;
    }

    /**
     * Set the visibility of visuals described by this message.
     * @param showing Visibility of visuals of this message's type
     */
    public void setShowing(boolean showing) {
        this.showing = showing;
    }

    /**
     * Get the category of visual affected by this message.
     * @return Category of visual affected by this message
     */
    public VisualType getType() {
        return type;
    }

    /**
     * Set the category of visual affected by this message.
     * @param type Category of visual affected by this message
     */
    public void setType(VisualType type) {
        this.type = type;
    }
}