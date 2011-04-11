package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import com.jme.scene.Node;
import org.jdesktop.wonderland.modules.path.common.PathNode;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This interface is used to define a class which is responsible for rendering a segment of a path.
 *
 * @author Carl Jokl
 */
public interface PathSegmentRenderer {
    
    /**
     * Render this path segment.
     * 
     * @param style A SegmentStyle object containing style information to be used in styling the rendered PathSegment.
     * @param startNode The PathNode at which the segment starts.
     * @param endNode The PathNode at which the segment ends.
     * @return A JME Node object containing the rendered path segment. This can be null if the specific renderer does
     *         not have any representation of the segment available or could not create a representation.
     */
    public Node render(SegmentStyle style, PathNode startNode, PathNode endNode);

    /**
     * Get SegmentStyleType which this PathSegmentRenderer is used to render.
     *
     * @return The SegmentStyleType of the type of segment which this PathSegmentRenderer
     *         is intended to render. This method can return null if the PathSegmentRenderer
     *         is not specific to any given SegmentStyleType.
     */
    public SegmentStyleType getRenderedType();
}
