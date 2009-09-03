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
package org.jdesktop.wonderland.modules.cmu.player.conversions.scenegraph.properties;

import com.jme.renderer.ColorRGBA;
import edu.cmu.cs.dennisc.color.Color4f;
import edu.cmu.cs.dennisc.property.FloatProperty;
import edu.cmu.cs.dennisc.property.Property;
import edu.cmu.cs.dennisc.property.event.PropertyEvent;
import edu.cmu.cs.dennisc.property.event.PropertyListener;
import edu.cmu.cs.dennisc.scenegraph.Appearance;
import edu.cmu.cs.dennisc.texture.BufferedImageTexture;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Extracts jME-compatible properties from a CMU Appearance object.
 * @author kevin
 */
public class AppearanceConverter implements PropertyListener {

    // Property identifiers
    final static private String[] TEXTURE_PROPERTIES = {
        "diffuseColorTexture",
        "bumpTexture",};
    final static private String AMBIENT_COLOR = "ambientColor";
    final static private String DIFFUSE_COLOR = "diffuseColor";
    final static private String OPACITY = "opacity";
    final static private String FILLING_STYLE = "fillingStyle";
    final static private String SHADING_STYLE = "shadingStyle";
    final static private String SPECULAR_HIGHLIGHT_COLOR = "specularHighlightColor";
    final static private String EMISSIVE_COLOR = "emissiveColor";
    final static private String SPECULAR_HIGHLIGHT_EXPONENT = "specularHighlightExponent";
    final static private String DIFFUSE_COLOR_TEXTURE_ALPHA_BLENDED = "isDiffuseColorTextureAlphaBlended";
    final static private String ETHEREAL = "isEthereal";
    private Appearance appearance;
    private BufferedImage texture = null;

    /**
     * Standard constructor.
     * @param app The Appearance object to translate
     */
    public AppearanceConverter(Appearance appearance) {

        this.appearance = appearance;
        appearance.addPropertyListener(this);

        //DEBUG: Make sure we're not missing any properties
        for (Property p : appearance.getProperties()) {
            if (!propertyRecognized(p)) {
                Logger.getLogger(AppearanceConverter.class.getName()).severe("Unrecognized appearance property: " + p);
            }
        }

    }

    private static boolean propertyRecognized(Property p) {
        List<String> recognizedNames = new ArrayList<String>();
        recognizedNames.add(AMBIENT_COLOR);
        recognizedNames.add(DIFFUSE_COLOR);
        recognizedNames.add(OPACITY);
        recognizedNames.add(FILLING_STYLE);
        recognizedNames.add(SHADING_STYLE);
        recognizedNames.add(SPECULAR_HIGHLIGHT_COLOR);
        recognizedNames.add(EMISSIVE_COLOR);
        recognizedNames.add(SPECULAR_HIGHLIGHT_EXPONENT);
        recognizedNames.add(DIFFUSE_COLOR_TEXTURE_ALPHA_BLENDED);
        recognizedNames.add(ETHEREAL);

        for (int i = 0; i < TEXTURE_PROPERTIES.length; i++) {
            recognizedNames.add(TEXTURE_PROPERTIES[i]);
        }

        if (recognizedNames.contains(p.getName())) {
            return true;
        }
        return false;
    }

    public ColorRGBA getAmbientColor() {
        Color4f ambientColor = (Color4f) appearance.getPropertyNamed(AMBIENT_COLOR).getValue(appearance);
        return new ColorRGBA(ambientColor.red, ambientColor.green, ambientColor.blue, ambientColor.alpha);
    }

    public ColorRGBA getDiffuseColor() {
        Color4f diffuseColor = (Color4f) appearance.getPropertyNamed(DIFFUSE_COLOR).getValue(appearance);
        return new ColorRGBA(diffuseColor.red, diffuseColor.green, diffuseColor.blue, diffuseColor.alpha);
    }

    public ColorRGBA getSpecularColor() {
        Color4f specularColor = (Color4f) appearance.getPropertyNamed(SPECULAR_HIGHLIGHT_COLOR).getValue(appearance);
        return new ColorRGBA(specularColor.red, specularColor.green, specularColor.blue, specularColor.alpha);
    }

    public ColorRGBA getEmissiveColor() {
        Color4f emissiveColor = (Color4f) appearance.getPropertyNamed(EMISSIVE_COLOR).getValue(appearance);
        return new ColorRGBA(emissiveColor.red, emissiveColor.green, emissiveColor.blue, emissiveColor.alpha);
    }

    public float getOpacity() {
        return ((FloatProperty) appearance.getPropertyNamed(OPACITY)).getValue(appearance);
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
        //Logger.getLogger(AppearanceConverter.class.getName()).warning("Appearance property changed at runtime: " + e);
    }
}
