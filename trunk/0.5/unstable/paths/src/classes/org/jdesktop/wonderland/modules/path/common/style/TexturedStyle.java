package org.jdesktop.wonderland.modules.path.common.style;

/**
 * This interface represents a style which contains a number of images.
 *
 * @author Carl Jokl
 */
public interface TexturedStyle {

    /**
     * Get the minimum number of images which
     * are expected to be part of this style.
     * If the style expects a fixed number of images
     * then the minimum image and maximum image values
     * will be the same.
     * 
     * @return The minimum number of images which this style is expected to use.
     */
    public int getMinImages();

    /**
     * Get the maximum number or images which are expected to be part of this
     * style. If the style expects a specific number of images then this number
     * will be the same as the minimum images.
     *
     * @return The maximum number of images expected to be used with this style.
     */
    public int getMaxImages();

    /**
     * This value represents the actual current number of images in the style.
     * If the style has a minimum number of images then the images in the style
     * should have already been populated with default values i.e. it is an
     * error for the image count to be less than the minimum images or greater
     * than the maximum images.
     *
     * @return The current number of images. This must be within the range of
     *         the minimum and maximum number of images.
     */
    public int getImageCount();

    /**
     * Get the style Image at the specified index.
     *
     * @param index The index of the image to be retrieved.
     * @return The image URI string in the style for the image at the specified index.
     * @throws IndexOutOfBoundsException If the specified index is outside the
     *         range of images in the style.
     */
    public String getImage(int index) throws IndexOutOfBoundsException;

    /**
     * Set the style Image at the specified index if the index specified
     * is one after the current range of images but the maximum images has
     * not been reached then the set image
     *
     * @param index The index of the image to be set.
     * @param imageURI The image URI string to be set in the style at the specified index.
     * @return True if the specified image URI was able to be set or false if the specified
     *              URI was null or could not be set.
     * @throws IndexOutOfBoundsException If the specified index is outside the
     *         range of images in the style.
     */
    public boolean setImage(int index, String imageURI) throws IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Add a image to the images within this style.
     *
     * @param imageURI The URI of the image to be added to this style.
     * @return True if the image was able to be added i.e. the maximum number
     *              of images has not been reached. False if the image could
     *              not be added i.e. due to the maximum number of images having
     *              been reached.
     */
    public boolean addImage(String imageURI);

    /**
     * Remove the last image in the list of images for this TexturedStyle.
     *         If the ImageedStyle does support having a variable number of images
     *         but the remove operation would reduce the number of images below
     *         the minimum then null is returned.
     *
     * @return The URI of the image which was removed from the style or null if the image
     *         could not be removed due to the minimum number of images have been reached.
     * @throws UnsupportedOperationException If removing images is not supported by
     *         the TexturedStyle i.e. due to the number of images being fixed.
     */
    public String removeImage() throws UnsupportedOperationException;

    /**
     * Remove a image at the specified index from the available images.
     *
     * @param index The index of the image to be removed. This index must be in the
     *        range from the minimum number of images to the maximum number of images -1.
     *        If the number of images is fixed then removing a image in this way is not
     *        possible.
     * @return The URI of the image removed from the specified index.
     * @throws IndexOutOfBoundsException If the index supplied at which the image
     *         was to be removed is outside the valid / allowed range.
     * @throws UnsupportedOperationException If removing images is not supported by
     *         the TexturedStyle i.e. due to the number of images being fixed.
     */
    public String removeImage(int index) throws IndexOutOfBoundsException, UnsupportedOperationException;

    /**
     * Get a descriptive label for the image at the specified index.
     * This descriptive label can be used in UI's for setting the image URI.
     *
     * @param index The index of the image for which to return the label.
     * @return The descriptive label for the image at the specified index.
     * @throws IndexOutOfBoundsException If the index of the image label to be
     *         retrieved was outside the valid range.
     */
    public String getImageLabel(int index) throws IndexOutOfBoundsException;
}
