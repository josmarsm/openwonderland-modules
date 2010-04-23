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

import com.jme.image.Texture;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import java.util.Collection;
import java.util.Collections;
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
     * Recursively pass the update down to this node's children stopping when
     * one of them processes the message.
     * @param message The message to be applied
     * @return The node which processed the message, or null if none of
     * our children could process it
     */
    public VisualParent applyUpdateToDescendant(NodeUpdateMessage message) {
        if (this.getChildren() == null) {
            return null;
        }
        for (Spatial child : this.getChildren()) {
            if (child instanceof VisualParent) {
                VisualParent node = ((VisualParent) child).applyUpdateToDescendant(message);
                if (node != null) {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * Recursively pass the removal message down to child nodes, removing
     * any who report that the message applies to them.
     * @param visualDeletedMessage The deletion message to be applied to relevant children
     */
    public void removeDescendant(VisualDeletedMessage visualDeletedMessage) {
        if (getChildren() != null) {
            Collection<Spatial> childrenCopy = Collections.unmodifiableCollection(getChildren());
            for (Spatial child : childrenCopy) {
                if (child instanceof VisualParent) {
                    ((VisualParent) child).removeDescendant(visualDeletedMessage);
                }
            }
        }
    }

    /**
     * Recursively update the visibility of our descendants; in the case
     * of a VisualParent, there is no visibility to update, so we don't
     * perform any actions on ourself.
     */
    public void updateVisibility() {
        if (this.getChildren() != null) {
            for (Spatial child : this.getChildren()) {
                if (child instanceof VisualParent) {
                    ((VisualParent) child).updateVisibility();
                }
            }
        }
    }

    /**
     * Unload the textures associated with the VisualParent's who are descendants
     * of this one, as well as the texture (if any) associated with this node
     * itself.
     */
    public void unloadVisualDescendantTextures() {
        if (getChildren() != null) {
            Collection<Spatial> childrenCopy = Collections.unmodifiableCollection(getChildren());
            for (Spatial child : childrenCopy) {
                if (child instanceof VisualParent) {
                    ((VisualParent) child).unloadVisualDescendantTextures();
                }
            }
        }
    }
}
