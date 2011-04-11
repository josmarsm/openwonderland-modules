package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;

/**
 * This class is used to represent a value range for a float value comprising of a lower limit and upper limit for the range.
 *
 * @author Carl Jokl
 */
public class FloatValueRange implements Serializable {

    /**
     * The minimum value for the range.
     */
    public final float min;

    /**
     * The maximum value for the range.
     */
    public final float max;

    /**
     * Whether the minimum value range is inclusive of the minimum value itself i.e.
     * if true the minimum range is greater than or equal to the minimum value
     * or if false the minimum range is only greater than the minimum value.
     */
    public final boolean minInclusive;

    /**
     * Whether the maximum value range is inclusive of the maximum value itself i.e.
     * if true the maximum range is less than or equal the the maximum value
     * or if false the maximum range is only greater than the maximum value.
     */
    public final boolean maxInclusive;

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
        return String.format("Min: %g %s to max: %g %s", min, minInclusive ? "inclusive" : "exclusive", max, maxInclusive ? "inclusive" : "exclusive");
    }
}
