package org.jdesktop.wonderland.modules.path.common.style;

import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import java.io.Serializable;

/**
 * This interface represents the style meta-data for a path.
 * Specific implementations will vary depending on the data 
 * which needs to be stored for each path type. Different
 * path types may need some extra details.
 *
 * @author Carl Jokl
 */
public interface PathStyle extends Serializable {

    /**
     * Get the start PathNode NodeStyle for this PathStyle.
     * This may be the same as the other NodeStyles.
     *
     * @return The start SegmentStyle for this PathStyle.
     */
    public SegmentStyle getStartSegmentStyle();

    /**
     * Get the start PathNode NodeStyle for this PathStyle.
     * This may be the same as the other NodeStyles.
     *
     * @return The start NodeStyle for this PathStyle.
     */
    public NodeStyle getStartNodeStyle();

    /**
     * Get the end SegmentStyle for this PathStyle.
     * This may be the same as the start and end SegmentStyles if
     * no other SegmentStyle is being used.
     *
     * @return The end SegmentStyle for the PathStyle.
     */
    public SegmentStyle getEndSegmentStyle();

    /**
     * Get the end PathNode NodeStyle for this PathStyle.
     * This may be the same as the start and end NodeStyles if
     * no other NodeStyle is being used.
     *
     * @return The end NodeStyle for the PathStyle.
     */
    public NodeStyle getEndNodeStyle();

    /**
     * Whether any SegmentStyle is currently set.
     *
     * @return True if any SegmentStyle is set.
     */
    public boolean isSegmentStyleSet();

    /**
     * Whether any NodeStyle is currently set.
     * 
     * @return True if any NodeStyle is set.
     */
    public boolean isNodeStyleSet();

    /**
     * Whether the PathStyle uses a single SegmentStyle.
     *
     * @return True if the PathStyle has just one SegmentStyle.
     */
    public boolean isSingleSegmentStyle();

    /**
     * Whether the PathStyle uses a single NodeStyle.
     *
     * @return True if the PathStyle has just one NodeStyle.
     */
    public boolean isSingleNodeStyle();

    /**
     * Get the number of SegmentStyles which are used within this PathStyle.
     *
     * @return The number of SegmentStyles which are used within this PathStyle.
     */
    public int noOfSegmentStyles();

    /**
     * Get the number of NodeStyles which are used within this PathStyle.
     *
     * @return The number of NodeStyles which are used within this PathStyle.
     */
    public int noOfNodeStyles();

    /**
     * Get the SegmentStyle at the specified index within the PathStyle.
     *
     * @param index The index of the SegmentStyle to be retrieved which is either the index relative to the number of
     *              SegmentStyles in this PathStyle or the index of the segment to which the style applies
     *              (taking into account SegmentStyles which span more than one segments). The next parameter
     *              determines which index type is intended to be used.
     * @param relativeToSegments Whether the index supplied is relative the number of segments or the number of SegmentStyles
     *                           (which may be less than the number of segments). True if the index is the index of the
     *                           segment to which the style applies or false if the index is relative to the number of
     *                           SegmentStyles.
     * @return The SegmentStyle at the specified index.
     * @throws IndexOutOfBoundsException If the specified index is outside the valid
     *                                   range of SegmentStyle indices.
     */
    public SegmentStyle getSegmentStyle(int index, boolean relativeToSegments) throws IndexOutOfBoundsException;

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
    public NodeStyle getNodeStyle(int index, boolean relativeToNodes) throws IndexOutOfBoundsException;

    /**
     * Append the specified SegmentStyle to the end of the SegmentStyles.
     *
     * @param style The SegmentStyle to be appended to the end of the SegmentStyles.
     * @return True if the SegmentStyle was able to be appended successfully.
     */
    public boolean append(SegmentStyle style);

    /**
     * Append the specified NodeStyle to the end of the NodeStyles.
     *
     * @param style The NodeStyle to be appended at the end of the NodeStyles.
     * @return True if the NodeStyle was able to be appended successfully.
     */
    public boolean append(NodeStyle style);

    /**
     * Insert the specified SegmentStyle to the start of the SegmentStyles.
     *
     * @param style The SegmentStyle to be inserted at the start of the SegmentStyles.
     * @return True if the SegmentStyle was able to be inserted at the start of the SegmentStyles successfully.
     */
    public boolean insertFirst(SegmentStyle style);

    /**
     * Insert the specified NodeStyle at the start of the NodeStyles.
     *
     * @param style The NodeStyle to be inserted at the start of the NodeStyles.
     * @return True if the NodeStyle was able to be inserted at the start of the NodeStyles successfully.
     */
    public boolean insertFirst(NodeStyle style);

    /**
     * Insert the specified SegmentStyle at the specified Index.
     *
     * @param index The index at which to insert the SegmentStyle which is relative to the number of SegmentStyles.
     * @param style The specified SegmentStyle to be inserted into the SegmentStyles.
     * @return True if the specified SegmentStyle was able to be inserted successfully.
     * @throws IndexOutOfBoundsException If the specified index at which to insert was outside the valid range.
     */
    public boolean insertAt(int index, SegmentStyle style) throws IndexOutOfBoundsException;

    /**
     * Insert the specified NodeStyle at the specified index.
     *
     * @param index The index at which to insert the NodeStyle which is relative to the number of NodeStyles.
     * @param style The specified NodeStyle to be inserted into the NodeStyles.
     * @return True if the specified NodeStyle was able to be inserted successfully.
     * @throws IndexOutOfBoundsException If the specified index at which to insert was outside the valid range.
     */
    public boolean insertAt(int index, NodeStyle style) throws IndexOutOfBoundsException;

    /**
     * Remove the specified SegmentStyle from the NodeStyles within this PathStyle.
     *
     * @param style The SegmentStyle to be removed from the SegmentStyles within this PathStyle.
     * @return True if the specified SegmentStyle was present in this PathStyle and was able to be removed successfully.
     */
    public boolean remove(SegmentStyle style);

    /**
     * Remove the specified NodeStyle from the NodeStyles within this PathStyle.
     * 
     * @param style The NodeStyle to be removed from the NodeStyles within the PathStyle.
     * @return True if the specified NodeStyle was present in this PathStyle and was able to be removed successfully.
     */
    public boolean remove(NodeStyle style);

    /**
     * Remove the specified SegmentStyle from the SegmentStyles at the specified index.
     *
     * @param index The index of the SegmentStyle to be removed, which is relative to the number of SegmentStyles.
     * @return The SegmentStyle removed from the specified index if successful or null if not successful.
     * @throws IndexOutOfBoundsException If the specified index at which the SegmentStyle was to be removed was outside
     *                                   the valid range of SegmentStyle indices.
     *
     */
    public SegmentStyle removeSegmentStyleAt(int index) throws IndexOutOfBoundsException;

    /**
     * Remove the specified NodeStyle from the NodeStyles at the specified index.
     *
     * @param index The index of the NodeStyle to be removed, which is relative to the number of NodeStyles.
     * @return The NodeStyle removed from the specified index if successful or null if not successful.
     * @throws IndexOutOfBoundsException If the specified index at which the NodeStyle was to be removed was outside
     *                                   the valid range of NodeStyle indices.
     */
    public NodeStyle removeNodeStyleAt(int index) throws IndexOutOfBoundsException;
}
