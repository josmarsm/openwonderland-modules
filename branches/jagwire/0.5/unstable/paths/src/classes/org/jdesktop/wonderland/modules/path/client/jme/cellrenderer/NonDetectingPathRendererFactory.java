package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer;

import java.util.HashMap;
import java.util.Map;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node.EditModePathNodeRenderer.EditModePathNodeRendererFactory;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node.HazardConeNodeRenderer.HazardConeNodeRendererFactory;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node.InvisibleNodeRenderer.InvisibleNodeRendererFactory;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node.PathNodeRendererFactory;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node.PoleNodeRenderer.PoleNodeRendererFactory;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node.SquarePostNodeRenderer.SquarePostNodeRendererFactory;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment.EditModePathSegmentRenderer.EditModePathSegmentRendererFactory;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment.InvisibleSegmentRenderer.InvisibleSegmentRendererFactory;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment.PathSegmentRendererFactory;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment.TapeSegmentRenderer.TapeSegmentRendererFactory;
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

    private Map<SegmentStyleType, PathSegmentRendererFactory> segmentRendererFactoriesByType;
    private Map<NodeStyleType, PathNodeRendererFactory> nodeRendererFactoriesByType;
    private PathSegmentRendererFactory editModeSegmentRendererFactory;
    private PathNodeRendererFactory editModeNodeRendererFactory;

    /**
     * Create a new instance of NonDetectingPathRendererFactory.
     */
    public NonDetectingPathRendererFactory() {
        segmentRendererFactoriesByType = new HashMap<SegmentStyleType, PathSegmentRendererFactory>();
        nodeRendererFactoriesByType = new HashMap<NodeStyleType, PathNodeRendererFactory>();
        editModeSegmentRendererFactory = new EditModePathSegmentRendererFactory();
        editModeNodeRendererFactory = new EditModePathNodeRendererFactory();
        add(new HazardConeNodeRendererFactory());
        add(new InvisibleNodeRendererFactory());
        add(new PoleNodeRendererFactory());
        add(new SquarePostNodeRendererFactory());
        add(new InvisibleSegmentRendererFactory());
        add(new TapeSegmentRendererFactory());
    }

    private void add(PathNodeRendererFactory factory) {
        nodeRendererFactoriesByType.put(factory.getRenderedNodeStyleType(), factory);
    }

    private void add(PathSegmentRendererFactory factory) {
        segmentRendererFactoriesByType.put(factory.getRenderedSegmentStyleType(), factory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathSegmentRendererFactory getSegmentRendererFactory(SegmentStyleType segmentStyleType) throws IllegalArgumentException, UnsupportedStyleException {
        if (segmentStyleType == null) {
            throw new IllegalArgumentException("The specified segment style type for which to get a path segment renderer was null!");
        }
        PathSegmentRendererFactory rendererFactory = segmentRendererFactoriesByType.get(segmentStyleType);
        if (rendererFactory == null) {
            throw new UnsupportedStyleException(segmentStyleType, String.format("The specified segment style %s does not currently have a renderer associated with it!", segmentStyleType.getName()));
        }
        return rendererFactory;
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
    public PathSegmentRendererFactory getEditSegmentRendererFactory() {
        return editModeSegmentRendererFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathNodeRendererFactory getEditNodeRendererFactory() {
        return editModeNodeRendererFactory;
    }
}
