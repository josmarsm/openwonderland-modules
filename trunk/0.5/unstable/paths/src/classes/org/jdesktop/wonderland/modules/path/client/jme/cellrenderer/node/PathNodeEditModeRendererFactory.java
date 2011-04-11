package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import org.jdesktop.wonderland.modules.path.client.ClientPathNode;

/**
 * Implementers of this interface are used to get all the PathNodeRenderers available to render PathNodes.
 *
 * @author Carl Jokl
 */
public interface PathNodeEditModeRendererFactory {

    /**
     * Create an edit mode Renderer for a ClientPathNode.
     *
     * @param node The ClientPathNode for which to create a renderer.
     * @return The PathNodeRenderer to render the specified ClientPathNode in edit mode
     * @throws IllegalArgumentException If the specified ClientPathNode is null.
     */
    public PathNodeRenderer createRenderer(ClientPathNode node) throws IllegalArgumentException;
}
