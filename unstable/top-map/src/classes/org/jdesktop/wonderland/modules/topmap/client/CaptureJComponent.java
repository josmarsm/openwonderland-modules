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
package org.jdesktop.wonderland.modules.topmap.client;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * A JComponent that draws a given buffered image into it.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CaptureJComponent extends JComponent {

    private BufferedImage image = null;

    /**
     * Default constructor, takes the BufferedImage from which to draw the
     * image.
     */
    public CaptureJComponent(BufferedImage image) {
        super();
        this.image = image;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }
    }

    /**
     * Returns the BufferedImage associated with this JComponent.
     *
     * @return The buffered image
     */
    public BufferedImage getBufferedImage() {
        return image;
    }
}
