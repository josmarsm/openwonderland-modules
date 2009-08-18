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

import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;

/**
 * Serializable transformation information for a particular CMU visual.
 * Can be matched with a particular visual by its node ID.
 * @author kevin
 */
public class TransformationMessage extends CMUClientMessage {

    private static final long serialVersionUID = 1L;
    private NodeID nodeID;
    private float scale;
    private Vector3f translation;
    private Matrix3f rotation;

    /**
     * Basic constructor.
     */
    public TransformationMessage() {
    }

    /**
     * Constructor with ID.
     * @param nodeID The ID of the node to which this transformation applies
     */
    public TransformationMessage(NodeID nodeID) {
        this();
        this.setNodeID(nodeID);
    }

    /**
     * Copy constructor.
     * @param toCopy The message to copy.
     */
    public TransformationMessage(TransformationMessage toCopy) {
        synchronized(toCopy) {
            setNodeID(toCopy.getNodeID());
            setScale(toCopy.getScale());
            setTranslation(toCopy.getTranslation());
            setRotation(toCopy.getRotation());
        }
    }

    /**
     * Set the node ID to which this transformation applies.
     * @param nodeID New node ID
     */
    public synchronized void setNodeID(NodeID nodeID) {
        this.nodeID = nodeID;
    }

    /**
     * Get the node ID to which this transformation applies.
     * @return Revelant node ID
     */
    public synchronized NodeID getNodeID() {
        return this.nodeID;
    }

    /**
     * Set the scale at which the relevant visual node is drawn.
     * @param scale New scale
     */
    public synchronized void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * Get the scale at which the relevant visual node is drawn.
     * @return Current scale
     */
    public synchronized float getScale() {
        return this.scale;
    }

    /**
     * Set the positional translation for the relevant visual.
     * @param translation New translation
     */
    public synchronized void setTranslation(Vector3f translation) {
        this.translation = translation;
    }

    /**
     * Get the positional translation for the relevant visual.
     * @return Current translation
     */
    public synchronized Vector3f getTranslation() {
        return this.translation;
    }

    /**
     * Set the rotation of the relevant visual.
     * @param rotation New rotation
     */
    public synchronized void setRotation(Matrix3f rotation) {
        this.rotation = rotation;
    }

    /**
     * Get the rotation of the relevant visual.
     * @return Current rotation
     */
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
}
