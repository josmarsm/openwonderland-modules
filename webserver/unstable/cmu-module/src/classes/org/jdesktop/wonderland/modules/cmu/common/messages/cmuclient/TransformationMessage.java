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
public class TransformationMessage extends NodeUpdateMessage {

    private Vector3f translation = null;
    private Matrix3f rotation = null;

    /**
     * Constructor with ID.
     * @param nodeID The ID of the node to which this transformation applies
     */
    public TransformationMessage(NodeID nodeID) {
        super(nodeID);
    }

    /**
     * Copy constructor.
     * @param toCopy The message to copy.
     */
    public TransformationMessage(TransformationMessage toCopy) {
        super(toCopy);
        synchronized (toCopy) {
            setTranslation(toCopy.getTranslation());
            setRotation(toCopy.getRotation());
        }
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

    /**
     * Get a String representation of the message, containing debug info.
     * @return String representation of the message
     */
    @Override
    public synchronized String toString() {
        String retVal = super.toString();
        retVal += "[translation=" + getTranslation() + "]";
        retVal += "[rotation=" + getRotation() + "]";
        return retVal;
    }
}
