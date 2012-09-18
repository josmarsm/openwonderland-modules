package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.modules.path.common.style.ItemStyle;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;
import org.jdesktop.wonderland.modules.path.common.style.StyleType;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This is a convenience class used to hold identification of an ItemStyle to which certain messages or changes relate.
 *
 * @author Carl Jokl
 */
public class ItemStyleIdentifier implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final int itemStyleIndex;
    private final StyleType itemStyleType;

    /**
     * Create a new ItemStyleIdentifier to use to find a given ItemStyle within a PathStyle.
     *
     * @param itemStyleIndex The index of the ItemStyle within the PathStyle.
     * @param itemStyleType The StyleType of the ItemStyle
     */
    public ItemStyleIdentifier(final int itemStyleIndex, final StyleType itemStyleType) throws IllegalArgumentException {
        if (itemStyleIndex < 0) {
            throw new IllegalArgumentException("The index of the item style cannot be negative!");
        }
        if (itemStyleType == null) {
            throw new IllegalArgumentException("The item style type for the item style to be identified cannot be null!");
        }
        this.itemStyleIndex = itemStyleIndex;
        this.itemStyleType = itemStyleType;
    }

    /**
     * Get the ItemStyle from the specified PathStyle which is identified by this ItemStyle identifier.
     *
     * @param pathStyle The PathStyle from which to get the ItemStyle.
     * @return The ItemStyle from the specified PathStyle which is identified by this ItemStyleIdentifier.
     *         If the PathStyle is null or there is no ItemStyle present in the PathStyle which is identified
     *         by this ItemStyleIdentifier then null will be returned.
     */
    public ItemStyle getItemStyle(PathStyle pathStyle) {
        if (pathStyle != null) {
            if (itemStyleType instanceof NodeStyleType) {
                if (pathStyle.noOfNodeStyles() > itemStyleIndex) {
                    return pathStyle.getNodeStyle(itemStyleIndex, false);
                }
            }
            else if (itemStyleType instanceof SegmentStyleType) {
                if (pathStyle.noOfSegmentStyles() > itemStyleIndex) {
                   return pathStyle.getSegmentStyle(itemStyleIndex, false);
                }
            }
        }
        return null;
    }

    /**
     * Remove the ItemStyle within the specified PathStyle which is identified by this ItemStyleIdentifier.
     *
     * @param pathStyle The PathStyle from which to remove the 
     * @return
     */
    public ItemStyle removeItemStyle(PathStyle pathStyle) {
        if (pathStyle != null) {
            if (itemStyleType instanceof NodeStyleType) {
                if (pathStyle.noOfNodeStyles() > itemStyleIndex) {
                    return pathStyle.removeNodeStyleAt(itemStyleIndex);
                }
            }
            else if (itemStyleType instanceof SegmentStyleType) {
                if (pathStyle.noOfSegmentStyles() > itemStyleIndex) {
                   return pathStyle.removeSegmentStyleAt(itemStyleIndex);
                }
            }
        }
        return null;
    }
}
