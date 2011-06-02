package org.jdesktop.wonderland.modules.path.common;

import java.io.Serializable;

/**
 * This class represents a simple IntegerValue implementation.
 *
 * @author Carl Jokl
 */
public class SimpleIntegerValue implements MutableIntegerValue, Comparable<IntegerValue>, Cloneable, Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private int value;
    private IntegerValueRange constraint;

    /**
     * Create a new unconstrained SimpleIntegerValue which has a default zero value.
     */
    public SimpleIntegerValue() {
        value = 0;
        constraint = null;
    }

    /**
     * Create a new unconstrained SimpleIntegerValue with the specified integer value to be set within it.
     *
     * @param value The primitive integer value to be set within this IntegerValue.
     */
    public SimpleIntegerValue(int value) {
        this.value = value;
        constraint = null;
    }

    /**
     * Create a new SimpleIntegerValue with the specified float value and IntegerValueRange constraint.
     *
     * @param value The primitive integer value which this IntegerValue is to have.
     * @param constraint The IntegerValueRange constraint which this IntegerValue is to have.
     * @throws IllegalArgumentException If the specified primitive integer value is outside the range permitted
     *                                  by the specified IntegerValueRange constraint.
     */
    public SimpleIntegerValue(int value, IntegerValueRange constraint) throws IllegalArgumentException {
        this.constraint = constraint;
        setInteger(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setInteger(int value) throws IllegalArgumentException {
        if (constraint == null || constraint.isInRange(value)) {
            this.value = value;
        }
        else {
            throw new IllegalArgumentException(String.format("The value: %d is outside the permitted range: %s!", value, constraint.toString()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConstrained() {
        return constraint != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IntegerValueRange getConstraint() {
        return constraint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConstraint(IntegerValueRange constraint) throws IllegalArgumentException {
        if (constraint == null || constraint.isInRange(value)) {
            this.constraint = constraint;
        }
        else {
            throw new IllegalArgumentException(String.format("The current value: %d is outside the supported range of the supplied constraint: %s!", value, constraint.toString()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInteger() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(IntegerValue otherIntegerValue) {
        if (otherIntegerValue != null) {
            int other = otherIntegerValue.getInteger();
            return other > value ? 1 : (other < value ? -1 : 0);
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new SimpleIntegerValue(value, constraint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimpleIntegerValue) {
            SimpleIntegerValue otherValue = (SimpleIntegerValue) obj;
            return (otherValue.getInteger() == value) && (constraint != null ? constraint.equals(otherValue.getConstraint()) : !otherValue.isConstrained());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
