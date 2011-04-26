package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This is an abstract base class for attribute of styles.
 * The concrete implementations will be tied to specific data types.
 *
 * @author Carl Jokl
 */
public abstract class StyleAttribute implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    @XmlTransient
    private String name;
    @XmlTransient
    private String description;

    /**
     * No argument constructor for use with
     * sub classes which also have no argument
     * constructors. The attribute name must
     * be set separately.
     */
    protected StyleAttribute() {}

    /**
     * A constructor for use when the name of
     * the StyleAttribute is intended to be know
     * at the time of creation of an instance
     * of this class.
     * 
     * @param name The name of she StyleAttribute.
     * @throws IllegalArgumentException
     */
    protected StyleAttribute(String name) throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("The name of this style attribute cannot be null!");
        }
        this.name = name;
    }

    /**
     * Get the name of this Float based style attribute.
     *
     * @return The name of this style attribute.
     */
    @XmlAttribute(name="name")
    public String getName() {
        return name;
    }

    /**
     * Set the name of this FloatStyleAttribute.
     *
     * @param name The name of this FloatStyleAttribute.
     * @throws IllegalArgumentException If the specified name is null.
     *                                  In spite of the fact that when this
     *                                  FloatStyleAttribute is created, the name
     *                                  may be null (as for compatibility with
     *                                  JAXB a default no-argument constructor is
     *                                  required) the name should never be left
     *                                  as null and so trying to set a null name
     *                                  causes an exception.
     *
     */
    public void setName(String name) throws IllegalArgumentException {
        this.name = name;
    }

    /**
     * Get the optional description of what this StyleAttribute represents.
     *
     * @return A user readable description of what this StyleAttribute represents.
     */
    @XmlAttribute(name="description")
    public String getDescription() {
        return description;
    }

    /**
     * Set an optional user readable description of what this StyleAttribute represents.
     *
     * @param description A user readable description of what this StyleAttribute represents.
     */
    public void setDecription(String description) {
        this.description = description;
    }
}
