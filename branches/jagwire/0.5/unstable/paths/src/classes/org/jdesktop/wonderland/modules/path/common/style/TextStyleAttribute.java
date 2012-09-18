package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.modules.path.common.Described;
import org.jdesktop.wonderland.modules.path.common.MutableTextValue;
import org.jdesktop.wonderland.modules.path.common.Named;
import org.jdesktop.wonderland.modules.path.common.SimpleTextValue;
import org.jdesktop.wonderland.modules.path.common.TextValue;

/**
 * This class represents a StyleAttribute which is a text value.
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="text-attribute")
public class TextStyleAttribute extends StyleAttribute implements MutableTextValue, Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    @XmlTransient
    private final TextValue value;

    /**
     * A no-argument constructor for the benefit of JAXB.
     */
    protected TextStyleAttribute() {
        value = new SimpleTextValue();
    }

    /**
     * Constructor used by derived wrapper classes which wrap the components of a TextStyleAttribute.
     *
     * @param name The Named object from which to get the name of the TextStyleAttribute.
     * @param description The Described object from which to get the description of the TextStyleAttribute.
     * @param value The TextValue object from which to get the text value for this TextStyleAttribute.
     */
    protected TextStyleAttribute(Named name, Described description, TextValue value) {
        super(name, description);
        this.value = value;
    }

    /**
     * Create a new instance of a TextStyleAttribute with the specified name.
     *
     * @param name The name of this TextStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this StyleAttribute is null.
     */
    public TextStyleAttribute(String name) throws IllegalArgumentException {
        super(name, null);
        value = new TextStyleAttribute();
    }

    /**
     * Create a new TextStyleAttribute with the specified name and text value.
     *
     * @param name The name of this TextStyleAttribute.
     * @param text The text value of this TextStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this StyleAttribute is null.
     */
    public TextStyleAttribute(String name, String text) throws IllegalArgumentException {
        super(name, null);
        value = new SimpleTextValue(text);
    }

    /**
     * Create a new TextStyleAttribute with the specified name and text value.
     *
     * @param name The name of this TextStyleAttribute.
     * @param description The description of this TextStyleAttribute.
     * @param text The text value of this TextStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this StyleAttribute is null.
     */
    public TextStyleAttribute(String name, String description, String text) throws IllegalArgumentException {
        super(name, description);
        value = new SimpleTextValue(text);
    }

    /**
     * Get the text of this TextStyleAttribute.
     *
     * @return The text of this TextStyleAttribute.
     */
    @Override
    @XmlElement(name="value")
    public String getText() {
        return value != null ? value.getText() : null;
    }

    /**
     * Set the text of this TextStyleAttribute.
     *
     * @param text The text of this TextStyleAttribute.
     */
    @Override
    public void setText(String text) {
        if (value instanceof MutableTextValue) {
            ((MutableTextValue) value).setText(text);
        }
    }

    /**
     * {@inheritDoc}
     */
    @XmlTransient
    @Override
    public boolean isTextSet() {
        return value != null && value.isTextSet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setFrom(StyleAttribute otherAttribute) {
        if (otherAttribute instanceof TextStyleAttribute && value instanceof MutableTextValue) {
            MutableTextValue mutableText = (MutableTextValue) value;
            TextStyleAttribute otherTextAttribute = (TextStyleAttribute) otherAttribute;
            mutableText.setText(otherTextAttribute.getText());
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextStyleAttribute) {
            TextStyleAttribute attribute = (TextStyleAttribute) obj;
            return equal(attribute.getName(), getName()) && equal(attribute.getDescription(), getDescription()) && equal(attribute.getText(), getText());
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
        return String.format("%s: %s", getName(), getText());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StyleAttribute clone() {
        return new TextStyleAttribute(getName(), getDescription(), getText());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StyleAttribute listeningWrapper(StyleAttributeChangeListener listener) {
        return listener != null ? new ListeningTextStyleAttribute(this, listener) : this;
    }

    /**
     * This class is a wrapper around a TextStyleAttribute
     */
    private static class ListeningTextStyleAttribute extends TextStyleAttribute {

        private final StyleAttributeChangeListener listener;

        /**
         * Create a new ListeningTextStyleAttribute to wrap the specified TextStyleAttribute and relay messages to the specified
         * StyleAttributeeChangeListener when the TextStyleAttribute changes.
         *
         * @param wrappedAttribute The TextStyleAttribute to be wrapped by this ListeningTextStyleAttribute.
         * @param listener The StyleAttributeChangeListener to be notified when the TextStyleAttribute is changed.
         */
        public ListeningTextStyleAttribute(final TextStyleAttribute wrappedAttribute, final StyleAttributeChangeListener listener) {
            super(wrappedAttribute, wrappedAttribute, wrappedAttribute);
            this.listener = listener;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setText(String text) {
            if (!equal(text, getText())) {
                super.setText(text);
                listener.attributeChanged(this);
            }
        }
    }
}
