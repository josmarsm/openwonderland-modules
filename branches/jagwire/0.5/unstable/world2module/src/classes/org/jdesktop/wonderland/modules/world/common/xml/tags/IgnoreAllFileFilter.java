package org.jdesktop.wonderland.modules.world.common.xml.tags;

import java.io.File;
import java.io.FileFilter;

/**
 * This FileFilter class is used when a TagContentHandler wants to ignore all files.
 *
 * @author Carl Jokl
 */
public class IgnoreAllFileFilter implements FileFilter {

    /**
     * Ignore all files therefore always returns false.
     * 
     * @param pathname The name of the file to test.
     * @return Always returns false to ignore all.
     */
    @Override
    public boolean accept(File pathname) {
        return false;
    }

}
