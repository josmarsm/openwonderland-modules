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
package org.jdesktop.wonderland.modules.cmu.client.jme.cellrenderer;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.util.Iterator;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.NodeUpdateMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualDeletedMessage;

/**
 * A jME node which is recognized as having children which are VisualNodes.
 * Identical to a standard Node, except for convenience methods that allow
 * messages to be passed down recursively to its children.
 * @author kevin
 */
public class VisualParent extends Node {

    /**
     * Recursively pass the transformation down to each child node which
     * can process it; VisualNode overrides this method to do actual computation
     * baserd on the transformation.
     * @param transformation The transformation to be applied to a matching child
     */
    public synchronized VisualParent applyMessageToChild(NodeUpdateMessage message) {
        if (this.getChildren() == null) {
            return null;
        }
        for (Spatial child : this.getChildren()) {
            if (child instanceof VisualParent) {
                VisualParent node = ((VisualParent) child).applyMessageToChild(message);
                if (node != null) {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * Recursively pass the removal message down to child nodes, who should
     * remove themselves if it applies to them.
     * @param deleted The deletion message to be applied to a matching child
     */
    public synchronized void removeChild(VisualDeletedMessage deleted) {
        removeChild(deleted.getNodeID());
    }

    protected synchronized boolean removeChild(NodeID id) {
        Iterator<Spatial> it = this.getChildren().iterator();
        while (it.hasNext()) {
            Spatial child = it.next();
            if (child instanceof VisualParent) {
                if (((VisualParent) child).removeChild(id)) {
                    it.remove();
                }
            }
        }
        return false;
    }

    public synchronized void updateVisibility() {
        if (this.getChildren() != null) {
            for (Spatial child : this.getChildren()) {
                if (child instanceof VisualParent) {
                    ((VisualParent)child).updateVisibility();
                }
            }
        }
    }
}
