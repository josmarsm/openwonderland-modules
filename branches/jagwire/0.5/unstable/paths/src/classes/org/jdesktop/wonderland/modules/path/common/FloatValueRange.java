package org.jdesktop.wonderland.modules.path.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This class is used to represent a value range for a float value comprising of a lower limit and upper limit for the range.
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="float-range")
public class FloatValueRange implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The minimum value for the range.
     */
    @XmlAttribute(name="min")
    public float min;

    /**
     * The maximum value for the range.
     */
    @XmlAttribute(name="max")
    public float max;

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
     * This no-argument constructor is provided for the benefit of JAXB.
     */
    public FloatValueRange() {

    }

    /**
     * Create a new FloatValueRange instance.
     *
     * @param min The minimum value for the range.
     * @param max The maximum value for the range.
     * @param minInclusive Whether the minimum of the range is inclusive of the minimum value i.e. >= rather than just >.
     * @param maxInclusive Whether the maximum of the range is inclusive of the maximum value i.e. <= rather than just <.
     */
    public FloatValueRange(final float min, final float max, final boolean minInclusive, final boolean maxInclusive) {
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
    @XmlTransient
    public boolean isInRange(float value) {
        return ((value > min || (minInclusive && value == min)) && (value < max || (maxInclusive && value == max)));
    }

    /**
     * Get a convenient text representation of the range.
     *
     * @return A text representation of the range.
     */
    @Override
    public String toString() {
        return String.format("%s %f & %s %f", minInclusive ? "≥" : ">", min, maxInclusive ? "≤" : "<", max);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FloatValueRange) {
            FloatValueRange otherRange = (FloatValueRange) obj;
            return otherRange.min == min && otherRange.max == max && otherRange.minInclusive == minInclusive && otherRange.maxInclusive == maxInclusive;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (int) ((min * max) / 11);
    }
}
