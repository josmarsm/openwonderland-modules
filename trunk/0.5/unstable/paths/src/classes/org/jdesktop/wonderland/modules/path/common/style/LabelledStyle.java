package org.jdesktop.wonderland.modules.path.common.style;

/**
 * This interface represents a style which incorporates text labels or captions.
 *
 * @author Carl Jokl
 */
public interface LabelledStyle {

    /**
     * Get the number of labels / captions which are used within this style.
     * 
     * @return The number of labels or captions which are used within this style.
     */
    public int getTextAttributeCount();


    /**
     * Get the style label or caption attribute at the specified index.
     *
     * @param index The index of the label or caption to be retrieved.
     * @return The label or caption value attribute at the specified index.
     * @throws IndexOutOfBoundsException If the specified index is outside the
     *         range of labels / captions in the style.
     */
    public String getLabelOrCaption(int index) throws IndexOutOfBoundsException;

    /**
     * Set the label or caption at the specified index.
     *
     * @param index The index of the label or caption to be set which must be within
     *              the valid range.
     * @param value The value to which the label or caption value attribute at the specified
     *              index is to be set.
     * @return True if the value was not null and was able to be set at the specified index.
     * @throws IndexOutOfBoundsException If the specified index is outside the
     *         range of size / offset attributes.
     */
    public boolean setLabelOrCaption(int index, String value) throws IndexOutOfBoundsException;

    /**
     * Get the name of the label or caption value at the specified index.
     * This descriptive name can be used in UI's for setting the label / caption
     * to show what the value represents.
     *
     * @param index The index of the label / caption attribute for which to return the name.
     * @return The name of the label / caption attribute at the specified index.
     * @throws IndexOutOfBoundsException If the index of the label / caption name to be
     *         retrieved was outside the valid range.
     */
    public String getLabelOrCaptionName(int index) throws IndexOutOfBoundsException;
}
