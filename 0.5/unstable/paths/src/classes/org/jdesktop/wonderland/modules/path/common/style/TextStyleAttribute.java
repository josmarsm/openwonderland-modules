package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This class represents a StyleAttribute which is a text value.
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="text-attribute")
public class TextStyleAttribute extends StyleAttribute implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    @XmlTransient
    private String text;

    /**
     * A no-argument constructor for the benefit of JAXB.
     */
    protected TextStyleAttribute() {}

    /**
     * Create a new instance of a TextStyleAttribute with the specified name.
     *
     * @param name The name of this TextStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this StyleAttribute is null.
     */
    public TextStyleAttribute(String name) throws IllegalArgumentException {
        super(name);
    }

    /**
     * Create a new TextStyleAttribute with the specified name and text value.
     *
     * @param name The name of this TextStyleAttribute.
     * @param text The text value of this TextStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this StyleAttribute is null.
     */
    public TextStyleAttribute(String name, String text) throws IllegalArgumentException {
        super(name);
        this.text = text;
    }

    /**
     * Get the text of this TextStyleAttribute.
     *
     * @return The text of this TextStyleAttribute.
     */
    @XmlElement(name="value")
    public String getText() {
        return text;
    }

    /**
     * Set the text of this TextStyleAttribute.
     *
     * @param text The text of this TextStyleAttribute.
     */
    public void setText(String text) {
        this.text = text;
    }
}
