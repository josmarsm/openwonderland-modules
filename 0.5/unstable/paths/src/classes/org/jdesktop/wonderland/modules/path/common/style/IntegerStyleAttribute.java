package org.jdesktop.wonderland.modules.path.common.style;

import org.jdesktop.wonderland.modules.path.common.IntegerValueRange;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.modules.path.common.Described;
import org.jdesktop.wonderland.modules.path.common.IntegerValue;
import org.jdesktop.wonderland.modules.path.common.MutableIntegerValue;
import org.jdesktop.wonderland.modules.path.common.Named;
import org.jdesktop.wonderland.modules.path.common.SimpleIntegerValue;

/**
 * This is a simple IntegerStyleAttribute which represents an attribute of style meta-data.
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="int-attribute")
public class IntegerStyleAttribute extends StyleAttribute implements MutableIntegerValue, Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Compare the the two IntegerValueRanges for equality. This method is designed
     * to handle null values.
     *
     * @param range1 The first IntegerValueRange to be compared.
     * @param range2 The second IntegerValueRange to be compared.
     * @return True if the two IntegerValueRanges are considered equal.
     */
    public static boolean equal(IntegerValueRange range1, IntegerValueRange range2) {
        return range1 != null ? range1.equals(range2) : range2 == null;
    }

    @XmlTransient
    private final IntegerValue value;

    /**
     * No argument constructor for the benefit of JAXB.
     */
    protected IntegerStyleAttribute() {
        value = new SimpleIntegerValue();
    }

    /**
     * Constructor for use by child wrapper classes which wrap the components of a given
     * IntegerStyleAttribute.
     *
     * @param name The Named object from which to get the name of this IntegerStyleAttribute.
     * @param description The Described object from which to get the description of this IntegerStyleAttribute.
     * @param value The IntegerValue object which holds the value of this IntegerStyleAttribute.
     */
    protected IntegerStyleAttribute(Named name, Described description, IntegerValue value) {
        super(name, description);
        this.value = value;
    }

    /**
     * Create a new instance of a IntegerStyleAttribute with the specified name.
     * 
     * @param name The name of this IntegerStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this style attribute is null.
     */
    public IntegerStyleAttribute(String name) throws IllegalArgumentException {
        super(name, null);
        value = new SimpleIntegerValue();
    }

    /**
     * Create a new instance of a IntegerStyleAttribute with the specified name.
     *
     * @param name The name of this IntegerStyleAttribute.
     * @param description The description of this IntegerStyleAttribute.
     * @throws IllegalArgumentException If the specified name of this style attribute is null.
     */
    public IntegerStyleAttribute(String name, String description) throws IllegalArgumentException {
        super(name, description);
        value = new SimpleIntegerValue();
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
        super(name, null);
        this.value = new SimpleIntegerValue(value, permittedRange);
    }

    /**
     * Create a new instance of a IntegerStyleAttribute with the specified name and value.
     *
     * @param name The name of this IntegerStyleAttribute.
     * @param description The description of this IntegerStyleAttribute.
     * @param permittedRange The permitted range for the value of this IntegerStyleAttribute or null if the range is unconstrained.
     * @param value The value of this IntegerStyleAttribute.
     * @throws IllegalArgumentException If the specified name of the attribute was null or the specified value was outside the permitted range.
     */
    public IntegerStyleAttribute(String name, String description, IntegerValueRange permittedRange, int value) throws IllegalArgumentException {
        super(name, description);
        this.value = new SimpleIntegerValue(value, permittedRange);
    }

    /**
     * Get the permitted range for the value set in this IntegerStyleAttribute.
     *
     * @return The IntegerValueRange which represents the permitted range for this
     *         IntegerStyleAttribute or null if the range is not constrained.
     */
    @Override
    @XmlElement(name="constraint")
    public IntegerValueRange getConstraint() {
        return value instanceof MutableIntegerValue ? ((MutableIntegerValue) value).getConstraint() : null;
    }

    /**
     * Set the permitted range for the value set in this IntegerStyleAttribute.
     *
     * @param permittedRange The IntegerValueRange of values which are valid for
     *                       this IntegerStyleAttribute. If this value is null
     *                       then the value is unconstrained.
     */
    @Override
    public void setConstraint(IntegerValueRange permittedRange) {
        if (value instanceof MutableIntegerValue) {
            ((MutableIntegerValue) value).setConstraint(permittedRange);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConstrained() {
        return value instanceof MutableIntegerValue && ((MutableIntegerValue) value).isConstrained();
    }

    /**
     * Get the value of this IntegerStyleAttribute.
     *
     * @return The float value of this IntegerStyleAttribute.
     */
    @Override
    @XmlElement(name="value")
    public int getInteger() {
        return value != null ? value.getInteger() : 0;
    }

    /**
     * Set the value of this IntegerStyleAttribute.
     *
     * @param value The value of this IntegerStyleAttribute.
     * @throws IllegalArgumentException If a permitted range is set and the specified
     *                                  value is outside of the permitted range.
     *
     */
    @Override
    public void setInteger(int value) throws IllegalArgumentException {
        if (this.value instanceof MutableIntegerValue) {
            ((MutableIntegerValue) this.value).setInteger(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlTransient
    public boolean setFrom(StyleAttribute otherAttribute) {
        if (otherAttribute instanceof IntegerStyleAttribute && value instanceof MutableIntegerValue) {
            IntegerStyleAttribute otherIntegerAttribute = (IntegerStyleAttribute) otherAttribute;
            MutableIntegerValue mutableValue = (MutableIntegerValue) value;
            mutableValue.setInteger(otherIntegerAttribute.getInteger());
            mutableValue.setConstraint(otherIntegerAttribute.getConstraint());
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntegerStyleAttribute) {
            IntegerStyleAttribute attribute = (IntegerStyleAttribute) obj;
            return equal(attribute.getName(), getName()) && equal(attribute.getDescription(), getDescription()) && attribute.getInteger() == getInteger() && equal(attribute.getConstraint(), getConstraint());
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
        return String.format("%s: %d", getName(), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StyleAttribute clone() {
        return new IntegerStyleAttribute(getName(), getDescription(), getConstraint(), getInteger());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StyleAttribute listeningWrapper(StyleAttributeChangeListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * This is a wrapper class which wraps an IntegerStyleAttribute and notifies a listener
     * when the IntegerStyleAttribute changes.
     */
    private static class ListeningIntegerStyleAttribute extends IntegerStyleAttribute {

        private final StyleAttributeChangeListener listener;

        /**
         * Create a new instance of a ListeningIntegerStyleAttribute.
         *
         * @param wrappedAttribute The IntegerStyleAttribute to be wrapped by this IntegerStyleAttribute.
         * @param listener The StyleAttributeChangeListener to be notified when the wrapped IntegerStyleAttribute changes.
         */
        public ListeningIntegerStyleAttribute(IntegerStyleAttribute wrappedAttribute, StyleAttributeChangeListener listener) {
            super(wrappedAttribute, wrappedAttribute, wrappedAttribute);
            this.listener = listener;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setInteger(int value) throws IllegalArgumentException {
            if (value != getInteger()) {
                super.setInteger(value);
                listener.attributeChanged(this);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setConstraint(IntegerValueRange permittedRange) {
            if (!equal(permittedRange, getConstraint())) {
                super.setConstraint(permittedRange);
                listener.attributeChanged(this);
            }
        }
    }
}
