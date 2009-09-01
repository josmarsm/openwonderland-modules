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
package org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient;

import org.jdesktop.wonderland.modules.cmu.common.NodeID;

/**
 *
 * @author kevin
 */
public class ModelPropertyMessage extends SingleNodeMessage {

    private float scale = 0.0f;
    private boolean visible = false;

    public ModelPropertyMessage(NodeID nodeID) {
        super(nodeID);
    }

    public ModelPropertyMessage(ModelPropertyMessage toCopy) {
        super(toCopy);
        setScale(toCopy.getScale());
        setVisible(toCopy.isVisible());
    }

    /**
     * Set the scale at which the relevant visual node is drawn.
     * @param scale New scale
     */
    public synchronized void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * Get the scale at which the relevant visual node is drawn.
     * @return Current scale
     */
    public synchronized float getScale() {
        return this.scale;
    }

    public synchronized boolean isVisible() {
        return visible;
    }

    public synchronized void setVisible(boolean visible) {
        this.visible = visible;
    }
}
