package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.modules.path.common.style.ItemStyle;
import org.jdesktop.wonderland.modules.path.common.style.StyleAttribute;

/**
 * This is an implementation of a StyleAttributeIdentifier which identifies
 * a StyleAttribute using it's index in the list of StyleAttributes.
 *
 * @author Carl Jokl
 */
public class IndexStyleAttributeIdentifier implements StyleAttributeIdentifier, Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final int index;

    /**
     * Create a new IndexStyleAttributeIdentifier using the specified index.
     *
     * @param index The index of the StyleAttribute identified.
     */
    public IndexStyleAttributeIdentifier(final int index) {
        this.index = index;
    }

    /**
     * Get the StyleAttribute from the specified ItemStyle using the index
     * of the StyleAttribute.
     *
     * @param itemStyle The ItemStyle from which to get the StyleAttribute.
     * @return The StyleAttribute at the list index contained within this
     *         IndexStyleAttributeIdentifier or null if no such StyleAttribute
     *         exists or the ItemStyle is null.
     */
    @Override
    public StyleAttribute getAttributeFrom(ItemStyle itemStyle) {
        if (itemStyle != null && index >= 0 && index < itemStyle.noOfAttributes()) {
            return itemStyle.getStyleAttribute(index);
        }
        return null;
    }

    /**
     * Remove the StyleAttribute from the specified ItemStyle using the index
     * of the StyleAttribute.
     *
     * @param itemStyle The ItemStyle from which to remove the StyleAttribute.
     * @return True if a StyleAttribute was present in the ItemStyle
     *         (which was not null) and was able to be removed.
     */
    @Override
    public boolean removeAttributeFrom(ItemStyle itemStyle) {
        if (itemStyle != null && index >= 0 && index < itemStyle.noOfAttributes()) {
            return itemStyle.removeStyleAttribute(index) != null;
        }
        return false;
    }

    /**
     * Get the index from this IndexStyleAttributeIdentifier.
     * 
     * @return The index from this IndexStyleAttributeIdentifier.
     */
    public int getIndex() {
        return index;
    }
}
