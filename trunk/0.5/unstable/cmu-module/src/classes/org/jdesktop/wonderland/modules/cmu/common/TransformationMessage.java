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
import java.io.Serializable;

/**
 *
 * @author kevin
 */
public class TransformationMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    private int nodeID;
    private float scale;
    private Vector3f translation;
    private Matrix3f rotation;

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

    public synchronized void setTranslation(Vector3f translation) {
        this.translation = translation;
    }

    public synchronized Vector3f getTranslation() {
        return this.translation;
    }

    public synchronized void setRotation(Matrix3f rotation) {
        this.rotation = rotation;
    }

    public synchronized Matrix3f getRotation() {
        return this.rotation;
    }

    @Override
    public synchronized String toString() {
        String retVal = "Transformation message\n[NodeID:" + getNodeID() + "]\n";
        retVal += "[Scale:" + getScale() + "]\n";
        retVal += "[Translation:" + getTranslation() + "]\n";
        retVal += "[Rotation:" + getRotation() + "]\n";
        return retVal;
    }

    public TransformationMessage getCopy() {
        TransformationMessage retVal = new TransformationMessage(this.getNodeID());
        retVal.setScale(this.getScale());
        retVal.setTranslation(this.getTranslation());
        retVal.setRotation(this.getRotation());
        return retVal;
    }
}
