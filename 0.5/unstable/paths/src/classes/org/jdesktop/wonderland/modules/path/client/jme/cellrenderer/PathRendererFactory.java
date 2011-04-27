package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer;

import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node.PathNodeRendererFactory;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment.PathSegmentRenderer;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment.PathSegmentRendererFactory;
import org.jdesktop.wonderland.modules.path.common.style.UnsupportedStyleException;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This interface represents a factory for creating PathCellRenderers and the associated rendering classes and factories for the
 * nodes and segments of the path.
 *
 * @author Carl Jokl
 */
public interface PathRendererFactory {

    /**
     * Get the PathSegmentRendererFactory for the specific SegmentStyleType.
     * 
     * @param segmentStyleType The SegmentStyleType which defines the type of path segment style which
     *                         should be used for rendering.
     * @return A PathSegmentRendererFactory for the specified SegmentStyleType.
     * @throws IllegalArgumentException If the specified SegmentStyleType is null.
     * @throws UnsupportedStyleException If no PathSegmentRenderer can be found to render segments of the specified SegmentStyleType.
     */
    public PathSegmentRendererFactory getSegmentRendererFactory(SegmentStyleType segmentStyleType) throws IllegalArgumentException, UnsupportedStyleException;

    /**
     * Get the PathNodeRendererFactory which can be used to create PathNodeRenderers for the specified NodeStyleType.
     *
     * @param nodeStyleType The NodeStyleType for which to find a PathNodeRendererFactory which can create PathNodeRenderers for that NodeStyleType.
     * @return A PathNodeRendererFactory which can be used to create a PathNodeRenderer for the specific NodeStyleType.
     * @throws IllegalArgumentException If the NodeStyleType specified was null.
     * @throws UnsupportedStyleException If no PathNodeRendererFactory could be found which supports the specified NodeStyleType.
     */
    public PathNodeRendererFactory getNodeRendererFactory(NodeStyleType nodeStyleType) throws IllegalArgumentException, UnsupportedStyleException;

    /**
     * Get the PathSegmentRendererFactory used to create PathSegmentRenderers to render path segments when displaying in edit mode.
     *
     * @return The PathSegmentRendererFactory used to create PathSegmentRenderers for rendering path segments when in edit mode.
     */
    public PathSegmentRendererFactory getEditSegmentRendererFactory();

    /**
     * Get the PathNodeEditModeRendererFactory used to create PathNodeRenderers to render PathNodes when the path is being displayed in edit mode.
     *
     * @return A PathNodeEditModeRendererFactory used to create PathNodeRenderers to render PathNode when in edit mode.
     */
    public PathNodeRendererFactory getEditNodeRendererFactory();
}
