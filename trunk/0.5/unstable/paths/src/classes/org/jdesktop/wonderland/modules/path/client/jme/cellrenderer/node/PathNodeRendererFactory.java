package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;

/**
 * Implementers of this interface are used to create PathNodeRenderers for a specific style of rendering
 * for individual PathNodes.
 *
 * @author Carl Jokl
 */
public interface PathNodeRendererFactory {

    /**
     * Create a Renderer for a client PathNode.
     *
     * @param node The ClientPathNode for which to create a renderer.
     * @return The PathNodeRenderer for the specified ClientPathNode.
     * @throws IllegalArgumentException If the specified ClientPathNode is null or the NodeStyle is null.
     */
    public PathNodeRenderer createRenderer(ClientPathNode node) throws IllegalArgumentException;

    /**
     * Get the NodeStyleType for which this PathNodeRendererFactory supports creating PathNodeRenderers.
     *
     * @return The NodeStyleType for which this PathNodeRendererFactory supports creating PathNodeRenderers.
     */
    public NodeStyleType getRenderedNodeStyleType();
}
