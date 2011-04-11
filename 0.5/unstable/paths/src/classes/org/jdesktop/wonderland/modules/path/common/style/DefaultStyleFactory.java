package org.jdesktop.wonderland.modules.path.common.style;

import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import org.jdesktop.wonderland.modules.path.common.style.segment.InvisibleSegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.InvisibleNodeStyle;
import org.jdesktop.wonderland.modules.path.common.style.segment.CoreSegmentStyleType;
import org.jdesktop.wonderland.modules.path.common.style.node.CoreNodeStyleType;
import java.util.List;

/**
 * This is a default implementation of a StyleFactory which supports the built in 
 * PathStyles and NodeStyles.
 *
 * @author Carl Jokl
 */
public class DefaultStyleFactory implements StyleFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public PathStyle createDefaultPathStyle() {
        return new StandardPathStyle(new InvisibleNodeStyle(), new InvisibleSegmentStyle());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathStyle createPathStyle(NodeStyleType nodeStyleType, SegmentStyleType segmentStyleType) throws UnsupportedStyleException {
        NodeStyle nodeStyle = createNodeStyle(nodeStyleType);
        SegmentStyle segmentStyle = createSegmentStyle(segmentStyleType);
        return new StandardPathStyle(nodeStyle, segmentStyle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathStyle createPathStyle(NodeStyle nodeStyle, SegmentStyle segmentStyle) throws IllegalArgumentException {
        return new StandardPathStyle(nodeStyle, segmentStyle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathStyle createPathStyle(NodeStyle[] nodeStyles, SegmentStyle[] segmentStyles) throws IllegalArgumentException {
        return new StandardPathStyle(nodeStyles, segmentStyles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathStyle createPathStyle(List<NodeStyle> nodeStyles, List<SegmentStyle> segmentStyles) throws IllegalArgumentException {
        return new StandardPathStyle(nodeStyles, segmentStyles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyle createNodeStyle(NodeStyleType nodeStyleType) throws UnsupportedStyleException {
        if (nodeStyleType == CoreNodeStyleType.INVISIBLE) {
            return new InvisibleNodeStyle();
        }
        else {
            throw new UnsupportedStyleException(nodeStyleType, "No supported NodeStyle is available for this NodeStyleType!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyle createSegmentStyle(SegmentStyleType segmentStyleType) throws UnsupportedStyleException {
        if (segmentStyleType == CoreSegmentStyleType.INVISIBLE) {
            return new InvisibleSegmentStyle();
        }
        else {
            throw new UnsupportedStyleException(segmentStyleType, "No supported NodeStyle is available for this NodeStyleType!");
        }
    }
}
