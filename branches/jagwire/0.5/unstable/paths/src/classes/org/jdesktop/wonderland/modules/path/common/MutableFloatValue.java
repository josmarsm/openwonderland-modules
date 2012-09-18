package org.jdesktop.wonderland.modules.path.common;

/**
 * This interface represents a FloatValue which can be modified.
 *
 * @author Carl Jokl
 */
public interface MutableFloatValue extends FloatValue {

    /**
     * Set the primitive internal value of this FloatValue.
     * 
     * @param value The primitive value to use for the value of this FloatValue.
     * @throws IllegalArgumentException If this FloatValue is constrained and the specified value is outside the constrained range.
     */
    public void setFloat(float value) throws IllegalArgumentException;

    /**
     * Whether the permitted value for the FloatValue is constrained within a range.
     *
     * @return True if the permitted value for the FloatValue is constrained.
     */
    public boolean isConstrained();

    /**
     * Get the FlotValueRange constraint for this FloatValue if any.
     *
     * @return The FloatValueRange constraint for this FloatValue or null if the value is unconstrained.
     */
    public FloatValueRange getConstraint();

    /**
     * Set the FloatValueRange constraint of this Float Value
     *
     * @param constraint The FloatValueRange constraint for this FloatValue.
     * @throws IllegalArgumentException If the specified constraint defines a value range for which the current value is not
     *                                  permitted.
     *
     */
    public void setConstraint(FloatValueRange constraint) throws IllegalArgumentException;
}
