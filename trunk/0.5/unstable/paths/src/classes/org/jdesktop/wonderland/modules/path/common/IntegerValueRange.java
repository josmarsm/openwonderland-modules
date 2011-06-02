package org.jdesktop.wonderland.modules.path.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class is used to represent a value range for an integer value comprising of a lower limit and upper limit for the range.
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="int-range")
public class IntegerValueRange implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The minimum value for the range.
     */
    @XmlAttribute(name="min")
    public int min;

    /**
     * The maximum value for the range.
     */
    @XmlAttribute(name="max")
    public int max;

    /**
     * Whether the minimum value range is inclusive of the minimum value itself i.e.
     * if true the minimum range is greater than or equal to the minimum value
     * or if false the minimum range is only greater than the minimum value.
     */
    @XmlAttribute(name="min-inclusive")
    public boolean minInclusive;

    /**
     * Whether the maximum value range is inclusive of the maximum value itself i.e.
     * if true the maximum range is less than or equal the the maximum value
     * or if false the maximum range is only greater than the maximum value.
     */
    @XmlAttribute(name="max-inclusive")
    public boolean maxInclusive;

    /**
     * This IntegerValueRange constructor is available for the benefit of JAXB which
     * requires a no argument constructor to be present.
     */
    public IntegerValueRange() {

    }

    /**
     * Create a new IntegerValueRange instance.
     *
     * @param min The minimum value for the range.
     * @param max The maximum value for the range.
     * @param minInclusive Whether the minimum of the range is inclusive of the minimum value i.e. >= rather than just >.
     * @param maxInclusive Whether the maximum of the range is inclusive of the maximum value i.e. <= rather than just <.
     */
    public IntegerValueRange(final int min, final int max, final boolean minInclusive, final boolean maxInclusive) {
        if (min < max) {
            this.min = min;
            this.max = max;
        }
        else {
            this.max = min;
            this.min = max;
        }
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

    /**
     * Whether the specified value is within this range.
     *
     * @param value The value to be checked to see whether it is within this range.
     * @return True if the specified value is in this range or false otherwise.
     */
    public boolean isInRange(int value) {
        return ((value > min || (minInclusive && value == min)) && (value < max || (maxInclusive && value == max)));
    }

    /**
     * Get a convenient text representation of the range.
     *
     * @return A text representation of the range.
     */
    @Override
    public String toString() {
        return String.format("%s %d & %s %d", minInclusive ? "≥" : ">", min, maxInclusive ? "≤" : "<", max);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntegerValueRange) {
            IntegerValueRange otherRange = (IntegerValueRange) obj;
            return otherRange.min == min && otherRange.max == max && otherRange.minInclusive == minInclusive && otherRange.maxInclusive == maxInclusive;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return ((min * max) / 11);
    }
}
