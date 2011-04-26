package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This is an abstract base class for ItemStyles which
 * implements some common functionality.
 *
 * @author Carl Jokl
 */
public abstract class AbstractItemStyle<T extends StyleType> implements ItemStyle<T>, Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private int span;
    @XmlElement(name="style-attributes")
    private List<StyleAttribute> attributes;
    @XmlTransient
    private Map<String, StyleAttribute> attributesByName;

    /**
     * Initialize this AbstractItemStyle with the default span of 1.
     */
    protected AbstractItemStyle() {
        span = 1;
        attributes = new ArrayList<StyleAttribute>();
        attributesByName = new HashMap<String, StyleAttribute>();
    }

    /**
     * Initialize this AbstractItemStyle to span the specified number of items.
     *
     * @param span The number of items which are to be spanned by this style which must be 1 or greater.
     * @throws IllegalArgumentException If the specified span is not greater than zero.
     */
    protected AbstractItemStyle(int span) throws IllegalArgumentException {
        this();
        if (span > 0) {
            this.span = span;
        }
        else {
            throw new IllegalArgumentException(String.format("The specified span %d is invalid because the span must be greater than zero!", span));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int span() {
        return span;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpan(int span) throws IllegalArgumentException {
        if (span > 0) {
            this.span = span;
        }
        else {
            throw new IllegalArgumentException(String.format("The specified span %d is invalid because the span must be greater than zero!", span));
        }
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
}
