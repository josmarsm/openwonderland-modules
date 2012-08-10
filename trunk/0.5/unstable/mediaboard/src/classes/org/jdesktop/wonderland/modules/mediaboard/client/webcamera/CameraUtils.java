/**
  * iSocial Project
  * http://isocial.missouri.edu
  *
  * Copyright (c) 2011, University of Missouri iSocial Project, All 
  * Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * The iSocial project designates this particular file as
  * subject to the "Classpath" exception as provided by the iSocial
  * project in the License file that accompanied this code.
  */
package org.jdesktop.wonderland.modules.mediaboard.client.webcamera;

import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_highgui;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.javacv.client.CameraFrameGrabber;

/**
 *
 * @author Ryan Babiuch
 */
public class CameraUtils {
    private static final Logger logger = Logger.getLogger(CameraUtils.class.getName());

          
 
    /**
     * Utility method to acquire an image from a camera
     * 
     * @param filename name of file
     * @param extension extension to file, currently supported: ".png" and ".jpeg"
     * @return A file containing the image from the camera
     */
    public static File CaptureImageToFile(String filename) {

        IplImage image = null;
        BufferedImage bImg = null;
        int captureTries = 0;
        File out = new File(filename);

        try {
            logger.warning("USING CameraFrameGrabber from JavaCV-Common");
            image = CameraFrameGrabber.INSTANCE.getJavaCVImage();

            opencv_highgui.cvSaveImage(filename, image);

            out = new File(filename);
            if (out.exists()) {
                logger.warning("file exists!");
            } else {
                logger.warning("file does not exist!");
            }

            logger.warning("Image: " + filename + " written!");

        } catch (Exception ex) {
//            System.out.println("Exception occured!");
            ex.printStackTrace();
        } finally {

            if (out == null) {
                logger.warning("file will be null!");
            }

            return out;
        }           
    }
    
    /**
     * Utility method to acquire an image from a camera. 
     * @return a BufferedImage representation of the acquired image.
     */
    public static BufferedImage CaptureImage() {
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        IplImage image = null;
        BufferedImage bImg = null;
        
        try {
            grabber.start();
            image = grabber.grab();
            bImg = image.getBufferedImage();
            grabber.stop();
            logger.warning("Image acquired!");
            
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            return bImg;
        }

    }
    
    public static boolean isWindows() {
        logger.warning("os.name="+System.getProperty("os.name"));
        
        return isWin(System.getProperty("os.name"));
    }
    
    private static boolean isWin(String OS) {
       return (OS.contains("Win"));
    }
    
}
