/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

package org.jdesktop.wonderland.modules.pdfpresentation.common;

import java.io.Serializable;
import java.util.List;

/**
 * A wrapper for a set of slides' layout information. Also contains descriptive
 * paramters for the layout.
 *
 * @author Drew Harry <dharry@media.mit.edu>
 */
public class PresentationLayout implements Serializable {

    /**
     * The default slide scale
     */
    public static float DEFAULT_SCALE = 1.0f;

    /**
     * The default slide spacing (meters)
     */
    public static float DEFAULT_SPACING = 4.0f;

    private float maxWidth;
    private float maxHeight;

    public enum LayoutType {
        LINEAR,
        SEMICIRCLE,
        CIRCLE
    }

    private List<SlideMetadata> slides;

    private float scale = 1.0f;
    private float spacing = 0.0f;
    private LayoutType layout;

    /** Default constructor, required by JAXB */
    public PresentationLayout() {
    }
    
    public PresentationLayout(LayoutType layout) {
        this.layout = layout;
    }

    public LayoutType getLayout() {
        return layout;
    }

    public void setLayout(LayoutType layout) {
        this.layout = layout;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public List<SlideMetadata> getSlides() {
        return slides;
    }

    public void setSlides(List<SlideMetadata> slides) {
        this.slides = slides;
    }

    public float getSpacing() {
        return spacing;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
    }

    public void setMaxSlideWidth(float maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void setMaxSlideHeight(float maxHeight) {
        this.maxHeight = maxHeight;
    }

    public float getMaxSlideHeight() {
        return maxHeight * this.scale;
    }

    public float getMaxSlideWidth() {
        return maxWidth * this.scale;
    }

    @Override
    public String toString() {
        return "[PresentationLayout scale: " + this.scale + "; spacing: " +
                this.spacing + "]";
    }
}