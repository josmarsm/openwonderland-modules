package org.jdesktop.wonderland.modules.path.common.style.segment;

import org.jdesktop.wonderland.modules.path.common.style.StyleTypeResolver;
import org.jdesktop.wonderland.modules.path.common.style.segment.CoreSegmentStyleType;

/**
 * This is the default built in StyleTypeResolver type which resolves core SegmentStyleTypes
 * from their corresponding unique integer id values.
 *
 * @author Carl Jokl
 */
public class CoreSegmentStyleTypeResolver implements StyleTypeResolver<SegmentStyleType> {

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyleType resolveStyleType(int styleTypeId) {
        for (SegmentStyleType type : CoreSegmentStyleType.values()) {
            if (type.getId() == styleTypeId) {
                return type;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGroupName() {
        return "core:segment-style-type";
    }
}
