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
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.presentationbase.client.MovingPlatformCell;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class MovingPlatformCellRenderer extends BasicRenderer {

    private static final Logger logger = Logger.getLogger(MovingPlatformCellRenderer.class.getName());

    MovingPlatformCell platformCell;

    public MovingPlatformCellRenderer(Cell cell) {
        super(cell);

        platformCell = (MovingPlatformCell)cell;
    }

    protected Node createSceneGraph(Entity entity) {
        Node root = new Node();

        logger.warning("About to create PLATFORM TRIMESH with dimensions: " + platformCell.getPlatformWidth() + "x" + platformCell.getPlatformDepth());
        TriMesh platform = new Box("platform", Vector3f.ZERO, platformCell.getPlatformWidth(), 0.25f, platformCell.getPlatformDepth());

        root.attachChild(platform);
        logger.warning("Attached platform.");
        root.setModelBound(new BoundingBox(Vector3f.ZERO, platformCell.getPlatformWidth(), 0.25f, platformCell.getPlatformDepth()));
        root.updateModelBound();

        logger.warning("Updated bounds.");

        return root;
    }

    @Override
    public void setStatus(CellStatus status,boolean increasing) {
        super.setStatus(status, increasing);
        logger.warning("setting renderer status: " + status + "; increasisng? " + increasing);
    }
}
