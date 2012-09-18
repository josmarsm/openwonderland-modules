package org.jdesktop.wonderland.modules.ruler.common;

/**
 * This enumeration is used for representing the different units of measurement
 * which a ruler may use i.e. metric meters vs imperial feet.
 *
 * @author Carl Jokl
 */
public enum MeasurementUnits {

    /**
     * Standard international metric unit of measure.
     */
    METERS_CENTIMETERS_AND_MILLIMETERS(1.0f, 100, 10, true, "Meters", "Centimeters", "Millimeters", "m", "cm", "mm", 5),

    /**
     * Predominant American unit of measure and also historical unit
     * of measure for other countries.
     */
    FEET_INCHES_AND_SIXTEENTHS(3.2808399f, 12, 16, false, "Feet", "Inches", "Sixteenths", "'", "\"", "1/16", 8, 4, 2),

    /**
     * Predominant American unit of measure and also historical unit
     * of measure for other countries. Larger size.
     */
    YARDS_INCHES_AND_SIXTEENTHS(0.9144f, 36, 16, false, "Yards", "Inches", "Sixteenths", "yrds", "\"", "1/16", 8, 4, 2);
    
    private final float mainUnitsPerMetre;
    private final int smallUnitsInLarge;
    private final int subUnitsInSmall;
    private final boolean countSmallest;
    private final String largeUnitName;
    private final String smallUnitName;
    private final String subUnitName;
    private final String largeUnitSuffix;
    private final String smallUnitSuffix;
    private final String subUnitSuffix;
    private final int[] significantCounts;

    /**
     * Create a new instance of a measurement unit enumeration.
     *
     * @param mainUnitsPerMetre The number of main units of measure per meter.
     * @param smallUnitsInLarge The number of small units per large unit.
     * @param subUnitsInSmall The number of sub units within the small unit.
     * @param countSmallest Whether the smallest unit should be used for counting (such as with millimeters) or not (such as with fractions of inches).
     * @param largeUnitName The name of the large unit.
     * @param smallUnitName The name of the small unit.
     * @param subUnitName The name of the sub unit.
     * @param largeUnitSuffix The suffix of the large unit e.g. m ' etc.
     * @param smallUnitSuffix The suffix of the small unit e.g. cm " etc.
     * @param subUnitSuffix The suffix of the sub unit e.g. mm.
     * @param significantCounts The numbers of the smallest units within a group which are significant i.e. on a ruler would normally have a longer mark.
     */
    private MeasurementUnits(final float mainUnitsPerMetre, final int smallUnitsInLarge, final int subUnitsInSmall, final boolean countSmallest, final String largeUnitName, final String smallUnitName, final String subUnitName, final String largeUnitSuffix, final String smallUnitSuffix, final String subUnitSuffix, final int... significantCounts) {
        this.mainUnitsPerMetre = mainUnitsPerMetre;
        this.smallUnitsInLarge = smallUnitsInLarge;
        this.subUnitsInSmall = subUnitsInSmall;
        this.countSmallest = countSmallest;
        this.largeUnitName = largeUnitName;
        this.smallUnitName = smallUnitName;
        this.subUnitName = subUnitName;
        this.largeUnitSuffix = largeUnitSuffix;
        this.smallUnitSuffix = smallUnitSuffix;
        this.subUnitSuffix = subUnitSuffix;
        this.significantCounts = significantCounts;
    }

    /**
     * Get the number of main units per metre.
     *
     * @return The number of main units per metre.
     */
    public float getMainUnitsPerMetre() {
        return mainUnitsPerMetre;
    }
    
    /**
     * Get the (calculated) number of small units per metre.
     * 
     * @return The number of small units per meter (calculated).
     */
    public float getSmallUnitsPerMetre() {
        return smallUnitsInLarge != 0 ? mainUnitsPerMetre / smallUnitsInLarge : 0;
    }

    /**
     * Get the number of small units in the large unit.
     *
     * @return The number of small units in the large unit
     *         such as millimetres in metres or inches in feet.
     */
    public int getSmallUnitsInLarge() {
        return smallUnitsInLarge;
    }

    /**
     * Get the number of sub units within the small unit.
     * This can be something like millimetres in the centimetres
     * or one sixteenth of an inch divisions in inches.
     *
     * @return The number of sub units within the small unit.
     */
    public int getSubUnitsInSmall() {
        return subUnitsInSmall;
    }

    /**
     * Get the name of the main large unit.
     *
     * @return The name of the main large unit.
     */
    public String getLargeUnitName() {
        return largeUnitName;
    }

    /**
     * Get the unit suffix for the large unit.
     *
     * @return The suffix for the large unit.
     */
    public String getLargeUnitSuffix() {
        return largeUnitSuffix;
    }

    /**
     * Get the name of the small unit.
     *
     * @return The name of the small unit.
     */
    public String getSmallUnitName() {
        return smallUnitName;
    }

    /**
     * Get the unit suffix for the small unit.
     *
     * @return The unit suffix for the small unit.
     */
    public String getSmallUnitSuffix() {
        return smallUnitSuffix;
    }

    /**
     * Get the name of the sub unit.
     *
     * @return The name of the sub unit.
     */
    public String getSubUnitName() {
        return subUnitName;
    }

    /**
     * Get the unit suffix for the sub unit.
     *
     * @return The unit suffix for the sub unit.
     */
    public String getSubUnitSuffix() {
        return subUnitSuffix;
    }

    /**
     * Weather the MesurementUnits are decimal i.e. there are a power of ten
     * small units in a large unit (as with metric measurements).
     *
     * @return True if the number of small units in the large unit is a power of ten.
     */
    public boolean isDecimal() {
        return smallUnitsInLarge > 0 && smallUnitsInLarge % 10 == 0;
    }

    /**
     * Whether the smallest unit is counted to form the number label for each group.
     *
     * @return True if the smallest unit is used for the group label (such as will millimeters) or whether
     *         the individual group index is counted instead (as with inches).
     */
    public boolean isSmallestUnitCounted() {
        return countSmallest;
    }

    /**
     * Get the numbers of the smallest units which are a significant count.
     *
     * @return An array of the counts of small units which are significant and should be emphasized on display.
     */
    public int[] getSignificantCounts() {
        return significantCounts;
    }
}
