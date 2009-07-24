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

package org.jdesktop.wonderland.modules.cmu.client.cell.jme.cellrenderer;

import org.jdesktop.wonderland.modules.cmu.common.cell.player.TransformationMessage;

/**
 *
 * @author kevin
 */
public class VisualNode extends TransformableParent {

    protected final int nodeID;

    public VisualNode(int nodeID) {
        super();
        this.nodeID = nodeID;
    }

    public int getNodeID() {
        return this.nodeID;
    }

    @Override
    public synchronized void applyTransformationToChild(TransformationMessage transformation) {
        transformation.applyToMatchingNode(this);
        super.applyTransformationToChild(transformation);
    }
}
