package org.jdesktop.wonderland.modules.path.common.style;

import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an abstract base class for a PathStyle and
 * contains common functionality which should be suitable for
 * most PathStyle implementations.
 *
 * @author Carl Jokl
 */
public class StandardPathStyle implements PathStyle, Serializable {

    /**
     * An array of the NodeStyles within this PathStyle.
     * If a single NodeStyle is used for all nodes then
     * the array will only contain a single element.
     * More complex / future implementations may be able to support
     * per node styling using this array.
     */
    protected List<NodeStyle> nodeStyles;
    protected List<SegmentStyle> segmentStyles;

    /**
     * Initialize this StandardPathStyle to be blank i.e. no style information for PathNodes or
     * the segments between them.
     */
    public StandardPathStyle() {
        nodeStyles = new ArrayList<NodeStyle>();
        segmentStyles = new ArrayList<SegmentStyle>();
    }

    /**
     * Initialize this StandardPathStyle to have the specified pathType which cannot be null.
     * 
     * @param nodeStyle The NodeStyle of the PathNodes styled by this PathStyle, this cannot be null.
     * @throws IllegalArgumentException If the specified NodeStyle or SegmentStyle was null.
     */
    public StandardPathStyle(final NodeStyle nodeStyle, final SegmentStyle segmentStyle) throws IllegalArgumentException {
        this();
        if (nodeStyle == null) {
            throw new IllegalArgumentException("The NodeStyle of this PathStyle cannot be null!");
        }
        if (segmentStyle == null) {
            throw new IllegalArgumentException("The SegmentStyle of this PathStyle cannot be null!");
        }
        nodeStyles.add(nodeStyle);
        segmentStyles.add(segmentStyle);
    }

    /**
     * Initialize this StandardPathStyle to have the the specified NodeStyles and the SegmentStyles.
     *
     * @param nodeStyles An array containing the NodeStyles of this PathStyle.
     * @param segmentStyles An array containing the SegmentStyles of the PathStyle.
     * @throws IllegalArgumentException If the specified NodeStyles or SegmentStyles are null or empty.
     */
    public StandardPathStyle(final NodeStyle[] nodeStyles, final SegmentStyle[] segmentStyles) throws IllegalArgumentException {
        this();
        if (nodeStyles == null || nodeStyles.length == 0) {
            throw new IllegalArgumentException("The node styles of this PathStyle cannot be null!");
        }
        if (segmentStyles == null || nodeStyles.length == 0) {
            throw new IllegalArgumentException("The segment styless of this PathStyle cannot be null!");
        }
        for (NodeStyle nodeStyle : nodeStyles) {
            this.nodeStyles.add(nodeStyle);
        }
        for (SegmentStyle segmentStyle : segmentStyles) {
            this.segmentStyles.add(segmentStyle);
        }
    }

    /**
     * Initialize this StandardPathStyle to have the the specified NodeStyles and the SegmentStyles.
     *
     * @param nodeStyles A list containing the NodeStyles of this PathStyle.
     * @param segmentStyles A list containing the SegmentStyles of the PathStyle.
     * @throws IllegalArgumentException If the specified NodeStyles or SegmentStyles are null or empty.
     */
    public StandardPathStyle(final List<NodeStyle> nodeStyles, final List<SegmentStyle> segmentStyles) throws IllegalArgumentException {
        this();
        if (nodeStyles == null || nodeStyles.isEmpty()) {
            throw new IllegalArgumentException("The node styles of this PathStyle cannot be null!");
        }
        if (segmentStyles == null || nodeStyles.isEmpty()) {
            throw new IllegalArgumentException("The segment styless of this PathStyle cannot be null!");
        }
        for (NodeStyle nodeStyle : nodeStyles) {
            this.nodeStyles.add(nodeStyle);
        }
        for (SegmentStyle segmentStyle : segmentStyles) {
            this.segmentStyles.add(segmentStyle);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSegmentStyleSet() {
        return !segmentStyles.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNodeStyleSet() {
        return !nodeStyles.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyle getStartSegmentStyle() {
        if (segmentStyles.isEmpty()) {
            //No SegmentStyle available.
            return null;
        }
        else {
            //If any SegmentStyle exists always use the first SegmentStyle.
            return segmentStyles.get(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyle getStartNodeStyle() {
        if (nodeStyles.isEmpty()) {
            //No NodeStyle available.
            return null;
        }
        else {
            //If any NodeStyle exists always use the first NodeStyle.
            return nodeStyles.get(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyle getEndSegmentStyle() {
        if (segmentStyles.isEmpty()) {
            //No SegmentStyle available.
            return null;
        }
        else {
            //If any SegmentStyle exists always use the last SegmentStyle.
            return segmentStyles.get(segmentStyles.size() - 1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyle getEndNodeStyle() {
        if (nodeStyles.isEmpty()) {
            //No NodeStyle available.
            return null;
        }
        else {
            //Several node styles therefore use the last NodeStyle.
            return nodeStyles.get(nodeStyles.size() - 1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSingleSegmentStyle() {
        return segmentStyles.size() == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSingleNodeStyle() {
        return nodeStyles.size() == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int noOfSegmentStyles() {
        return segmentStyles.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int noOfNodeStyles() {
        return nodeStyles.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyle getNodeStyle(int index, boolean relativeToNodes) throws IndexOutOfBoundsException {
        if (relativeToNodes) {
            if (index >= 0) {
                if (nodeStyles.isEmpty()) {
                    return null;
                }
                else {
                    int currentIndex = 0;
                    for (NodeStyle style : nodeStyles) {
                        currentIndex += style.span();
                        if (currentIndex < index) {
                            return style;
                        }
                    }
                    return nodeStyles.get(nodeStyles.size() - 1);
                }

            }
            else {
                throw new IndexOutOfBoundsException(String.format("The node index %d is invalid as node indices cannot be negative!", index));
            }
        }
        else {
            if (index >= 0 && index < nodeStyles.size()) {
                return nodeStyles.get(index);
            }
            else {
                throw new IndexOutOfBoundsException(String.format("The node style index: %d is outside the range of the %d node styles!", index, nodeStyles.size()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyle getSegmentStyle(int index, boolean relativeToSegments) throws IndexOutOfBoundsException {
        if (relativeToSegments) {
            if (index >= 0) {
                if (segmentStyles.isEmpty()) {
                    return null;
                }
                else {
                    int currentIndex = 0;
                    for (SegmentStyle style : segmentStyles) {
                        currentIndex += style.span();
                        if (currentIndex < index) {
                            return style;
                        }
                    }
                    return segmentStyles.get(segmentStyles.size() - 1);
                }

            }
            else {
                throw new IndexOutOfBoundsException(String.format("The segment index %d is invalid as node indices cannot be negative!", index));
            }
        }
        else {
            if (index >= 0 && index < segmentStyles.size()) {
                return segmentStyles.get(index);
            }
            else {
                throw new IndexOutOfBoundsException(String.format("The segment style index: %d is outside the range of the %d segment styles!", index, nodeStyles.size()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean append(SegmentStyle style) {
        return style != null && segmentStyles.add(style);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean append(NodeStyle style) {
        return style != null && nodeStyles.add(style);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean insertFirst(SegmentStyle style) {
        if (style != null) {
            segmentStyles.add(0, style);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean insertFirst(NodeStyle style) {
        if (style != null) {
            nodeStyles.add(0, style);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean insertAt(int index, SegmentStyle style) throws IndexOutOfBoundsException {
        if (style != null) {
            if (index >= 0 && index < segmentStyles.size()) {
                segmentStyles.add(index, style);
                return true;
            }
            else {
                throw new IndexOutOfBoundsException(String.format("The index: %d at which the segment style was to be added is outside the valid range of segment style indices!", index));
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean insertAt(int index, NodeStyle style) throws IndexOutOfBoundsException {
        if (style != null) {
            if (index >= 0 && index < nodeStyles.size()) {
                nodeStyles.add(index, style);
                return true;
            }
            else {
                throw new IndexOutOfBoundsException(String.format("The index: %d at which the node style was to be added is outside the valid range of node style indices!", index));
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(SegmentStyle style) {
        return style != null && segmentStyles.remove(style);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(NodeStyle style) {
        return style != null && nodeStyles.remove(style);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyle removeSegmentStyleAt(int index) throws IndexOutOfBoundsException {
        if (index >= 0 && index < segmentStyles.size()) {
            return segmentStyles.remove(index);
        }
        else {
            throw new IndexOutOfBoundsException(String.format("The index: %d from which the segment style was to be removed is outside the valid range of segment style indices!", index));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyle removeNodeStyleAt(int index) throws IndexOutOfBoundsException {
        if (index >= 0 && index < nodeStyles.size()) {
            return nodeStyles.remove(index);
        }
        else {
            throw new IndexOutOfBoundsException(String.format("The index: %d from which the node style was to be removed is outside the valid range of node style indices!", index));
        }
    }
}
