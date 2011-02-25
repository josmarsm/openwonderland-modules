package org.jdesktop.wonderland.modules.world.common.xml;

import java.io.File;
import java.io.FileFilter;

/**
 * Simple FileFilter which is used to filter directories.
 *
 * @author Carl Jokl
 */
public class DirectoryFilter implements FileFilter {

    /**
     * Check the supplied File object and check if it is a Directory.
     *
     * @param pathname The File object to be checked to see if it is a Directory.
     * @return True if the specified File is a directory.
     */
    @Override
    public boolean accept(File pathname) {
        return pathname.isDirectory();
    }
}
