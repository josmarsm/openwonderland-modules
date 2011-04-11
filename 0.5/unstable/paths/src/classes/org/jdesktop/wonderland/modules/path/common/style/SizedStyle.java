package org.jdesktop.wonderland.modules.path.common.style;

/**
 * This interface represents a style which incorporates size quantities sizing
 * and setting distance offset values as needed.
 * Most styles will involve some kind of dimensions and some will have more than
 * others. To keep this functionality generic the interface also allows access
 * to the labels of the dimensions.
 *
 * @author Carl Jokl
 */
public interface SizedStyle {

    /**
     * Get the number of size attributes which this style has. The number of
     * size attributes is expected to be constant for a given style.
     *
     * @return The number of size and offset values which the implementing style has.
     */
    public int getSizeAttributeCount();

    /**
     * Get the style size or offset attribute at the specified index.
     *
     * @param index The index of the size or offset to be retrieved.
     * @return The size / offset value attribute at the specified index.
     * @throws IndexOutOfBoundsException If the specified index is outside the
     *         range of sizes and offsets in the style.
     */
    public float getSizeOrOffset(int index) throws IndexOutOfBoundsException;

    /**
     * Set the size or offset at the specified index.
     *
     * @param index The index of the size or offset to be set which must be within
     *              the valid range.
     * @param value The value to which the size or offset value attribute at the specified
     *              index is to be set.
     * @throws IndexOutOfBoundsException If the specified index is outside the
     *         range of size / offset attributes.
     * @throw IllegalArgumentException if the specified value is outside the permitted range for the value.
     */
    public void setSizeOrOffset(int index, float value) throws IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Get a descriptive label for the size or offset at the specified index.
     * This descriptive label can be used in UI's for setting the size / offset value.
     *
     * @param index The index of the size / offset attribute for which to return the label.
     * @return The descriptive label for the size / offset at the specified index.
     * @throws IndexOutOfBoundsException If the index of the size / offset label to be
     *         retrieved was outside the valid range.
     */
    public String getSizeOrOffsetLabel(int index) throws IndexOutOfBoundsException;

    /**
     * Get the permitted value range for the value at the specified index.
     *
     * @param index The index of the size or offset attribute for which to get the permitted value range.
     * @return The permitted value range for the size or offset at the specified index.
     * @throws IndexOutOfBoundsException If the index of the size or offset attribute to be set was outside
     *                                   the permitted range.
     */
    public FloatValueRange getSizeOrOffsetRange(int index) throws IndexOutOfBoundsException;
}
