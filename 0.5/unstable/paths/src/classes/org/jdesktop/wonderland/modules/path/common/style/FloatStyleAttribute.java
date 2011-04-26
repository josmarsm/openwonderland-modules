package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This is a simple FloatStyleAttribute which represents an attribute of style meta-data.
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="float-attribute")
public class FloatStyleAttribute extends StyleAttribute implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    @XmlTransient
    private float value;
    @XmlTransient
    private FloatValueRange permittedRange;

    /**
     * No argument constructor for the benefit of JAXB.
     */
    protected FloatStyleAttribute() { }

    /**
     * Create a new instance of a FloatStyleAttribute with the specified name.
     * 
     * @param name The name of this FloatStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this FloatStyleAttribute is null.
     */
    public FloatStyleAttribute(String name) throws IllegalArgumentException {
        super(name);
    }

    /**
     * Create a new instance of a FloatStyleAttribute with the specified name and value.
     *
     * @param name The name of this FloatStyleAttribute.
     * @param permittedRange The permitted range of values for this FloatStyleAttribute or null if the value is unconstrained.
     * @param value The value of this FloatValueRange.
     * @throws IllegalArgumentException If the specified name of this FloatStyleAttribute is null or the specified value is
     *                                  outside the supported range.
     */
    public FloatStyleAttribute(String name, FloatValueRange permittedRange, float value) throws IllegalArgumentException {
        super(name);
        this.permittedRange = permittedRange;
        setValue(value);
    }

    /**
     * Get the permitted range for the value set in this FloatStyleAttribute.
     *
     * @return The FloatValueRange which represents the permitted range for this
     *         FloatStyleAttribute or null if the range is not constrained.
     */
    @XmlElement(name="permitted-range")
    public FloatValueRange getPermittedRange() {
        return permittedRange;
    }

    /**
     * Set the permitted range for the value set in this FloatStyleAttribute.
     *
     * @param permittedRange The FloatValueRange of values which are valid for
     *                       this FloatStyleAttribute. If this value is null
     *                       then the value is unconstrained.
     */
    public void setPermittedRange(FloatValueRange permittedRange) {
        this.permittedRange = permittedRange;
    }

    /**
     * Get the value of this FloatStyleAttribute.
     *
     * @return The float value of this FloatStyleAttribute.
     */
    @XmlElement(name="value")
    public float getValue() {
        return value;
    }

    /**
     * Set the value of this FloatStyleAttribute.
     *
     * @param value The value of this FloatStyleAttribute.
     * @throws IllegalArgumentException If a permitted range is set and the specified
     *                                  value is outside of the permitted range.
     *
     */
    public final void setValue(float value) throws IllegalArgumentException {
        if (permittedRange == null || permittedRange.isInRange(value)) {
            this.value = value;
        }
        else {
            throw new IllegalArgumentException(String.format("The value: %f is outside the permitted range: %s!", value, permittedRange.toString()));
        }
    }

    /**
     * True if the range of this FloatStyleAttribute is constrained.
     *
     * @return True if this FloatStyleAttribute has a permitted range set.
     *         False if no permitted range is set and any value can be used.
     */
    @XmlTransient
    public boolean isRangeConstrained() {
        return permittedRange != null;
    }
}
