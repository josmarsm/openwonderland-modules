package org.jdesktop.wonderland.modules.path.common.style;

/**
 * This interface represents a style whereby the style includes
 * a texture which should repeated relative to the number of meters
 * in the distance it covers. This can be used for styles which
 * cover geometry of variable lengths so that the texture is not
 * stretched but rather the pattern will be repeated up until
 * the end of the geometry.
 *
 * @author Carl Jokl
 */
public interface HTexPerMRepeatingStyle {

    /**
     * Get the number of horizontal texture repeats per meter.
     * 
     * @return Get the number of horizontal texture repeats per meter.
     */
    public float getHTexRepeatsPerM();

    /**
     * Set the number of horizontal texture repeats per meter.
     *
     * @param repeatsPerMeter The number of horizontal texture repeats per meter.
     */
    public void setHTexRepreatsPerM(float repeatsPerMeter) throws IllegalArgumentException;

    /**
     * Get the range of supported horizontal texture repeats per meter values.
     * 
     * @return A FloatValueRange which contains the minimum and maximum number of
     *         horizontal texture repeats per meter for this style.
     */
    public FloatValueRange getHTexRepeatsPerMRange();
}
