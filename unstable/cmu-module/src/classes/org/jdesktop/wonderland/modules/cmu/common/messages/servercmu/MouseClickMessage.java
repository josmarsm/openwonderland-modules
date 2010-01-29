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
package org.jdesktop.wonderland.modules.cmu.common.messages.servercmu;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;

/**
 * Message to inform the CMU program manager that a mouse click
 * has occurred, and to provide information about the click.  Only
 * left-clicks are processed.
 * @author kevin
 */
public class MouseClickMessage extends ServerCMUMessage {

    private NodeID nodeID;

    /**
     * Standard constructor.
     * @param cellID The cell whose program has received the mouse click
     * @param nodeID The particular node which has received the mouse click
     */
    public MouseClickMessage(CellID cellID, NodeID nodeID) {
        super(cellID);
        this.nodeID = nodeID;
    }

    /**
     * Get the ID of the node which has received the mouse click.
     * @return ID of the node which has been clicked
     */
    public NodeID getNodeID() {
        return nodeID;
    }

    /**
     * Set the ID of the node which has received the mouse click.
     * @param nodeID ID of the node which has been clicked
     */
    public void setNodeID(NodeID nodeID) {
        this.nodeID = nodeID;
    }
}