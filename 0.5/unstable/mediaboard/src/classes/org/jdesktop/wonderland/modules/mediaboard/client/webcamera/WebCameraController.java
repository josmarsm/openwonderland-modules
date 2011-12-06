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

import java.awt.Toolkit;
import java.awt.image.BufferedImage;

/**
 *
 * @author Ryan
 */
public class WebCameraController {
    private WebCameraAdapter adapter = null;

    public WebCameraController() {
        initialize();
        BufferedImage image = new BufferedImage(1, 2, 4);
        
    }

    private void initialize() {
        String OS = System.getProperty("os.name");

        if(isWin(OS)) {
//            adapter = new WindowsWebCameraAdapter();
            adapter.initialize();
            //load windows adapter
            return;
        }

        if(isMac(OS)) {
            adapter = new MacWebCameraAdapter();
            adapter.initialize();
            //load mac adapter
            return;
        }
    }

    public BufferedImage takePicture() {
        return adapter.captureToImage(0);        
    }
    
    //<editor-fold defaultstate="collapsed" desc="utility methods">
    private boolean isWin(String OS) {
       return (OS.indexOf("win") >= 0);
    }

    private boolean isMac(String OS) {
        return (OS.indexOf("mac") >= 0);
    }
    //</editor-fold>
}
