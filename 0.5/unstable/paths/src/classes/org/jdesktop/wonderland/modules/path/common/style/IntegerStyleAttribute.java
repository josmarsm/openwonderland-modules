package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This is a simple IntegerStyleAttribute which represents an attribute of style meta-data.
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="int-attribute")
public class IntegerStyleAttribute extends StyleAttribute implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    @XmlTransient
    private int value;
    @XmlTransient
    private IntegerValueRange permittedRange;

    /**
     * No argument constructor for the benefit of JAXB.
     */
    protected IntegerStyleAttribute() { }

    /**
     * Create a new instance of a IntegerStyleAttribute with the specified name.
     * 
     * @param name The name of this IntegerStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this style attribute is null.
     */
    public IntegerStyleAttribute(String name) throws IllegalArgumentException {
        super(name);
    }

    /**
     * Create a new instance of a IntegerStyleAttribute with the specified name and value.
     *
     * @param name The name of this IntegerStyleAttribute.
     * @param permittedRange The permitted range for the value of this IntegerStyleAttribute or null if the range is unconstrained.
     * @param value The value of this IntegerStyleAttribute.
     * @throws IllegalArgumentException If the specified name of the attribute was null or the specified value was outside the permitted range.
     */
    public IntegerStyleAttribute(String name, IntegerValueRange permittedRange, int value) throws IllegalArgumentException {
        super(name);
        this.permittedRange = permittedRange;
        setValue(value);
    }

    /**
     * Get the permitted range for the value set in this IntegerStyleAttribute.
     *
     * @return The IntegerValueRange which represents the permitted range for this
     *         IntegerStyleAttribute or null if the range is not constrained.
     */
    @XmlElement(name="permitted-range")
    public IntegerValueRange getPermittedRange() {
        return permittedRange;
    }

    /**
     * Set the permitted range for the value set in this IntegerStyleAttribute.
     *
     * @param permittedRange The IntegerValueRange of values which are valid for
     *                       this IntegerStyleAttribute. If this value is null
     *                       then the value is unconstrained.
     */
    public void setPermittedRange(IntegerValueRange permittedRange) {
        this.permittedRange = permittedRange;
    }

    /**
     * Get the value of this IntegerStyleAttribute.
     *
     * @return The float value of this IntegerStyleAttribute.
     */
    @XmlElement(name="value")
    public int getValue() {
        return value;
    }

    /**
     * Set the value of this IntegerStyleAttribute.
     *
     * @param value The value of this IntegerStyleAttribute.
     * @throws IllegalArgumentException If a permitted range is set and the specified
     *                                  value is outside of the permitted range.
     *
     */
    public final void setValue(int value) throws IllegalArgumentException {
        if (permittedRange == null || permittedRange.isInRange(value)) {
            this.value = value;
        }
        else {
            throw new IllegalArgumentException(String.format("The value: %f is outside the permitted range: %s!", value, permittedRange.toString()));
        }
    }

    /**
     * True if the range of this IntegerStyleAttribute is constrained.
     *
     * @return True if this IntegerStyleAttribute has a permitted range set.
     *         False if no permitted range is set and any value can be used.
     */
    @XmlTransient
    public boolean isRangeConstrained() {
        return permittedRange != null;
    }
}
