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
package org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient;

import com.jme.scene.Geometry;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;

/**
 * Message to inform a client of updates to its non-persistent geometries
 * (e.g. text, etc.).
 * @author kevin
 */
public class GeometryUpdateMessage extends NodeUpdateMessage {

    private final Collection<Geometry> geometries = new Vector<Geometry>();

    /**
     * Standard constructor.
     * @param nodeID ID for the node whose geometries are being updated
     * @param geometries The new collection of non-persistent geometries
     * for the node
     */
    public GeometryUpdateMessage(NodeID nodeID, Collection<Geometry> geometries) {
        super(nodeID);
        for (Geometry geometry : geometries) {
            addGeometry(geometry);
        }
    }

    /**
     * Copy constructor.
     * @param toCopy The message to copy
     */
    public GeometryUpdateMessage(GeometryUpdateMessage toCopy) {
        super(toCopy);
        for (Geometry geometry : toCopy.getGeometries()) {
            addGeometry(geometry);
        }
    }

    /**
     * Add a geometry to this message.
     * @param geometry The geometry to add
     */
    protected void addGeometry(Geometry geometry) {
        geometries.add(geometry);
    }

    /**
     * Get the collection of geometries which should be added to the node
     * for this message.
     * @return Geometries for the node refferred to by this message
     */
    public Collection<Geometry> getGeometries() {
        return Collections.unmodifiableCollection(geometries);
    }
}