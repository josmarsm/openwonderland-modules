package org.jdesktop.wonderland.modules.path.common.style.segment;

import org.jdesktop.wonderland.modules.path.common.style.ColorStyleAttribute;
import org.jdesktop.wonderland.modules.path.common.style.FloatStyleAttribute;
import org.jdesktop.wonderland.modules.path.common.FloatValueRange;
import org.jdesktop.wonderland.modules.path.common.style.StandardStyleAttribute;
import org.jdesktop.wonderland.modules.path.common.style.TextureStyleAttribute;

/**
 * This SegmentStyleBuilder is used to build SegmentStyle for the tape SegmentStyleType.
 *
 * @author Carl Jokl
 */
public class TapeSegmentStyleBuilder implements SegmentStyleBuilder {

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyle createSegmentStyle() {
        SegmentStyle style = new SegmentStyle(CoreSegmentStyleType.TAPE);
        style.addStyleAttribute(new FloatStyleAttribute(StandardStyleAttribute.WIDTH.getName(), new FloatValueRange(0.0f, 128.0f, false, true) , 0.03125f));
        style.addStyleAttribute(new FloatStyleAttribute(StandardStyleAttribute.X_OFFSET.getName(), new FloatValueRange(-512.0f, 512.0f, true, true), 0.0f));
        style.addStyleAttribute(new FloatStyleAttribute(StandardStyleAttribute.Y_OFFSET.getName(), new FloatValueRange(-512.0f, 512.0f, true, true), 0.5f));
        style.addStyleAttribute(new FloatStyleAttribute(StandardStyleAttribute.Z_OFFSET.getName(), new FloatValueRange(-512.0f, 512.0f, true, true), 0.0f));
        style.addStyleAttribute(new ColorStyleAttribute(StandardStyleAttribute.COLOR.getName(), 0.0f, 0.0f, 0.0f, 1.0f));
        style.addStyleAttribute(new TextureStyleAttribute(StandardStyleAttribute.TEXTURE.getName()));
        return style;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyleType getBuiltStyleType() {
        return CoreSegmentStyleType.TAPE;
    }
}
