package org.jdesktop.wonderland.modules.path.common.style.segment;

import org.jdesktop.wonderland.modules.path.common.style.ItemStyle;

/**
 * This interface represents meta data information about the styling of a path segment.
 * Specific implementations can hold more data depending on the SegmentStyleType.
 * Some meta-data is only applicable to certain SegmentStyleType.
 *
 * @author Carl Jokl
 */
public interface SegmentStyle extends ItemStyle<SegmentStyleType> {

}
