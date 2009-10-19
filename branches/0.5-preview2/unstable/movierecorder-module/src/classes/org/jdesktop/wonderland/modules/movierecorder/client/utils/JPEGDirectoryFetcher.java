/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.movierecorder.client.utils;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;

import javax.imageio.ImageIO;

/**
 * This class is a convenience class for fetching JPEG images from a directory.
 * It acts as a JPEGImageProducer, which enables it to be passed directly to a ImagesDataSource.
 * Note that since JPEGImageProducer requires all images to have the same size, so does this class.
 *
 * @author Mikael Nordenberg, <a href="http://www.ikanos.se">www.ikanos.se</a>
 */
public class JPEGDirectoryFetcher implements JPEGImageProducer {
    private File[] files;
    private int count = 0;

    public static final FilenameFilter JPEG_FILTER = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            if(name.toLowerCase().endsWith(".jpg")) {
                return true;
            } else {
                return false;
            }
        }
    };

    /**
     * This constructor takes all files ending with ".jpg" (case insensitive)
     * and process them in the files natural order.
     * @param directory     the directory to get JPEGs from
     */
    public JPEGDirectoryFetcher(File directory) {
        // Create a filter that accepts JPG-files
        this(directory, JPEG_FILTER);
    }
    
    /**
     * This constructor takes all files ending with ".jpg" (case insensitive)
     * and process them in the files natural order.
     * @param directory     the directory to get JPEGs from
     */
    public JPEGDirectoryFetcher(File directory, FilenameFilter filter) {
        this(directory, filter, null);
    }

    /**
     * Constructor that enables custom filtering and sorting.
     * @param directory     the directory to get JPEGs from
     * @param filter        a filter to select files to process
     * @param sorter        a sorter to determine the sort order
     */
    public JPEGDirectoryFetcher(File directory, FilenameFilter filter, Comparator<File> sorter) {
        files = directory.listFiles(filter);
        if(sorter != null) {
            Arrays.sort(files, sorter);
        } else {
            // Use the natural ordering.
            Arrays.sort(files);
        }
    }

    /**
     * This method returns the dimension of the first image that will be processed.
     * @return the first image's size
     * @throws IOException if an IOException occurred while reading the first image
     */
    public Dimension getSuggestedSize() throws IOException {
        BufferedImage image = ImageIO.read(files[0]);
        return new Dimension(image.getWidth(), image.getHeight());
    }

    /**
     * Returns the next image as a raw data array.
     * @return the next image
     */
    public byte[] getNextImage() {
        if(count < files.length) {
            try {
                RandomAccessFile raf = new RandomAccessFile(files[count++], "r");
                byte[] result = new byte[(int)raf.length()];
                raf.readFully(result);
                raf.close();
                return result;
            } catch(IOException ioe) {
                return null;
            }
        } else {
            return null;
        }
    }
}

