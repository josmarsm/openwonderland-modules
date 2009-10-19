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

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Sphere;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.presentationbase.client.PresentationCell;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class PresentationCellRenderer extends BasicRenderer {

    private static final Logger logger = Logger.getLogger(PresentationCellRenderer.class.getName());

    PresentationCell presentationCell;

    public PresentationCellRenderer(Cell cell) {
        super(cell);

        presentationCell = (PresentationCell)cell;
    }

    protected Node createSceneGraph(Entity entity) {
        Node root = new Node();

//        BoundingBox boundingBox = (BoundingBox) cell.getLocalBounds();

//        logger.warning("About to create PLATFORM TRIMESH with dimensions: " + presentationCell.getPlatformWidth() + "x" + presentationCell.getPlatformDepth());
//        TriMesh bounds = new Box("platform", Vector3f.ZERO, boundingBox.xExtent, boundingBox.yExtent, boundingBox.zExtent);

        TriMesh dot = new Sphere("origin sphere", Vector3f.ZERO, 10, 10, 0.25f);

//        logger.warning("Setting bounds: " + boundingBox.xExtent + "-" + boundingBox.yExtent + "-" + boundingBox.zExtent);

        root.attachChild(dot);
        logger.warning("Attached platform.");
        root.setModelBound(cell.getLocalBounds());
        root.updateModelBound();

        logger.warning("Updated bounds.");

//        WireframeState wiState = (WireframeState)ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_WIREFRAME);
//        wiState.setEnabled(true);
//        root.setRenderState(wiState);

        return root;
    }

    @Override
    public void setStatus(CellStatus status,boolean increasing) {
        super.setStatus(status, increasing);
        logger.warning("setting renderer status: " + status + "; increasisng? " + increasing);
    }
}
