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
package org.jdesktop.wonderland.modules.timeline.common.provider;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.io.Serializable;

/**
 * An image with an associated date
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class DatedModel implements DatedObject, Serializable {
    private TimelineDate date;
    
    private String modelURI;
    private Vector3f modelTranslation;
    private Vector3f modelScale;
    private Quaternion modelRotation;
    private String modelLoader;

    // bounds and description are optional
    private BoundingVolume bounds;
    private String description;

    public DatedModel(TimelineDate date, String modelURI,
                      Vector3f modelTranslation, Vector3f modelScale,
                      Quaternion modelRotation, String modelLoader)
    {
        this.date = date;
        this.modelURI = modelURI;
        this.modelTranslation = modelTranslation;
        this.modelScale = modelScale;
        this.modelRotation = modelRotation;
        this.modelLoader = modelLoader;

    }

    public TimelineDate getDate() {
        return date;
    }

    public String getModelURI() {
        return modelURI;
    }

    public Vector3f getModelTranslation() {
        return modelTranslation;
    }

    public Vector3f getModelScale() {
        return modelScale;
    }

    public Quaternion getModelRotation() {
        return modelRotation;
    }

    public String getModelLoader() {
        return modelLoader;
    }

    /**
     * Return the model bounds, or null if not defined
     */
    public BoundingVolume getBounds() {
        return bounds;
    }
    
    public void setBounds(BoundingVolume modelBounds) {
        this.bounds = modelBounds;
    }

    /**
     * Return the image description, or null if there is no description
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "[DatedModel " + modelURI + "]";
    }
}
