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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

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
    public static File CaptureImageToFile(String filename, String extension) {
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        IplImage image = null;
        BufferedImage bImg = null;
        File out = null;// = new File(filename+extension);
        try {
            grabber.start();
            image = grabber.grab();
            bImg = image.getBufferedImage();
            
            
//            boolean success = ImageIO.write(bImg, extension, out);
           opencv_highgui.cvSaveImage(filename+extension, image);
           
           out = new File(filename+extension);
           if(out.exists()) {
               logger.warning("file exists!");
           } else {
               logger.warning("file does not exist!");
           }
//            if(!success) {
//                logger.warning("Error writing to file!");
//                return null;
//            } 
//            
            logger.warning("Image: " +filename+extension+" written!");        
            grabber.stop();
        } catch(Exception e) {
            e.printStackTrace();
            
        } finally {

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
}
