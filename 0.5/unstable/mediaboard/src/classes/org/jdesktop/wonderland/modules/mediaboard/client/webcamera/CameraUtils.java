/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
