package org.jdesktop.wonderland.modules.path.common;

import java.io.Serializable;

/**
 * This class represents a simple FloatValue implementation.
 *
 * @author Carl Jokl
 */
public class SimpleFloatValue implements MutableFloatValue, Comparable<FloatValue>, Cloneable, Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private float value;
    private FloatValueRange constraint;

    /**
     * Create a new unconstrained SimpleFloatValue which has a default zero value.
     */
    public SimpleFloatValue() {
        value = 0.0f;
        constraint = null;
    }

    /**
     * Create a new unconstrained SimpleFloatValue with the specified float value to be set within it.
     *
     * @param value The primitive float value to be set within this FloatValue.
     */
    public SimpleFloatValue(float value) {
        this.value = value;
        constraint = null;
    }

    /**
     * Create a new SimpleFloatValue with the specified float value and FloatValueRange constraint.
     *
     * @param value The primitive float value which this FloatValue is to have.
     * @param constraint The FloatValueRange constraint which this FloatValue is to have.
     * @throws IllegalArgumentException If the specified primitive float value is outside the range permitted
     *                                  by the specified FloatValueRange constraint.
     */
    public SimpleFloatValue(float value, FloatValueRange constraint) throws IllegalArgumentException {
        this.constraint = constraint;
        setFloat(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setFloat(float value) throws IllegalArgumentException {
        if (constraint == null || constraint.isInRange(value)) {
            this.value = value;
        }
        else {
            throw new IllegalArgumentException(String.format("The value: %f is outside the permitted range: %s!", value, constraint.toString()));
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
    public FloatValueRange getConstraint() {
        return constraint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConstraint(FloatValueRange constraint) throws IllegalArgumentException {
        if (constraint == null || constraint.isInRange(value)) {
            this.constraint = constraint;
        }
        else {
            throw new IllegalArgumentException(String.format("The current value: %f is outside the supported range of the supplied constraint: %s!", value, constraint.toString()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFloat() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(FloatValue otherFloatValue) {
        if (otherFloatValue != null) {
            float other = otherFloatValue.getFloat();
            return other > value ? 1 : (other < value ? -1 : 0);
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new SimpleFloatValue(value, constraint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimpleFloatValue) {
            SimpleFloatValue otherValue = (SimpleFloatValue) obj;
            return (otherValue.getFloat() == value) && (constraint != null ? constraint.equals(otherValue.getConstraint()) : !otherValue.isConstrained());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (int) value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Float.toString(value);
    }
}
