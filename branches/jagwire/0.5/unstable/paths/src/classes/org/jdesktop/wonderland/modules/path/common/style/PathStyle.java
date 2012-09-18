package org.jdesktop.wonderland.modules.path.common.style;

import java.util.Arrays;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is an abstract base class for a PathStyle and
 * contains common functionality which should be suitable for
 * most PathStyle implementations.
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="style")
public class PathStyle implements Cloneable, Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * An array of the NodeStyles within this PathStyle.
     * If a single NodeStyle is used for all nodes then
     * the array will only contain a single element.
     * More complex styling may have multiple NodeStyles
     * or even per node styling.
     */
    private final List<NodeStyle> nodeStyles;

    /**
     * An array of the SegmentStyles within this PathStyle.
     * If a single SegmentStyle is used for all segments then
     * the array will only contain a single element.
     * More complex styling may have multiple SegmentStyles
     * or even per segment styling.
     */
    private final List<SegmentStyle> segmentStyles;

    /**
     * Initialize this PathStyle to be blank i.e. no style information for PathNodes or
     * the segments between them.
     */
    public PathStyle() {
        nodeStyles = new ArrayList<NodeStyle>();
        segmentStyles = new ArrayList<SegmentStyle>();
    }

    /**
     * This is a constructor for use by extending classes which are used to wrap
     * another PathStyle instance. This can for example be used in the
     * ServerMessageSendingPathStyle so that a PathStyle's internal lists can be
     * reused in the ServerMessageSendingPathStyle to provide some level of
     * wrapper / decorator / proxy functionality.
     *
     * @param PathStyle The PathStyle which is to be wrapped by this PathStyle.
     * @throws IllegalArgumentException If the specified wrapped style is null.
     */
    protected PathStyle(PathStyle wrappedStyle) throws IllegalArgumentException {
        if (wrappedStyle == null) {
            throw new IllegalArgumentException("The PathStyle which is to be wrapped by this PathStyle cannot be null!");
        }
        this.nodeStyles = wrappedStyle.getInternalNodeStyles();
        this.segmentStyles = wrappedStyle.getInternalSegmentStyles();
    }

    /**
     * This private method allows direct access to the PathStyle's internal NodeStyle list.
     * This method is intended to be used in classes like the ServerMessageSendingClientNodePath
     * which can reference the NodeStyle list from another PathStyle and then the method calls of
     * that class will manipulates the values of both PathStyles.
     *
     * @return The internal NodeStyle list for use by wrapper classes / proxies.
     */
    private List<NodeStyle> getInternalNodeStyles() {
        return nodeStyles;
    }

    /**
     * This private method allows direct access to the PathStyle's internal SegmentStyle list.
     * This method is intended to be used in classes like the ServerMessageSendingClientNodePath
     * which can reference the SegmentStyle list from another PathStyle and then the method calls of
     * that class will manipulate the values of both PathStyles.
     *
     * @return The internal SegmentStyle list for use by wrapper classes / proxies.
     */
    private List<SegmentStyle> getInternalSegmentStyles() {
        return segmentStyles;
    }

    /**
     * Initialize this PathStyle to have the specified pathType which cannot be null.
     * 
     * @param nodeStyle The NodeStyle of the PathNodes styled by this PathStyle, this cannot be null.
     * @throws IllegalArgumentException If the specified NodeStyle or SegmentStyle was null.
     */
    public PathStyle(final NodeStyle nodeStyle, final SegmentStyle segmentStyle) throws IllegalArgumentException {
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
     * Initialize this PathStyle to have the the specified NodeStyles and the SegmentStyles.
     *
     * @param nodeStyles An array containing the NodeStyles of this PathStyle.
     * @param segmentStyles An array containing the SegmentStyles of the PathStyle.
     * @throws IllegalArgumentException If the specified NodeStyles or SegmentStyles are null or empty.
     */
    public PathStyle(final NodeStyle[] nodeStyles, final SegmentStyle[] segmentStyles) throws IllegalArgumentException {
        if (nodeStyles == null || nodeStyles.length == 0) {
            throw new IllegalArgumentException("The node styles of this PathStyle cannot be null!");
        }
        if (segmentStyles == null || nodeStyles.length == 0) {
            throw new IllegalArgumentException("The segment styless of this PathStyle cannot be null!");
        }
        this.nodeStyles = new ArrayList<NodeStyle>(Arrays.asList(nodeStyles));
        this.segmentStyles = new ArrayList<SegmentStyle>(Arrays.asList(segmentStyles));
    }

    /**
     * Initialize this PathStyle to have the the specified NodeStyles and the SegmentStyles.
     *
     * @param nodeStyles A list containing the NodeStyles of this PathStyle.
     * @param segmentStyles A list containing the SegmentStyles of the PathStyle.
     * @throws IllegalArgumentException If the specified NodeStyles or SegmentStyles are null or empty.
     */
    public PathStyle(final List<NodeStyle> nodeStyles, final List<SegmentStyle> segmentStyles) throws IllegalArgumentException {
        if (nodeStyles == null || nodeStyles.isEmpty()) {
            throw new IllegalArgumentException("The node styles of this PathStyle cannot be null!");
        }
        if (segmentStyles == null || nodeStyles.isEmpty()) {
            throw new IllegalArgumentException("The segment styless of this PathStyle cannot be null!");
        }
        this.nodeStyles = new ArrayList<NodeStyle>(nodeStyles);
        this.segmentStyles = new ArrayList<SegmentStyle>(segmentStyles);
    }

    /**
     * Whether any SegmentStyle is currently set.
     *
     * @return True if any SegmentStyle is set.
     */
    public boolean isSegmentStyleSet() {
        synchronized (segmentStyles) {
            return !segmentStyles.isEmpty();
        }
    }

    /**
     * Whether any NodeStyle is currently set.
     *
     * @return True if any NodeStyle is set.
     */
    public boolean isNodeStyleSet() {
        synchronized (nodeStyles) {
            return !nodeStyles.isEmpty();
        }
    }

    /**
     * Get the start PathNode NodeStyle for this PathStyle.
     * This may be the same as the other NodeStyles.
     *
     * @return The start SegmentStyle for this PathStyle.
     */
    public SegmentStyle getStartSegmentStyle() {
        synchronized(segmentStyles) {
            if (segmentStyles.isEmpty()) {
                //No SegmentStyle available.
                return null;
            }
            else {
                //If any SegmentStyle exists always use the first SegmentStyle.
                return segmentStyles.get(0);
            }
        }
    }

    /**
     * Get the start PathNode NodeStyle for this PathStyle.
     * This may be the same as the other NodeStyles.
     *
     * @return The start NodeStyle for this PathStyle.
     */
    public NodeStyle getStartNodeStyle() {
        synchronized(nodeStyles) {
            if (nodeStyles.isEmpty()) {
                //No NodeStyle available.
                return null;
            }
            else {
                //If any NodeStyle exists always use the first NodeStyle.
                return nodeStyles.get(0);
            }
        }
    }

    /**
     * Get the end SegmentStyle for this PathStyle.
     * This may be the same as the start and end SegmentStyles if
     * no other SegmentStyle is being used.
     *
     * @return The end SegmentStyle for the PathStyle.
     */
    public SegmentStyle getEndSegmentStyle() {
        synchronized(segmentStyles) {
            if (segmentStyles.isEmpty()) {
                //No SegmentStyle available.
                return null;
            }
            else {
                //If any SegmentStyle exists always use the last SegmentStyle.
                return segmentStyles.get(segmentStyles.size() - 1);
            }
        }
    }


    /**
     * Get the end PathNode NodeStyle for this PathStyle.
     * This may be the same as the start and end NodeStyles if
     * no other NodeStyle is being used.
     *
     * @return The end NodeStyle for the PathStyle.
     */
    public NodeStyle getEndNodeStyle() {
        synchronized(nodeStyles) {
            if (nodeStyles.isEmpty()) {
                //No NodeStyle available.
                return null;
            }
            else {
                //Several node styles therefore use the last NodeStyle.
                return nodeStyles.get(nodeStyles.size() - 1);
            }
        }
    }

    /**
     * Whether the PathStyle uses a single SegmentStyle.
     *
     * @return True if the PathStyle has just one SegmentStyle.
     */
    public boolean isSingleSegmentStyle() {
        synchronized (segmentStyles) {
            return segmentStyles.size() == 1;
        }
    }

    /**
     * Whether the PathStyle uses a single NodeStyle.
     *
     * @return True if the PathStyle has just one NodeStyle.
     */
    public boolean isSingleNodeStyle() {
        synchronized (nodeStyles) {
            return nodeStyles.size() == 1;
        }
    }

    /**
     * Get the number of SegmentStyles which are used within this PathStyle.
     *
     * @return The number of SegmentStyles which are used within this PathStyle.
     */
    public int noOfSegmentStyles() {
        synchronized(segmentStyles) {
            return segmentStyles.size();
        }
    }

    /**
     * Get the number of NodeStyles which are used within this PathStyle.
     *
     * @return The number of NodeStyles which are used within this PathStyle.
     */
    public int noOfNodeStyles() {
        synchronized(nodeStyles) {
            return nodeStyles.size();
        }
    }

    /**
     * Get the NodeStyle at the specified index within the PathStyle.
     *
     * @param index The index of the NodeStyle to be retrieved. This is either the index of the PathNode to which
     *              the style applies or it is the index of the NodeStyle (where the number of node styles may be
     *              less than the number of nodes if a NodeStyle spans more than one PathNode). The next parameter
     *              specifies which type of index was intended.
     * @param relativeToNodes Whether the index supplied is relative the number of PathNodes or the number of NodeStyles
     *                        (which may be less than the number of PathNodes). True if the index is the index of the
     *                        PathNode to which the style applies or false if the index is relative to the number of
     *                        NodeStyles.
     * @return The NodeStyle at the specified index.
     * @throws IndexOutOfBoundsException If the specified index is outside the valid
     *                                   range of NodeStyle indices.
     */
    public NodeStyle getNodeStyle(int index, boolean relativeToNodes) throws IndexOutOfBoundsException {
        synchronized(nodeStyles) {
            if (relativeToNodes) {
                if (index >= 0) {
                    if (nodeStyles.isEmpty()) {
                        return null;
                    }
                    else {
                        int currentIndex = 0;
                        for (NodeStyle style : nodeStyles) {
                            currentIndex += style.getSpan();
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
    }

    /**
     * Get the NodeStyle at the specified index within the PathStyle.
     *
     * @param index The index of the NodeStyle to be retrieved. This is either the index of the PathNode to which
     *              the style applies or it is the index of the NodeStyle (where the number of node styles may be
     *              less than the number of nodes if a NodeStyle spans more than one PathNode). The next parameter
     *              specifies which type of index was intended.
     * @param relativeToNodes Whether the index supplied is relative the number of PathNodes or the number of NodeStyles
     *                        (which may be less than the number of PathNodes). True if the index is the index of the
     *                        PathNode to which the style applies or false if the index is relative to the number of
     *                        NodeStyles.
     * @return The NodeStyle at the specified index.
     * @throws IndexOutOfBoundsException If the specified index is outside the valid
     *                                   range of NodeStyle indices.
     */
    public SegmentStyle getSegmentStyle(int index, boolean relativeToSegments) throws IndexOutOfBoundsException {
        synchronized(segmentStyles) {
            if (relativeToSegments) {
                if (index >= 0) {
                    if (segmentStyles.isEmpty()) {
                        return null;
                    }
                    else {
                        int currentIndex = 0;
                        for (SegmentStyle style : segmentStyles) {
                            currentIndex += style.getSpan();
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
    }

    /**
     * Append the specified SegmentStyle to the end of the SegmentStyles.
     *
     * @param style The SegmentStyle to be appended to the end of the SegmentStyles.
     * @return True if the SegmentStyle was able to be appended successfully.
     */
    public boolean append(SegmentStyle style) {
        synchronized(segmentStyles) {
            return style != null && segmentStyles.add(style);
        }
    }

    /**
     * Append the specified NodeStyle to the end of the NodeStyles.
     *
     * @param style The NodeStyle to be appended at the end of the NodeStyles.
     * @return True if the NodeStyle was able to be appended successfully.
     */
    public boolean append(NodeStyle style) {
        synchronized(nodeStyles) {
            return style != null && nodeStyles.add(style);
        }
    }

    /**
     * Insert the specified SegmentStyle to the start of the SegmentStyles.
     *
     * @param style The SegmentStyle to be inserted at the start of the SegmentStyles.
     * @return True if the SegmentStyle was able to be inserted at the start of the SegmentStyles successfully.
     */
    public boolean insertFirst(SegmentStyle style) {
        synchronized(nodeStyles) {
            if (style != null) {
                segmentStyles.add(0, style);
                return true;
            }
            return false;
        }
    }

    /**
     * Insert the specified NodeStyle at the start of the NodeStyles.
     *
     * @param style The NodeStyle to be inserted at the start of the NodeStyles.
     * @return True if the NodeStyle was able to be inserted at the start of the NodeStyles successfully.
     */
    public boolean insertFirst(NodeStyle style) {
        synchronized(nodeStyles) {
            if (style != null) {
                nodeStyles.add(0, style);
                return true;
            }
            return false;
        }
    }

    /**
     * Insert the specified SegmentStyle at the specified Index.
     *
     * @param index The index at which to insert the SegmentStyle which is relative to the number of SegmentStyles.
     * @param style The specified SegmentStyle to be inserted into the SegmentStyles.
     * @return True if the specified SegmentStyle was able to be inserted successfully.
     * @throws IndexOutOfBoundsException If the specified index at which to insert was outside the valid range.
     */
    public boolean insertAt(int index, SegmentStyle style) throws IndexOutOfBoundsException {
        synchronized(segmentStyles) {
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
    }

    /**
     * Insert the specified NodeStyle at the specified index.
     *
     * @param index The index at which to insert the NodeStyle which is relative to the number of NodeStyles.
     * @param style The specified NodeStyle to be inserted into the NodeStyles.
     * @return True if the specified NodeStyle was able to be inserted successfully.
     * @throws IndexOutOfBoundsException If the specified index at which to insert was outside the valid range.
     */
    public boolean insertAt(int index, NodeStyle style) throws IndexOutOfBoundsException {
        synchronized(nodeStyles) {
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
    }

    /**
     * Remove the specified SegmentStyle from the NodeStyles within this PathStyle.
     *
     * @param style The SegmentStyle to be removed from the SegmentStyles within this PathStyle.
     * @return True if the specified SegmentStyle was present in this PathStyle and was able to be removed successfully.
     */
    public boolean remove(SegmentStyle style) {
        synchronized(segmentStyles) {
            return style != null && segmentStyles.remove(style);
        }
    }

    /**
     * Remove the specified NodeStyle from the NodeStyles within this PathStyle.
     *
     * @param style The NodeStyle to be removed from the NodeStyles within the PathStyle.
     * @return True if the specified NodeStyle was present in this PathStyle and was able to be removed successfully.
     */
    public boolean remove(NodeStyle style) {
        synchronized(nodeStyles) {
            return style != null && nodeStyles.remove(style);
        }
    }

    /**
     * Remove the specified SegmentStyle from the SegmentStyles at the specified index.
     *
     * @param index The index of the SegmentStyle to be removed, which is relative to the number of SegmentStyles.
     * @return The SegmentStyle removed from the specified index if successful or null if not successful.
     * @throws IndexOutOfBoundsException If the specified index at which the SegmentStyle was to be removed was outside
     *                                   the valid range of SegmentStyle indices.
     */
    public SegmentStyle removeSegmentStyleAt(int index) throws IndexOutOfBoundsException {
        synchronized(segmentStyles) {
            if (index >= 0 && index < segmentStyles.size()) {
                return segmentStyles.remove(index);
            }
            else {
                throw new IndexOutOfBoundsException(String.format("The index: %d from which the segment style was to be removed is outside the valid range of segment style indices!", index));
            }
        }
    }

    /**
     * Remove the specified NodeStyle from the NodeStyles at the specified index.
     *
     * @param index The index of the NodeStyle to be removed, which is relative to the number of NodeStyles.
     * @return The NodeStyle removed from the specified index if successful or null if not successful.
     * @throws IndexOutOfBoundsException If the specified index at which the NodeStyle was to be removed was outside
     *                                   the valid range of NodeStyle indices.
     */
    public NodeStyle removeNodeStyleAt(int index) throws IndexOutOfBoundsException {
        synchronized(nodeStyles) {
            if (index >= 0 && index < nodeStyles.size()) {
                return nodeStyles.remove(index);
            }
            else {
                throw new IndexOutOfBoundsException(String.format("The index: %d from which the node style was to be removed is outside the valid range of node style indices!", index));
            }
        }
    }

    /**
     * Get the index of the specified SegmentStyle within the PathStyle's list of SegmentStyles.
     *
     * @param style The SegmentStyle for which to find the index in the SegmentStyles or this PathStyle.
     * @return The index of the SegmentStyle within the PathStyle's SegmentStyles or -1 if the SegmentStyle
     *         is not present.
     */
    public int indexOf(SegmentStyle style) {
        return segmentStyles.indexOf(style);
    }

    /**
     * Get the index of the specified NodeStyle within the PathStyle's list of NodeStyles.
     *
     * @param style The NodeStyle for which to find the index in the NodeStyles or this PathStyle.
     * @return The index of the SegmentStyle within the PathStyle's NodeStyles or -1 if the NodeStyle
     *         is not present.
     */
    public int indexOf(NodeStyle style) {
        return nodeStyles.indexOf(style);
    }

    /**
     * Create a clone of this PathStyle, cloning the child style items as needed.
     *
     * @return A clone of this PathStyle.
     */
    @Override
    public PathStyle clone() {
        List<NodeStyle> clonedNodeStyles = new ArrayList<NodeStyle>(nodeStyles.size());
        List<SegmentStyle> clonedSegmentStyles = new ArrayList<SegmentStyle>(segmentStyles.size());
        for (NodeStyle nodeStyle : nodeStyles) {
            clonedNodeStyles.add(nodeStyle.clone());
        }
        for (SegmentStyle segmentStyle : segmentStyles) {
            clonedSegmentStyles.add(segmentStyle.clone());
        }
        return new PathStyle(clonedNodeStyles, clonedSegmentStyles);
    }
}
