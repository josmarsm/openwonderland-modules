package org.jdesktop.wonderland.modules.path.common.style.node;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.wonderland.modules.path.common.style.AbstractItemStyle;
import org.jdesktop.wonderland.modules.path.common.style.ColoredStyle;
import org.jdesktop.wonderland.modules.path.common.style.FloatValueRange;
import org.jdesktop.wonderland.modules.path.common.style.HeightHoldingStyle;
import org.jdesktop.wonderland.modules.path.common.style.HeightOffsetStyle;
import org.jdesktop.wonderland.modules.path.common.style.SizedStyle;
import org.jdesktop.wonderland.modules.path.common.style.TexturedStyle;
import org.jdesktop.wonderland.modules.path.common.style.WidthHoldingStyle;

/**
 * This is a class containing the style meta-data for SquarePostNodeStyle.
 *
 * @author Carl Jokl
 */
public class SquarePostNodeStyle extends AbstractItemStyle<NodeStyleType> implements SizedStyle, ColoredStyle, TexturedStyle, HeightHoldingStyle, WidthHoldingStyle, HeightOffsetStyle, Serializable {

    /**
     * The range of permitted values for the height of the post.
     */
    public static final FloatValueRange HEIGHT_RANGE = new FloatValueRange(0.0f, 1000.0f, false, true);

    /**
     * The range of permitted values for the width of the post.
     */
    public static final FloatValueRange WIDTH_RANGE = new FloatValueRange(0.0f, 500.0f, false, true);

    /**
     * The range of permitted values for the vertical offset of the post.
     */
    public static final FloatValueRange HEIGHT_OFFSET_RANGE = new FloatValueRange(-1000.0f, 1000.0f, true, true);

    private float height;
    private float width;
    private float heightOffset;
    private List<Color> colors;
    private List<String> textureURIs;

    /**
     * Create a new instance of SquarePostNodeStyle.
     */
    public SquarePostNodeStyle() {
        height = 1.0f;
        width = 0.0625f;
        heightOffset = 0.5f;
        colors = new ArrayList<Color>(1);
        textureURIs = new ArrayList(1);
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
                return width;
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
    public void setSizeOrOffset(int index, float value) throws IndexOutOfBoundsException {
        switch (index) {
            case 0:
                setHeight(value);
                break;
            case 1:
                setWidth(value);
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
                return WIDTH_RANGE;
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
                return "Width";
            case 2:
                return "Height Offset";
            default:
                throw new IndexOutOfBoundsException(String.format("The specified size index: %d  is outside the range of the two size attributes!", index));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyleType getStyleType() {
        return CoreNodeStyleType.SQUARE_POST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMinColors() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxColors() {
        return 6;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColorCount() {
        return colors.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getColor(int index) throws IndexOutOfBoundsException {
        if (index >= 0 && index < colors.size()) {
            return colors.get(index);
        }
        else {
            throw new IndexOutOfBoundsException(String.format("The color index: %d is outside the range of %d color indices for this SquarePostNodeStyle", index, colors.size()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setColor(int index, Color color) throws IndexOutOfBoundsException {
        if (color != null) {
            if (index >= 0 && index < colors.size()) {
                colors.set(index, color);
                return true;
            }
            else {
                throw new IndexOutOfBoundsException(String.format("The index: %d was outside the range or %d colors!", index, colors.size()));
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addColor(Color color) {
        return color != null && colors.size() < 6 && colors.add(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color removeColor() {
        if (colors.size() > 1) {
            return colors.remove(colors.size() - 1);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color removeColor(int index) throws IndexOutOfBoundsException {
        if (colors.size() > 1) {
            if (index >= 0 && index < colors.size()) {
                return colors.remove(index);
            }
            else {
                throw new IndexOutOfBoundsException(String.format("The specified index: %d at which to remove a color was outside the range of valid indices!"));
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColorLabel(int index) throws IndexOutOfBoundsException {
        switch (colors.size()) {
            case 1:
                if (index == 0) {
                    return "Post Color";
                }
            case 2:
                switch (index) {
                    case 0:
                        return "Post Sides Color";
                    case 1:
                        return "Post Top and Bottom Color";
                }
            case 3:
                switch (index) {
                    case 0:
                        return "Post Sides Color";
                    case 1:
                        return "Post Front and Back Color";
                    case 2:
                        return "Post Top and Bottom Color";
                }
            case 4:
                switch (index) {
                    case 0:
                        return "Post Sides Color";
                    case 1:
                        return "Post Front Color";
                    case 2:
                        return "Post Back Color";
                    case 3:
                        return "Post Top and Bottom Color";
                }
            case 5:
                switch (index) {
                    case 0:
                        return "Post Left Side Color";
                    case 1:
                        return "Post Right Side Color";
                    case 2:
                        return "Post Front Color";
                    case 3:
                        return "Post Back Color";
                    case 4:
                        return "Post Top and Bottom Color";
                }
            case 6:
                switch (index) {
                    case 0:
                        return "Post Left Side Color";
                    case 1:
                        return "Post Right Side Color";
                    case 2:
                        return "Post Front Color";
                    case 3:
                        return "Post Back Color";
                    case 4:
                        return "Post Top Color";
                    case 5:
                        return "Post Bottom Color";
                }
        }
        throw new IndexOutOfBoundsException(String.format("The specified color index: %d is outside the range of valid color indices!", index));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMinImages() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxImages() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getImageCount() {
        return textureURIs.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getImage(int index) throws IndexOutOfBoundsException {
        if (index >= 0 && index < textureURIs.size()) {
            return textureURIs.get(index);
        }
        else {
            throw new IndexOutOfBoundsException(String.format("The specified post texture index: %d is outside of the valid range! No of textures: %d.", index, textureURIs.size()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setImage(int index, String imageURI) throws IndexOutOfBoundsException {
        if (imageURI != null) {
            if (index >= 0 && index < textureURIs.size()) {
                textureURIs.set(index, imageURI);
                return true;
            }
            else {
                throw new IndexOutOfBoundsException(String.format("The specified post texture index: %d is outside of the valid range! No of textures: %d.", index, textureURIs.size()));
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addImage(String imageURI) {
        return imageURI != null && textureURIs.size() < 6 && textureURIs.add(imageURI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String removeImage() throws UnsupportedOperationException {
        return textureURIs.isEmpty() ? null : textureURIs.remove(textureURIs.size() - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String removeImage(int index) throws IndexOutOfBoundsException, UnsupportedOperationException {
        if (index >= 0 && index < textureURIs.size()) {
            return textureURIs.get(index);
        }
        else {
            throw new IndexOutOfBoundsException(String.format("The texture image index: %d of the texture to be removed was outside the valid range! No of textures: %d.", index, textureURIs));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getImageLabel(int index) throws IndexOutOfBoundsException {
        switch (colors.size()) {
            case 1:
                if (index == 0) {
                    return "Post Texture";
                }
            case 2:
                switch (index) {
                    case 0:
                        return "Post Sides Texture";
                    case 1:
                        return "Post Top and Bottom Texture";
                }
            case 3:
                switch (index) {
                    case 0:
                        return "Post Sides Texture";
                    case 1:
                        return "Post Front and Back Texture";
                    case 2:
                        return "Post Top and Bottom Texture";
                }
            case 4:
                switch (index) {
                    case 0:
                        return "Post Sides Texture";
                    case 1:
                        return "Post Front Texture";
                    case 2:
                        return "Post Back Texture";
                    case 3:
                        return "Post Top and Bottom Texture";
                }
            case 5:
                switch (index) {
                    case 0:
                        return "Post Left Side Texture";
                    case 1:
                        return "Post Right Side Texture";
                    case 2:
                        return "Post Front Texture";
                    case 3:
                        return "Post Back Texture";
                    case 4:
                        return "Post Top and Bottom Texture";
                }
            case 6:
                switch (index) {
                    case 0:
                        return "Post Left Side Texture";
                    case 1:
                        return "Post Right Side Texture";
                    case 2:
                        return "Post Front Texture";
                    case 3:
                        return "Post Back Texture";
                    case 4:
                        return "Post Top Texture";
                    case 5:
                        return "Post Bottom Texture";
                }
        }
        throw new IndexOutOfBoundsException(String.format("The specified color index: %d is outside the range of valid color indices!", index));
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
            throw new IllegalArgumentException(String.format("The specified height: %g is outside the valid range!", height));
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
    public float getWidth() {
        return width;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWidth(float width) throws IllegalArgumentException {
        if (WIDTH_RANGE.isInRange(width)) {
            this.width = width;
        }
        else {
            throw new IllegalArgumentException(String.format("The specified width: %g is outside the valid range!", width));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FloatValueRange getSupportedWidthRange() {
        return WIDTH_RANGE;
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
            throw new IllegalArgumentException(String.format("The specified height: %g is outside the valid range!", height));
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
