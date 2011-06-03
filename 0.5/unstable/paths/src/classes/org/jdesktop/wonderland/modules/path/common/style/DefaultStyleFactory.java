package org.jdesktop.wonderland.modules.path.common.style;

import java.util.HashMap;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import java.util.List;
import java.util.Map;
import org.jdesktop.wonderland.modules.path.common.style.node.HazardConeNodeStyleBuilder;
import org.jdesktop.wonderland.modules.path.common.style.node.InvisibleNodeStyleBuilder;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleBuilder;
import org.jdesktop.wonderland.modules.path.common.style.node.PoleNodeStyleBuilder;
import org.jdesktop.wonderland.modules.path.common.style.node.SquarePostNodeStyleBuilder;
import org.jdesktop.wonderland.modules.path.common.style.segment.InvisibleSegmentStyleBuilder;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleBuilder;
import org.jdesktop.wonderland.modules.path.common.style.segment.TapeSegmentStyleBuilder;

/**
 * This is a default implementation of a StyleFactory which supports the built in 
 * PathStyles and NodeStyles.
 *
 * @author Carl Jokl
 */
public class DefaultStyleFactory implements StyleFactory {

    private NodeStyleBuilder defaultNodeStyleBuilder;
    private SegmentStyleBuilder defaultSegmentStyleBuilder;
    private Map<NodeStyleType, NodeStyleBuilder> nodeStyleBuilders;
    private Map<SegmentStyleType, SegmentStyleBuilder> segmentStyleBuilders;

    /**
     * Add the specified NodeStyleBuilder to the map of NodeStyleTypes to NodeStyleBuilders.
     * 
     * @param builder The NodeStyleBuilder to be added to the Map.
     * @param builders The Map to which the NodeStyleBuilder will be added.
     */
    private static void add(NodeStyleBuilder builder, Map<NodeStyleType, NodeStyleBuilder> builders) {
        builders.put(builder.getBuiltStyleType(), builder);
    }

    /**
     * Add the specified SegmentStyleBuilder to the map of SegmentStyleTypes to SegmentStyleBuilders.
     *
     * @param builder The SegmentStyleBuilder to be added to the Map.
     * @param builders The Map to which the SegmentStyleBuilder will be added.
     */
    private static void add(SegmentStyleBuilder builder, Map<SegmentStyleType, SegmentStyleBuilder> builders) {
        builders.put(builder.getBuiltStyleType(), builder);
    }

    /**
     * Create and initialize the DefaultStyleFactory.
     */
    public DefaultStyleFactory() {
        defaultNodeStyleBuilder = new PoleNodeStyleBuilder();
        defaultSegmentStyleBuilder = new TapeSegmentStyleBuilder();
        nodeStyleBuilders = new HashMap<NodeStyleType, NodeStyleBuilder>();
        segmentStyleBuilders = new HashMap<SegmentStyleType, SegmentStyleBuilder>();
        add(defaultNodeStyleBuilder, nodeStyleBuilders);
        add(defaultSegmentStyleBuilder, segmentStyleBuilders);
        add(new InvisibleNodeStyleBuilder(), nodeStyleBuilders);
        add(new InvisibleSegmentStyleBuilder(), segmentStyleBuilders);
        add(new SquarePostNodeStyleBuilder(), nodeStyleBuilders);
        add(new HazardConeNodeStyleBuilder(), nodeStyleBuilders);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathStyle createDefaultPathStyle() {
        return new PathStyle(defaultNodeStyleBuilder.createNodeStyle(), defaultSegmentStyleBuilder.createSegmentStyle());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathStyle createPathStyle(NodeStyleType nodeStyleType, SegmentStyleType segmentStyleType) throws UnsupportedStyleException {
        NodeStyle nodeStyle = createNodeStyle(nodeStyleType);
        SegmentStyle segmentStyle = createSegmentStyle(segmentStyleType);
        return new PathStyle(nodeStyle, segmentStyle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathStyle createPathStyle(NodeStyle nodeStyle, SegmentStyle segmentStyle) throws IllegalArgumentException {
        return new PathStyle(nodeStyle, segmentStyle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathStyle createPathStyle(NodeStyle[] nodeStyles, SegmentStyle[] segmentStyles) throws IllegalArgumentException {
        return new PathStyle(nodeStyles, segmentStyles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathStyle createPathStyle(List<NodeStyle> nodeStyles, List<SegmentStyle> segmentStyles) throws IllegalArgumentException {
        return new PathStyle(nodeStyles, segmentStyles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyle createNodeStyle(NodeStyleType nodeStyleType) throws UnsupportedStyleException {
        if (nodeStyleType != null) {
            NodeStyleBuilder builder = nodeStyleBuilders.get(nodeStyleType);
            if (builder != null) {
                return builder.createNodeStyle();
            }
            else {
                throw new UnsupportedStyleException(nodeStyleType, String.format("Could not create node style for the node style type: %s as it is not supported!", nodeStyleType.getName()));
            }
        }
        else {
            throw new UnsupportedStyleException(null, "The specified node style type was null!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyle createSegmentStyle(SegmentStyleType segmentStyleType) throws UnsupportedStyleException {
        if (segmentStyleType != null) {
            SegmentStyleBuilder builder = segmentStyleBuilders.get(segmentStyleType);
            if (builder != null) {
                return builder.createSegmentStyle();
            }
            else {
                throw new UnsupportedStyleException(segmentStyleType, String.format("Could not create segment style for the segment style type: %s as it is not supported!", segmentStyleType.getName()));
            }
        }
        else {
            throw new UnsupportedStyleException(null, "The specified segment style type was null!");
        }
    }


}
