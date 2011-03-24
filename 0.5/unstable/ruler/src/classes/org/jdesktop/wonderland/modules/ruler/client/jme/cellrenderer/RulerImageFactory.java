package org.jdesktop.wonderland.modules.ruler.client.jme.cellrenderer;

import com.jme.image.Texture;
import com.jme.math.Vector2f;
import com.jme.renderer.ColorRGBA;
import com.jme.util.TextureManager;
import java.util.Arrays;

/**
 * This class is used to dynamically create an image containing the ruler notches
 * to be used as a texture.
 *
 * @author Carl Jokl
 */
public abstract class RulerImageFactory {

    /**
     * The minimum number of pixels to use for a group width.
     */
    private static final int MIN_GROUP_PIXEL_WIDTH = 16;

    private static int getMaxTextureDimension() {
        return MaxTextureSizeUtil.getMaxTextureSize();
    }

    /**
     * Check if a number is a power of two.
     *
     * @param dimension The dimension to be checked to see if it is a power of two.
     * @return True if the dimension specified is a power of two.
     */
    public static boolean isPowerOfTwo(int dimension) {
        return dimension > 0 && (dimension & (dimension - 1)) == 0;
    }

    /**
     * Calculate the first power of two value which is greater than the specified value.
     *
     * @param value The value for which to get a power of two to contain.
     * @return The smallest power of two value which is large enough to hold the specified value.
     */
    public static int getFirstPowerOfTwoGreaterThan(int value) {
        if (value < 0x40000000) {
            int powerOfTwo = 1;
            while (powerOfTwo < value) {
                powerOfTwo <<= 1;
            }
            return powerOfTwo;
        }
        return 0;
    }

    /**
     * Get the number of rows required in order to fit the specified number of rows
     * given the maximum number of groups per row to use.
     *
     * @param maxGroupsPerRow The maximum number of groups which can be put on a row.
     * @param noOfGroups The total number of groups.
     * @return The number of rows required to store the specified number of groups
     *         when limited to the specified number of groups per row.
     */
    public static int getNoOfRows(int maxGroupsPerRow, int noOfGroups) {
        int noOfRows = (noOfGroups / maxGroupsPerRow);
        if (noOfGroups % maxGroupsPerRow > 0) {
            noOfRows++;
        }
        return noOfRows;
    }

    /**
     * Convert the specified x coordinate within an image of the specified width to
     * the corresponding texture U value.
     *
     * @param x The x pixel position within the texture to be converted to a U value.
     * @param textureWidth The width of the texture.
     * @return The texture U value between 0.0 and 1.0 showing where in the texture to set
     *         the vertex to be in order to correspond to that specified pixel position
     */
    public static float getTextureUOffset(int x, int textureWidth) {
        return ((x * 1.0f) / textureWidth);
    }

    /**
     * Convert the specified y coordinate within an image of the specified width to
     * the corresponding texture V value.
     *
     * @param y The y pixel position within the texture to be converted to a U value.
     * @param textureHeight The height of the texture.
     * @return The texture V value between 0.0 and 1.0 showing where in the texture to set
     *         the vertex to be in order to correspond to that specified pixel position
     */
    public static float getTextureVOffset(int y, int textureHeight) {
        return ((y * 1.0f) / textureHeight);
    }

    /**
     * Exception thrown if an image is to be converted with dimensions which are not a power of two.
     *
     * OpenGL generally requires that texture images have dimensions which are a power of 2.
     * Some newer graphics cards do support non power of two textures but for the sake of compatibility
     * it may be better to just stick with power of two texture dimensions.
     */
    public static class NonPowerOfTwoDimensionException extends RuntimeException {

        /**
         * Create a new instance of a NonPowerOfTwoDimensionException.
         *
         * @param isWidth Whether the dimension which was not a power of two was the width. True for the width,
         *                false for if the dimension is the height.
         * @param dimension The size of the dimension which was not a power of two.
         */
        public NonPowerOfTwoDimensionException(boolean isWidth, int dimension) {
            super(String.format("Could not convert the texture as OpenGL requires texture dimensions to be a power of two and the %s was %d pixels!", isWidth ? "width" : "height"));
        }
    }

    /**
     * This is a simple value holding object which should be passed into
     * the createTexture method so that the texture coordinates which
     * should be used for the vertices to filter out just the bit of the
     * image which should be used.
     */
    public static class TextureOffsets {

        /**
         * The minimum texture U value to use to trim
         * the texture drawn to just the first group.
         */
        public float group1MinU;

        /**
         * The maximum texture U value to use to trim
         * the texture drawn to just the first group.
         */
        public float group1MaxU;

        /**
         * The minimum texture U value to use to trim
         * the texture drawn to just the second group.
         */
        public float group2MinU;

        /**
         * The maximum texture U value to use to trim
         * the texture drawn to just the second group.
         */
        public float group2MaxU;
    }

    /**
     * This is a simple class / structure for holding a group of attributes for creating a ruler image.
     * This is used to reduce the number of arguments needing to be supplied to to ruler image creation methods.
     */
    public static class RulerDisplayMetaData {
        
        public final int noOfGroups;
        public final int noOfNotchesInGroup;
        public final RulerImageHeight groupHeight;
        public final int startLabelNo;
        public final boolean labelNotchCount;
        public final float lastGroupScale;
        public final int[] midNotches;

        /**
         * Create a new instance or RulerDisplayMetaData containing the specified information.
         *
         * @param noOfGroups The number of notch display groups (groups generally represent things like inches or centimeters on a ruler.
         * @param noOfNotchesInGroup The number of notches which are drawn within each group.
         * @param groupHeight The height of the groups of the ruler display image.
         * @param startLabelNo The number at which the units for this ruler starts (0 if this is the start of the ruler).
         * @param labelNotchCount Whether the label should be the count or the notches or the count of groups. True if it is to be the
         *                        count of the notches and false if it is to be the count of the groups.
         * @param lastGroupScale The scale of the last group (which is used if the last group is smaller than the other groups).
         * @param midNotches A variable number of integer notch counts which are significant and should be marked with a larger notch.
         *                   The larger notch counts will be displayed more significantly than the smaller ones.
         */
        public RulerDisplayMetaData(final int noOfGroups, final int noOfNotchesInGroup, final RulerImageHeight groupHeight, final int startLabelNo, final boolean labelNotchCount, final float lastGroupScale, final int... midNotches) {
            if (noOfGroups <= 0) {
                throw new IllegalArgumentException("The number of groups for the ruler must be greater than zero!");
            }
            if (noOfNotchesInGroup < 0) {
                throw new IllegalArgumentException("The number of notches in the group cannot be negative!");
            }
            this.noOfGroups = noOfGroups;
            this.noOfNotchesInGroup = noOfNotchesInGroup;
            this.groupHeight = groupHeight != null ? groupHeight : RulerImageHeight.MEDIUM;
            this.startLabelNo = startLabelNo;
            this.labelNotchCount = labelNotchCount;
            this.lastGroupScale = lastGroupScale;
            this.midNotches = midNotches;
            if (midNotches != null) {
                Arrays.sort(midNotches);
            }
        }

        /**
         * Get the number of height pixels in each ruler group.
         *
         * @return The number of height pixels in each ruler group.
         */
        public int getGroupPixelHeight() {
            return groupHeight.getPixelHeight();
        }

        /**
         * Get the number of pixels in width each ruler group should be given the number of notches and the number of pixels per notch
         * and the minimum allowed width.
         *
         * @param minWidth The minimum width for a group on the ruler.
         * @return The width in pixels for a group on the ruler.
         */
        public int getGroupPixelWidth(int minWidth) {
            return Math.max(groupHeight.getPixelWidthPerNotch() * noOfNotchesInGroup, minWidth);
        }
    }

    /**
     * This enumeration represents the predefined supported heights for the ruler image (per span of the ruler).
     */
    public enum RulerImageHeight {

        /**
         * Ruler image is short.
         */
        SHORT(16, 2),

        /**
         * Ruler image is medium height.
         */
        MEDIUM(32, 3),

        /**
         * Ruler image is tall height.
         */
        TALL(64, 4);

        private final int pixelHeight;
        private final int pixelWidthPerNotch;

        /**
         * Create a new RulerImageHeight enumeration instance with
         * the specified height in pixels.
         *
         * @param pixelHeight The height in pixels for the ruler image.
         * @param pixelWidthPerNotch The number of pixels wide to make each notch when using this height;
         */
        private RulerImageHeight(final int pixelHeight, final int pixelWidthPerNotch) {
            this.pixelHeight = pixelHeight;
            this.pixelWidthPerNotch = pixelWidthPerNotch;
        }

        /**
         * Get the height in pixels of the ruler image (per spanning width of the ruler).
         *
         * @return The number of pixels used in the height of the image which spans the ruler.
         */
        public int getPixelHeight() {
            return pixelHeight;
        }

        /**
         * The number of pixels width per notch to use when using this height.
         *
         * @return The number of pixels width needed per notch when using this height.
         */
        public int getPixelWidthPerNotch() {
            return pixelWidthPerNotch;
        }
    }

    /**
     * This class is used for grouping together style attributes for styling the ruler display image.
     */
    public static class RulerStyleMetaData {

        private ColorRGBA[] foregroundColors;
        private ColorRGBA[] backgroundColors;

        /**
         * Create RulerStyleMetaData which contains the default style.
         */
        public RulerStyleMetaData() {
            foregroundColors = new ColorRGBA[] { ColorRGBA.black.clone() };
            backgroundColors = new ColorRGBA[] { new ColorRGBA(1.0f, 1.0f, 1.0f, 0.0f) };
        }

        /**
         * Create a new instance of RulerStyleMetaData with the specified style information.
         *
         * @param foregroundCount The number of the supplied colors which are foreground colors.
         *                        The remainder will be treated as background colors.
         * @param colors The colors to use for the ruler.
         */
        public RulerStyleMetaData(int foregroundCount, ColorRGBA... colors) {
            if (colors == null || colors.length == 0) {
                foregroundColors = new ColorRGBA[] { ColorRGBA.black.clone() };
                backgroundColors = new ColorRGBA[] { new ColorRGBA(1.0f, 1.0f, 1.0f, 0.0f) };
            }
            else if (foregroundCount > 0 && foregroundCount < colors.length) {
                foregroundColors = new ColorRGBA[foregroundCount];
                for (int colorIndex = 0; colorIndex < foregroundCount; colorIndex++) {
                    foregroundColors[colorIndex] = colors[colorIndex];
                }
                final int remaining = colors.length - foregroundCount;
                if (remaining > 0) {
                    backgroundColors = new ColorRGBA[remaining];
                    for (int sourceIndex = foregroundCount, destIndex = 0; destIndex < remaining; sourceIndex++, destIndex++) {
                        backgroundColors[destIndex] = colors[sourceIndex];
                    }
                }
                else {
                    backgroundColors = new ColorRGBA[] { new ColorRGBA(1.0f, 1.0f, 1.0f, 0.0f) };
                }
            }
            else {
                throw new IllegalArgumentException(String.format("The number of foreground colors %d is invalid for the %d supplied colors!", foregroundCount, colors.length));
            }
        }

        /**
         * Get the foreground colors to use for the ruler image.
         *
         * @return The foreground colors to use for the ruler image.
         */
        public ColorRGBA[] getForegroundColors() {
            return foregroundColors;
        }

        /**
         * Get the background colors to use for the ruler image.
         *
         * @return The background colors to use for the ruler image.
         */
        public ColorRGBA[] getBackgroundColors() {
            return backgroundColors;
        }
    }

    /**
     * Create a texture to use to display notches on a ruler.
     *
     * @param displayMetaData The meta-data used to define the parameters for creating a ruler image.
     * @param styleMetaData The style meta-data used for styling the ruler display in the image.
     * @param textureCoords An array of the Texture coordinates to use for deciding how to divide the image up into groups.
     *                      This will have its elements populated with texture U,V coordinates.
     * @return An Image to be used as the texture for a RulerFace or as the icon for the module.
     */
    public static java.awt.Image createImage(RulerDisplayMetaData displayMetaData, RulerStyleMetaData styleMetaData, Vector2f[] textureVertices) {
        if (displayMetaData != null) {
            if (styleMetaData == null) {
                styleMetaData = new RulerStyleMetaData();
            }
            final int groupWidth = displayMetaData.getGroupPixelWidth(MIN_GROUP_PIXEL_WIDTH);
            final int groupHeight = displayMetaData.getGroupPixelHeight();
            final int totalRulerPixelWidth = groupWidth * displayMetaData.noOfGroups;
            //ToDo calculate.
            int maxImagePixelDimension = getMaxTextureDimension();
            int noOfRows = 1;
            int groupsPerRow = displayMetaData.noOfGroups;
            //Group width which must be a power of two.
            int textureWidth = getFirstPowerOfTwoGreaterThan(totalRulerPixelWidth);
            int textureHeight = groupHeight;
            if (textureWidth > maxImagePixelDimension) {
                int noOfPixels = 0;
                int bestNoOfPixels = Integer.MAX_VALUE;
                int maxGroupsPerRow = displayMetaData.noOfGroups - 1;
                int bestGroupsPerRow = 0;
                int bestTextureWidth = 0;
                int bestTextureHeight = 0;
                int lastTextureWidth = 0;
                while (maxGroupsPerRow > 0) {
                    textureWidth = getFirstPowerOfTwoGreaterThan(groupWidth * maxGroupsPerRow);
                    if (textureWidth != lastTextureWidth && textureWidth <= maxImagePixelDimension) {
                        noOfRows = getNoOfRows(maxGroupsPerRow, displayMetaData.noOfGroups);
                        textureHeight = getFirstPowerOfTwoGreaterThan(groupHeight * noOfRows);
                        if (textureHeight > maxImagePixelDimension) {
                            break;
                        }
                        else {
                            noOfPixels = textureWidth * textureHeight;
                            if (noOfPixels < bestNoOfPixels) {
                                bestNoOfPixels = noOfPixels;
                                bestGroupsPerRow = maxGroupsPerRow;
                                bestTextureWidth = textureWidth;
                                bestTextureHeight = textureHeight;
                            }
                        }
                        lastTextureWidth = textureWidth;
                    }
                    else if (textureWidth > maxImagePixelDimension * 3) {
                        maxGroupsPerRow /= 2;
                    }
                    else {
                        maxGroupsPerRow--;
                    }
                }
                textureWidth = bestTextureWidth;
                textureHeight = bestTextureHeight;
                groupsPerRow = bestGroupsPerRow;
                noOfRows = bestGroupsPerRow > 0 ? getNoOfRows(groupsPerRow, displayMetaData.noOfGroups) : 0;
            }
            if (noOfRows > 0) {
                return createImage(displayMetaData, styleMetaData, textureWidth, textureHeight, noOfRows, groupsPerRow, textureVertices);
            }
            else {
                throw new IllegalArgumentException("The number or required groups and marks results in a texture which is too large to be supported by the current platform graphics card!");
            }
        }
        else {
            throw new IllegalArgumentException("The number of groups and/or notches for the ruler must be greater than zero!");
        }
    }

    /**
     * Create a texture to use to display notches on a ruler.
     *
     * @param displayMetaData The meta-data used to define the parameters for creating a ruler texture.
     * @param styleMetaData The style meta-data used for styling the ruler display in the texture.
     * @param textureCoords An array of the Texture coordinates to use for deciding how to divide the image up into groups.
     *                      This will have its elements populated with texture U,V coordinates.
     *
     * @return A Texture to be used as the texture for a RulerFace.
     */
    public static Texture createTexture(RulerDisplayMetaData displayMetaData, RulerStyleMetaData styleMetaData, Vector2f[] textureCoords) {
        java.awt.Image textureImage = createImage(displayMetaData, styleMetaData, textureCoords);
        return TextureManager.loadTexture(textureImage, Texture.MinificationFilter.Trilinear, Texture.MagnificationFilter.Bilinear, false);
    }

    /**
     * This is an internal method to perform the actual drawing once the number or rows and groups per row have been
     * calculated.
     *
     * @param textureWidth The width of the texture.
     * @param textureHeight The height of the texture.
     * @param groupWidth The width in pixels of a group.
     * @param groupHeight The height in pixels of a group.
     * @param noOfRows The number of group rows in the image.
     * @param groupsPerRow The number of groups to store per row of the image.
     * @param noOfNotches The number of notches to be drawn per row.
     * @param midPerXNotches The number of notches at which a longer notch will be drawn to denote that it is significant.
     * @param bgColors An array of the background colors to be used for the groups.
     * @param fgColors An array of the foreground colors to be used for the notches of the groups.
     * @param textureCoords The texture coordinates to be populated from the image being built.
     * @param lastGroupScale The scaling factor for the texture coordinates for the last group. This can be used if the last group is notably smaller than the others.
     * @return The image containing the ruler texture for the specified parameters.
     */
    private static java.awt.Image createImage(RulerDisplayMetaData displayMetaData, RulerStyleMetaData styleMetaData, final int textureWidth, final int textureHeight, final int noOfRows, final int groupsPerRow, Vector2f[] textureCoords) {
        final int noOfGroups = displayMetaData.noOfGroups;
        final int noOfNotches = displayMetaData.noOfNotchesInGroup;
        final int groupWidth = displayMetaData.getGroupPixelWidth(MIN_GROUP_PIXEL_WIDTH);
        final int groupHeight = displayMetaData.getGroupPixelHeight();
        final int pixelWidthPerNotch = displayMetaData.groupHeight.getPixelWidthPerNotch();
        final int majNotchStart = groupHeight - (groupHeight / 8);
        final int normNotchStart = (groupHeight / 4);
        final int midNotchStart =  groupHeight / 2;
        final int[] midNotches = displayMetaData.midNotches;
        boolean isMidNotch = false;
        final int midNotchOffset = (midNotches != null && midNotches.length > 1 && ((midNotchStart - normNotchStart) / (midNotches.length - 1)) > 0) ? (midNotchStart - normNotchStart) / (midNotches.length - 1) : 1;
        int currentBGColorIndex = 0;
        int currentFGColorIndex = 0;
        int x = 0;
        int y = 0;
        float uMax = 0;
        float uMin = 0;
        float vMax = 0;
        float vMin = 0;
        int texCoordIndex = textureCoords != null ? textureCoords.length - 1 : 0;
        int totalGroups = 0;
        final ColorRGBA[] fgColors = styleMetaData.getForegroundColors();
        final ColorRGBA[] bgColors = styleMetaData.getBackgroundColors();
        ColorRGBA currentBackground = null;
        ColorRGBA currentForeground = null;
        String currentLabel = null;
        java.awt.GraphicsEnvironment environment = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        java.awt.GraphicsDevice device = environment.getDefaultScreenDevice();
        java.awt.GraphicsConfiguration configuration = device.getDefaultConfiguration();
        java.awt.image.VolatileImage image = configuration.createCompatibleVolatileImage(textureWidth, textureHeight);
        java.awt.Graphics graphics = image.createGraphics();
        java.awt.FontMetrics fontMetrics = graphics.getFontMetrics();
        java.awt.Font labelFont = getFontToDisplay(fontMetrics, groupWidth - 4, (groupHeight / 2) - 2, (displayMetaData.labelNotchCount ? noOfGroups * noOfNotches : noOfGroups));
        if (labelFont != null) {
            graphics.setFont(labelFont);
        }
        java.awt.geom.Rectangle2D labelBounds = null;
        for (int rowIndex = 0; rowIndex < noOfRows; rowIndex++, y += groupHeight) {
            x = 0;
            for (int rowGroupIndex = 0; rowGroupIndex < groupsPerRow && totalGroups < noOfGroups; rowGroupIndex++, totalGroups++) {
                if (textureCoords != null  && texCoordIndex < textureCoords.length && (texCoordIndex - 3) >= 0) {
                    uMin = getTextureUOffset(x, textureWidth);
                    vMin = getTextureVOffset(y, textureHeight);
                    if (texCoordIndex == textureCoords.length - 1) {
                        uMax = getTextureUOffset(x + (groupWidth - 1), textureWidth);
                        uMax = uMin + ((uMax - uMin) * displayMetaData.lastGroupScale);
                    }
                    else {
                        uMax = getTextureUOffset(x + (groupWidth - 1), textureWidth);
                    }
                    vMax = getTextureVOffset(y + (groupHeight - 1), textureHeight);
                    textureCoords[texCoordIndex] = new Vector2f(uMin, vMax);
                    texCoordIndex--;
                    textureCoords[texCoordIndex] = new Vector2f(uMin, vMin);
                    texCoordIndex--;
                    textureCoords[texCoordIndex] = new Vector2f(uMax, vMax);
                    texCoordIndex--;
                    textureCoords[texCoordIndex] = new Vector2f(uMax, vMin);
                    texCoordIndex--;
                }
                //Set the colors for this group.
                currentBackground = bgColors[currentBGColorIndex];
                currentBGColorIndex = (currentBGColorIndex + 1) % bgColors.length;
                currentForeground = fgColors[currentFGColorIndex];
                currentFGColorIndex = (currentFGColorIndex + 1) % fgColors.length;
                //Draw group background.
                graphics.setColor(new java.awt.Color(currentBackground.r, currentBackground.g, currentBackground.b, currentBackground.a));
                graphics.fillRect(x, y, groupWidth, groupHeight);
                //Draw group notches.
                graphics.setColor(new java.awt.Color(currentForeground.r, currentForeground.g, currentForeground.b, currentForeground.a));
                //Check font is set
                if (labelFont != null) {
                    currentLabel = Integer.toString(displayMetaData.labelNotchCount ? noOfNotches * (totalGroups + 1) : (totalGroups + 1));
                    labelBounds = labelFont.getStringBounds(currentLabel, fontMetrics.getFontRenderContext());
                    graphics.drawString(currentLabel, x + ((groupWidth - 3) - (int) labelBounds.getWidth()), y + midNotchStart + (int) labelBounds.getHeight() + 1);
                }
                //Offset back one at the start of the group so that when the drawing starts the first line is drawn in the right place.
                x--;
                for (int notchCount = 1; notchCount <= noOfNotches; notchCount++) {
                    x += pixelWidthPerNotch;
                    if (notchCount == noOfNotches) {
                        graphics.drawLine(x, y + majNotchStart, x, y);
                    }
                    else if (midNotches != null) {
                        isMidNotch = false;
                        for (int midNotchIndex = 0; midNotchIndex < midNotches.length; midNotchIndex++) {
                            if (notchCount % midNotches[midNotchIndex] == 0) {
                                isMidNotch = true;
                                graphics.drawLine(x, y + (midNotchStart + (midNotchOffset * midNotchIndex)), x, y);
                                break;
                            }
                        }
                        if (!isMidNotch) {
                            graphics.drawLine(x, y + normNotchStart, x, y);
                        }
                    }
                    else {
                        graphics.drawLine(x, y + normNotchStart, x, y);
                    }
                }
                x++;
            }
        }
        graphics.dispose();
        return image;
    }

    /**
     * Find a font which is of the right size to draw labels for the different sizes.
     *
     * @param fontMetrics The FontMetrics of the Graphics used for the current font.
     * @param width The width into which the label must fit.
     * @param height The height into which the label must fit.
     * @param maxNumberLabel The largest label number which must be drawn.
     * @return The font which is small enough to draw text for that label or null if the space is too small for any font to fit.
     */
    private static java.awt.Font getFontToDisplay(java.awt.FontMetrics fontMetrics, int width, int height, final int maxNumberLabel) {
        float fontSize = 12.0f;
        int noOfDigits = 1;
        int refNumber = 10;
        while (maxNumberLabel >= refNumber) {
            noOfDigits++;
            refNumber *= 10;
        }
        char[] widestChars = new char[noOfDigits];
        for (int charIndex = 0; charIndex < noOfDigits; charIndex++) {
            widestChars[charIndex] = '0';
        }
        String widest = new String(widestChars);
        java.awt.Font existingFont = fontMetrics.getFont();
        java.awt.font.FontRenderContext renderContext = fontMetrics.getFontRenderContext();
        java.awt.Font font = null;
        java.awt.geom.Rectangle2D bounds = null;
        while (fontSize > 0.0f) {
            font = existingFont.deriveFont(fontSize);
            bounds = font.getStringBounds(widest, renderContext);
            if (bounds.getWidth() <= width && bounds.getHeight() <= height) {
                break;
            }
            else {
                fontSize -= 0.5f;
            }
        }
        return font;
    }
}
