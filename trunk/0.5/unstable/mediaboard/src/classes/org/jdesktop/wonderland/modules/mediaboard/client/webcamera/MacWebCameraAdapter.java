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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;

/**
 *
 * @author Ryan
 */
public class MacWebCameraAdapter implements WebCameraAdapter {

    public void initialize() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public BufferedImage captureToImage(int index) {
        //throw new UnsupportedOperationException("Not supported yet.");
        File pictureFile = captureToFile(index);
        BufferedImage image = null;
        try {
            image = ImageIO.read(pictureFile);
        } catch (IOException ex) {
            Logger.getLogger(MacWebCameraAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return image;
    }

    public File captureToFile(int index) {
        
        File pictureFile = null;
        Runtime rt = Runtime.getRuntime();
        String filename;
        try {
            //Process pr = rt.exec("imagecapture myPicture.jpg");
            ImageTagRegistry.getRegistry().flushCache();

            //craft file name
            Cell cell = ClientContextJME.getViewManager().getPrimaryViewCell();
            String name = cell.getCellCache().getSession().getUserID().getUsername();
            name = name.replace(' ', '_');

            filename = name+Integer.toString(index)+ ".jpg";
            System.out.println("Processing file: " + filename);

            Process pr = rt.exec("./imagesnap " + filename);
            pr.waitFor();
            pictureFile = new File(filename);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            return pictureFile;
        }

    }

}
