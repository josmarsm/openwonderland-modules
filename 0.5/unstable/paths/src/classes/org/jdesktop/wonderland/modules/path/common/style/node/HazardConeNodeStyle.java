package org.jdesktop.wonderland.modules.path.common.style.node;

import java.awt.Color;
import java.io.Serializable;
import org.jdesktop.wonderland.modules.path.common.style.AbstractItemStyle;
import org.jdesktop.wonderland.modules.path.common.style.ColoredStyle;
import org.jdesktop.wonderland.modules.path.common.style.FloatValueRange;
import org.jdesktop.wonderland.modules.path.common.style.HeightHoldingStyle;
import org.jdesktop.wonderland.modules.path.common.style.HeightOffsetStyle;
import org.jdesktop.wonderland.modules.path.common.style.RadiusHoldingStyle;
import org.jdesktop.wonderland.modules.path.common.style.SizedStyle;

/**
 * This class represents the style meta-data for rendering a PathNode as a hazard cone.
 *
 * @author Carl Jokl
 */
public class HazardConeNodeStyle extends AbstractItemStyle<NodeStyleType> implements SizedStyle, ColoredStyle, HeightHoldingStyle, RadiusHoldingStyle, HeightOffsetStyle, Serializable {

    /**
     * The range of permitted values for the height of the hazard cone.
     */
    public static final FloatValueRange HEIGHT_RANGE = new FloatValueRange(0.0f, 1000.0f, false, true);

    /**
     * The range of permitted values for the thickness of the hazard cone.
     */
    public static final FloatValueRange RADIUS_RANGE = new FloatValueRange(0.0f, 500.0f, false, true);

    /**
     * The range of permitted values for the vertical offset of the hazard cone.
     */
    public static final FloatValueRange HEIGHT_OFFSET_RANGE = new FloatValueRange(-1000.0f, 1000.0f, true, true);

    private float height;
    private float radius;
    private float heightOffset;
    private Color bodyColor;
    private Color stripeColor;
    private Color baseColor;

    /**
     * Create a new instance of HazardConeNodeStyle with default settings.
     */
    public HazardConeNodeStyle() {
        height = 0.5f;
        radius =  0.125f;
        heightOffset = 0.5f;
        bodyColor = Color.ORANGE;
        stripeColor = Color.WHITE;
        baseColor = Color.BLACK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSizeAttributeCount() {
        return 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getSizeOrOffset(int index) throws IndexOutOfBoundsException {
        switch (index) {
            case 0:
                return height;
            case 1:
                return radius;
            case 2:
                return heightOffset;
            default:
                throw new IndexOutOfBoundsException(String.format("The specified size index: %d  is outside the range of the two size and one offset attribute!", index));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSizeOrOffset(int index, float value) throws IndexOutOfBoundsException, IllegalArgumentException {
        switch (index) {
            case 0:
                setHeight(value);
                break;
            case 1:
                setRadius(value);
                break;
            case 2:
                setHeightOffset(value);
                break;
            default:
                throw new IndexOutOfBoundsException(String.format("The specified size index: %d  is outside the range of the two size and one offset attribute!", index));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FloatValueRange getSizeOrOffsetRange(int index) throws IndexOutOfBoundsException {
        switch (index) {
            case 0:
                return HEIGHT_RANGE;
            case 1:
                return RADIUS_RANGE;
            case 2:
                return HEIGHT_OFFSET_RANGE;
            default:
                throw new IndexOutOfBoundsException(String.format("The specified size index: %d  is outside the range of the two size and one offset attribute!", index));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSizeOrOffsetLabel(int index) throws IndexOutOfBoundsException {
        switch (index) {
            case 0:
                return "Height";
            case 1:
                return "Radius";
            case 2:
                return "Vertical Offset";
            default:
                throw new IndexOutOfBoundsException(String.format("The specified size index: %d  is outside the range of the two size attributes!", index));
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyleType getStyleType() {
        return CoreNodeStyleType.HAZARD_CONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMinColors() {
        return 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxColors() {
        return 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColorCount() {
        return 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getColor(int index) throws IndexOutOfBoundsException {
        switch (index) {
            case 0:
                return bodyColor;
            case 1:
                return stripeColor;
            case 2:
                return baseColor;
            default:
                throw new IndexOutOfBoundsException(String.format("The color index: %d is outside the range of %d color indices for this HazardConeNodeStyle", index, 3));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setColor(int index, Color color) throws IndexOutOfBoundsException {
        switch (index) {
            case 0:
                bodyColor = color;
                return true;
            case 1:
                stripeColor = color;
                return true;
            case 2:
                baseColor = color;
                return true;
            default:
                throw new IndexOutOfBoundsException(String.format("The color index: %d is outside the range of %d color indices for this HazardConeNodeStyle", index, 3));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addColor(Color color) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color removeColor() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color removeColor(int index) throws IndexOutOfBoundsException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColorLabel(int index) throws IndexOutOfBoundsException {
        switch (index) {
            case 0:
                return "Cone Body Color";
            case 1:
                return "Cone Stripe Color";
            case 2:
                return "Cone Base Color";
            default:
                throw new IndexOutOfBoundsException(String.format("The specified color index: %d is outside the range of valid color indices!", index));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getHeight() {
        return height;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHeight(float height) throws IllegalArgumentException {
        if (HEIGHT_RANGE.isInRange(height)) {
            this.height = height;
        }
        else {
            throw new IllegalArgumentException(String.format("The height value: %g was outside the valid range: %s!", height, HEIGHT_RANGE.toString()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FloatValueRange getSupportedHeightRange() {
        return HEIGHT_RANGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getRadius() {
        return radius;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRadius(float radius) throws IllegalArgumentException {
        if (RADIUS_RANGE.isInRange(radius)) {
            this.radius = radius;
        }
        else {
            throw new IllegalArgumentException(String.format("The radius value: %g was outside the valid range: %s!", radius, RADIUS_RANGE.toString()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FloatValueRange getSupportedRadiusRange() {
        return RADIUS_RANGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getHeightOffset() {
        return heightOffset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHeightOffset(float offset) throws IllegalArgumentException {
        if (HEIGHT_OFFSET_RANGE.isInRange(offset)) {
            heightOffset = offset;
        }
        else {
            throw new IllegalArgumentException(String.format("The vertical offset value: %g was outside the valid range: %s!", offset, HEIGHT_OFFSET_RANGE.toString()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FloatValueRange getSupportedHeightOffsetRange() {
        return HEIGHT_OFFSET_RANGE;
    }
}
