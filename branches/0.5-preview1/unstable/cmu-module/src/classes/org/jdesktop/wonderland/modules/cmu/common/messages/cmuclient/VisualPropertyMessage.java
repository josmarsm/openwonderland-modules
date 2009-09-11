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

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;

/**
 * Message containing basic properties for a CMU visual (i.e. scale,
 * whether the visual is currently visible).
 * @author kevin
 */
public class VisualPropertyMessage extends NodeUpdateMessage {

    private Vector3f scale = null;
    private boolean visible = false;

    /**
     * Standard constructor.
     * @param nodeID The ID corresponding to this visual
     */
    public VisualPropertyMessage(NodeID nodeID) {
        super(nodeID);
    }

    /**
     * Copy constructor.
     * @param toCopy The message to copy
     */
    public VisualPropertyMessage(VisualPropertyMessage toCopy) {
        super(toCopy);
        setScale(toCopy.getScale());
        setVisible(toCopy.isVisible());
    }

    /**
     * Set the scale at which the relevant visual node is drawn.
     * @param scale Scale at which to draw the node
     */
    public synchronized void setScale(Vector3f scale) {
        this.scale = scale;
    }

    /**
     * Get the scale at which the relevant visual node is drawn.
     * @return Current scale
     */
    public synchronized Vector3f getScale() {
        return this.scale;
    }

    /**
     * Find out whether the visual is visible in the scene.
     * @return Whether the visual is currently visible
     */
    public synchronized boolean isVisible() {
        return visible;
    }

    /**
     * Set the visibility of the visual in the scene.
     * @param visible Whether the visual is currently visible
     */
    public synchronized void setVisible(boolean visible) {
        this.visible = visible;
    }
}
