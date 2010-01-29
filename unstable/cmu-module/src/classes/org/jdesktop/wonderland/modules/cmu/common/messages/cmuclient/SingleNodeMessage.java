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

import org.jdesktop.wonderland.modules.cmu.common.NodeID;

/**
 * A message which applies to a single CMU node, referenced by node ID.
 * @author kevin
 */
public abstract class SingleNodeMessage extends CMUClientMessage {

    private NodeID nodeID;

    /**
     * Standard constructor.
     * @param nodeID ID for the node to which this message applies
     */
    public SingleNodeMessage(NodeID nodeID) {
        setNodeID(nodeID);
    }

    /**
     * Get the ID for the node to which this message applies.
     * @return ID for the node to which this message applies
     */
    public NodeID getNodeID() {
        return nodeID;
    }

    /**
     * Set the ID for the node to which this message applies.
     * @param nodeID ID for the node to which this message applies
     */
    public void setNodeID(NodeID nodeID) {
        this.nodeID = nodeID;
    }

    /**
     * Get a String representation of the message, containing debugging info.
     * @return String representation of the message
     */
    @Override
    public String toString() {
        return super.toString() + "[nodeID=" + getNodeID() + "]";
    }
}
