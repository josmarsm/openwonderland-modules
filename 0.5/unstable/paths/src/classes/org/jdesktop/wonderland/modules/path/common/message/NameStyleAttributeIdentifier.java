package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.modules.path.common.style.ItemStyle;
import org.jdesktop.wonderland.modules.path.common.style.StyleAttribute;

/**
 * This is an implementation of a StyleAttributeIdentifier which identifies
 * a StyleAttribute using it's name.
 *
 * @author Carl Jokl
 */
public class NameStyleAttributeIdentifier implements StyleAttributeIdentifier, Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final String name;

    /**
     * Create a new NameStyleAttributeIdentifier using the specified name.
     *
     * @param name The name of the StyleAttribute identified.
     */
    public NameStyleAttributeIdentifier(final String name) {
        this.name = name;
    }

    /**
     * Get the StyleAttribute from the specified ItemStyle using the name
     * of the StyleAttribute.
     *
     * @param itemStyle The ItemStyle from which to get the StyleAttribute.
     * @return The StyleAttribute with the name contained within this
     *         NameStyleAttributeIdentifier or null if no such StyleAttribute
     *         exists or the ItemStyle is null.
     */
    @Override
    public StyleAttribute getAttributeFrom(ItemStyle itemStyle) {
        if (itemStyle != null) {
            return itemStyle.getStyleAttribute(name);
        }
        return null;
    }

    /**
     * Remove the StyleAttribute from the specified ItemStyle using the name
     * of the StyleAttribute.
     *
     * @param itemStyle The ItemStyle from which to remove the StyleAttribute.
     * @return True if a StyleAttribute was present in the ItemStyle
     *         (which was not null) and was able to be removed.
     */
    @Override
    public boolean removeAttributeFrom(ItemStyle itemStyle) {
        if (itemStyle != null) {
            return itemStyle.removeStyleAttribute(name) != null;
        }
        return false;
    }

    /**
     * Get the name from this NameStyleAttributeIdentifier.
     * 
     * @return The name from this NameStyleAttributeIdentifier.
     */
    public String getName() {
        return name;
    }
}
