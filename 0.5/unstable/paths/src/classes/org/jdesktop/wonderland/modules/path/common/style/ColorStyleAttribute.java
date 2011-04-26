package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This class represents a StyleAttribute which is a text value.
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="text-attribute")
public class ColorStyleAttribute extends StyleAttribute implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    @XmlTransient
    private float red;
    @XmlTransient
    private float green;
    @XmlTransient
    private float blue;
    @XmlTransient
    private float alpha;

    /**
     * A no-argument constructor for the benefit of JAXB.
     */
    protected ColorStyleAttribute() {
        red = 0.0f;
        green = 0.0f;
        blue = 0.0f;
        alpha = 1.0f;
    }

    /**
     * Create a new instance of a ColorStyleAttribute with the specified name.
     *
     * @param name The name of this ColorStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this ColorStyleAttribute is null.
     */
    public ColorStyleAttribute(String name) throws IllegalArgumentException {
        super(name);
        red = 0.0f;
        green = 0.0f;
        blue = 0.0f;
        alpha = 1.0f;
    }

    /**
     * Create a new instance of a ColorStyleAttribute with the specified name and color attributes.
     * The alpha value will be set to 1.0.
     *
     * @param name The name of this ColorStyleAttribute.
     * @param red The red component value which should be from 0.0 to 1.0.
     * @param green The green component value which should be from 0.0 to 1.0.
     * @param blue The blue component value which should be from 0.0 to 1.0.
     * @throws IllegalArgumentException If the specified name of this ColorStyleAttribute is null or
     *                                  the red, green or blue value is outside the permitted range from 0.0 to 1.0.
     */
    public ColorStyleAttribute(String name, float red, float green, float blue) throws IllegalArgumentException {
        super(name);
        setRed(red);
        setGreen(green);
        setBlue(blue);
        alpha = 1.0f;
    }

    /**
     * Create a new instance of a ColorStyleAttribute with the specified name and color attributes.
     *
     * @param name The name of this ColorStyleAttribute.
     * @param red The red component value which should be from 0.0 to 1.0.
     * @param green The green component value which should be from 0.0 to 1.0.
     * @param blue The blue component value which should be from 0.0 to 1.0.
     * @param alpha The blue component value which should be from 0.0 to 1.0.
     * @throws IllegalArgumentException If the specified name of this ColorStyleAttribute is null or
     *                                  the red, green, blue or alpha value is outside the permitted range from 0.0 to 1.0.
     */
    public ColorStyleAttribute(String name, float red, float green, float blue, float alpha) throws IllegalArgumentException {
        super(name);
        setRed(red);
        setGreen(green);
        setBlue(blue);
        setAlpha(alpha);
    }

    /**
     * Get the red portion of the color value.
     * Values should be between 0.0 and 1.0.
     *
     * @return The red portion of the color value.
     */
    @XmlAttribute(name="red")
    public float getRed() {
        return red;
    }

    /**
     * Set the red portion of the color value.
     * Values should be between 0.0 and 1.0.
     *
     * @param red The red portion of the color value.
     * @throws IllegalArgumentException If the specified red value was outside the valid range from 0.0 to 1.0.
     */
    public final void setRed(float red) throws IllegalArgumentException {
        if (red >= 0.0f && red <= 1.0f) {
            this.red = red;
        }
        else {
            throw new IllegalArgumentException(String.format("The specified red value: %f was not in the supported range from 0.0 to 1.0!", red));
        }
    }

    /**
     * Get the green portion of the color value.
     * Values should be between 0.0 (none) and 1.0 (full).
     *
     * @return The green portion of the color value.
     */
    @XmlAttribute(name="green")
    public float getGreen() {
        return green;
    }

    /**
     * Set the green portion of the color value.
     * Values should be between 0.0 (none) and 1.0 (full).
     *
     * @param green The green portion of the color value.
     * @throws IllegalArgumentException If the specified green value was outside the valid range from 0.0 to 1.0.
     */
    public final void setGreen(float green) throws IllegalArgumentException {
        if (green >= 0.0f && green <= 1.0f) {
            this.green = green;
        }
        else {
            throw new IllegalArgumentException(String.format("The specified green value: %f was not in the supported range from 0.0 to 1.0!", green));
        }
    }

    /**
     * Get the blue portion of the color value.
     * Values should be between 0.0 (none) and 1.0 (full).
     *
     * @return The blue portion of the color value.
     */
    @XmlAttribute(name="blue")
    public float getBlue() {
        return blue;
    }

    /**
     * Set the blue portion of the color value.
     * Values should be between 0.0 (none) and 1.0 (full).
     *
     * @param blue The blue portion of the color value.
     * @throws IllegalArgumentException If the specified blue value was outside the valid range from 0.0 to 1.0.
     */
    public final void setBlue(float blue) throws IllegalArgumentException {
        if (blue >= 0.0f && blue <= 1.0f) {
            this.blue = blue;
        }
        else {
            throw new IllegalArgumentException(String.format("The specified blue value: %f was not in the supported range from 0.0 to 1.0!", blue));
        }
    }

    /**
     * Get the alpha portion of the color value.
     * Values should be between 0.0 (transparent) and 1.0 (opaque).
     *
     * @return The alpha portion of the color value.
     */
    @XmlAttribute(name="alpha")
    public float getAlpha() {
        return alpha;
    }

    /**
     * Set the alpha portion of the color value.
     * Values should be between 0.0 (transparent) and 1.0 (opaque).
     *
     * @param alpha The alpha portion of the color value.
     * @throws IllegalArgumentException If the specified alpha value was outside the valid range from 0.0 to 1.0.
     */
    public final void setAlpha(float alpha) throws IllegalArgumentException {
        if (alpha >= 0.0f && alpha <= 1.0f) {
            this.alpha = alpha;
        }
        else {
            throw new IllegalArgumentException(String.format("The specified alpha value: %f was not in the supported range from 0.0 to 1.0!", alpha));
        }
    }

    /**
     * Set the component values of this color attribute. The
     * alpha component will be set to 1.0 (fully opaque).
     *
     * All color component values should be between 0.0 (none) and 1.0 (full).
     *
     * @param red The red component value which the color is to have.
     * @param green The green component value which the color is to have.
     * @param blue The blue component value which the color is to have.
     * @throws IllegalArgumentException If the specified red, green or blue values are outside the supported range
     *                                  from 0.0 to 1.0.
     */
    @XmlTransient
    public final void set(float red, float green, float blue) throws IllegalArgumentException {
        setRed(red);
        setGreen(green);
        setBlue(blue);
        alpha = 1.0f;
    }

    /**
     * Set the component values of this color attribute. The
     * alpha component will be set to 1.0 (fully opaque).
     *
     * All color component values should be between 0.0 (none) and 1.0 (full).
     *
     * @param red The red component value which the color is to have.
     * @param green The green component value which the color is to have.
     * @param blue The blue component value which the color is to have.
     * @throws IllegalArgumentException If the specified red, green, blue or alpha values are outside the supported range
     *                                  from 0.0 to 1.0.
     */
    @XmlTransient
    public final void set(float red, float green, float blue, float alpha) throws IllegalArgumentException {
        setRed(red);
        setGreen(green);
        setBlue(blue);
        setAlpha(alpha);
    }

    /**
     * Convert the color components to an array of RGBA float values.
     *
     * @return An array of four elements containing the red, green, blue
     *         and alpha values respectively.
     */
    public float[] toArray() {
        return new float[] { red, green, blue, alpha };
    }
}
