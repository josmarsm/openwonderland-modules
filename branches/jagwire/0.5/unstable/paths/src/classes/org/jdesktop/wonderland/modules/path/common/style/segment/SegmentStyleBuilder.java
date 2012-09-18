package org.jdesktop.wonderland.modules.path.common.style.segment;

/**
 * This is an interface for an object which builds default SegmentStyle for a given SegmentStyleType.
 *
 * @author Carl Jokl
 */
public interface SegmentStyleBuilder {

    /**
     * Create the default SegmentStyle for the particular SegmentStyleStype which is supported
     * by this SegmentStyleBuilder.
     * 
     * @return An instance of the default SegmentStyle (which can be updated) for the SegmentStyleType for which this
     *         SegmentStyleBuilder builds SegmenteStyles.
     */
    public SegmentStyle createSegmentStyle();

    /**
     * Get the SegmentStyleType for which this SegmentStyleBuilder builds SegmentStyles.
     *
     * @return The SegmentStyleType for which this SegmentStyleBuilder builds SegmentStyles.
     */
    public SegmentStyleType getBuiltStyleType();
}
