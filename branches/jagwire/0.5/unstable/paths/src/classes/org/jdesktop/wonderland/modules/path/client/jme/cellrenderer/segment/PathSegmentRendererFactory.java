package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * Implementers of this interface are used to create instances of PathSegmentRenderers for a given style type
 * for individual path segments to be rendered..
 *
 * @author Carl Jokl
 */
public interface PathSegmentRendererFactory {

    /**
     * Create a Renderer for a path segment.
     *
     * @param startNode The ClientPathNode to which the path segment belongs which is to be rendered.
     * @throws IllegalArgumentException If the specified start ClientPathNode was null.
     */
    public PathSegmentRenderer createRenderer(ClientPathNode startNode) throws IllegalArgumentException;

    /**
     * Get the SegmentStyleType for which this PathSegmentRendererFactory supports creating PathSegmentRenderers.
     *
     * @return The SegmentStyleType for which this PathSegmentRendererFactory supports creating PathSegmentRenderers.
     */
    public SegmentStyleType getRenderedSegmentStyleType();
}
