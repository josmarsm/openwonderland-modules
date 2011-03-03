package org.jdesktop.wonderland.modules.world.server.converter.xml.tags;

import org.jdesktop.wonderland.modules.world.common.xml.tags.ContentHandlingConstants;
import java.io.File;
import java.io.FileFilter;
import org.jdesktop.wonderland.modules.world.common.xml.tags.SimpleModuleConversionTagContentHandler;
import org.jdesktop.wonderland.modules.world.server.converter.WonderlandDirectoryConstants;

/**
 * This tag handler is used for performing conversions within Collada xml documents when converting a world to a module.
 *
 * @author Carl Jokl
 */
public class ColladaTagHandler extends SimpleModuleConversionTagContentHandler {

    /**
     * This is the name of the Collada model URL tag.
     */
    public static final String MODEL_URL_TAG_NAME = "modelURL";

    /**
     * This is the name deployed model URL tag which defiles the location of the
     * deployed mode.
     */
    public static final String DEPLOYED_MODEL_URL_TAG_NAME = "deployedModelURL";

    /**
     * This is the name of the loader data URL tag.
     */
    public static final String LOADER_DATA_URL_TAG_NAME = "loaderDataURL";

    /**
     * This is the name of the Collada model cell root tag.
     */
    public static final String MODEL_CELL_ROOT_TAG_NAME = "model-cell";

    /**
     * This is the name of the Collada deployed model root tag.
     */
    public static final String DEPLOYED_MODEL_ROOT_TAG_NAME = "deployed-model";

    /**
     * The name of the optional images directory in the deployed Collada directory.
     */
    public static final String OPTIONAL_IMAGES_DIR_NAME = "images";

    private final String artDirectoryMatchString;
    
    /**
     * Create a new instance of ColladaTagHandler to perform conversions of tag contents within
     * the XML tags of Collada XML files.
     * 
     */
    public ColladaTagHandler() {
        super(new ColladaXMLFileFilter(), MODEL_URL_TAG_NAME, DEPLOYED_MODEL_URL_TAG_NAME, LOADER_DATA_URL_TAG_NAME);
        setCompatibleRootTags(MODEL_CELL_ROOT_TAG_NAME, DEPLOYED_MODEL_ROOT_TAG_NAME);
        artDirectoryMatchString = String.format("/%s/", WonderlandDirectoryConstants.DEFAULT_ART_DIRECTORY_NAME);
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
                int artIndex = value.lastIndexOf(artDirectoryMatchString);
                if (artIndex > 0) {
                    String modified = String.format("%s%s/%s",
                                                    ContentHandlingConstants.WONDERLAND_ART_PREFIX,
                                                    moduleName,
                                                    value.substring(artIndex + artDirectoryMatchString.length()));
                    dependencyFound(value, modified);
                    if (MODEL_URL_TAG_NAME.equals(qName) && MODEL_URL_TAG_NAME.equals(localName)) {
                        int slashIndex = value.lastIndexOf('/');
                        if (slashIndex > 0) {
                            int dotIndex = value.indexOf('.', slashIndex);
                            if (dotIndex > 0) {
                                String imgDirURI = value.substring(0, dotIndex);
                                String destImgDirURI = String.format("%s%s/%s",
                                                                     ContentHandlingConstants.WONDERLAND_ART_PREFIX, 
                                                                     moduleName,
                                                                     imgDirURI.substring(artIndex + artDirectoryMatchString.length()));
                                optionalDependencyCheck(imgDirURI, destImgDirURI);
                            }
                        }
                    }
                    else if (DEPLOYED_MODEL_URL_TAG_NAME.equals(qName) && DEPLOYED_MODEL_URL_TAG_NAME.equals(localName)) {
                        int slashIndex = value.lastIndexOf('/');
                        if (slashIndex > 0) {
                            String imgDirURI = value.substring(0, slashIndex + 1).concat(OPTIONAL_IMAGES_DIR_NAME );
                            String destImgDirURI = String.format("%s%s/%s",
                                                                 ContentHandlingConstants.WONDERLAND_ART_PREFIX,
                                                                 moduleName,
                                                                 imgDirURI.substring(artIndex + artDirectoryMatchString.length()));
                            optionalDependencyCheck(imgDirURI, destImgDirURI);
                        }
                    }
                    return modified;
                }
            }
        }
        return value;
    }

    /**
     * This FileFilter is used to filter out just the Collada XML files for which this tag
     * hander is setup to modify.
     */
    public static class ColladaXMLFileFilter implements FileFilter {

        /**
         * This is the file name suffix used by the Collada XML files which the Tag Handler is setup
         * to handle.
         */
        public static final String COLLADA_XML_FILE_SUFFIX = ".kmz.dep";

        /**
         * Accept the files which have the Collada dependency file name/extension
         * suffix.
         *
         * @param pathname The File which is to be checked to see if it appears to be a Collada XML file.
         * @return True if the specified File matched the expected criteria to be a Collada XML file.
         */
        @Override
        public boolean accept(File pathname) {
            return (pathname.getName().toLowerCase().endsWith(COLLADA_XML_FILE_SUFFIX));
        }

    }
}
