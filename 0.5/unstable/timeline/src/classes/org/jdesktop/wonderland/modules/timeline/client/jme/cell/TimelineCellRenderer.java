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

package org.jdesktop.wonderland.modules.timeline.client.jme.cell;

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
 *  
 */
public class TimelineCellRenderer extends BasicRenderer {


    public TimelineCellRenderer(Cell cell) {
        super(cell);
    }
    
    @Override
    protected Node createSceneGraph(Entity entity) {
        TriMesh box = new Box("box", Vector3f.ZERO, 3.0f, 3.0f, 3.0f);

        Node root = new Node();
        root.attachChild(box);

        root.setModelBound(new BoundingBox(Vector3f.ZERO, 3.0f, 3.0f, 3.0f));
        root.updateModelBound();

        return root;
    }

}
