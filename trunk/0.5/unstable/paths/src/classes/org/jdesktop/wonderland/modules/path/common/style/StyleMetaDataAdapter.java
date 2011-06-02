package org.jdesktop.wonderland.modules.path.common.style;

import java.net.URI;
import org.jdesktop.wonderland.modules.path.common.FloatValue;
import org.jdesktop.wonderland.modules.path.common.IntegerValueRange;
import org.jdesktop.wonderland.modules.path.common.FloatValueRange;
import org.jdesktop.wonderland.modules.path.common.IntegerValue;
import org.jdesktop.wonderland.modules.path.common.MutableFloatValue;
import org.jdesktop.wonderland.modules.path.common.MutableIntegerValue;
import org.jdesktop.wonderland.modules.path.common.MutableTextValue;
import org.jdesktop.wonderland.modules.path.common.TextValue;
import org.jdesktop.wonderland.modules.path.common.URIValue;

/**
 * This class is an adapter wrapper / decorator around an ItemStyle for use to retrieve or set different style information
 * which may or may not actually be present in the specific ItemStyle.
 *
 * @author Carl Jokl
 */
public class StyleMetaDataAdapter {

    private ItemStyle wrappedStyle;

    /**
     * Create a new StyleMetaDataAdapter to wrap the specified ItemStyle and provide easy access to
     * common style attributes.
     *
     * @param wrappedStyle The ItemStyle which is wrapped by this StyleMetaDataAdapter.
     */
    public StyleMetaDataAdapter(ItemStyle wrappedStyle) {
        this.wrappedStyle = wrappedStyle;
    }

    /**
     * Get the ItemStyle which has been wrapped by this StyleMetaDataAdapter.
     *
     * @return The ItemStyle which is wrapped by this StyleMetaDataAdapter
     */
    public ItemStyle getStyle() {
        return wrappedStyle;
    }

    /**
     * Get the specified FloatStyleAttribute from the style if it exists.
     * 
     * @param attributeName The name of the FloatStyleAttribute to be returned.
     * @param defaultValue The default value of use if no FloatStyleAttribute exists in the style with the specified name.
     * @return The value of the FloatStyleAttribute or the default value if no FloatStyleAttribute exists in the style with
     *         the specified name.
     */
    public float getFloatAttribute(String attributeName, float defaultValue) {
        if (wrappedStyle != null) {
            StyleAttribute floatAttribute = wrappedStyle.getStyleAttribute(attributeName);
            if (floatAttribute instanceof FloatValue) {
                return ((FloatValue) floatAttribute).getFloat();
            }
        }
        return defaultValue;
    }

    /**
     * Set the specified FloatStyleAttribute to the specified value.
     *
     * @param value The float value of the FloatStyleAttribute to be set.
     * @return True if the FloatStyleAttribute was available to use to set the float value.
     * @throws IllegalArgumentException If the specified float value is outside the supported range.
     */
    public boolean setFloatAttribute(String attributeName, float value) throws IllegalArgumentException {
        if (wrappedStyle != null) {
            StyleAttribute floatAttribute = wrappedStyle.getStyleAttribute(attributeName);
            if (floatAttribute instanceof MutableFloatValue) {
                ((MutableFloatValue) floatAttribute).setFloat(value);
                return true;
            }
        }
        return false;
    }

    /**
     * Set the FloatStyleAttribute with the specified name for this style. If the FloatStyleAttribute does not already exist
     * then create one using the specified value range constraint.
     *
     * @param attributeName The name of the FloatStyleAttribute attribute to be set.
     * @param value The value of the FloatStyleAttribute to be set.
     * @param range The supported value range for the FloatStyleAttribute if it has to be created.
     *              The range can be null if the value range is to be unconstrained.
     * @return True if a FloatStyleAttribute was available to use to set this value or was able to be created.
     * @throws IllegalArgumentException If the specified height value is outside the supported range.
     */
    public boolean setFloatAttribute(String attributeName, float value, FloatValueRange range) throws IllegalArgumentException {
        if (wrappedStyle != null && attributeName != null) {
            StyleAttribute floatAttribute = wrappedStyle.getStyleAttribute(attributeName);
            if (floatAttribute instanceof MutableFloatValue) {
                ((MutableFloatValue) floatAttribute).setFloat(value);
                return true;
            }
            else if (floatAttribute == null) {
                return wrappedStyle.addStyleAttribute(new FloatStyleAttribute(attributeName, range, value));
            }
        }
        return false;
    }

    /**
     * Get the supported range of the FloatStyleAttribute with the specified name.
     *
     * @param attributeName The name of the FloatStyleAttribute for which to get the supported value range.
     * @return The FloatValueRange of the supported values for the specified FloatValueRange or null if the
     *         FloatStyleAttribute with the specified name does not exist or has no value constraint.
     */
    public FloatValueRange getSupportedFloatAttributeRange(String attributeName) {
        if (wrappedStyle != null) {
            StyleAttribute floatAttribute = wrappedStyle.getStyleAttribute(attributeName);
            if (floatAttribute instanceof MutableFloatValue) {
                return ((MutableFloatValue) floatAttribute).getConstraint();
            }
        }
        return null;
    }

    /**
     * Whether a FloatStyleAttribute with the specified attribute name is present in the style.
     *
     * @param attributeName The name of the FloatStyleAttribute to be checked to see if it is present in the style.
     * @return True if a FloatStyleAttribute exists with the specified attribute name in the style. False if no
     *         StyleAttribute exists with that name or an attribute which is not a FloatStyleAttribute exists.
     */
    public boolean isFloatAttributePresent(String attributeName) {
        return wrappedStyle != null && wrappedStyle.getStyleAttribute(attributeName) instanceof FloatValue;
    }

    /**
     * Get the specified IntegerStyleAttribute from the style if it exists.
     *
     * @param attributeName The name of the IntegerStyleAttribute to be returned.
     * @param defaultValue The default value of use if no IntegerStyleAttribute exists in the style with the specified name.
     * @return The value of the IntegerStyleAttribute or the default value if no IntegerStyleAttribute exists in the style with
     *         the specified name.
     */
    public int getIntegerAttribute(String attributeName, int defaultValue) {
        if (wrappedStyle != null) {
            StyleAttribute integerAttribute = wrappedStyle.getStyleAttribute(attributeName);
            if (integerAttribute instanceof IntegerValue) {
                return ((IntegerValue) integerAttribute).getInteger();
            }
        }
        return defaultValue;
    }

    /**
     * Set the specified IntegerStyleAttribute to the specified value.
     *
     * @param value The integer value of the IntegerStyleAttribute to be set.
     * @return True if the IntegerStyleAttribute was available to use to set the integer value.
     * @throws IllegalArgumentException If the specified integer value is outside the supported range.
     */
    public boolean setIntegerAttribute(String attributeName, int value) throws IllegalArgumentException {
        if (wrappedStyle != null) {
            StyleAttribute integerAttribute = wrappedStyle.getStyleAttribute(attributeName);
            if (integerAttribute instanceof MutableIntegerValue) {
                ((MutableIntegerValue) integerAttribute).setInteger(value);
                return true;
            }
        }
        return false;
    }

    /**
     * Set the IntegerStyleAttribute with the specified name for this style. If the IntegerStyleAttribute does not already exist
     * then create one using the specified value range constraint.
     *
     * @param attributeName The name of the IntegerStyleAttribute attribute to be set.
     * @param value The value of the IntegerStyleAttribute to be set.
     * @param range The supported value range for the IntegerValueAttribute if it has to be created.
     *              The range can be null if the value range is to be unconstrained.
     * @return True if an IntegerStyleAttribute was available to use to set this value or was able to be created.
     * @throws IllegalArgumentException If the specified integer value is outside the supported range.
     */
    public boolean setIntegerAttribute(String attributeName, int value, IntegerValueRange range) throws IllegalArgumentException {
        if (wrappedStyle != null && attributeName != null) {
            StyleAttribute integerAttribute = wrappedStyle.getStyleAttribute(attributeName);
            if (integerAttribute instanceof MutableIntegerValue) {
                ((MutableIntegerValue) integerAttribute).setInteger(value);
                return true;
            }
            else if (integerAttribute == null) {
                return wrappedStyle.addStyleAttribute(new IntegerStyleAttribute(attributeName, range, value));
            }
        }
        return false;
    }

    /**
     * Get the supported range of the IntegerStyleAttribute with the specified name.
     *
     * @param attributeName The name of the IntegerStyleAttribute for which to get the supported value range.
     * @return The IntegerValueRange of the supported values for the specified IntegerValueRange or null if the
     *         IntegerStyleAttribute with the specified name does not exist or has no value constraint.
     */
    public IntegerValueRange getSupportedIntegerAttributeRange(String attributeName) {
        if (wrappedStyle != null) {
            StyleAttribute integerAttribute = wrappedStyle.getStyleAttribute(attributeName);
            if (integerAttribute instanceof MutableIntegerValue) {
                return ((MutableIntegerValue) integerAttribute).getConstraint();
            }
        }
        return null;
    }

    /**
     * Whether a IntegerStyleAttribute with the specified attribute name is present in the style.
     *
     * @param attributeName The name of the IntegerStyleAttribute to be checked to see if it is present in the style.
     * @return True if a IntegerStyleAttribute exists with the specified attribute name in the style. False if no
     *         StyleAttribute exists with that name or an attribute which is not an IntegerStyleAttribute exists.
     */
    public boolean isIntegerAttributePresent(String attributeName) {
        return wrappedStyle != null && wrappedStyle.getStyleAttribute(attributeName) instanceof IntegerValue;
    }

    /**
     * Get the specified TextStyleAttribute from the style if it exists.
     *
     * @param attributeName The name of the TextStyleAttribute to be returned.
     * @param defaultValue The default value of use if no TextStyleAttribute exists in the style with the specified name.
     * @return The value of the TextStyleAttribute or the default value if no TextStyleAttribute exists in the style with
     *         the specified name.
     */
    public String getTextAttribute(String attributeName, String defaultValue) {
        if (wrappedStyle != null) {
            StyleAttribute textAttribute = wrappedStyle.getStyleAttribute(attributeName);
            if (textAttribute instanceof TextValue) {
                return ((TextValue) textAttribute).getText();
            }
        }
        return defaultValue;
    }

    /**
     * Set the specified TextStyleAttribute to the specified value.
     *
     * @param value The text value of the TextStyleAttribute to be set.
     * @param createIfMissing Whether a TextStyleAttribute should be created if no existing attribute exists with the specified name.
     * @return True if the TextStyleAttribute was available to use to set the text value.
     */
    public boolean setTextAttribute(String attributeName, String value, boolean createIfMissing) {
        if (wrappedStyle != null && attributeName != null) {
            StyleAttribute textAttribute = wrappedStyle.getStyleAttribute(attributeName);
            if(textAttribute instanceof MutableTextValue) {
                ((MutableTextValue) textAttribute).setText(value);
                return true;
            }
            else if(createIfMissing && textAttribute == null) {
                return wrappedStyle.addStyleAttribute(new TextStyleAttribute(attributeName, value));
            }
        }
        return false;
    }

    /**
     * Check if the TextStyleAttribute with the specified name is present within the style. The StyleAttribute may or may
     * not have a value set within it.
     *
     * @param attributeName The name of the TextStyleAttribute to be checked to see if it is present in the style.
     * @return True if a TextStyleAttribute exists in the style which has the specified name. False if no StyleAttribute
     *         with that name exists or a StyleAttribute which is not a TextStyleAttribute exists.
     */
    public boolean isTextAttributePresent(String attributeName) {
        return wrappedStyle != null && wrappedStyle.getStyleAttribute(attributeName) instanceof TextValue;
    }

    /**
     * Get the specified TextureStyleAttribute attribute from the style if it exists.
     *
     * @param attributeName The name of the TextureStyleAttribute to be returned.
     * @param defaultURI The default texture URI value of use if no TextureStyleAttribute exists in the style with the specified name.
     * @return The value of the TextureStyleAttribute or the default value if no TextStyleAttribute exists in the style with
     *         the specified name.
     */
    public URI getTextureAttribute(String attributeName, URI defaultURI) {
        if (wrappedStyle != null) {
            StyleAttribute textureAttribute = wrappedStyle.getStyleAttribute(attributeName);
            if (textureAttribute instanceof URIValue) {
                return ((URIValue) textureAttribute).getURI();
            }
        }
        return defaultURI;
    }

    /**
     * Set the specified TextureStyleAttribute to the specified value.
     *
     * @param uri The texture URI value of the TextureStyleAttribute to be set.
     * @param createIfMissing Whether a TextureStyleAttribute should be created if no existing attribute exists with the specified name.
     * @return True if the TextureStyleAttribute was available to use to set the texture URI value.
     */
    public boolean setTextureAttribute(String attributeName, URI uri, boolean createIfMissing) {
        if (wrappedStyle != null && attributeName != null) {
            StyleAttribute textureAttribute = wrappedStyle.getStyleAttribute(attributeName);
            if (textureAttribute instanceof TextureStyleAttribute) {
                ((TextureStyleAttribute) textureAttribute).setURI(uri);
                return true;
            }
            else if (createIfMissing && textureAttribute == null) {
                return wrappedStyle.addStyleAttribute(new TextureStyleAttribute(attributeName, uri));
            }
        }
        return false;
    }

    /**
     * Get whether a TextureStyleAttribute with the specified name is present within the style. This TextureStyleAttribute may or may
     * not have a texture URI set.
     *
     * @param attributeName The name of the TextureStyleAttribute to be checked to see if it is present within the style.
     * @return True if a TextureStyleAttribute with the specified name is present in the style. False if no StyleAttribute is present
     *         in the style with the specified name or the StyleAttribute with that name was not a TextureStyleAttribute.
     */
    public boolean isTextureAttributePresent(String attributeName) {
        return wrappedStyle != null && wrappedStyle.getStyleAttribute(attributeName) instanceof TextureStyleAttribute;
    }

    /**
     * Get the specified ColorStyleAttribute from the style if it exists.
     *
     * @param attributeName The name of the TextureStyleAttribute to be returned.
     * @param defaultRed The default red component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultGreen The default green component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultBlue The default blue component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @return The an array of color components representing the color in the specified color attribute or the default value
     *         if no ColorStyleAttribute exists in the style with the specified name.
     */
    public float[] getColorAttribute(String attributeName, float defaultRed, float defaultGreen, float defaultBlue) {
        return getColorAttribute(attributeName, defaultRed, defaultGreen, defaultBlue, 1.0f);
    }

    /**
     * Get the specified ColorStyleAttribute from the style if it exists.
     *
     * @param attributeName The name of the TextureStyleAttribute to be returned.
     * @param defaultRed The default red component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultGreen The default green component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultBlue The default blue component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultAlpha The default alpha component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @return The an array of color components representing the color in the specified color attribute or the default value
     *         if no ColorStyleAttribute exists in the style with the specified name.
     */
    public float[] getColorAttribute(String attributeName, float defaultRed, float defaultGreen, float defaultBlue, float defaultAlpha) {
        if (wrappedStyle != null) {
            StyleAttribute colorAttribute = wrappedStyle.getStyleAttribute(attributeName);
            if (colorAttribute instanceof ColorStyleAttribute) {
                return ((ColorStyleAttribute) colorAttribute).toArray();
            }
        }
        return new float[] { defaultRed, defaultGreen, defaultBlue, defaultAlpha };
    }

     /**
     * Set the specified ColorStyleAttribute in the style if it exists.
     *
     * @param attributeName The name of the color attribute to be set.
     * @param red The red component value to use in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param green The green component value to use in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param blue The blue component value to use if in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @return True if the color value was not null and the right size and the ColorStyleAttribute with the specified name was available
     *         to set the value.
     */
    public boolean setColorAttribute(String attributeName, float red, float green, float blue, boolean createIfMissing) {
        return setColorAttribute(attributeName, red, green, blue, 1.0f, createIfMissing);
    }

    /**
     * Set the specified ColorStyleAttribute in the style if it exists.
     *
     * @param attributeName The name of the color attribute to be set.
     * @param red The red component value to use in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param green The green component value to use in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param blue The blue component value to use if in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param alpha The alpha component value to use if in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @return True if the color value was not null and the right size and the ColorStyleAttribute with the specified name was available
     *         to set the value.
     */
    public boolean setColorAttribute(String attributeName, float red, float green, float blue, float alpha, boolean createIfMissing) {
        if (wrappedStyle != null) {
            StyleAttribute colorAttribute = wrappedStyle.getStyleAttribute(attributeName);
            if (colorAttribute instanceof ColorStyleAttribute) {
               ((ColorStyleAttribute) colorAttribute).set(red, green, blue, alpha);
            }
        }
        return false;
    }

    /**
     * Whether a ColorStyleAttribute with the specified name is present in the style.
     *
     * @param attributeName The name of the ColorStyleAttribute to check to find if it is present.
     * @return True if a ColorStyleAttribute with the specified name is present in the style.
     *
     */
    public boolean isColorAttributePresent(String attributeName) {
        return wrappedStyle != null && wrappedStyle.getStyleAttribute(attributeName) instanceof ColorStyleAttribute;
    }

    /**
     * Get the primary height value of this style.
     *
     * @param defaultHeight The default height value to use if no other height value is available.
     * @return The primary height value of this style or the specified default height value if no height StyleAttribute is set.
     */
    public float getHeight(float defaultHeight) {
        return getFloatAttribute(StandardStyleAttribute.HEIGHT.getName(), defaultHeight);
    }

    /**
     * Set the primary height value of this style.
     *
     * @param height The height value of this style.
     * @return True if a height attribute was available to use to set this height value.
     * @throws IllegalArgumentException If the specified height value is outside the supported range.
     */
    public boolean setHeight(float height) throws IllegalArgumentException {
        return setFloatAttribute(StandardStyleAttribute.HEIGHT.getName(), height);
    }

    /**
     * Set the primary height value of this style. If the height attribute does not already exist
     * then create one using the specified value range constraint.
     *
     * @param height The height value of this style.
     * @param range The supported value range for the height attribute if it has to be created.
     *              The range can be null if the value range is to be unconstrained.
     * @return True if a height attribute was available to use to set this height value.
     * @throws IllegalArgumentException If the specified height value is outside the supported range.
     */
    public boolean setHeight(float height, FloatValueRange range) throws IllegalArgumentException {
        return setFloatAttribute(StandardStyleAttribute.HEIGHT.getName(), height, range);
    }

    /**
     * Get the supported range of height values.
     *
     * @return The range of supported height values or null if no height StyleAttribute is set.
     */
    public FloatValueRange getSupportedHeightRange() {
        return getSupportedFloatAttributeRange(StandardStyleAttribute.HEIGHT.getName());
    }

    /**
     * Get whether the height StyleAttribute is present within this style.
     *
     * @return True if the height style attribute is present within this style. False otherwise.
     */
    public boolean isHeightPresent() {
        return isFloatAttributePresent(StandardStyleAttribute.HEIGHT.getName());
    }

    /**
     * Get the primary length value of this style.
     *
     * @param defaultLength The default length to use for this style if no length StyleAttribute is set.
     * @return The primary length value of this style or the specified default value if no length StyleAttribute is set.
     */
    public float getLength(float defaultLength) {
        return getFloatAttribute(StandardStyleAttribute.LENGTH.getName(), defaultLength);
    }

    /**
     * Set the primary length value of this style.
     *
     * @param length The length value of this style.
     * @throws IllegalArgumentException If the specified length value is outside the supported range.
     */
    public boolean setLength(float length) throws IllegalArgumentException {
        return setFloatAttribute(StandardStyleAttribute.LENGTH.getName(), length);
    }

    /**
     * Get the supported range of length values.
     *
     * @return The range of supported length values.
     */
    public FloatValueRange getSupportedLengthRange() {
        return getSupportedFloatAttributeRange(StandardStyleAttribute.LENGTH.getName());
    }

    /**
     * Get whether the length StyleAttribute is present within this style.
     *
     * @return True if the length StyleAttribute is present within this style. False otherwise.
     */
    public boolean isLengthPresent() {
        return isFloatAttributePresent(StandardStyleAttribute.LENGTH.getName());
    }

    /**
     * Get the primary width value of this style.
     *
     * @return The primary width value of this style.
     */
    public float getWidth(float defaultWidth) {
        return getFloatAttribute(StandardStyleAttribute.LENGTH.getName(), defaultWidth);
    }

    /**
     * Set the primary width value of this style.
     *
     * @param width The width value of this style.
     * @throws IllegalArgumentException If the specified width value is outside the supported range.
     */
    public boolean setWidth(float width) throws IllegalArgumentException {
        return setFloatAttribute(StandardStyleAttribute.LENGTH.getName(), width);
    }

    /**
     * Get the supported range of width values.
     *
     * @return The range of supported width values.
     */
    public FloatValueRange getSupportedWidthRange() {
        return getSupportedFloatAttributeRange(StandardStyleAttribute.LENGTH.getName());
    }

    /**
     * Get whether the width StyleAttribute is present within this style.
     *
     * @return True if the width StyleAttribute is present within this style. False otherwise.
     */
    public boolean isWidthPresent() {
        return isFloatAttributePresent(StandardStyleAttribute.WIDTH.getName());
    }

    /**
     * Get the x dimension offset value for this style.
     *
     * @param defaultOffset The default x offset value to use if no x offset StyleAttribute is set.
     * @return Either the x offset value from a StyleAttribute or the default supplied value if it is not set.
     */
    public float getXOffset(float defaultOffset) {
        return getFloatAttribute(StandardStyleAttribute.X_OFFSET.getName(), defaultOffset);
    }

    /**
     * Set the x offset value of this style.
     *
     * @param offset The x offset value of this style.
     * @return True if the x offset StyleAttribute existed and the value was able to be set.
     * @throws IllegalArgumentException If the specified x offset value is outside the supported range.
     */
    public boolean setXOffset(float offset) throws IllegalArgumentException {
        return setFloatAttribute(StandardStyleAttribute.X_OFFSET.getName(), offset);
    }

    /**
     * Get the supported range of x offset values.
     *
     * @return The range of supported x offset values or null if the range is not constrained.
     */
    public FloatValueRange getSupportedXOffsetRange() {
        return getSupportedFloatAttributeRange(StandardStyleAttribute.X_OFFSET.getName());
    }

    /**
     * Get whether the x offset StyleAttribute is present within this style.
     *
     * @return True if the x offset StyleAttribute is present within this style. False otherwise.
     */
    public boolean isXOffsetPresent() {
        return isFloatAttributePresent(StandardStyleAttribute.X_OFFSET.getName());
    }

    /**
     * Get the y dimension offset value for this style.
     *
     * @param defaultOffset The default y offset value to use if no y offset StyleAttribute is set.
     * @return Either the y offset value from a StyleAttribute or the default supplied value if it is not set.
     */
    public float getYOffset(float defaultOffset) {
        return getFloatAttribute(StandardStyleAttribute.Y_OFFSET.getName(), defaultOffset);
    }

    /**
     * Set the y offset value of this style.
     *
     * @param offset The y offset value of this style.
     * @return True if the y offset StyleAttribute existed and the value was able to be set.
     * @throws IllegalArgumentException If the specified y offset value is outside the supported range.
     */
    public boolean setYOffset(float offset) throws IllegalArgumentException {
        return setFloatAttribute(StandardStyleAttribute.Y_OFFSET.getName(), offset);
    }

    /**
     * Get the supported range of y offset values.
     *
     * @return The range of supported y offset values or null if the range is not constrained.
     */
    public FloatValueRange getSupportedYOffsetRange() {
        return getSupportedFloatAttributeRange(StandardStyleAttribute.Y_OFFSET.getName());
    }

    /**
     * Get whether the y offset StyleAttribute is present within this style.
     *
     * @return True if the y offset StyleAttribute is present within this style. False otherwise.
     */
    public boolean isYOffsetPresent() {
        return isFloatAttributePresent(StandardStyleAttribute.Y_OFFSET.getName());
    }

    /**
     * Get the z dimension offset value for this style.
     *
     * @param defaultOffset The default z offset value to use if no z offset StyleAttribute is set.
     * @return Either the z offset value from a StyleAttribute or the default supplied value if it is not set.
     */
    public float getZOffset(float defaultOffset) {
        return getFloatAttribute(StandardStyleAttribute.Z_OFFSET.getName(), defaultOffset);
    }

    /**
     * Set the z offset value of this style.
     *
     * @param offset The z offset value of this style.
     * @return True if the z offset StyleAttribute existed and the value was able to be set.
     * @throws IllegalArgumentException If the specified z offset value is outside the supported range.
     */
    public boolean setZOffset(float offset) throws IllegalArgumentException {
        return setFloatAttribute(StandardStyleAttribute.Z_OFFSET.getName(), offset);
    }

    /**
     * Get the supported range of z offset values.
     *
     * @return The range of supported z offset values or null if the range is not constrained.
     */
    public FloatValueRange getSupportedZOffsetRange() {
        return getSupportedFloatAttributeRange(StandardStyleAttribute.Z_OFFSET.getName());
    }

    /**
     * Get whether the z offset StyleAttribute is present within this style.
     *
     * @return True if the z offset StyleAttribute is present within this style. False otherwise.
     */
    public boolean isZOffsetPresent() {
        return isFloatAttributePresent(StandardStyleAttribute.Z_OFFSET.getName());
    }
    
    /**
     * Get the radius value for this style.
     *
     * @param defaultRadius The default radius value to use if no radius StyleAttribute is set.
     * @return Either the radius value from a StyleAttribute or the default supplied radius value if it is not set.
     */
    public float getRadius(float defaultRadius) {
        return getFloatAttribute(StandardStyleAttribute.RADIUS.getName(), defaultRadius);
    }

    /**
     * Set the radius value of this style.
     *
     * @param radius The radius value of this style.
     * @return True if the radius StyleAttribute existed and the value was able to be set.
     * @throws IllegalArgumentException If the specified radius value is outside the supported range.
     */
    public boolean setRadius(float radius) throws IllegalArgumentException {
        return setFloatAttribute(StandardStyleAttribute.RADIUS.getName(), radius);
    }

    /**
     * Get the supported range of radius values.
     *
     * @return The range of supported radius values or null if the range is not constrained.
     */
    public FloatValueRange getSupportedRadiusRange() {
        return getSupportedFloatAttributeRange(StandardStyleAttribute.RADIUS.getName());
    }

    /**
     * Get whether the radius StyleAttribute is present within this style.
     *
     * @return True if the radius StyleAttribute is present within this style. False otherwise.
     */
    public boolean isRadiusPresent() {
        return isFloatAttributePresent(StandardStyleAttribute.RADIUS.getName());
    }

    /**
     * Get the radius 1 value for this style.
     *
     * @param defaultRadius The default radius 1 value to use if no radius 1 StyleAttribute is set.
     * @param cascade Whether if no radius 1 StyleAttribute exists the radius value should be cascaded and used instead.
     * @return Either the radius 1 value from a StyleAttribute or the radius attribute value if it has been cascaded
     *         or the default supplied radius 1 value if it is not set.
     */
    public float getRadius1(float defaultRadius, boolean cascade) {
        if (isFloatAttributePresent(StandardStyleAttribute.RADIUS_1.getName())) {
            return getFloatAttribute(StandardStyleAttribute.RADIUS_1.getName(), defaultRadius);
        }
        else if (isFloatAttributePresent(StandardStyleAttribute.RADIUS.getName())) {
            return getFloatAttribute(StandardStyleAttribute.RADIUS.getName(), defaultRadius);
        }
        else {
            return defaultRadius;
        }
    }

    /**
     * Set the radius 1 value for this style.
     *
     * @param radius The radius 1 value for this style.
     * @return True if the radius 1 StyleAttribute existed and the value was able to be set.
     * @throws IllegalArgumentException If the specified radius 1 value is outside the supported range.
     */
    public boolean setRadius1(float radius) throws IllegalArgumentException {
        return setFloatAttribute(StandardStyleAttribute.RADIUS_1.getName(), radius);
    }

    /**
     * Get the supported range of radius 1 values.
     *
     * @param cascade Whether if no radius 1 StyleAttribute exists the radius value should be cascaded and used instead.
     *
     * @return The range of supported radius 1 values or null if the range is not constrained.
     */
    public FloatValueRange getSupportedRadius1Range(boolean cascade) {
        if (isFloatAttributePresent(StandardStyleAttribute.RADIUS_1.getName())) {
            return getSupportedFloatAttributeRange(StandardStyleAttribute.RADIUS_1.getName());
        }
        else if (cascade && isFloatAttributePresent(StandardStyleAttribute.RADIUS.getName())) {
            return getSupportedFloatAttributeRange(StandardStyleAttribute.RADIUS.getName());
        }
        else {
            return null;
        }
    }

    /**
     * Get whether the radius 1 StyleAttribute is present within this style.
     *
     * @param cascade Whether if no radius 1 StyleAttribute exists the radius value should be cascaded and used instead.
     *
     * @return True if the radius 1 StyleAttribute is present (or the radius value is set and will be cascaded) within this style.
     *         False otherwise.
     */
    public boolean isRadius1Present(boolean cascade) {
        return isFloatAttributePresent(StandardStyleAttribute.RADIUS_1.getName()) && (cascade && isFloatAttributePresent(StandardStyleAttribute.RADIUS.getName()));
    }

    /**
     * Get the radius 2 value for this style.
     *
     * @param defaultRadius The default radius 2 value to use if no radius 2 StyleAttribute is set.
     * @param cascade Whether if no radius 2 StyleAttribute exists the radius value should be cascaded and used instead.
     * @return Either the radius 2 value from a StyleAttribute or the radius attribute value if it has been cascaded
     *         or the default supplied radius 2 value if it is not set.
     */
    public float getRadius2(float defaultRadius, boolean cascade) {
        if (isFloatAttributePresent(StandardStyleAttribute.RADIUS_2.getName())) {
            return getFloatAttribute(StandardStyleAttribute.RADIUS_2.getName(), defaultRadius);
        }
        else if (cascade) {
            if (isFloatAttributePresent(StandardStyleAttribute.RADIUS.getName())) {
                return getFloatAttribute(StandardStyleAttribute.RADIUS.getName(), defaultRadius);
            }
            else if (isFloatAttributePresent(StandardStyleAttribute.RADIUS_1.getName())) {
                return getFloatAttribute(StandardStyleAttribute.RADIUS_1.getName(), defaultRadius);
            }
        }
        return defaultRadius;
    }

    /**
     * Set the radius 2 value for this style.
     *
     * @param radius The radius 2 value for this style.
     * @return True if the radius 2 StyleAttribute existed and the value was able to be set.
     * @throws IllegalArgumentException If the specified radius 2 value is outside the supported range.
     */
    public boolean setRadius2(float radius) throws IllegalArgumentException {
        return setFloatAttribute(StandardStyleAttribute.RADIUS_2.getName(), radius);
    }

    /**
     * Get the supported range of radius 2 values.
     *
     * @return The range of supported radius 2 values or null if the range is not constrained.
     */
    public FloatValueRange getSupportedRadius2Range(boolean cascade) {
        if (isFloatAttributePresent(StandardStyleAttribute.RADIUS_2.getName())) {
            return getSupportedFloatAttributeRange(StandardStyleAttribute.RADIUS_2.getName());
        }
        else if (cascade) {
            if (isFloatAttributePresent(StandardStyleAttribute.RADIUS.getName())) {
                return getSupportedFloatAttributeRange(StandardStyleAttribute.RADIUS.getName());
            }
            else if (isFloatAttributePresent(StandardStyleAttribute.RADIUS_1.getName())) {
                return getSupportedFloatAttributeRange(StandardStyleAttribute.RADIUS_1.getName());
            }
        }
        return null;
    }

    /**
     * Get whether the radius 2 StyleAttribute is present within this style.
     *
     * @param cascade Whether if no radius 2 StyleAttribute exists the radius or radius 1 values should be cascaded and used instead.
     *
     * @return True if the radius 2 StyleAttribute is present (or the radius or radius 1 value is set and will be cascaded) within this style.
     *         False otherwise.
     */
    public boolean isRadius2Present(boolean cascade) {
        return isFloatAttributePresent(StandardStyleAttribute.RADIUS_2.getName()) && (cascade && isFloatAttributePresent(StandardStyleAttribute.RADIUS.getName()) && isFloatAttributePresent(StandardStyleAttribute.RADIUS_1.getName()));
    }

    /**
     * Get the texture U direction texture repeats per meter value for this style.
     *
     * @param defaultRepeats The default U direction texture repeats per meter value to use if no StyleAttribute for this value is set.
     * @return Either the U direction texture repeats per meter value from a StyleAttribute or the default supplied value if it is not set.
     */
    public float getUTextRepeatsPerM(float defaultRepeats) {
        return getFloatAttribute(StandardStyleAttribute.U_REPEATS_PER_M.getName(), defaultRepeats);
    }

    /**
     * Set the texture U direction texture repeats per meter value of this style.
     *
     * @param repeats The U direction texture repeats per meter value to set for this style.
     * @return True if the U direction texture repeats per meter StyleAttribute existed and the value was able to be set.
     * @throws IllegalArgumentException If the specified U direction texture repeats per meter value is outside the supported range.
     */
    public boolean setUTextRepeatsPerM(float repeats) throws IllegalArgumentException {
        return setFloatAttribute(StandardStyleAttribute.U_REPEATS_PER_M.getName(), repeats);
    }

    /**
     * Get the supported range of U direction texture repeats per meter values.
     *
     * @return The range of supported U direction texture repeats per meter values or null if the range is not constrained.
     */
    public FloatValueRange getSupportedUTextRepeatsPerMRange() {
        return getSupportedFloatAttributeRange(StandardStyleAttribute.U_REPEATS_PER_M.getName());
    }

    /**
     * Get whether the U direction texture repeats per meter StyleAttribute is present within this style.
     *
     * @return True if the U direction texture repeats per meter StyleAttribute is present within this style. False otherwise.
     */
    public boolean isUTextRepeatsPerMPresent() {
        return isFloatAttributePresent(StandardStyleAttribute.U_REPEATS_PER_M.getName());
    }

    /**
     * Get the texture V direction texture repeats per meter value for this style.
     *
     * @param defaultRepeats The default V direction texture repeats per meter value to use if no StyleAttribute for this value is set.
     * @return Either the V direction texture repeats per meter value from a StyleAttribute or the default supplied value if it is not set.
     */
    public float getVTextRepeatsPerM(float defaultRepeats) {
        return getFloatAttribute(StandardStyleAttribute.V_REPEATS_PER_M.getName(), defaultRepeats);
    }

    /**
     * Set the texture V direction texture repeats per meter value of this style.
     *
     * @param repeats The V direction texture repeats per meter value to set for this style.
     * @return True if the V direction texture repeats per meter StyleAttribute existed and the value was able to be set.
     * @throws IllegalArgumentException If the specified V direction texture repeats per meter value is outside the supported range.
     */
    public boolean setVTextRepeatsPerM(float repeats) throws IllegalArgumentException {
        return setFloatAttribute(StandardStyleAttribute.V_REPEATS_PER_M.getName(), repeats);
    }

    /**
     * Get the supported range of V direction texture repeats per meter values.
     *
     * @return The range of supported V direction texture repeats per meter values or null if the range is not constrained.
     */
    public FloatValueRange getSupportedVTextRepeatsPerMRange() {
        return getSupportedFloatAttributeRange(StandardStyleAttribute.V_REPEATS_PER_M.getName());
    }

    /**
     * Get whether the V direction texture repeats per meter StyleAttribute is present within this style.
     *
     * @return True if the V direction texture repeats per meter StyleAttribute is present within this style. False otherwise.
     */
    public boolean isVTextRepeatsPerMPresent() {
        return isFloatAttributePresent(StandardStyleAttribute.V_REPEATS_PER_M.getName());
    }

    /**
     * Get the main text used in the style (if any).
     *
     * @param defaultText The default text to use if no main text attribute is set from which to
     *                    get the text value.
     * @return The main text from a TextStyleAttribute set within the style or the specified
     *         default texture URI supplied.
     */
    public String getMainText(String defaultText) {
        return getTextAttribute(StandardStyleAttribute.TEXT.getName(), defaultText);
    }

    /**
     * Set the main text used in the style (if any).
     *
     * @param text The text to be set.
     * @param createIfMissing Whether a TextStyleAttribute should be created if none already exists.
     * @return True if the TextStyleAttribute of the main text was able to be set or a
     *         TextStyleAttribute was able to be created if specified one should be created if missing
     *         and no exiting TextStyleAttribute existed.
     */
    public boolean setMainText(String text, boolean createIfMissing) {
        return setTextAttribute(StandardStyleAttribute.TEXT.getName(), text, createIfMissing);
    }

    /**
     * Whether a TextStyleAttribute is present for the main text within the style.
     *
     * @return True if a TextStyleAttribute exists for the main text within the style.
     *         False otherwise.
     */
    public boolean isMainTextPresent() {
        return isTextAttributePresent(StandardStyleAttribute.TEXT.getName());
    }

    /**
     * Get the URI of the main texture used in the style (if any).
     *
     * @param defaultTextureURI The default texture URI to use if no main texture attribute is set from which to
     *                          get the texture URI.
     * @return The URI of the main texture from a TextureStyleAttribute set within the style or the specified
     *         default texture URI supplied.
     */
    public URI getMainTexture(URI defaultTextureURI) {
        return getTextureAttribute(StandardStyleAttribute.TEXTURE.getName(), defaultTextureURI);
    }

    /**
     * Set the URI of the main texture used in the style (if any).
     *
     * @param textureURI The URI of the texture to be set.
     * @param createIfMissing Whether a TextureStyleAttribute should be created if none already exists.
     * @return True if the URI of the TextureStyleAttribute of the main texture was able to be set or a
     *         TextureStyleAttribute was able to be created if specified one should be created if missing
     *         and no exiting TextureStyleAttribute existed.
     */
    public boolean setMainTexture(String textureURI, boolean createIfMissing) {
        return setTextAttribute(StandardStyleAttribute.TEXTURE.getName(), textureURI, createIfMissing);
    }

    /**
     * Whether a TextureStyleAttribute is present for the main texture within the style.
     *
     * @return True if a TextureStyleAttribute exists for the main texture within the style.
     *         False otherwise.
     */
    public boolean isMainTexturePresent() {
        return isTextureAttributePresent(StandardStyleAttribute.TEXTURE.getName());
    }

    /**
     * Get the main color which is part of this style.
     *
     * @param defaultRed The default red component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultGreen The default green component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultBlue The default blue component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultAlpha The default alpha component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @return The main color for the style from a ColorStyleAttribute or the default color if no
     *         ColorStyleAttribute is provided.
     */
    public float[] getMainColor(float defaultRed, float defaultGreen, float defaultBlue, float defaultAlpha) {
        return getColorAttribute(StandardStyleAttribute.COLOR.getName(), defaultRed, defaultGreen, defaultBlue, defaultAlpha);
    }

    /**
     * Set the main color of this style to the specified value.
     *
     * @param red The red component value to use in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param green The green component value to use in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param blue The blue component value to use if in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param alpha The alpha component value to use if in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param createIfMissing Whether a ColorStyleAttribute should be created if there is no existing ColorStyleAttribute for
     *                        the main color.
     * @return True if the main color was able to be set. If no ColorStyleAttribute existed and createIfMissing was true and 
     *              a ColorStyleAttribute was able to be created and added true is also returned. If the supplied color array
     *              is null or not 3 or 4 elements in length or in any other case false is returned.
     */
    public boolean setMainColor(float red, float green, float blue, float alpha, boolean createIfMissing) {
        return setColorAttribute(StandardStyleAttribute.COLOR.getName(), red, green, blue, alpha, createIfMissing);
    }

    /**
     * Whether a ColorStyleAttribute exists for the main color within the style.
     *
     * @return True if a ColorStyleAttribute exists for the main color within the style.
     */
    public boolean isMainColorPresent() {
        return isColorAttributePresent(StandardStyleAttribute.COLOR.getName());
    }

    /**
     * Get the foreground color which is part of this style.
     *
     * @param defaultRed The default red component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultGreen The default green component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultBlue The default blue component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultAlpha The default alpha component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param cascade Whether if the specific foreground ColorStyleAttribute is not set but the main
     *                ColorStyleAttribute is set the main color should cascade to be used as the
     *                foreground color.
     * @return The foreground color for the style from a ColorStyleAttribute or a cascaded color if applicable
     *         or the default color if no other ColorStyleAttribute is provided which can be used.
     */
    public float[] getForegroundColor(float defaultRed, float defaultGreen, float defaultBlue, float defaultAlpha, boolean cascade) {
        if (isColorAttributePresent(StandardStyleAttribute.FOREGROUND_COLOR.getName())) {
            return getColorAttribute(StandardStyleAttribute.FOREGROUND_COLOR.getName(), defaultRed, defaultGreen, defaultBlue, defaultAlpha);
        }
        else if (cascade && isColorAttributePresent(StandardStyleAttribute.COLOR.getName())) {
            return getColorAttribute(StandardStyleAttribute.COLOR.getName(), defaultRed, defaultGreen, defaultBlue, defaultAlpha);
        }
        else {
            return new float[] { defaultRed, defaultGreen, defaultBlue, defaultAlpha };
        }
    }

    /**
     * Set the foreground color of this style to the specified value.
     *
     * @param red The red component value to use in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param green The green component value to use in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param blue The blue component value to use if in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param alpha The alpha component value to use if in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param createIfMissing Whether a ColorStyleAttribute should be created if there is no existing ColorStyleAttribute for
     *                        the foreground color.
     * @return True if the foreground color was able to be set. If no ColorStyleAttribute existed and createIfMissing was true and
     *              a ColorStyleAttribute was able to be created and added true is also returned. If the supplied color component array
     *              is null or not 3 or 4 elements in length or in any other case false is returned.
     */
    public boolean setForegroundColor(float red, float green, float blue, float alpha, boolean createIfMissing) {
        return setColorAttribute(StandardStyleAttribute.FOREGROUND_COLOR.getName(), red, green, blue, alpha, createIfMissing);
    }

    /**
     * Whether a ColorStyleAttribute exists for the foreground color within the style.
     *
     * @param cascade Whether if the specific foreground ColorStyleAttribute is not set but the main
     *                ColorStyleAttribute is set the main color should cascade to be used as the
     *                foreground color.
     * @return True if a ColorStyleAttribute exists for the foreground color within the style (or can be cascaded).
     */
    public boolean isForegroundColorPresent(boolean cascade) {
        return isColorAttributePresent(StandardStyleAttribute.FOREGROUND_COLOR.getName()) || (cascade && isColorAttributePresent(StandardStyleAttribute.COLOR.getName()));
    }

    /**
     * Get the background color which is part of this style.
     *
     * @param defaultRed The default red component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultGreen The default green component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultBlue The default blue component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultAlpha The default alpha component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param cascade Whether if the specific background ColorStyleAttribute is not set but the main
     *                ColorStyleAttribute is set the main color should cascade to be used as the
     *                background color.
     * @return The background color for the style from a ColorStyleAttribute or the main color cascaded down
     *         no background color exists and the main color is cascaded instead or the default color if no
     *         ColorStyleAttribute is available.
     */
    public float[] getBackgroundColor(float defaultRed, float defaultGreen, float defaultBlue, float defaultAlpha, boolean cascade) {
        if (isColorAttributePresent(StandardStyleAttribute.BACKGROUND_COLOR.getName())) {
            return getColorAttribute(StandardStyleAttribute.BACKGROUND_COLOR.getName(), defaultRed, defaultGreen, defaultBlue, defaultAlpha);
        }
        else if (cascade && isColorAttributePresent(StandardStyleAttribute.COLOR.getName())) {
            return getColorAttribute(StandardStyleAttribute.COLOR.getName(), defaultRed, defaultGreen, defaultBlue, defaultAlpha);
        }
        else {
            return new float[] { defaultRed, defaultGreen, defaultBlue, defaultAlpha };
        }
    }

    /**
     * Set the background color of this style to the specified value.
     *
     * @param red The red component value to use in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param green The green component value to use in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param blue The blue component value to use if in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param alpha The alpha component value to use if in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param createIfMissing Whether a ColorStyleAttribute should be created if there is no existing ColorStyleAttribute for
     *                        the background color.
     * @return True if the background color was able to be set. If no ColorStyleAttribute existed and createIfMissing was true and
     *              a ColorStyleAttribute was able to be created and added true is also returned. If the supplied color array was null or 
     *              not the right size or in any other case false is returned.
     */
    public boolean setBackgroundColor(float red, float green, float blue, float alpha, boolean createIfMissing) {
        return setColorAttribute(StandardStyleAttribute.BACKGROUND_COLOR.getName(), red, green, blue, alpha, createIfMissing);
    }

    /**
     * Whether a ColorStyleAttribute exists for the main color within the style.
     *
     * @param cascade Whether if the specific background ColorStyleAttribute is not set but the main
     *                ColorStyleAttribute is set the main color should cascade to be used as the
     *                background color.
     * @return True if a ColorStyleAttribute exists for the background color within the style (or can be cascaded).
     */
    public boolean isBackgroundColorPresent(boolean cascade) {
        return isColorAttributePresent(StandardStyleAttribute.BACKGROUND_COLOR.getName()) || (cascade && isColorAttributePresent(StandardStyleAttribute.COLOR.getName()));
    }

    /**
     * Get the front color which is part of this style.
     *
     * @param defaultRed The default red component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultGreen The default green component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultBlue The default blue component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param defaultAlpha The default alpha component value to use if no ColorStyleAttribute is avaliable (the value should be between 0.0 and 1.0).
     * @param cascade Whether if the specific front ColorStyleAttribute is not set but the main
     *                ColorStyleAttribute or other less specific color attributes which could cover the front
     *                are set the color should cascade to be used as the front color.
     * @return The front color for the style from a ColorStyleAttribute or a cascaded color if applicable
     *         or the default color if no other ColorStyleAttribute is provided which can be used.
     */
    public float[] getFrontColor(float defaultRed, float defaultGreen, float defaultBlue, float defaultAlpha, boolean cascade) {
        if (isColorAttributePresent(StandardStyleAttribute.FRONT_COLOR.getName())) {
            return getColorAttribute(StandardStyleAttribute.FRONT_COLOR.getName(), defaultRed, defaultGreen, defaultBlue, defaultAlpha);
        }
        else if (cascade) {
            if (isColorAttributePresent(StandardStyleAttribute.FRONT_AND_BACK_COLOR.getName())) {
                return getColorAttribute(StandardStyleAttribute.FRONT_AND_BACK_COLOR.getName(), defaultRed, defaultGreen, defaultBlue, defaultAlpha);
            }
            if (isColorAttributePresent(StandardStyleAttribute.COLOR.getName())) {
                return getColorAttribute(StandardStyleAttribute.COLOR.getName(), defaultRed, defaultGreen, defaultBlue, defaultAlpha);
            }
        }
        return new float[] { defaultRed, defaultGreen, defaultBlue, defaultAlpha };
    }

    /**
     * Set the front color of this style to the specified value.
     *
     * @param red The red component value to use in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param green The green component value to use in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param blue The blue component value to use if in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param alpha The alpha component value to use if in setting the ColorStyleAttribute (the value should be between 0.0 and 1.0).
     * @param createIfMissing Whether a ColorStyleAttribute should be created if there is no existing ColorStyleAttribute for
     *                        the front color.
     * @return True if the front color was able to be set. If no ColorStyleAttribute existed and createIfMissing was true and
     *              a ColorStyleAttribute was able to be created and added true is also returned. If the supplied color component array
     *              is null or not 3 or 4 elements in length or in any other case false is returned.
     */
    public boolean setFrontColor(float red, float green, float blue, float alpha, boolean createIfMissing) {
        return setColorAttribute(StandardStyleAttribute.FOREGROUND_COLOR.getName(), red, green, blue, alpha, createIfMissing);
    }

    /**
     * Whether a ColorStyleAttribute exists for the front color within the style.
     *
     * @param cascade Whether if the specific front ColorStyleAttribute is not set but the main
     *                ColorStyleAttribute or other suitable ColorStyleAttrubtes are set an can cascade to be used as the
     *                front color.
     * @return True if a ColorStyleAttribute exists for the front color within the style (or can be cascaded).
     */
    public boolean isFrontColorPresent(boolean cascade) {
        return isColorAttributePresent(StandardStyleAttribute.FRONT_COLOR.getName()) || (cascade && (isColorAttributePresent(StandardStyleAttribute.FRONT_AND_BACK_COLOR.getName()) || isColorAttributePresent(StandardStyleAttribute.COLOR.getName())));
    }
}
