package org.jdesktop.wonderland.modules.path.common.style;

import java.awt.Color;

/**
 * This interface represents a style which contains a number of colors.
 * In spite of being British and the pain is causes me to spell the
 * interface this way, as I hope this module will be reused and
 * therefore use the American spelling to be consistent with
 * the JDK.
 *
 * @author Carl Jokl
 */
public interface ColoredStyle {

    /**
     * Get the minimum number of colors which
     * are expected to be part of this style.
     * If the style expects a fixed number of colors
     * then the minimum color and maximum color values
     * will be the same.
     * 
     * @return The minimum number of colors which this style is expected to use.
     */
    public int getMinColors();

    /**
     * Get the maximum number or colors which are expected to be part of this
     * style. If the style expects a specific number of colors then this number
     * will be the same as the minimum colors.
     *
     * @return The maximum number of colors expected to be used with this style.
     */
    public int getMaxColors();

    /**
     * This value represents the actual current number of colors in the style.
     * If the style has a minimum number of colors then the colors in the style
     * should have already been populated with default values i.e. it is an
     * error for the color count to be less than the minimum colors or greater
     * than the maximum colors.
     *
     * @return The current number of colors. This must be within the range of
     *         the minimum and maximum number of colors.
     */
    public int getColorCount();

    /**
     * Get the style Color at the specified index.
     *
     * @param index The index of the color to be retrieved.
     * @return The color in the style at the specified index.
     * @throws IndexOutOfBoundsException If the specified index is outside the
     *         range of colors in the style.
     */
    public Color getColor(int index) throws IndexOutOfBoundsException;

    /**
     * Set the style Color at the specified index if the index specified
     * is one after the current range of colors but the maximum colors has
     * not been reached then the set color
     *
     * @param index The index of the color to be set.
     * @param color The color to be set in the style at the specified index.
     * @return True if the color was able to be set. False if the supplied color was null or could not be set.
     * @throws IndexOutOfBoundsException If the specified index is outside the
     *         range of colors in the style.
     */
    public boolean setColor(int index, Color color) throws IndexOutOfBoundsException;

    /**
     * Add a color to the colors within this style.
     *
     * @param color The color to be added to this style.
     * @return True if the color was able to be added i.e. the maximum number
     *              of colors has not been reached. False if the color could
     *              not be added i.e. due to the maximum number of colors having
     *              been reached.
     */
    public boolean addColor(Color color);

    /**
     * Remove the last color in the list of colors for this colored style.
     *         If the ColoredStyle does support having a variable number of colors
     *         but the remove operation would reduce the number of colors below
     *         the minimum then null is returned.
     *
     * @return The color which was removed from the style or null if the color
     *         could not be removed such as due to the minimum number of colors having been reached.
     * @throws UnsupportedOperationException If removing colors is not supported by
     *         the ColoredStyle i.e. due to the number of colors being fixed.
     */
    public Color removeColor();

    /**
     * Remove a color at the specified index from the available colors.
     *
     * @param index The index of the color to be removed. This index must be in the
     *        range from the minimum number of colors to the maximum number of colors -1.
     *        If the number of colors is fixed then removing a color in this way is not
     *        possible.
     * @return The color removed from the specified index or null if the color could not be removed.
     * @throws IndexOutOfBoundsException If the index supplied at which the color
     *         was to be removed is outside the valid / allowed range.
     * @throws UnsupportedOperationException If removing colors is not supported by
     *         the ColoredStyle i.e. due to the number of colors being fixed.
     */
    public Color removeColor(int index) throws IndexOutOfBoundsException;

    /**
     * Get a descriptive label for the color at the specified index.
     * This descriptive label can be used in UI's for setting the color.
     *
     * @param index The index of the color for which to return the label.
     * @return The descriptive label for the color at the specified index.
     * @throws IndexOutOfBoundsException If the index of the color label to be
     *         retrieved was outside the valid range.
     */
    public String getColorLabel(int index) throws IndexOutOfBoundsException;
}
