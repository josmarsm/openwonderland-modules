package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This is an abstract base class for ItemStyles which
 * implements some common functionality.
 *
 * @author Carl Jokl
 */
public abstract class AbstractItemStyle<T extends StyleType> implements ItemStyle<T>, Cloneable, Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final Span span;
    @XmlElement(name="style-attributes")
    private List<StyleAttribute> attributes;
    @XmlTransient
    private Map<String, StyleAttribute> attributesByName;

    /**
     * Initialize this AbstractItemStyle with the default getSpan of 1.
     */
    protected AbstractItemStyle() {
        span = new Span();
        attributes = new ArrayList<StyleAttribute>();
        attributesByName = new HashMap<String, StyleAttribute>();
    }

    /**
     * Protected constructor used by derived classes which are intended to be used as wrappers around another ItemStyle.
     *
     * @param wrappedStyle The style to be wrapped by this AbstractItemStyle.
     */
    protected AbstractItemStyle(AbstractItemStyle<T> wrappedStyle) {
        if (wrappedStyle == null) {
            throw new IllegalArgumentException("The specified item style to be wrapped by this item style cannot be null!");
        }
        span = wrappedStyle.getInternalSpan();
        attributes = wrappedStyle.getInternalStyleAttributeList();
        attributesByName = wrappedStyle.getInternalStyleAttributeMap();
    }

    /**
     * Protected constructor for use in derived classes for cloning themselves.
     *
     * @param styleAttribues A list of StyleAttributes to be used in this ItemStyle. The list will have been cloned already.
     * @param getSpan The number of items to be spanned by this ItemStyle.
     * @throw IllegalArgumentException If the specified styleAttributes are null.
     */
    protected AbstractItemStyle(final List<StyleAttribute> styleAttributes, int span) {
        if (styleAttributes == null) {
            throw new IllegalArgumentException("The specified style attributes to use for cloning this item style cannot be null!");
        }
        this.span = new Span(span);
        attributes = styleAttributes;
        attributesByName = new HashMap<String, StyleAttribute>(styleAttributes.size());
        for (StyleAttribute attribute : styleAttributes) {
            attributesByName.put(attribute.getName(), attribute);
        }
    }

    /**
     * Initialize this AbstractItemStyle to getSpan the specified number of items.
     *
     * @param getSpan The number of items which are to be spanned by this style which must be 1 or greater.
     * @throws IllegalArgumentException If the specified getSpan is not greater than zero.
     */
    protected AbstractItemStyle(int span) throws IllegalArgumentException {
        this();
        this.span.setSpan(span);
    }

    /**
     * Get the internal list of StyleAttributes for this AbstractItemStyle. This is used for wrapper classes such as those which also send
     * update messages to the server when attributes are changed.
     *
     * @return The internal list representation of the StyleAttribute of this AbstractItemStyle.
     */
    @XmlTransient
    protected List<StyleAttribute> getInternalStyleAttributeList() {
        return attributes;
    }

    /**
     * Get the internal mapping of StyleAttribute names to StyleAttributes for this AbstractItemStyle. This is used for wrapper classes such
     * as those which also send update messages to the server when attributes are changed.
     *
     * @return The internal mapping of StyleAttribute names to StyleAttributes for this AbstractItemStyle.
     */
    @XmlTransient
    private Map<String, StyleAttribute> getInternalStyleAttributeMap() {
        return attributesByName;
    }

    /**
     * Get the internal Span object reference. This can be used for wrapper classes.
     *
     * @return A reference to the internal Span object.
     */
    @XmlTransient
    private Span getInternalSpan() {
        return span;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlAttribute(name="span")
    public int getSpan() {
        return span.getSpan();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpan(int span) throws IllegalArgumentException {
        this.span.setSpan(span);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int noOfAttributes() {
        return attributes.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StyleAttribute getStyleAttribute(int attributeIndex) throws IndexOutOfBoundsException {
        if (attributeIndex >= 0 && attributeIndex < attributes.size()) {
            return attributes.get(attributeIndex);
        }
        else {
            throw new IllegalArgumentException(String.format("The specified index: %d of the attribute to be retrieved was outside the valid range of attributes! No of attributes: %d.", attributeIndex, attributes.size()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StyleAttribute getStyleAttribute(String attributeName) {
        return attributeName != null ? attributesByName.get(attributeName) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addStyleAttribute(StyleAttribute styleAttribute) {
        if (styleAttribute != null) {
            String name = styleAttribute.getName();
            if (name != null) {
                if (!attributesByName.containsKey(name)) {
                    attributesByName.put(name, styleAttribute);
                    return attributes.add(styleAttribute);
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StyleAttribute replaceStyleAttribute(StyleAttribute styleAttribute) {
        if (styleAttribute != null) {
            String name = styleAttribute.getName();
            if (name != null) {
                StyleAttribute replacedAttribute = attributesByName.remove(name);
                if (replacedAttribute != null) {
                    attributes.remove(replacedAttribute);
                    attributes.add(styleAttribute);
                    attributesByName.put(name, styleAttribute);
                    return replacedAttribute;
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeStyleAttribute(StyleAttribute styleAttribute) {
        return styleAttribute != null && attributes.remove(styleAttribute);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StyleAttribute removeStyleAttribute(int attributeIndex) throws IndexOutOfBoundsException {
        if (attributeIndex >= 0 && attributeIndex < attributes.size()) {
            StyleAttribute removedAttribute = attributes.remove(attributeIndex);
            String name = removedAttribute.getName();
            if (name != null) {
                attributesByName.remove(name);
            }
            return removedAttribute;
        }
        else {
            throw new IllegalArgumentException(String.format("The specified index: %d of the attribute to be removed was outside the valid range of attributes! No of attributes: %d.", attributeIndex, attributes.size()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StyleAttribute removeStyleAttribute(String attributeName) {
        if (attributeName != null) {
            StyleAttribute removedAttribute = attributesByName.get(attributeName);
            if (removedAttribute != null) {
                attributes.remove(removedAttribute);
            }
            return removedAttribute;
        }
        return null;
    }

    /**
     * Get the index of the specified style attribute within the list of StyleAttributes.
     *
     * @param attribute The StyleAttribute for which to find the index in the list of attributes.
     * @return The index of the specified StyleAttribute in the list of StyleAttributes or -1 if
     *         the StyleAttribute was null or was not present in the list of StyleAttributes.
     */
    public int indexOf(StyleAttribute attribute) {
        if (attribute != null) {
            return attributes.indexOf(attribute);
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StyleAttribute[] getStyleAttributes() {
        return attributes.toArray(new StyleAttribute[attributes.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAllAttributes() {
        attributes.clear();
        attributesByName.clear();
    }

    /**
     * This method can be called after de-serializing such a from
     * JAXB to remap the attribute names to attributes. This will
     * clear out the internal map and re-populate it from the internal
     * list.
     */
    protected void remapAttributes() {
        attributesByName.clear();
        for (StyleAttribute attribute : attributes) {
            attributesByName.put(attribute.getName(), attribute);
        }
    }

    /**
     * This private class represents the getSpan of an ItemStyle. Using an object for this
     * means that the Span can be referenced from more than one object i.e. when using
     * a wrapper and when the getSpan is updated in either ItemStyle then both ItemStyles
     * will reflect the changed value.
     */
    private static class Span implements Serializable {

        /**
         * The version number for serialization.
         */
        private static final long serialVersionUID = 1L;

        private int span;

        /**
         * Create a new instance of a Span with the default value of 1.
         */
        public Span() {
            span = 1;
        }

        /**
         * Create a new instance of a Span with the specified number of items to getSpan.
         *
         * @param getSpan The number of items to Span.
         * @throws IllegalArgumentException If the number of items to getSpan is not greater than zero.
         */
        public Span(int span) throws IllegalArgumentException {
            setSpan(span);
        }

        /**
         * Get the number of items spanned.
         *
         * @return The number of items spanned.
         */
        public int getSpan() {
            return span;
        }

        /**
         * Set the number of items spanned.
         *
         * @param getSpan The number of items spanned which should be greater than zero.
         * @throws IllegalArgumentException If the specified getSpan is not greater than zero.
         */
        public final void setSpan(int span) throws IllegalArgumentException {
            if (span > 0) {
                this.span = span > 0 ? span : 1;
            }
            else {
                throw new IllegalArgumentException(String.format("The specified span %d is invalid because the span must be greater than zero!", span));
            }
        }
    }
}
