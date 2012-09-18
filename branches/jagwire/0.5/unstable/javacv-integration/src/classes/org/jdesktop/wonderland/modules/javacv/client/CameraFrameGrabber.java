
package org.jdesktop.wonderland.modules.javacv.client;

import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

/**
 *
 * @author JagWire
 */
public enum CameraFrameGrabber {
    
    INSTANCE;
    private static final OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
    private static final Logger logger = Logger.getLogger(CameraFrameGrabber.class.getName());
    static {
        try {
            grabber.start();
        } catch (Exception ex) {
//            Logger.getLogger(CameraFrameGrabber.class.getName()).log(Level.SEVERE, null, ex);
            logger.warning("Unable to start camera grabber!");
            ex.printStackTrace();
        }
    }
    
    /**
     * This should really only ever be called on startup.
     */
    public void init() {
        //this is only here to give us some way to load CameraFrameGrabber
        //at load time so the static block above will get executed.
        logger.warning("initializing camera.");
    }
    
    /**
     * Grab a frame as a java BufferedImage object.
     * @return a BufferedImage representation of a frame
     * @throws Exception 
     */
    public synchronized BufferedImage getBufferedImage() throws Exception {
        IplImage i = null;
        
        i = grabber.grab();
        BufferedImage bImg = i.getBufferedImage();
        
        return bImg;
    }
    
    /**
     * Grab a frame as a JavaCV-bundled image. This is useful for image processing
     * 
     * @return
     * @throws Exception 
     */
    public synchronized IplImage getJavaCVImage() throws Exception {
        
        return grabber.grab();
    }
    
    /**
     * This method will turn the camera off or throw an exception if something
     * goes wrong.
     * 
     * @throws Exception 
     */
    public synchronized void stopCamera() throws Exception {
        grabber.stop();
    }
    
    
}
