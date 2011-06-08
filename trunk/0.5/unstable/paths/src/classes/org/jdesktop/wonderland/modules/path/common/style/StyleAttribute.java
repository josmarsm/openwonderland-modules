package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.modules.path.common.Described;
import org.jdesktop.wonderland.modules.path.common.MutableDescribed;
import org.jdesktop.wonderland.modules.path.common.MutableNamed;
import org.jdesktop.wonderland.modules.path.common.Named;
import org.jdesktop.wonderland.modules.path.common.SimpleDescription;
import org.jdesktop.wonderland.modules.path.common.SimpleName;

/**
 * This is an abstract base class for attribute of styles.
 * The concrete implementations will be tied to specific data types.
 *
 * @author Carl Jokl
 */
public abstract class StyleAttribute implements Named, MutableDescribed, Cloneable, Comparable<StyleAttribute>, Serializable {

    /**
     * Check whether the two objects are equal protecting against null values.
     *
     * @param object1 The first object to compare.
     * @param object2 The second object to compare.
     * @return True if the two objects are considered equal or false otherwise.
     */
    protected static boolean equal(Object object1, String object2) {
        return object1 != null ? object1.equals(object2) : object2 == null;
    }

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    @XmlTransient
    private final Named name;
    @XmlTransient
    private final Described description;

    /**
     * No argument constructor for use with
     * sub classes which also have no argument
     * constructors. The attribute name must
     * be set separately.
     */
    protected StyleAttribute() {
        name = new SimpleName();
        description = new SimpleDescription();
    }

    /**
     * A constructor for use when the name and description of the StyleAttribute is
     * intended to be known at the time of creation of an instance of this class.
     *
     * @param name The name of the StyleAttribute.
     * @param description The description of the StyleAttribute or null if the description
     *                    is not known or blank.
     * @throws IllegalArgumentException If the specified StyleAttribute name was null.
     */
    protected StyleAttribute(String name, String description) throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("The name of this style attribute cannot be null!");
        }
        this.name = new SimpleName(name);
        this.description = new SimpleDescription(description);
    }

    /**
     * This constructor is for use when creating StyleAttributes which act as wrappers
     * around other StyleAttributes such that the Name and Description can be taken
     * indirectly from somewhere else.
     *
     * @param name The name of the StyleAttribute.
     * @param description The description of the StyleAttribute.
     */
    protected StyleAttribute(Named name, Described description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Get the name of this style attribute.
     *
     * @return The name of this style attribute.
     */
    @Override
    @XmlAttribute(name="name")
    public String getName() {
        return name != null ? name.getName() : null;
    }

    /**
     * Protected attribute setter for the sake of JAXB.
     *
     * @param name The name to be set via JAXB de-serialization.
     */
    protected void setName(String name) {
        if (this.name instanceof MutableNamed) {
            ((MutableNamed) this.name).setName(name);
        }
    }

    /**
     * Get the optional description of what this StyleAttribute represents.
     *
     * @return A user readable description of what this StyleAttribute represents.
     */
    @Override
    @XmlAttribute(name="description")
    public String getDescription() {
        return description != null ? description.getDescription() : null;
    }

    /**
     * Set an optional user readable description of what this StyleAttribute represents.
     *
     * @param description A user readable description of what this StyleAttribute represents.
     */
    @Override
    public void setDescription(String description) {
        if (this.description instanceof MutableDescribed) {
            ((MutableDescribed) this.description).setDescription(description);
        }
    }

    /**
     * Get whether the description of this StyleAttribute is set.
     *
     * @return True if the description of this StyleAttribute is set. False otherwise.
     */
    @Override
    public boolean isDescriptionSet() {
        return description != null && description.getDescription() != null;
    }

    /**
     * Create a clone of this StyleAttribute. Any concrete subclasses must implement this method.
     *
     * @return A clone of the StyleAttribute.
     */
    @Override
    public abstract StyleAttribute clone();

    /**
     * Compare this StyleAttribute against another StyleAttribute for ordering.
     * ordering.
     *
     * @param styleAttribute The other StyleAttribute with which this StyleAttribute will be compared.
     * @return Positive if the specified StyleAttribute object is greater than this object.
     *         Negative if the specified StyleAttribute object is less that this object.
     *         Zero if the specified StyleAttribute object has the same description as this object.
     */
    @Override
    public int compareTo(StyleAttribute styleAttribute) {
        if (styleAttribute == null) {
            return 1;
        }
        else if (name == null || name.getName() == null) {
            return -1;
        }
        else {
            String otherName = styleAttribute.getName();
            if (otherName == null) {
                return 1;
            }
            else {
                return name.getName().compareTo(otherName);
            }
        }
    }


    /**
     * Set the value of this StyleAttribute using the specified other StyleAttribute.
     * This method does not inform of a change to the value of the attribute. The reason
     * for not informing of the change is that this method is intended for use in response
     * to an event elsewhere where the value was changed. Firing a notification that the value
     * changed could result in a never ending cycle of notifications.
     *
     * @param otherAttribute The other StyleAttribute from which to set this StyleAttribute.
     * @return True if the specified other attribute was not null and was the same type of StyleAttribute
     *         and the value of this StyleAttribute was able to be set from the specified StyleAttribute successfully.
     */
    @XmlTransient
    public abstract boolean setFrom(StyleAttribute otherAttribute);

    /**
     * Create a listening wrapper around this StyleAttribute which informs of changes
     * when the value is changed.
     *
     * @param listener The ValueChangeListener which will listen for changes being made to the value of this StyleAttribute.
     * @return A listening wrapper which listens for changes being made to the the value of the specified StyleAttribute.
     */
    public abstract StyleAttribute listeningWrapper(StyleAttributeChangeListener listener);
}
