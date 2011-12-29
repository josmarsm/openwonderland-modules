
package org.jdesktop.wonderland.modules.javacv.client;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * This class will activate your webcam (if supported by OpenCV) upon the start
 * of the Open Wonderland client. It's important to note that just because the
 * camera has started, frames won't be automatically captured and displayed 
 * anywhere unless explicitly written to do so.
 * 
 * @author JagWire
 */
@Plugin
public class CameraInitClientPlugin extends BaseClientPlugin {
    
    @Override
    public void activate() {
        //TODO: There's probably a property we could set somewhere that would
        //allow the user to choose whether or not they want their webcam to 
        //start. Another idea might be to store a properties file in .wonderland/
        //and check this file on startup. If it doesn't exist, prompt the user
        //if they want to enable their webcam or not. The response will be 
        //reflected in a newly created properties file in the .wonderland/
        //directory.
        CameraFrameGrabber.INSTANCE.init();
    }
    
    @Override
    public void cleanup() {
        try {
            CameraFrameGrabber.INSTANCE.stopCamera();
        } catch (Exception ex) {
            Logger.getLogger(CameraInitClientPlugin.class.getName()).log(Level.WARNING, null, ex);
        }
    }
}
