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
import edu.cmu.cs.dennisc.color.property.Color4fProperty;
import edu.cmu.cs.dennisc.property.FloatProperty;
import edu.cmu.cs.dennisc.property.Property;
import edu.cmu.cs.dennisc.scenegraph.Appearance;
import edu.cmu.cs.dennisc.texture.BufferedImageTexture;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jdesktop.wonderland.modules.cmu.player.conversions.pattern.PropertyOwnerConverter;

/**
 * Extracts jME-compatible properties from a CMU Appearance object.
 * @author kevin
 */
public class AppearanceConverter extends PropertyOwnerConverter {

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
     * @param appearance The CMU Appearance to convert
     */
    public AppearanceConverter(Appearance appearance) {
        super(appearance);
        this.appearance = appearance;
    }

    /**
     * Get the ambient color associated with this Appearance.
     * @return Ambient color associated with this Appearance
     */
    public ColorRGBA getAmbientColor() {
        Color4f ambientColor = ((Color4fProperty) appearance.getPropertyNamed(AMBIENT_COLOR)).getValue();
        return new ColorRGBA(ambientColor.red, ambientColor.green, ambientColor.blue, ambientColor.alpha);
    }

    /**
     * Get the diffuse color associated with this Appearance.
     * @return Diffuse color associated with this Appearance
     */
    public ColorRGBA getDiffuseColor() {
        Color4f diffuseColor = ((Color4fProperty) appearance.getPropertyNamed(DIFFUSE_COLOR)).getValue();
        return new ColorRGBA(diffuseColor.red, diffuseColor.green, diffuseColor.blue, diffuseColor.alpha);
    }

    /**
     * Get the specular color associated with this Appearance.
     * @return Specular color associated with this Appearance
     */
    public ColorRGBA getSpecularColor() {
        Color4f specularColor = ((Color4fProperty) appearance.getPropertyNamed(SPECULAR_HIGHLIGHT_COLOR)).getValue();
        return new ColorRGBA(specularColor.red, specularColor.green, specularColor.blue, specularColor.alpha);
    }

    /**
     * Get the emissive color associated with this Appearance.
     * @return Emissive color associated with this Appearance
     */
    public ColorRGBA getEmissiveColor() {
        Color4f emissiveColor = ((Color4fProperty) appearance.getPropertyNamed(EMISSIVE_COLOR)).getValue();
        return new ColorRGBA(emissiveColor.red, emissiveColor.green, emissiveColor.blue, emissiveColor.alpha);
    }

    /**
     * Get the opacity associated with this Appearance (as a float between 0
     * and 1).
     * @return Opacity for this Appearance
     */
    public float getOpacity() {
        return ((FloatProperty) appearance.getPropertyNamed(OPACITY)).getValue();
    }

    /**
     * Get the exponent for the specular highlight color.
     * @return Specular highlight exponent
     */
    public float getSpecularExponent() {
        return ((FloatProperty) appearance.getPropertyNamed(SPECULAR_HIGHLIGHT_EXPONENT)).getValue();
    }

    /**
     * Get the texture represented by the Appearance.
     * @return Appearance texture
     */
    public Image getTexture() {
        loadTextureIfNecessary();
        return texture;
    }

    /**
     * Get the height of the texture represented by the Appearance.
     * @return Texture height
     */
    public int getTextureHeight() {
        loadTextureIfNecessary();
        return texture.getHeight();
    }

    /**
     * Get the width of the texture represented by the Appearance.
     * @return Texture width
     */
    public int getTextureWidth() {
        loadTextureIfNecessary();
        return texture.getWidth();
    }

    /**
     * Load the texture associated with this visual, if it hasn't been already.
     */
    protected void loadTextureIfNecessary() {
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

    /**
     * For debugging purposes, get the properties which are expected in an
     * Appearance.
     * @return Properties expected in an Appearance instance
     */
    @Override
    protected Collection<String> getExpectedPropertyNames() {
        List<String> expectedNames = new ArrayList<String>();
        expectedNames.add(AMBIENT_COLOR);
        expectedNames.add(DIFFUSE_COLOR);
        expectedNames.add(OPACITY);
        expectedNames.add(FILLING_STYLE);
        expectedNames.add(SHADING_STYLE);
        expectedNames.add(SPECULAR_HIGHLIGHT_COLOR);
        expectedNames.add(EMISSIVE_COLOR);
        expectedNames.add(SPECULAR_HIGHLIGHT_EXPONENT);
        expectedNames.add(DIFFUSE_COLOR_TEXTURE_ALPHA_BLENDED);
        expectedNames.add(ETHEREAL);

        for (int i = 0; i < TEXTURE_PROPERTIES.length; i++) {
            expectedNames.add(TEXTURE_PROPERTIES[i]);
        }
        return expectedNames;
    }
}
