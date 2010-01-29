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

package org.jdesktop.wonderland.modules.pdfpresentation.server;

import com.jme.bounding.BoundingBox;

/**
 * Utility interface so presentation-base doesn't have to depend on the PDF
 * spreading module, but can still call into tha object to get data out of it.
 *
 * Provides methods that tell presentation base about the PDF's layout to figure
 * out where to put cameras, platforms, etc.
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public interface SlidesCell {
        
    public int getNumSlides();

    /**
     *
     * @return The distance between the edges of the slides.
     */
    public float getInterslideSpacing();

    /**
     *
     * @return The distance between the centers of the slides. 
     */
    public float getCenterSpacing();

    public String getCreatorName();

    public float getMaxSlideWidth();

    public float getScale();

    public BoundingBox getPDFBounds();
}
