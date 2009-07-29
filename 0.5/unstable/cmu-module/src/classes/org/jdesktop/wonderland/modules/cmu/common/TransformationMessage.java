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
package org.jdesktop.wonderland.modules.cmu.common;

import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import edu.cmu.cs.dennisc.math.OrthogonalMatrix3x3;
import edu.cmu.cs.dennisc.math.Point3;
import java.io.Serializable;

/**
 *
 * @author kevin
 */
public class TransformationMessage implements Serializable {

    private int nodeID;
    private float scale = 1;
    private Vector3f translation = null;
    private Matrix3f rotation = null;

    public TransformationMessage() {
        
    }

    public TransformationMessage(int nodeID) {
        this();
        this.setNodeID(nodeID);
    }

    public synchronized void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public synchronized int getNodeID() {
        return this.nodeID;
    }

    public synchronized void setScale(float scale) {
        this.scale = scale;
    }

    public synchronized float getScale() {
        return this.scale;
    }

    public synchronized void setTranslation(Point3 translation) {
        this.translation = new Vector3f(
                (float) translation.x,
                (float) translation.y,
                (float) translation.z);
    }

    public synchronized Vector3f getTranslation() {
        return this.translation;
    }

    public synchronized void setRotation(OrthogonalMatrix3x3 rotation) {
        this.rotation = new Matrix3f(
                (float) rotation.right.x, (float) rotation.up.x, (float) rotation.backward.x,
                (float) rotation.right.y, (float) rotation.up.y, (float) rotation.backward.y,
                (float) rotation.right.z, (float) rotation.up.z, (float) rotation.backward.z);
    }

    public synchronized Matrix3f getRotation() {
        return this.rotation;
    }
}
