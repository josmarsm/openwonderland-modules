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

import edu.cmu.cs.dennisc.scenegraph.Text;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import org.jdesktop.wonderland.modules.cmu.common.jme.CMUText;

/**
 * Extracts jME-compatible text data from a CMU textual geometry.
 * @param <TextType> Subclass of text which is being converted
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
    public CMUText getJMEGeometry() {
        return new CMUText(getText(), getFont());
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

    /**
     * Get the depth of the text in this geometry.
     * @return Text depth for this geometry
     */
    public float getDepth() {
        return getCMUGeometry().depth.getValue().floatValue();
    }

    /**
     * Textual geometries are expected to change (e.g. if the text they
     * represent is updated), so they are not persistent.
     * @return False in any case
     */
    @Override
    public boolean isPersistent() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<String> getExpectedPropertyNames() {
        Collection<String> retVal = new ArrayList<String>();
        retVal.add("text");
        retVal.add("font");
        retVal.add("depth");
        retVal.add("leftToRightAlignment");
        retVal.add("topToBottomAlignment");
        retVal.add("frontToBackAlignment");
        return retVal;
    }
}