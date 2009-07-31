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
package org.jdesktop.wonderland.modules.cmu.player.conversions;

import edu.cmu.cs.dennisc.scenegraph.Appearance;
import edu.cmu.cs.dennisc.texture.BufferedImageTexture;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 *
 * @author kevin
 */
public class AppearanceConverter {

    final static private String TEXTURE_PROPERTY = "diffuseColorTexture";
    final static private String TEXTURE_PROPERTY_ALTERNATE = "bumpTexture";

    final private BufferedImage texture;

    public AppearanceConverter(Appearance app) {
        // Set texture properties.
        edu.cmu.cs.dennisc.texture.Texture cmuText = (edu.cmu.cs.dennisc.texture.Texture) (app.getPropertyNamed(TEXTURE_PROPERTY).getValue(app));
        if (cmuText == null) {
            cmuText = (edu.cmu.cs.dennisc.texture.Texture) (app.getPropertyNamed(TEXTURE_PROPERTY_ALTERNATE).getValue(app));
        }

        if (cmuText != null && BufferedImageTexture.class.isAssignableFrom(cmuText.getClass())) {
            texture = ((BufferedImageTexture) cmuText).getBufferedImage();
        }
        else {
            texture = null;
        }

        //TODO: Handle other appearance properties.
        //for (Property p : app.getProperties()) {
        //    System.out.println("APPEARANCE PROPERTY: " + p);
        //    System.out.println(p.getValue(app));
        //}
    }

    public Image getTexture() {
        return texture;
    }

    public int getTextureHeight() {
        return texture.getHeight();
    }

    public int getTextureWidth() {
        return texture.getWidth();
    }
}
