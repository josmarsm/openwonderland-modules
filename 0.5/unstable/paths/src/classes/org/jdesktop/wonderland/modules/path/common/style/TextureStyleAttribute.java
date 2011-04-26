package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This class represents a StyleAttribute which is a texture uri value.
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="texture-attribute")
public class TextureStyleAttribute extends StyleAttribute implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    @XmlTransient
    private String uri;

    /**
     * A no-argument constructor for the benefit of JAXB.
     */
    protected TextureStyleAttribute() {}

    /**
     * Create a new instance of a TextureStyleAttribute with the specified name.
     *
     * @param name The name of this TextureStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this StyleAttribute is null.
     */
    public TextureStyleAttribute(String name) throws IllegalArgumentException {
        super(name);
    }

    /**
     * Create a new instance of a TextureStyleAttribute with the specified name.
     *
     * @param name The name of this TextureStyleAttribute.
     * @param uri The URI of the texture for this TextureStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this StyleAttribute is null.
     */
    public TextureStyleAttribute(String name, String uri) throws IllegalArgumentException {
        super(name);
        this.uri = uri;
    }

    /**
     * Get the texture URI of this TextureStyleAttribute.
     *
     * @return The texture URI of this TextureStyleAttribute.
     */
    @XmlElement(name="value")
    public String getURI() {
        return uri;
    }

    /**
     * Set the texture URI of this TextStyleAttribute.
     *
     * @param text The texture URI of this TextStyleAttribute.
     */
    public void setURI(String uri) {
        this.uri = uri;
    }
}
