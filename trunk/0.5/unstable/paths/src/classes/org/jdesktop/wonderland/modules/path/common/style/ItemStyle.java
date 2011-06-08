package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;

/**
 * This is a common base interface for the styles of PathNodes and the segments between the nodes.
 *
 * @author Carl Jokl
 */
public interface ItemStyle<T extends StyleType> extends Serializable {

    /**
     * The number of item over which the style spans.
     *
     * @return The number of items over which the style spans.
     */
    public int getSpan();

    /**
     * Set the number of items over which the style spans.
     *
     * @param getSpan The number of items over which the style spans which must be
     * @throws IllegalArgumentException If the specified getSpan is not 1 or greater.
     */
    public void setSpan(int span) throws IllegalArgumentException;

    /**
     * Get the type of style which this ItemStyle represents.
     *
     * @return A StyleType instance (or generic subclass) which represents the type of style
     *         which this ItemStyle represents.
     */
    public T getStyleType();

    /**
     * Set the StyleType for this ItemStyle meta-data object.
     *
     * @param styleType The StyleType of this ItemStyle meta-data object.
     */
    public void setStyleType(T styleType);

    /**
     * The number of style attributes which are part of this ItemStyle.
     * 
     * @return The number of StyleAttributes which are part of this ItemStyle.
     */
    public int noOfAttributes();

    /**
     * Get the StyleAttribute at the specified index.
     *
     * @param attributeIndex The index of the StyleAttribute to be returned. This index must
     *                       be within the range from zero to one less than the number of style
     *                       attributes.
     * @return The StyleAttribute at the specified index.
     * @throws IndexOutOfBoundsException If the specified attribute index is outside the range of attribute indices.
     */
    public StyleAttribute getStyleAttribute(int attributeIndex) throws IndexOutOfBoundsException;

    /**
     * Get the StyleAttribute with the specified name.
     * 
     * @param attributeName The name of the StyleAttribute to be retrieved.
     * @return The StyleAttribute with the specified name.
     */
    public StyleAttribute getStyleAttribute(String attributeName);

    /**
     * Add the specified StyleAttribute to this ItemStyle.
     *
     * @param styleAttribute The StyleAttribute to be added to this ItemStyle.
     * @return True if the specified StyleAttribute was not null and a StyleAttribute with the same
     *         name did not already exist and the StyleAttribute was able to be added successfully.
     */
    public boolean addStyleAttribute(StyleAttribute styleAttribute);

    /**
     * Replace any existing StyleAttribute which has the same name as the specified style
     * attribute with the specified StyleAttribute. An existing StyleAttribute must exist
     * with the same name in order for the operation to succeed. If no existing StyleAttribute
     * exists with the same name then the specified StyleAttribute will not be added.
     *
     * @param styleAttribute The StyleAttribute which is to replace an existing attribute.
     * @return The StyleAttribute which was replaced by the specified style if the replace
     *         succeeded or null otherwise.
     */
    public StyleAttribute replaceStyleAttribute(StyleAttribute styleAttribute);

    /**
     * Remove the specified StyleAttribute from this ItemStyle.
     *
     * @param styleAttribute The StyleAttribute to be removed from this ItemStyle.
     * @return True if the specified styleAttribute was not null and it was part of
     *              this ItemStyle and was able to be removed successfully.
     */
    public boolean removeStyleAttribute(StyleAttribute styleAttribute);

    /**
     * Remove the StyleAttribute at the specified index from this ItemStyle.
     *
     * @param attributeIndex The index of the StyleAttribute to be removed (which should be between zero
     *                       and one less than the number of style attributes.
     * @return The StyleAttribute which was removed from the specified index.
     * @throws IndexOutOfBoundsException If the specified attributeIndex of the StyleAttribute to be removed
     *                                   is outside the valid range of
     */
    public StyleAttribute removeStyleAttribute(int attributeIndex) throws IndexOutOfBoundsException;

    /**
     * Remove the StyleAttribute with the specified name from this ItemStyle.
     *
     * @param attributeName The name of the StyleAttribute to be removed from this ItemStyle.
     * @return StyleAttribute The StyleAttribute with the specified name which was removed from
     *                        the ItemStyle. Null will be returned if the attribute name is null
     *                        or no attribute exists within this ItemStyle with the specified name.
     */
    public StyleAttribute removeStyleAttribute(String attributeName);

    /**
     * Remove all of the ItemStyle StyleAttributes.
     */
    public void removeAllAttributes();

    /**
     * Get all of the StyleAttributes.
     *
     * @return An array of all the StyleAttributes.
     */
    public StyleAttribute[] getStyleAttributes();
}
