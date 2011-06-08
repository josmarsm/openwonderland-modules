package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.modules.path.common.Described;
import org.jdesktop.wonderland.modules.path.common.Named;

/**
 * A protected interface which represents an object which contains the basic color components.
 */
interface ColorComponents extends Serializable {

    /**
     * Get the red portion of the value.
     *
     * @return The red portion of the value.
     */
    public float getRed();

    /**
     * Set the red portion of the value.
     *
     * @param red The red portion of the value.
     */
    public void setRed(float red);

    /**
     * Get the green portion of the value.
     *
     * @return The green portion of the value.
     */
    public float getGreen();

    /**
     * Set the green portion of the value.
     *
     * @param green The green portion of the value.
     */
    public void setGreen(float green);

    /**
     * Get the blue portion of the value.
     *
     * @return The blue portion of the value.
     */
    public float getBlue();

    /**
     * Set the blue portion of the value.
     *
     * @param blue The blue portion of the value.
     */
    public void setBlue(float blue);

    /**
     * Get the alpha portion of the value.
     *
     * @return The alpha portion of the value.
     */
    public float getAlpha();

    /**
     * Set the alpha portion of the value.
     *
     * @param alpha The alpha portion of the value.
     */
    public void setAlpha(float alpha);
}

/**
 * This is a simple form of a ColorComponentArray which stores the ColorComponents
 * in array form.
 */
class ColorComponentArray implements ColorComponents {

    private final float[] components;

    /**
     * Create a new ColorComponentArray with default values.
     */
    public ColorComponentArray() {
        components = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
    }

    /**
     * Create a new ColorComponentArray with the specified red
     * green and blue values and a default alpha value.
     * Component values should be between 0.0f and 1.0f.
     *
     * @param red The red component.
     * @param green The green component.
     * @param blue The blue component.
     * @throws IllegalArgumentException If any of the specified components were outside the range
     *                                  from 0.0f to 1.0f.
     */
    public ColorComponentArray(float red, float green, float blue) throws IllegalArgumentException {
        components = new float[4];
        setRed(red);
        setGreen(green);
        setBlue(blue);
        components[3] = 1.0f;
    }

    /**
     * Create a new ColorComponentArray with the specified red
     * green and blue values and a default alpha value.
     *
     * @param red The red component.
     * @param green The green component.
     * @param blue The blue component.
     * @param alpha The alpha component.
     */
    public ColorComponentArray(float red, float green, float blue, float alpha) throws IllegalArgumentException {
        components = new float[4];
        setRed(red);
        setGreen(green);
        setBlue(blue);
        setAlpha(alpha);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getRed() {
        return components[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setRed(float red) {
        if (red >= 0.0f && red <= 1.0f) {
            components[0] = red;
        }
        else {
            throw new IllegalArgumentException(String.format("The specified red value: %f was not in the supported range from 0.0 to 1.0!", red));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getGreen() {
        return components[1];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setGreen(float green) {
        if (green >= 0.0f && green <= 1.0f) {
            components[1] = green;
        }
        else {
            throw new IllegalArgumentException(String.format("The specified green value: %f was not in the supported range from 0.0 to 1.0!", green));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getBlue() {
        return components[2];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setBlue(float blue) {
        if (blue >= 0.0f && blue <= 1.0f) {
            components[2] = blue;
        }
        else {
            throw new IllegalArgumentException(String.format("The specified blue value: %f was not in the supported range from 0.0 to 1.0!", blue));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getAlpha() {
        return components[3];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setAlpha(float alpha) {
        if (alpha >= 0.0f && alpha <= 1.0f) {
            components[3] = alpha;
        }
        else {
            throw new IllegalArgumentException(String.format("The specified alpha value: %f was not in the supported range from 0.0 to 1.0!", alpha));
        }
    }
}

/**
 * This class represents a StyleAttribute which is a text value.
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="text-attribute")
public class ColorStyleAttribute extends StyleAttribute implements ColorComponents, Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    @XmlTransient
    final ColorComponents components;

    /**
     * A no-argument constructor for the benefit of JAXB.
     */
    protected ColorStyleAttribute() {
        components = new ColorComponentArray();
    }

    /**
     * Create a new ColorStyleAttribute using the specified float
     * array for the components.
     *
     * @param name A Named object which holds the name of this StyleAttribute.
     * @param description A Described object which holds the description of this StyleAttribute.
     * @param components The components of this ColorStyleAttribute.
     */
    ColorStyleAttribute(final Named name, final Described description, final ColorComponents components) {
        super(name, description);
        this.components = components;
    }

    /**
     * Create a new instance of a ColorStyleAttribute with the specified name.
     *
     * @param name The name of this ColorStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this ColorStyleAttribute is null.
     */
    public ColorStyleAttribute(String name) throws IllegalArgumentException {
        super(name, null);
        components = new ColorComponentArray();
    }

    /**
     * Create a new instance of a ColorStyleAttribute with the specified name.
     *
     * @param name The name of this ColorStyleAttribute.
     * @param description The description of this ColorStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this ColorStyleAttribute is null.
     */
    public ColorStyleAttribute(String name, String description) throws IllegalArgumentException {
        super(name, description);
        components = new ColorComponentArray();
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
        super(name, null);
        components = new ColorComponentArray(red, green, blue);
    }

    /**
     * Create a new instance of a ColorStyleAttribute with the specified name and color attributes.
     * The alpha value will be set to 1.0.
     *
     * @param name The name of this ColorStyleAttribute.
     * @param description The description of this 
     * @param red The red component value which should be from 0.0 to 1.0.
     * @param green The green component value which should be from 0.0 to 1.0.
     * @param blue The blue component value which should be from 0.0 to 1.0.
     * @throws IllegalArgumentException If the specified name of this ColorStyleAttribute is null or
     *                                  the red, green or blue value is outside the permitted range from 0.0 to 1.0.
     */
    public ColorStyleAttribute(String name, String description, float red, float green, float blue) throws IllegalArgumentException {
        super(name, description);
        components = new ColorComponentArray(red, green, blue);
    }

    /**
     * Create a new instance of a ColorStyleAttribute with the specified name and color attributes.
     *
     * @param name The name of this ColorStyleAttribute.
     * @param description The description of this
     * @param red The red component value which should be from 0.0 to 1.0.
     * @param green The green component value which should be from 0.0 to 1.0.
     * @param blue The blue component value which should be from 0.0 to 1.0.
     * @param alpha The blue component value which should be from 0.0 to 1.0.
     * @throws IllegalArgumentException If the specified name of this ColorStyleAttribute is null or
     *                                  the red, green, blue or alpha value is outside the permitted range from 0.0 to 1.0.
     */
    public ColorStyleAttribute(String name, float red, float green, float blue, float alpha) throws IllegalArgumentException {
        super(name, null);
        components = new ColorComponentArray(red, green, blue, alpha);
    }

    /**
     * Create a new instance of a ColorStyleAttribute with the specified name and color attributes.
     *
     * @param name The name of this ColorStyleAttribute.
     * 
     * @param red The red component value which should be from 0.0 to 1.0.
     * @param green The green component value which should be from 0.0 to 1.0.
     * @param blue The blue component value which should be from 0.0 to 1.0.
     * @param alpha The blue component value which should be from 0.0 to 1.0.
     * @throws IllegalArgumentException If the specified name of this ColorStyleAttribute is null or
     *                                  the red, green, blue or alpha value is outside the permitted range from 0.0 to 1.0.
     */
    public ColorStyleAttribute(String name, String description, float red, float green, float blue, float alpha) throws IllegalArgumentException {
        super(name, description);
        components = new ColorComponentArray(red, green, blue, alpha);
    }

    /**
     * Get the red portion of the color value.
     * Values should be between 0.0 and 1.0.
     *
     * @return The red portion of the color value.
     */
    @Override
    @XmlAttribute(name="red")
    public float getRed() {
        return components.getRed();
    }

    /**
     * Set the red portion of the color value.
     * Values should be between 0.0 and 1.0.
     *
     * @param red The red portion of the color value.
     * @throws IllegalArgumentException If the specified red value was outside the valid range from 0.0 to 1.0.
     */
    @Override
    public void setRed(float red) throws IllegalArgumentException {
        components.setRed(red);
    }

    /**
     * Get the green portion of the color value.
     * Values should be between 0.0 (none) and 1.0 (full).
     *
     * @return The green portion of the color value.
     */
    @Override
    @XmlAttribute(name="green")
    public float getGreen() {
        return components.getGreen();
    }

    /**
     * Set the green portion of the color value.
     * Values should be between 0.0 (none) and 1.0 (full).
     *
     * @param green The green portion of the color value.
     * @throws IllegalArgumentException If the specified green value was outside the valid range from 0.0 to 1.0.
     */
    @Override
    public void setGreen(float green) throws IllegalArgumentException {
        components.setGreen(green);
    }

    /**
     * Get the blue portion of the color value.
     * Values should be between 0.0 (none) and 1.0 (full).
     *
     * @return The blue portion of the color value.
     */
    @Override
    @XmlAttribute(name="blue")
    public float getBlue() {
        return components.getBlue();
    }

    /**
     * Set the blue portion of the color value.
     * Values should be between 0.0 (none) and 1.0 (full).
     *
     * @param blue The blue portion of the color value.
     * @throws IllegalArgumentException If the specified blue value was outside the valid range from 0.0 to 1.0.
     */
    @Override
    public void setBlue(float blue) throws IllegalArgumentException {
        components.setBlue(blue);
    }

    /**
     * Get the alpha portion of the color value.
     * Values should be between 0.0 (transparent) and 1.0 (opaque).
     *
     * @return The alpha portion of the color value.
     */
    @Override
    @XmlAttribute(name="alpha")
    public float getAlpha() {
        return components.getAlpha();
    }

    /**
     * Set the alpha portion of the color value.
     * Values should be between 0.0 (transparent) and 1.0 (opaque).
     *
     * @param alpha The alpha portion of the color value.
     * @throws IllegalArgumentException If the specified alpha value was outside the valid range from 0.0 to 1.0.
     */
    @Override
    public void setAlpha(float alpha) throws IllegalArgumentException {
        components.setAlpha(alpha);
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
    public void set(float red, float green, float blue) throws IllegalArgumentException {
        components.setRed(red);
        components.setGreen(green);
        components.setBlue(blue);
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
    public void set(float red, float green, float blue, float alpha) throws IllegalArgumentException {
        components.setRed(red);
        components.setGreen(green);
        components.setBlue(blue);
        components.setAlpha(alpha);
    }

    /**
     * Convert the color components to an array of RGBA float values.
     *
     * @return An array of four elements containing the red, green, blue
     *         and alpha values respectively.
     */
    public float[] toArray() {
        return new float[] { components.getRed(), components.getGreen(), components.getBlue(), components.getAlpha() };
    }

    /**
     * {@inheritDoc}
     */
    @XmlTransient
    @Override
    public boolean setFrom(StyleAttribute otherAttribute) {
        if (otherAttribute instanceof ColorStyleAttribute) {
            ColorStyleAttribute otherColorAttribute = (ColorStyleAttribute) otherAttribute;
            components.setRed(otherColorAttribute.getRed());
            components.setGreen(otherColorAttribute.getGreen());
            components.setBlue(otherColorAttribute.getBlue());
            components.setAlpha(otherColorAttribute.getAlpha());
            return true;
        }
        return false;
    }

    /**
     * Create a copy of this ColorStyleAttribute.
     *
     * @return A copy of this ColorStyleAttribute.
     */
    @Override
    public StyleAttribute clone() {
        return new ColorStyleAttribute(getName(), getDescription(), components.getRed(), components.getGreen(), components.getBlue(), components.getAlpha());
    }

    @Override
    public ColorStyleAttribute listeningWrapper(StyleAttributeChangeListener listener) {
        return listener != null ? new ListeningColorStyleAttribute(this, listener) : this;
    }



    /**
     * Get a String representation of the Color attribute.
     *
     * @return A String representation of the color attribute.
     */
    @Override
    public String toString() {
        return String.format("%s [red: %f, green: %f, blue: %f, alpha: %f]", getName(), components.getRed(), components.getGreen(), components.getBlue(), components.getAlpha());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ColorStyleAttribute) {
            ColorStyleAttribute attribute = (ColorStyleAttribute) obj;
            return equal(attribute.getName(), getName())
                      && equal(attribute.getDescription(), getDescription())
                      && attribute.getRed() == components.getRed()
                      && attribute.getGreen() == components.getGreen()
                      && attribute.getBlue() == components.getBlue()
                      && attribute.getAlpha() == components.getAlpha();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        String name = getName();
        return name != null ? name.hashCode() : -1;
    }

    /**
     * This is a private internal class which is used to create a listening wrapper around a ColorStyleAttribute;
     */
    private static class ListeningColorStyleAttribute extends ColorStyleAttribute {

        private final StyleAttributeChangeListener changeListener;

        /**
         * Create a ListeningColorStyleAttribute to wrap the specified ColorStyleAttribute.
         *
         * @param wrappedAttribute The ColorStyleAttribute to be wrapped by this ColorStyleAttribute.
         */
        public ListeningColorStyleAttribute(ColorStyleAttribute wrappedAttribute, StyleAttributeChangeListener changeListener) {
            super(wrappedAttribute, wrappedAttribute, wrappedAttribute);
            this.changeListener = changeListener;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setRed(float red) throws IllegalArgumentException {
            if (red != components.getRed()) {
                components.setRed(red);
                changeListener.attributeChanged(this);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setGreen(float green) throws IllegalArgumentException {
            if (green != components.getGreen()) {
                components.setGreen(green);
                changeListener.attributeChanged(this);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setBlue(float blue) throws IllegalArgumentException {
            if (blue != components.getBlue()) {
                components.setBlue(blue);
                changeListener.attributeChanged(this);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setAlpha(float alpha) throws IllegalArgumentException {
            if (alpha != components.getAlpha()) {
                components.setAlpha(alpha);
                changeListener.attributeChanged(this);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void set(float red, float green, float blue) throws IllegalArgumentException {
            if (red != components.getRed() || green != components.getGreen() || blue != components.getBlue()) {
                components.setRed(red);
                components.setGreen(green);
                components.setBlue(blue);
                changeListener.attributeChanged(this);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void set(float red, float green, float blue, float alpha) throws IllegalArgumentException {
            if (red != components.getRed() || green != components.getGreen() || blue != components.getBlue() || alpha != components.getAlpha()) {
                components.setRed(red);
                components.setGreen(green);
                components.setBlue(blue);
                components.setAlpha(alpha);
                changeListener.attributeChanged(this);
            }
        }
    }
}
