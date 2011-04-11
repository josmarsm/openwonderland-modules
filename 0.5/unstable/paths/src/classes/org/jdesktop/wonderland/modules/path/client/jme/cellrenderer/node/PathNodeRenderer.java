package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;

/**
 * Implementors of this interface are intended to render PathNodes of a given type.
 *
 * @author Carl Jokl
 */
public interface PathNodeRenderer extends CellRenderer {

    /**
     * Get the ClientPathNode contained within this PathNodeRenderer.
     * 
     * @return The ClientPathNode contained within this PathNodeRenderer.
     */
    public ClientPathNode getPathNode();

    /**
     * Get the type of PathNode which this PathNodeRenderer is intended to render.
     *
     * @return The NodeStyleType which represents the specific type of PathNode
     *         which this PathNodeRenderer is intended to render.
     */
    public NodeStyleType getRenderedType();

    /**
     * Dispose of this PathNodeRenderer and any resources which may be held by it.
     */
    public void dispose();
}
