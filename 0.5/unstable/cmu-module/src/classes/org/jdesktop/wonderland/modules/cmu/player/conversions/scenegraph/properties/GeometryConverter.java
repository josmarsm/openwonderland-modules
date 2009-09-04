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

import edu.cmu.cs.dennisc.property.event.PropertyEvent;
import edu.cmu.cs.dennisc.property.event.PropertyListener;
import edu.cmu.cs.dennisc.scenegraph.Geometry;

/**
 * Abstract class to convert a CMU geometry into something that jME can
 * recognize.
 * @author kevin
 */
public abstract class GeometryConverter<GeometryType extends Geometry> implements PropertyListener {

    private final GeometryType geometry;

    /**
     * Standard constructor.
     * @param geometry The geometry to convert
     */
    public GeometryConverter(GeometryType geometry) {
        this.geometry = geometry;
        geometry.addPropertyListener(this);
    }

    /**
     * Get the geometry being wrapped.
     * @return Geometry for this object
     */
    public GeometryType getCMUGeometry() {
        return geometry;
    }

    /**
     * Get the jME geometry equivalent to the wrapped CMU geometry.
     * @return Equivalent jME geometry
     */
    public abstract com.jme.scene.Geometry getJMEGeometry();
    
    /**
     * Find out whether the wrapped geometry is unchanging (i.e. a polygonal
     * model) or not (i.e. a textual geometry that might change as its
     * text is updated) in terms of its properties.  We allow persistent
     * geometries to be uploaded to the content repository and stored there,
     * whereas non-persistent geometries are sent and updated explicitly
     * using messsages.
     * @return Whether the wrapped geometry is expected to change
     */
    //public abstract boolean isPersistent();

    public void propertyChanging(PropertyEvent e) {
        // No action
    }

    public void propertyChanged(PropertyEvent e) {
        System.out.println("Geometry property changed: " + e);
    }
}
