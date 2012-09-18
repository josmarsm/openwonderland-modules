package org.jdesktop.wonderland.modules.path.common.style.node;

import org.jdesktop.wonderland.modules.path.common.style.ColorStyleAttribute;
import org.jdesktop.wonderland.modules.path.common.style.FloatStyleAttribute;
import org.jdesktop.wonderland.modules.path.common.FloatValueRange;
import org.jdesktop.wonderland.modules.path.common.style.StandardStyleAttribute;

/**
 * This class represents a builder used to build NodeStyle for use with the hazard cone NodeStyleType.
 *
 * @author Carl Jokl
 */
public class HazardConeNodeStyleBuilder implements NodeStyleBuilder {

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyle createNodeStyle() {
        NodeStyle style = new NodeStyle(CoreNodeStyleType.HAZARD_CONE);
        style.addStyleAttribute(new FloatStyleAttribute(StandardStyleAttribute.WIDTH.getName(), new FloatValueRange(0.0f, 128.0f, false, true) , 0.25f));
        style.addStyleAttribute(new FloatStyleAttribute(StandardStyleAttribute.HEIGHT.getName(), new FloatValueRange(0.0f, 256.0f, false, true), 0.5f));
        style.addStyleAttribute(new FloatStyleAttribute(StandardStyleAttribute.RADIUS_1.getName(), new FloatValueRange(0.0f, 128.0f, false, true), 0.0625f));
        style.addStyleAttribute(new FloatStyleAttribute(StandardStyleAttribute.RADIUS_2.getName(), new FloatValueRange(0.0f, 128.0f, false, true), 0.125f));
        style.addStyleAttribute(new FloatStyleAttribute(StandardStyleAttribute.X_OFFSET.getName(), new FloatValueRange(-512.0f, 512.0f, true, true), 0.0f));
        style.addStyleAttribute(new FloatStyleAttribute(StandardStyleAttribute.Y_OFFSET.getName(), new FloatValueRange(-512.0f, 512.0f, true, true), 0.25f));
        style.addStyleAttribute(new FloatStyleAttribute(StandardStyleAttribute.Z_OFFSET.getName(), new FloatValueRange(-512.0f, 512.0f, true, true), 0.0f));
        style.addStyleAttribute(new ColorStyleAttribute(StandardStyleAttribute.BACKGROUND_COLOR.getName(), 1.0f, 0.31f, 0.0f, 1.0f));
        style.addStyleAttribute(new ColorStyleAttribute(StandardStyleAttribute.FOREGROUND_COLOR.getName(), 1.0f, 1.0f, 1.0f, 1.0f));
        return style;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyleType getBuiltStyleType() {
        return CoreNodeStyleType.HAZARD_CONE;
    }

}
