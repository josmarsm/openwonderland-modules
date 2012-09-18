package org.jdesktop.wonderland.modules.path.common;

/**
 * This interface represents an IntegerValue which can be modified.
 *
 * @author Carl Jokl
 */
public interface MutableIntegerValue extends IntegerValue {

    /**
     * Set the primitive value of this IntegerValue.
     *
     * @param value The primitive Value of this IntegerValue.
     * @throws IllegalArgumentException If the value of the IntegerValue is constrained and the specified value
     *                                  is outside of the supported range.
     */
    public void setInteger(int value) throws IllegalArgumentException;

    /**
     * Whether this IntegerValue has a constraint on the permitted value
     * range.
     *
     * @return True if this IntegerValue has a constraint on the permitted value range. False otherwise.
     */
    public boolean isConstrained();

    /**
     * Get the IntegerValueRange which constrains the supported value of this IntegerValue or null
     * if the IntegerValueRange is unconstrained.
     *
     * @return The IntegerValueRange which constrains this Integer value or null if the IntegerValue is unconstrained.
     */
    public IntegerValueRange getConstraint();

    /**
     * Set the IntegertValueRange constraint of this Float Value
     *
     * @param constraint The IntegerValueRange constraint for this IntegerValue.
     * @throws IllegalArgumentException If the specified constraint defines a value range for which the current value is not
     *                                  permitted.
     *                             
     */
    public void setConstraint(IntegerValueRange constraint) throws IllegalArgumentException;
}
