package org.jdesktop.wonderland.modules.path.common.style.node;

import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;
import org.jdesktop.wonderland.modules.path.common.style.StyleType;

/**
 * This interface is used for different styles of nodes. An interface is used to support
 * other modules creating custom node styles (where using an enumeration would make this
 * difficult).
 *
 * @author Carl Jokl
 */
public interface NodeStyleType extends StyleType {

    /**
     * Get the default SegmentStyleType for this NodeStyleType.
     *
     * @return The SegmentStyleType of the default SegmentStyleType to be used with
     *         this NodeStyleType.
     */
    public SegmentStyleType getDefaultSegmentStyleType();
}
