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

import org.jdesktop.wonderland.modules.cmu.common.web.VisualAttributes.VisualRepoIdentifier;

/**
 * Serializable information about a CMU visual; stores geometry and texture
 * information, as well as transformation information (as a complete
 * TransformationMessage.  Associated with a unique node ID.
 * @author kevin
 */
public class VisualMessage extends SingleNodeMessage {

    private static final long serialVersionUID = 1L;
    private final TransformationMessage transformation;
    private final ModelPropertyMessage properties;
    private final VisualRepoIdentifier id;

    public VisualMessage(VisualRepoIdentifier id, TransformationMessage initialTransform, ModelPropertyMessage initialProperties) {
        super(initialTransform.getNodeID());
        assert initialTransform.getNodeID().equals(initialProperties.getNodeID());
        this.transformation = initialTransform;
        this.id = id;
        this.properties = initialProperties;
    }

    /**
     * Get transformation info as a TransformationMessage, which can be updated
     * directly.
     * @return Updatable transformation information
     */
    public TransformationMessage getTransformation() {
        return this.transformation;
    }

    public ModelPropertyMessage getProperties() {
        return this.properties;
    }

    public VisualRepoIdentifier getVisualID() {
        return this.id;
    }

    @Override
    public String toString() {
        return "Visual message: [NodeID:" + getNodeID() + "]";
    }
}