package org.jdesktop.wonderland.modules.path.common.style;

import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import java.util.List;

/**
 * This is a factory to be used to create instances of PathStyles or NodeStyles.
 *
 * @author Carl Jokl
 */
public interface StyleFactory {

    /**
     * Create the default PathStyle for this factory.
     * 
     * @return An instance of the default PathStyle for this factory.
     */
    public PathStyle createDefaultPathStyle();

    /**
     * Create a PathStyle with the default settings for the specified
     * NodeStyleType and SegmentStyleType.
     *
     * @param nodeStyleType The NodeStypeType of the PathStyle to be created.
     * @param segmentStyleType The SegmentStyleType of the PathStyle to be created.
     * @return The PathStyle instance with default values for the specified
     *         path type.
     * @throws UnsupportedStyleException If the corresponding PathStyle for the SegmentStyleType
     *         was not supported by this factory.
     */
    public PathStyle createPathStyle(NodeStyleType nodeStyleType, SegmentStyleType segmentStyleType) throws UnsupportedStyleException;

    /**
     * Create a PathStyle with the default settings for the specified
     * NodeStyleType and SegmentStyleType.
     *
     * @param nodeStyle The NodeStyle of the PathStyle to be created.
     * @param segmentStyle The SegmentStyle of the PathStyle to be created.
     * @return The PathStyle instance with the specified NodeStyle and SegmentStyle for the whole length of the path.
     * @throws IllegalArgumentException If the supplied NodeStyle or SegmentStyle was null.
     */
    public PathStyle createPathStyle(NodeStyle nodeStyle, SegmentStyle segmentStyle) throws IllegalArgumentException;

    /**
     * Create a PathStyle with the default settings for the specified
     * NodeStyleType and SegmentStyleType.
     *
     * @param nodeStyle An array containing the NodeStyles of the PathStyle to be created.
     * @param segmentStyle An array containing the SegmentStyles of the PathStyle to be created.
     * @return The PathStyle instance with the specified node
     * @throws IllegalArgumentException If the supplied NodeStyles or SegmentStyles were null or an empty array.
     */
    public PathStyle createPathStyle(NodeStyle[] nodeStyles, SegmentStyle[] segmentStyles) throws IllegalArgumentException;

    /**
     * Create a PathStyle with the default settings for the specified
     * NodeStyleType and SegmentStyleType.
     *
     * @param nodeStyle The A list containing the NodeStyles of the PathStyle to be created.
     * @param segmentStyle A list containing the SegmentStyles of the PathStyle to be created.
     * @return The PathStyle instance with default values for the specified
     *         path type.
     * @throws IllegalArgumentException If the supplied NodeStyle or SegmentStyle were null or an empty list.
     */
    public PathStyle createPathStyle(List<NodeStyle> nodeStyles, List<SegmentStyle> segmentStyles) throws IllegalArgumentException;

    /**
     * Create a new instance of the NodeStyle of the specified NodeStyleType.
     *
     * @param nodeStyleType The NodeStyleType for which to create an instance of the NodeStyle.
     * @return An instance of the NodeStyle with the specified NodeStyleType.
     * @throws UnsupportedStyleException If the corresponding NodeStyle for the NodeStyleType
     *         was not supported by this factory.
     */
    public NodeStyle createNodeStyle(NodeStyleType nodeStyleType) throws UnsupportedStyleException;

    /**
     * Create a new instance of the SegmentStyle of the specified SegmentStyleType.
     *
     * @param segmentStyleType The SegmentStyleType for which to create an instance of the SegmentStyle.
     * @return An instance of the SegmentStyle with the specified SegmentStyleType.
     * @throws UnsupportedStyleException If the corresponding SegmentStyle for the SegmentStyleType
     *         was not supported by this factory.
     */
    public SegmentStyle createSegmentStyle(SegmentStyleType segmentStyleType) throws UnsupportedStyleException;
}
