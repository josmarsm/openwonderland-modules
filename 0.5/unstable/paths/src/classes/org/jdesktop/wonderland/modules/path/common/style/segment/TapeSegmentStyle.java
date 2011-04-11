package org.jdesktop.wonderland.modules.path.common.style.segment;

import java.io.Serializable;
import org.jdesktop.wonderland.modules.path.common.style.AbstractItemStyle;
import org.jdesktop.wonderland.modules.path.common.style.FloatValueRange;
import org.jdesktop.wonderland.modules.path.common.style.HTexPerMRepeatingStyle;
import org.jdesktop.wonderland.modules.path.common.style.HeightHoldingStyle;
import org.jdesktop.wonderland.modules.path.common.style.HeightOffsetStyle;
import org.jdesktop.wonderland.modules.path.common.style.SizedStyle;
import org.jdesktop.wonderland.modules.path.common.style.TexturedStyle;

/**
 * This SegmentStyle is used for rendering ribbon based segment such as hazard tape or a tape cordon such as for queue management.
 *
 * @author Carl Jokl
 */
public class TapeSegmentStyle extends AbstractItemStyle<SegmentStyleType> implements SizedStyle, TexturedStyle, HTexPerMRepeatingStyle, HeightHoldingStyle, HeightOffsetStyle, Serializable {

    /**
     * The range of permitted values for the tape height of the rendered tape between the nodes.
     */
    public static final FloatValueRange TAPE_HEIGHT_RANGE = new FloatValueRange(0.0f, 2.0f, false, true);

    /**
     * The range of permitted values for the vertical offset of the hazard cone.
     */
    public static final FloatValueRange HEIGHT_OFFSET_RANGE = new FloatValueRange(-1000.0f, 1000.0f, true, true);

    /**
     * The range of permitted values for number of times to repeat the tape texture per meter on the tape.
     */
    public static final FloatValueRange H_TEXTURE_REPEATS_PER_METER_RANGE = new FloatValueRange(1.0f, 100.0f, true, true);

    private float tapeHeight;
    private float heightOffset;
    private float hTexRepeatsPerMeter;
    private String tapeCustomTexture;

    /**
     * Create a new instance of TapeSegmentStyle with default values.
     */
    public TapeSegmentStyle() {
        tapeHeight = 0.05f;
        heightOffset = 1.0f;
        hTexRepeatsPerMeter = 1.0f;
        tapeCustomTexture = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyleType getStyleType() {
        return CoreSegmentStyleType.TAPE;
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
                return tapeHeight;
            case 1:
                return heightOffset;
            case 2:
                return hTexRepeatsPerMeter;
            default:
                throw new IndexOutOfBoundsException(String.format("The specified size/offset value index: %d is outside the range of the three valid size/offset indices!", index));
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
                setHeightOffset(value);
                break;
            case 2:
                setHTexRepreatsPerM(value);
                break;
            default:
                throw new IndexOutOfBoundsException(String.format("The specified size/offset value index: %d is outside the range of the three valid size/offset indices!", index));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSizeOrOffsetLabel(int index) throws IndexOutOfBoundsException {
        switch (index) {
            case 0:
                return "Tape Height";
            case 1:
                return "Height Offset";
            case 2:
                return "Texture Repeats Per Meter";
            default:
                throw new IndexOutOfBoundsException(String.format("The specified size/offset value index: %d is outside the range of the three valid size/offset indices!", index));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FloatValueRange getSizeOrOffsetRange(int index) throws IndexOutOfBoundsException {
        switch (index) {
            case 0:
                return TAPE_HEIGHT_RANGE;
            case 1:
                return HEIGHT_OFFSET_RANGE;
            case 2:
                return H_TEXTURE_REPEATS_PER_METER_RANGE;
            default:
                throw new IndexOutOfBoundsException(String.format("The specified size/offset value index: %d is outside the range of the three valid size/offset indices!", index));
        }
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
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getImageCount() {
        return tapeCustomTexture != null ? 1 : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getImage(int index) throws IndexOutOfBoundsException {
        if (index == 0) {
            return tapeCustomTexture;
        }
        else {
            throw new IndexOutOfBoundsException(String.format("The specified image index: %d is outside the range of l image!", index));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setImage(int index, String imageURI) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (index == 0) {
            tapeCustomTexture = imageURI;
            return imageURI != null;
        }
        else {
            throw new IndexOutOfBoundsException(String.format("The specified image index: %d is outside the range of l image!", index));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addImage(String imageURI) {
        if (tapeCustomTexture == null) {
            tapeCustomTexture = imageURI;
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String removeImage() throws UnsupportedOperationException {
        if (tapeCustomTexture != null) {
            String textureURI = tapeCustomTexture;
            tapeCustomTexture = null;
            return textureURI;
        }
        else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String removeImage(int index) throws IndexOutOfBoundsException, UnsupportedOperationException {
        if (index == 0) {
            return removeImage();
        }
        else {
            throw new IndexOutOfBoundsException(String.format("The specified index: %d is outside the range of the one texture index!", index));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getImageLabel(int index) throws IndexOutOfBoundsException {
        if (index == 0) {
            return "Optional Tape Texture";
        }
        else {
            throw new IndexOutOfBoundsException(String.format("The specified index: %d is outside the range of the one texture index!", index));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getHeight() {
        return tapeHeight;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHeight(float height) throws IllegalArgumentException {
        if (TAPE_HEIGHT_RANGE.isInRange(height)) {
            tapeHeight = height;
        }
        else {
            throw new IllegalArgumentException(String.format("The tape span value: %g was outside the valid range: %s!", height, TAPE_HEIGHT_RANGE.toString()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FloatValueRange getSupportedHeightRange() {
        return TAPE_HEIGHT_RANGE;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public float getHTexRepeatsPerM() {
        return hTexRepeatsPerMeter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHTexRepreatsPerM(float repeatsPerMeter) throws IllegalArgumentException {
        if (H_TEXTURE_REPEATS_PER_METER_RANGE.isInRange(repeatsPerMeter)) {
            hTexRepeatsPerMeter = repeatsPerMeter;
        }
        else {
            throw new IllegalArgumentException(String.format("The texture horizontal repeats per meter value: %g was outside the valid range: %s!", repeatsPerMeter, H_TEXTURE_REPEATS_PER_METER_RANGE.toString()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FloatValueRange getHTexRepeatsPerMRange() {
        return H_TEXTURE_REPEATS_PER_METER_RANGE;
    }
}
