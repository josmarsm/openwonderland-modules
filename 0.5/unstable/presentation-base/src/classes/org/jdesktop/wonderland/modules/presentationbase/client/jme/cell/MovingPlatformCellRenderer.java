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

package org.jdesktop.wonderland.modules.presentationbase.client.jme.cell;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class MovingPlatformCellRenderer extends BasicRenderer {


    public MovingPlatformCellRenderer(Cell cell) {
        super(cell);
    }

    protected Node createSceneGraph(Entity entity) {
        Node root = new Node();

        TriMesh platform = new Box("platform", Vector3f.ZERO, 10.0f, 0.25f, 10.0f);

        root.attachChild(platform);
        root.setModelBound(new BoundingBox());
        root.updateModelBound();

        return root;
    }

}
