package org.jdesktop.wonderland.modules.path.common.style.segment;

/**
 * This is a SegmentStyleBuilder to build SegmentStyle for the invisible SegmentStyleType.
 *
 * @author Carl Jokl
 */
public class InvisibleSegmentStyleBuilder implements SegmentStyleBuilder {

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyle createSegmentStyle() {
        return new SegmentStyle(CoreSegmentStyleType.INVISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyleType getBuiltStyleType() {
        return CoreSegmentStyleType.INVISIBLE;
    }
}
