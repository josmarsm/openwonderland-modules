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

import com.jme.scene.Geometry;
import edu.cmu.cs.dennisc.scenegraph.Text;
import java.awt.Font;

/**
 * Extracts jME-compatible text data from a CMU textual geometry.
 * @author kevin
 */
public class TextConverter<TextType extends Text> extends GeometryConverter<TextType> {

    /**
     * Standard constructor.
     * @param text The geometry to translate
     */
    public TextConverter(TextType text) {
        super(text);
    }

    /**
     * Get the wrapped geometry for this object.
     * @return Wrapped geometry
     */
    @Override
    public TextType getCMUGeometry() {
        return super.getCMUGeometry();
    }

    /**
     * Get the jME geometry for this object.
     * @return jME geometry for the obect
     */
    @Override
    public Geometry getJMEGeometry() {
        return new com.jme.scene.Text(getText(), getText());
    }

    /**
     * Get the String which this Text represents.
     * @return String for this geometry
     */
    public String getText() {
        return getCMUGeometry().text.getValue();
    }

    /**
     * Get the font for this geometry.
     * @return Font for this geometry
     */
    public Font getFont() {
        return getCMUGeometry().font.getValue();
    }
}