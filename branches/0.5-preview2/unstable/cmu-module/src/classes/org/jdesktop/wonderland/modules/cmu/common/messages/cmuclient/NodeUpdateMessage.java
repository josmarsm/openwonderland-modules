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
 * Message representing an update for the properties of a particular node -
 * transformation, visual properties, etc.  This differs from other
 * types of SingleNodeMessage's in that the message should be able to be
 * applied directly to the node (with no notion of context), rather than
 * simply being <i>about</i> the node.
 * @author kevin
 */
public abstract class NodeUpdateMessage extends SingleNodeMessage {

    /**
     * Standard constructor.
     * @param nodeID ID for the node to which this message applies
     */
    public NodeUpdateMessage(NodeID nodeID) {
        super(nodeID);
    }

    /**
     * Copy constructor.
     * @param toCopy Message to copy
     */
    public NodeUpdateMessage(NodeUpdateMessage toCopy) {
        this(toCopy.getNodeID());
    }
}
