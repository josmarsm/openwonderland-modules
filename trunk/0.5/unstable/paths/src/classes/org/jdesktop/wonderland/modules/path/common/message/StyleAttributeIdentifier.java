package org.jdesktop.wonderland.modules.path.common.message;

import java.io.Serializable;
import org.jdesktop.wonderland.modules.path.common.style.ItemStyle;
import org.jdesktop.wonderland.modules.path.common.style.StyleAttribute;

public interface StyleAttributeIdentifier extends Serializable {

    /**
     * Get the StyleAttribute from the specified ItemStyle which is identified by this StyleAttributeIdentifier.
     *
     * @param itemStyle The ItemStyle from which to get the StyleAttribute which is identified by this StyleAttributeIdentifier.
     * @return The StyleAttribute which is identified by this StyleAttributeIdentifier from the specified ItemStyle or null if the
     *         StyleAttribute was not present or the ItemStyle was null.
     */
    public StyleAttribute getAttributeFrom(ItemStyle itemStyle);

    /**
     * Use the StyleAttributeIdentifier to remove a specified StyleAttribute from the
     * specified ItemStyle.
     *
     * @param itemStyle The ItemStyle from which to remove the StyleAttribute identified by this
     *                  StyleAttributeIdentifier.
     * @return True if the StyleAttribute identified by this StyleAttributeIdentifier was present in
     *         the specified ItemStyle and the specified ItemStyle was not null.
     */
    public boolean removeAttributeFrom(ItemStyle itemStyle);

}
