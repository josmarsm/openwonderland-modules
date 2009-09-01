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

import edu.cmu.cs.dennisc.property.Property;
import edu.cmu.cs.dennisc.property.event.PropertyEvent;
import edu.cmu.cs.dennisc.property.event.PropertyListener;
import edu.cmu.cs.dennisc.scenegraph.Appearance;
import edu.cmu.cs.dennisc.texture.BufferedImageTexture;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * Extracts jME-compatible properties from a CMU Appearance object.
 * @author kevin
 */
public class AppearanceConverter implements PropertyListener {

    final static private String[] TEXTURE_PROPERTIES = {
        "diffuseColorTexture",
        "bumpTexture",};
    private Appearance appearance;
    private BufferedImage texture = null;

    /**
     * Standard constructor.
     * @param app The Appearance object to translate
     */
    public AppearanceConverter(Appearance appearance) {

        this.appearance = appearance;
        appearance.addPropertyListener(this);


        //TODO: Handle other appearance properties.
        for (Property p : appearance.getProperties()) {
            //System.out.println("APPEARANCE PROPERTY for " + app.getName() + ": " + p.getName() + ", " + p.getValue(app));
            //System.out.println(p);
        }

    }

    /**
     * Get the texture represented by the Appearance.
     * @return Appearance texture
     */
    public Image getTexture() {
        loadTexture();
        return texture;
    }

    /**
     * Get the height of the texture represented by the Appearance.
     * @return Texture height
     */
    public int getTextureHeight() {
        loadTexture();
        return texture.getHeight();
    }

    /**
     * Get the width of the texture represented by the Appearance.
     * @return Texture width
     */
    public int getTextureWidth() {
        loadTexture();
        return texture.getWidth();
    }

    protected void loadTexture() {
        if (texture == null) {
            // Set texture properties.
            edu.cmu.cs.dennisc.texture.Texture cmuText = null;
            for (int i = 0; i < TEXTURE_PROPERTIES.length && cmuText == null; i++) {
                cmuText = (edu.cmu.cs.dennisc.texture.Texture) (appearance.getPropertyNamed(TEXTURE_PROPERTIES[i]).getValue(appearance));
            }

            if (cmuText != null && cmuText instanceof BufferedImageTexture) {
                texture = ((BufferedImageTexture) cmuText).getBufferedImage();
            } else {
                texture = null;
            }
        }
    }

    public void propertyChanging(PropertyEvent e) {
        // No action
    }

    public void propertyChanged(PropertyEvent e) {
        //Logger.getLogger(AppearanceConverter.class.getName()).severe("Appearance property changed at runtime: " + e);
    }
}
