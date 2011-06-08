
package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;
import java.net.URI;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.modules.path.common.Described;
import org.jdesktop.wonderland.modules.path.common.MutableURIValue;
import org.jdesktop.wonderland.modules.path.common.Named;
import org.jdesktop.wonderland.modules.path.common.SimpleURIValue;
import org.jdesktop.wonderland.modules.path.common.URIValue;

/**
 * This class represents a StyleAttribute which is a texture uri value.
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="texture-attribute")
public class TextureStyleAttribute extends StyleAttribute implements MutableURIValue, Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Compare the two URI values (taking into account that the values could be null).
     *
     * @param uri1 The first URI value to be compared (which might be null).
     * @param uri2 The second URI value to be compared (which might be null).
     * @return True if the two URIs are considered equal.
     */
    public static boolean equal(URI uri1, URI uri2) {
        return uri1 != null ? uri1.equals(uri2) : uri2 == null;
    }

    @XmlTransient
    private final URIValue value;

    /**
     * A no-argument constructor for the benefit of JAXB.
     */
    protected TextureStyleAttribute() {
        value = new SimpleURIValue();
    }

    /**
     * Protected constructor for use by derived wrapper classes which need to wrap the
     * components of a TextureStyleAttribute.
     *
     * @param name A Named object containing the name of this TextureStyleAttribute.
     * @param description A Described object containing the description of this TextureStyleAttribute.
     * @param value A URIValue object containing the URI value for this TextureStyleAttribute.
     */
    protected TextureStyleAttribute(Named name, Described description, URIValue value) {
        super(name, description);
        this.value = value;
    }

    /**
     * Create a new instance of a TextureStyleAttribute with the specified name.
     *
     * @param name The name of this TextureStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this StyleAttribute is null.
     */
    public TextureStyleAttribute(String name) throws IllegalArgumentException {
        super(name, null);
        value = new SimpleURIValue();
    }

    /**
     * Create a new instance of a TextureStyleAttribute with the specified name.
     *
     * @param name The name of this TextureStyleAttribute.
     * @param description The description of this TextureStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this StyleAttribute is null.
     */
    public TextureStyleAttribute(String name, String description) throws IllegalArgumentException {
        super(name, description);
        value = new SimpleURIValue();
    }

    /**
     * Create a new instance of a TextureStyleAttribute with the specified name.
     *
     * @param name The name of this TextureStyleAttribute.
     * @param uri The URI of the texture for this TextureStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this StyleAttribute is null.
     */
    public TextureStyleAttribute(String name, URI uri) throws IllegalArgumentException {
        super(name, null);
        value = new SimpleURIValue(uri);
    }

    /**
     * Create a new instance of a TextureStyleAttribute with the specified name.
     *
     * @param name The name of this TextureStyleAttribute.
     * @param description The description of this TextureStyleAttribute.
     * @param uri The URI of the texture for this TextureStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this StyleAttribute is null.
     */
    public TextureStyleAttribute(String name, String description, URI uri) throws IllegalArgumentException {
        super(name, description);
        value = new SimpleURIValue(uri);
    }

    /**
     * Get the texture URI of this TextureStyleAttribute.
     *
     * @return The texture URI of this TextureStyleAttribute.
     */
    @Override
    @XmlElement(name="value")
    public URI getURI() {
        return value != null ? value.getURI() : null;
    }

    /**
     * Set the texture URI of this TextStyleAttribute.
     *
     * @param text The texture URI of this TextStyleAttribute.
     */
    @Override
    public void setURI(URI uri) {
        if (value instanceof MutableURIValue) {
            ((MutableURIValue) value).setURI(uri);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlTransient
    public boolean isURISet() {
        return value != null && value.isURISet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setFrom(StyleAttribute otherAttribute) {
        if (otherAttribute instanceof TextureStyleAttribute && value instanceof MutableURIValue) {
            TextureStyleAttribute otherTextureAttribute = (TextureStyleAttribute) otherAttribute;
            ((MutableURIValue) value).setURI(otherTextureAttribute.getURI());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextureStyleAttribute) {
            TextureStyleAttribute attribute = (TextureStyleAttribute) obj;
            return equal(attribute.getName(), getName()) && equal(attribute.getDescription(), getDescription()) && equal(attribute.getURI(), getURI());
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
     * Create a String representation of the TextStyleAttribute.
     *
     * @return A String representation of the TextStyleAttribute.
     */
    @Override
    public String toString() {
        return String.format("%s: (uri) %s", getName(), value != null ? value.toString() : "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StyleAttribute clone() {
        return new TextureStyleAttribute(getName(), getDescription(), getURI());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StyleAttribute listeningWrapper(StyleAttributeChangeListener listener) {
        return listener != null ? new ListeningTextureStyleAttribute(this, listener) : this;
    }

    /**
     * This class is a wrapper around a TextureStyleAttribute that informs a StyleAttributeChangleListener whenever the
     * TextureStyleAttribute changes.
     */
    private static class ListeningTextureStyleAttribute extends TextureStyleAttribute {

        private final StyleAttributeChangeListener listener;

        /**
         * Create a new instance of a ListeningTextureStyleAttribute to wrap the specified TextureStyleAttribute and send
         * notifications to the specified StyleAttributeChangeListener when the TextureStyleAttribute changes.
         *
         * @param wrappedAttribute The TextureStyleAttribute
         * @param listener
         */
        public ListeningTextureStyleAttribute(TextureStyleAttribute wrappedAttribute, StyleAttributeChangeListener listener) {
            super(wrappedAttribute, wrappedAttribute, wrappedAttribute);
            this.listener = listener;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setURI(URI uri) {
            if (!equal(uri, getURI())) {
                super.setURI(uri);
                listener.attributeChanged(this);
            }
        }
    }
}
