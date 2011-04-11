package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer;

import java.util.HashMap;
import java.util.Map;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node.PathNodeEditModeRendererFactory;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node.PathNodeRendererFactory;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment.EditModePathSegmentRenderer;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment.PathSegmentRenderer;
import org.jdesktop.wonderland.modules.path.common.style.UnsupportedStyleException;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This class represents a basic PathRendererFactory which is hard coded to support
 * the node and segment rendering classes within the module and does not support
 * detecting more using reflection / annotations.
 *
 * @author Carl Jokl
 */
public class NonDetectingPathRendererFactory implements PathRendererFactory {

    private Map<SegmentStyleType, PathSegmentRenderer> segmentRenderersByType;
    private Map<NodeStyleType, PathNodeRendererFactory> nodeRendererFactoriesByType;
    private PathSegmentRenderer editModeSegmentRenderer;
    private PathNodeEditModeRendererFactory editModeNodeRendererFactory;

    /**
     * Create a new instance of NonDetectingPathRendererFactory.
     */
    public NonDetectingPathRendererFactory() {
        segmentRenderersByType = new HashMap<SegmentStyleType, PathSegmentRenderer>();
        nodeRendererFactoriesByType = new HashMap<NodeStyleType, PathNodeRendererFactory>();
        editModeSegmentRenderer = new EditModePathSegmentRenderer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathSegmentRenderer getSegmentRenderer(SegmentStyleType segmentStyleType) throws IllegalArgumentException, UnsupportedStyleException {
        if (segmentStyleType == null) {
            throw new IllegalArgumentException("The specified segment style type for which to get a path segment renderer was null!");
        }
        PathSegmentRenderer renderer = segmentRenderersByType.get(segmentStyleType);
        if (renderer == null) {
            throw new UnsupportedStyleException(segmentStyleType, String.format("The specified segment style %s does not currently have a renderer associated with it!", segmentStyleType.getName()));
        }
        return renderer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathNodeRendererFactory getNodeRendererFactory(NodeStyleType nodeStyleType) throws IllegalArgumentException, UnsupportedStyleException {
        if (nodeStyleType == null) {
            throw new IllegalArgumentException("The specified node style type for which to get a path node renderer factory was null!");
        }
        PathNodeRendererFactory rendererFactory = nodeRendererFactoriesByType.get(nodeStyleType);
        if (rendererFactory == null) {
            throw new UnsupportedStyleException(nodeStyleType, String.format("The specified node style %s does not currently have a renderer factory associated with it!", nodeStyleType.getName()));
        }
        return rendererFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathSegmentRenderer getEditSegmentRenderer() {
        return editModeSegmentRenderer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathNodeEditModeRendererFactory getEditNodeRenderer() {
        return editModeNodeRendererFactory;
    }

}
