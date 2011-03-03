package org.jdesktop.wonderland.modules.world.server.converter.xml.tags;

import org.jdesktop.wonderland.modules.world.common.xml.tags.ContentHandlingConstants;
import org.jdesktop.wonderland.modules.world.common.xml.tags.IgnoreAllFileFilter;
import org.jdesktop.wonderland.modules.world.common.xml.tags.SimpleModuleConversionTagContentHandler;

/**
 * This tag handler is used for performing conversions within Image xml documents when converting a snapshot to a module.
 *
 * @author Carl Jokl
 */
public class ImageTagHandler extends SimpleModuleConversionTagContentHandler {

    /**
     * The tag used for setting the URL of an image in Wonderland image content.
     */
    public static final String IMAGE_URI_TAG_NAME = "image-uri";

    /**
     * The tag name of the root tag for an image cell.
     */
    public static final String IMAGE_CELL_ROOT_TAG_NAME = "image-viewer-cell";
    
    /**
     * Create a new instance of ColladaTagHandler to perform conversions of tag contents within
     * the XML tags of Collada XML files.
     * 
     */
    public ImageTagHandler() {
        super(new IgnoreAllFileFilter(), IMAGE_URI_TAG_NAME);
        setCompatibleRootTags(IMAGE_CELL_ROOT_TAG_NAME);
    }

    /**
     * Modify the value of the Collada tags to change the content reference locations
     * from being stored within user directories to instead be stored within module art
     * directories.
     *
     * @param value The original value contained within the Collada XML tags.
     * @param uri The URI of the tag for which the contents are being converted.
     * @param localName The local name of the XML tag for which the contents are being converted.
     * @param qName The qualified name of the XML tag for which the contents are being converted.
     * @return The modified value which should be put inside the tags instead of the original one.
     */
    @Override
    public String modifyValue(String value, String uri, String localName, String qName) {
        if (moduleName != null && value != null) {
            value = value.trim();
            if (value.startsWith(ContentHandlingConstants.WONDERLAND_CONTENT_PREFIX)) {
                int slashIndex = value.lastIndexOf('/');
                if (slashIndex > 0) {
                    String modified = String.format("%s%s/%s",
                                                    ContentHandlingConstants.WONDERLAND_ART_PREFIX,
                                                    moduleName,
                                                    value.substring(slashIndex + 1));
                    dependencyFound(value, modified);
                    return modified;
                }
            }
        }
        return value;
    }
}
