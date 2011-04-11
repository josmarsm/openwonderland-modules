package org.jdesktop.wonderland.modules.path.common.style;

/**
 * This interface represents a style whereby the style includes
 * a texture which should repeated relative to the number of meters
 * in the distance it covers. This can be used for styles which
 * cover geometry of variable heights so that the texture is not
 * stretched but rather the pattern will be repeated up until
 * the end of the geometry.
 *
 * @author Carl Jokl
 */
public interface VTexPerMRepeatingStyle {

    /**
     * Get the number of vertical texture repeats per meter.
     * 
     * @return Get the number of vertical texture repeats per meter.
     */
    public float getVTexRepeatsPerM();

    /**
     * Set the number of vertical texture repeats per meter.
     *
     * @param repeatsPerMeter The number of vertical texture repeats per meter.
     */
    public void setVTexRepreatsPerM(float repeatsPerMeter) throws IllegalArgumentException;

    /**
     * Get the range of supported vertical texture repeats per meter values.
     * 
     * @return A FloatValueRange which contains the minimum and maximum number of
     *         vertical texture repeats per meter for this style.
     */
    public FloatValueRange getVTexRepeatsPerMRange();
}
