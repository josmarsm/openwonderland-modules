package org.jdesktop.wonderland.modules.path.common.style.node;

import org.jdesktop.wonderland.modules.path.common.style.ColorStyleAttribute;
import org.jdesktop.wonderland.modules.path.common.style.FloatStyleAttribute;
import org.jdesktop.wonderland.modules.path.common.FloatValueRange;
import org.jdesktop.wonderland.modules.path.common.style.StandardStyleAttribute;
import org.jdesktop.wonderland.modules.path.common.style.TextureStyleAttribute;

/**
 * This is a NodeStyleBuider to build NodeStyle for the SquarePost NodeStyle.
 *
 * @author Carl Jokl
 */
public class SquarePostNodeStyleBuilder implements NodeStyleBuilder {

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyle createNodeStyle() {
        NodeStyle style = new NodeStyle(CoreNodeStyleType.SQUARE_POST);
        style.addStyleAttribute(new FloatStyleAttribute(StandardStyleAttribute.WIDTH.getName(), new FloatValueRange(0.0f, 128.0f, false, true) , 0.05f));
        style.addStyleAttribute(new FloatStyleAttribute(StandardStyleAttribute.HEIGHT.getName(), new FloatValueRange(0.0f, 256.0f, false, true), 1.0f));
        style.addStyleAttribute(new FloatStyleAttribute(StandardStyleAttribute.X_OFFSET.getName(), new FloatValueRange(-512.0f, 512.0f, true, true), 0.0f));
        style.addStyleAttribute(new FloatStyleAttribute(StandardStyleAttribute.Y_OFFSET.getName(), new FloatValueRange(-512.0f, 512.0f, true, true), 0.5f));
        style.addStyleAttribute(new FloatStyleAttribute(StandardStyleAttribute.Z_OFFSET.getName(), new FloatValueRange(-512.0f, 512.0f, true, true), 0.0f));
        style.addStyleAttribute(new ColorStyleAttribute(StandardStyleAttribute.COLOR.getName(), 0.52f, 0.31f, 0.0f, 1.0f));
        style.addStyleAttribute(new TextureStyleAttribute(StandardStyleAttribute.TEXTURE.getName()));
        return style;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyleType getBuiltStyleType() {
        return CoreNodeStyleType.SQUARE_POST;
    }

}
