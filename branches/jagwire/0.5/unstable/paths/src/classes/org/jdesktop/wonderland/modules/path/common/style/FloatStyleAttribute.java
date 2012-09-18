package org.jdesktop.wonderland.modules.path.common.style;

import org.jdesktop.wonderland.modules.path.common.FloatValueRange;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.modules.path.common.Described;
import org.jdesktop.wonderland.modules.path.common.FloatValue;
import org.jdesktop.wonderland.modules.path.common.MutableFloatValue;
import org.jdesktop.wonderland.modules.path.common.Named;
import org.jdesktop.wonderland.modules.path.common.SimpleFloatValue;

/**
 * This is a simple FloatStyleAttribute which represents an attribute of style meta-data.
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="float-attribute")
public class FloatStyleAttribute extends StyleAttribute implements MutableFloatValue, Serializable {

    /**
     * Compare the the two FloatValueRanges for equality. This method is designed
     * to handle null values.
     * 
     * @param range1 The first FloatValueRange to be compared.
     * @param range2 The second FloatValueRange to be compared.
     * @return True if the two FloatValueRanges are considered equal.
     */
    public static boolean equal(FloatValueRange range1, FloatValueRange range2) {
        return range1 != null ? range1.equals(range2) : range2 == null;
    }

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    @XmlTransient
    private final FloatValue value;

    /**
     * No argument constructor for the benefit of JAXB.
     */
    protected FloatStyleAttribute() {
        value = new SimpleFloatValue();
    }

    /**
     * Constructor used for creating wrapper subclasses.
     * 
     * @param name An object holding the name of this StyleAttribute.
     * @param description An object holding the description of this StyleAttribute.
     * @param value An object holding the value of this StyleAttribute.
     */
    protected FloatStyleAttribute(Named name, Described description, FloatValue value) {
        super(name, description);
        this.value = value;
    }

    /**
     * Create a new instance of a FloatStyleAttribute with the specified name.
     * 
     * @param name The name of this FloatStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this FloatStyleAttribute is null.
     */
    public FloatStyleAttribute(String name) throws IllegalArgumentException {
        super(name, null);
        value = new SimpleFloatValue();
    }

    /**
     * Create a new instance of a FloatStyleAttribute with the specified name.
     *
     * @param name The name of this FloatStyleAttribute.
     * @param description The description of this FloatStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this FloatStyleAttribute is null.
     */
    public FloatStyleAttribute(String name, String description) throws IllegalArgumentException {
        super(name, description);
        value = new SimpleFloatValue();
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
        super(name, null);
        this.value = new SimpleFloatValue(value, permittedRange);
    }

    /**
     * Create a new instance of a FloatStyleAttribute with the specified name and value.
     *
     * @param name The name of this FloatStyleAttribute.
     * @param description The description of this FloatStyleAttribute.
     * @param permittedRange The permitted range of values for this FloatStyleAttribute or null if the value is unconstrained.
     * @param value The value of this FloatValueRange.
     * @throws IllegalArgumentException If the specified name of this FloatStyleAttribute is null or the specified value is
     *                                  outside the supported range.
     */
    public FloatStyleAttribute(String name, String description, FloatValueRange permittedRange, float value) throws IllegalArgumentException {
        super(name, description);
        this.value = new SimpleFloatValue(value, permittedRange);
    }

    /**
     * Get the permitted range for the value set in this FloatStyleAttribute.
     *
     * @return The FloatValueRange which represents the permitted range for this
     *         FloatStyleAttribute or null if the range is not constrained.
     */
    @Override
    @XmlElement(name="constraint")
    public FloatValueRange getConstraint() {
        return value instanceof MutableFloatValue ? ((MutableFloatValue) value).getConstraint() : null;
    }

    /**
     * Set the permitted range for the value set in this FloatStyleAttribute.
     *
     * @param permittedRange The FloatValueRange of values which are valid for
     *                       this FloatStyleAttribute. If this value is null
     *                       then the value is unconstrained.
     */
    @Override
    public void setConstraint(FloatValueRange permittedRange) {
        if (value instanceof MutableFloatValue) {
            ((MutableFloatValue) value).setConstraint(permittedRange);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConstrained() {
        return value instanceof MutableFloatValue && ((MutableFloatValue) value).isConstrained();
    }

    /**
     * Get the value of this FloatStyleAttribute.
     *
     * @return The float value of this FloatStyleAttribute.
     */
    @Override
    @XmlElement(name="value")
    public float getFloat() {
        return value != null ? value.getFloat() : 0.0f;
    }

    /**
     * Set the value of this FloatStyleAttribute.
     *
     * @param value The value of this FloatStyleAttribute.
     * @throws IllegalArgumentException If a permitted range is set and the specified
     *                                  value is outside of the permitted range.
     *
     */
    @Override
    public void setFloat(float value) throws IllegalArgumentException {
        if (this.value instanceof MutableFloatValue) {
            ((MutableFloatValue) this.value).setFloat(value);
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
        return value instanceof MutableFloatValue && ((MutableFloatValue) value).isConstrained();
    }

    /**
     * {@inheritDoc}
     */
    @XmlTransient
    @Override
    public boolean setFrom(StyleAttribute otherAttribute) {
        if (otherAttribute instanceof FloatStyleAttribute && value instanceof MutableFloatValue) {
            FloatStyleAttribute otherFloatAttribute = (FloatStyleAttribute) otherAttribute;
            MutableFloatValue mutableValue = (MutableFloatValue) value;
            mutableValue.setFloat(otherFloatAttribute.getFloat());
            mutableValue.setConstraint(otherFloatAttribute.getConstraint());
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FloatStyleAttribute) {
            FloatStyleAttribute attribute = (FloatStyleAttribute) obj;
            return equal(attribute.getName(), getName()) && equal(attribute.getDescription(), getDescription()) && attribute.getFloat() == getFloat() && equal(attribute.getConstraint(), getConstraint());
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
     * Create a String representation of the FloatStyleAttribute.
     *
     * @return A String representation of the FloatStyleAttribute.
     */
    @Override
    public String toString() {
        return String.format("%s: %f", getName(), value);
    }

    /**
     * Create a copy of this FloatStyleAttribute.
     *
     * @return A copy of this FloatStyleAttribute.
     */
    @Override
    public StyleAttribute clone() {
        return new FloatStyleAttribute(getName(), getDescription(), getConstraint(), getFloat());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FloatStyleAttribute listeningWrapper(StyleAttributeChangeListener listener) {
        return listener != null ? new ListeningFloatStyleAttribute(this, listener) : this;
    }

    /**
     * This class is a wrapper around a FloatStyleAttribute. It adds notifications to a
     * listener whenever that FloatStyleAttribute is changed.
     */
    private static class ListeningFloatStyleAttribute extends FloatStyleAttribute {

        private final StyleAttributeChangeListener listener;

        /**
         * Create a new ListeningFloatStyleAttribute to wrap the specified FloatStyleAttribute and notify the specified
         * StyleAttributeChangeListener whenever the wrapped attribute is changed.
         *
         * @param wrappedAttribute The FloatStyleAttribute which is going to be wrapped by this ListeningFloatStyleAttribute.
         * @param listener The StyleAttributeChangeListener which will be notified when the FloatStyleAttribute changes.
         */
        public ListeningFloatStyleAttribute(FloatStyleAttribute wrappedAttribute, StyleAttributeChangeListener listener) {
            super(wrappedAttribute, wrappedAttribute, wrappedAttribute);
            this.listener = listener;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setFloat(float value) throws IllegalArgumentException {
            if (value != getFloat()) {
                super.setFloat(value);
                listener.attributeChanged(this);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setConstraint(FloatValueRange permittedRange) {
            if (!equal(permittedRange, getConstraint())) {
                super.setConstraint(permittedRange);
                listener.attributeChanged(this);
            }
        }
    }
}
