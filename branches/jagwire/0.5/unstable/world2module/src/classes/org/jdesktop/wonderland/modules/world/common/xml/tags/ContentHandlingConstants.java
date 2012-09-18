package org.jdesktop.wonderland.modules.world.common.xml.tags;

import java.io.File;
import java.io.FileFilter;

/**
 * This class holds certain constants which can be reused between different TagContentHandlers handlers.
 *
 * @author Carl Jokl
 */
public abstract class ContentHandlingConstants {

    /**
     * This prefix is used to denote the Wonderland server content directory
     * for data stored in the Wonderland Server data cache.
     */
    public static final String WONDERLAND_CONTENT_PREFIX = "wlcontent://";

    /**
     * This prefix is used to denote the the Wonderland server art directory
     * or repository when referring to content stored in a module.
     */
    public static final String WONDERLAND_ART_PREFIX = "wla://";
    
    /**
     * This is the file name suffix given to Wonderland content XML files.
     */
    public static final String WONDERLAND_CONTENT_XML_FILE_SUFFIX = "-wlc.xml";

    /**
     * File Filter used to filter out just the Wonderland content files in the WFS directory.
     */
    public static final FileFilter WONDERLAND_CONTENT_FILE_FILTER = new WonderlandContentXmlFileFilter();

    /**
     * Simple FileFilter class for filtering out Wonderland content XML files.
     */
    private static class WonderlandContentXmlFileFilter implements FileFilter {

        /**
         * Filter out just those files which are expected to be Wonderland content
         * XML files.
         *
         * @param pathname The file to be checked to see if it looks like it is a
         *                 Wonderland content XML file.
         * @return         True if the supplied file matches the expected criteria
         *                 to identify it as a Wonderland content XML file.
         */
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().toLowerCase().endsWith(WONDERLAND_CONTENT_XML_FILE_SUFFIX);
        }
    }
}
