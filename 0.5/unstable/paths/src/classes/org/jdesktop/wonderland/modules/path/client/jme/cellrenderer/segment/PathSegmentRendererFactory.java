package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
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
     * @param path The ClienNodePath for to which the path segment belongs for which to create a renderer.
     * @param segmentIndex The index of the path segment within the NodePath which is to be rendered.
     * @param startNodeIndex The index of the PathNode at which the segment to be rendered begins.
     * @param endNodeIndex The index of the PathNode at which the segment to be rendered ends.
     * @return The PathNodeRenderer for the specified ClientPathNode.
     * @throws IllegalArgumentException If the specified ClientNodePath is null.
     */
    public PathSegmentRenderer createRenderer(ClientNodePath path, int segmentIndex, int startNodeIndex, int endNodeIndex) throws IllegalArgumentException;

    /**
     * Get the SegmentStyleType for which this PathSegmentRendererFactory supports creating PathSegmentRenderers.
     *
     * @return The SegmentStyleType for which this PathSegmentRendererFactory supports creating PathSegmentRenderers.
     */
    public SegmentStyleType getRenderedSegmentStyleType();
}
