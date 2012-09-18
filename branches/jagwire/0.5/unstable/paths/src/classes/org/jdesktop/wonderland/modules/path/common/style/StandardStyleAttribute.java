package org.jdesktop.wonderland.modules.path.common.style;

/**
 * This is a convenience enumeration which represents the core attributes
 * which are normally present in style meta data objects for different style
 * types.
 *
 * @author Carl Jokl
 */
public enum StandardStyleAttribute {

    /**
     * The length involved in the style.
     */
    LENGTH("length"),

    /**
     * The width involved in the style.
     */
    WIDTH("width"),

    /**
     * The height involved in the style.
     */
    HEIGHT("height"),

    /**
     * The x offset of the style.
     */
    X_OFFSET("x-offset"),

    /**
     * The y offset of the style.
     */
    Y_OFFSET("y-offset"),

    /**
     * The z offset of the style.
     */
    Z_OFFSET("z-offset"),

    /**
     * The radius of the style.
     */
    RADIUS("radius"),

    /**
     * The first radius of the style.
     */
    RADIUS_1("radius-1"),

    /**
     * The second radius of the style.
     */
    RADIUS_2("radius-2"),

    /**
     * The u texture offset of the style.
     */
    U_OFFSET("u-offset"),

    /**
     * The v texture offset of the style.
     */
    V_OFFSET("v-offset"),

    /**
     * The number of U dimension texture repeats per meter.
     */
    U_REPEATS_PER_M("u-reps/m"),

    /**
     * The number of V dimension texture repeats per meter.
     */
    V_REPEATS_PER_M("v-reps/m"),

    /**
     * The text used in the style.
     */
    TEXT("text"),

    /**
     * The color for a style which only has one color.
     */
    COLOR("color"),

    /**
     * The foreground color for the style.
     */
    FOREGROUND_COLOR("foreground-color"),

    /**
     * The background color for the style.
     */
    BACKGROUND_COLOR("background-color"),

    /**
     * The color for the front of drawn style item.
     */
    FRONT_COLOR("front-color"),

    /**
     * The color for the back of drawn style item.
     */
    BACK_COLOR("back-color"),

    /**
     * The color for the front and back of drawn style item.
     */
    FRONT_AND_BACK_COLOR("front-and-back-color"),

    /**
     * The color for the top of drawn style item.
     */
    TOP_COLOR("top-color"),

    /**
     * The color for the bottom of drawn style item.
     */
    BOTTOM_COLOR("bottom-color"),

    /**
     * The color for the top and bottom of drawn style item.
     */
    TOP_AND_BOTTOM_COLOR("top-and-bottom-color"),

    /**
     * The color for the left side of drawn style item.
     */
    LEFT_SIDE_COLOR("left-side-color"),

    /**
     * The color for the right side of drawn style item.
     */
    RIGHT_SIDE_COLOR("right-side-color"),

    /**
     * The color for the sides of drawn style item.
     */
    SIDE_COLOR("side-color"),

    /**
     * The texture style attribute for styles which only have one texture.
     */
    TEXTURE("texture"),

    /**
     * The texture for the front of drawn style item.
     */
    FRONT_TEXTURE("front-texture"),

    /**
     * The texture for the back of drawn style item.
     */
    BACK_TEXTURE("back-texture"),

    /**
     * The texture for front and back of drawn style item.
     */
    FRONT_AND_BACK_TEXTURE("front-and-back-texture"),

    /**
     * The texture for the top of drawn style item.
     */
    TOP_TEXTURE("top-texture"),

    /**
     * The texture for the bottom of drawn style item.
     */
    BOTTOM_TEXTURE("bottom-texture"),

    /**
     * The texture for the left side of drawn style item.
     */
    LEFT_SIDE_TEXTURE("left-side-texture"),

    /**
     * The texture for the right side of drawn style item.
     */
    RIGHT_SIDE_TEXTURE("right-side-texture"),

    /**
     * The texture for the sides of drawn style item.
     */
    SIDE_TEXTURE("side-texture");

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final String name;

    /**
     * Create an instance of a StandardStyleAttribute enumeration.
     *
     * @param name The name of the standard style attribute.
     * @param description The description of the standard style attribute.
     */
    private StandardStyleAttribute(final String name) {
        this.name = name;
    }

    /**
     * Get the name of the StandardStyleAttribute.
     * This name can be used to look up the attribute within
     * an ItemStyle.
     *
     * @return The name of the StyleAttribute.
     */
    public String getName() {
        return name;
    }
}
