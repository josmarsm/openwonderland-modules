package org.jdesktop.wonderland.modules.path.common.style.segment;

import org.jdesktop.wonderland.modules.path.common.style.segment.CoreSegmentStyleType;
import java.io.Serializable;
import org.jdesktop.wonderland.modules.path.common.style.AbstractItemStyle;

/**
 * This class represents an implementation of SegmentStyle which is invisible.
 * This can be used with any PathStyle where the segments are not intended to be drawn.
 *
 * @author Carl Jokl
 */
public class InvisibleSegmentStyle extends AbstractItemStyle<SegmentStyleType> implements SegmentStyle, Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new instance of InvisibleNodeStyle with the default span value of 1.
     */
    public InvisibleSegmentStyle() {

    }

    /**
     * Create a new instance of InvisibleNodeStyle with the specified span value.
     *
     * @param span The number of nodes which should be spanned by this style.
     * @throws IllegalArgumentException If the specified span was not greater than zero.
     */
    public InvisibleSegmentStyle(int span) throws IllegalArgumentException {
        super(span);
    }

    /**
     * Get the SegmentStyleType for this InvisibleSegmentStyle.
     *
     * @return The SegmentStyleType of the InvisibleSegmentStyle i.e. CoreNodeStyleType.INVISIBLE.
     */
    @Override
    public SegmentStyleType getStyleType() {
        return CoreSegmentStyleType.INVISIBLE;
    }
}
