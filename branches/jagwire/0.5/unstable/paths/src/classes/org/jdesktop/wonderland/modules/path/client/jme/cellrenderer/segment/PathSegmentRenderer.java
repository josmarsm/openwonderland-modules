package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.ChildRenderer;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This interface is used to define a class which is responsible for rendering a segment of a path.
 *
 * @author Carl Jokl
 */
public interface PathSegmentRenderer extends ChildRenderer {

    /**
     * Get SegmentStyleType which this PathSegmentRenderer is used to render.
     *
     * @return The SegmentStyleType of the type of segment which this PathSegmentRenderer
     *         is intended to render. This method can return null if the PathSegmentRenderer
     *         is not specific to any given SegmentStyleType.
     */
    public SegmentStyleType getRenderedType();

    /**
     * Get the ClientPathNode which is the start node for the rendered NodePath segment.
     *
     * @return The ClientPathNode which is the start node for the rendered NodePath
     */
    public ClientPathNode getStartNode();
}
