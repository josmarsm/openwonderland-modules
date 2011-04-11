package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.style.UnsupportedStyleException;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;

/**
 * Implementers of this interface are used to get all the PathNodeRenderers available to render PathNodes.
 *
 * @author Carl Jokl
 */
public interface PathNodeRendererFactory {

    /**
     * Create a Renderer for a client PathNode.
     *
     * @param node The ClientPathNode for which to create a renderer.
     * @param style The style to use in order to render the specified node.
     * @return The PathNodeRenderer for the specified ClientPathNode.
     * @throws IllegalArgumentException If the specified ClientPathNode is null or the NodeStyle is null.
     * @throws UnsupportedStyleException If the node style is not of the right type for this PathNodeRendererFactory.
     */
    public PathNodeRenderer createRenderer(ClientPathNode node, NodeStyle style) throws IllegalArgumentException, UnsupportedStyleException;

    /**
     * Get the NodeStyleType for which this PathNodeRendererFactory supports creating PathNodeRenderers.
     *
     * @return The NodeStyleType for which this PathNodeRendererFactory supports creating PathNodeRenderers.
     */
    public NodeStyleType getRenderedNodeStyleType();
}
