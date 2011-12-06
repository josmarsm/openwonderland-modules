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

import java.awt.image.BufferedImage;
import java.io.File;

/**
 *
 * @author Ryan
 */
public interface WebCameraAdapter {
    public void initialize();

    /**
     * Take a picture with the connected webcam and put it into a BufferedImage
     * object
     *
     * @return the picture as a BufferedImage
     */
    public BufferedImage captureToImage(int index);


    /**
     * Take a picture with the connected webcam and put it into a file on disk.
     *
     * @return The picture as a File object
     */
    public File captureToFile(int index);
}
