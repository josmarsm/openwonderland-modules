/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

package org.jdesktop.wonderland.modules.pdfpresentation.client.jme.cell;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.pdfpresentation.client.MovingPlatformCell;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class MovingPlatformCellRenderer extends BasicRenderer {

    private static final Logger logger = Logger.getLogger(MovingPlatformCellRenderer.class.getName());

    private TriMesh platform;
    private Node root;

    MovingPlatformCell platformCell;

    public MovingPlatformCellRenderer(Cell cell) {
        super(cell);

        platformCell = (MovingPlatformCell)cell;
    }

    protected Node createSceneGraph(Entity entity) {
        root = new Node();



        logger.warning("About to create PLATFORM TRIMESH with dimensions: " + platformCell.getPlatformWidth() + "x" + platformCell.getPlatformDepth());
        platform = new Box("platform", Vector3f.ZERO, platformCell.getPlatformWidth(), 0.10f, platformCell.getPlatformDepth());

        root.attachChild(platform);
        logger.warning("Attached platform.");
        root.setModelBound(new BoundingBox(Vector3f.ZERO, platformCell.getPlatformWidth(), 0.15f, platformCell.getPlatformDepth()));
        root.updateModelBound();

        logger.warning("Updated bounds.");


        // Per comments in PresentationCellRenderer, we need to apply a rotation
        // to the node so our coordiante system matches that of our parent cell.
//        root.setLocalRotation(new Quaternion().fromAngleNormalAxis((float) (Math.PI / 2), new Vector3f(0,1,0)));

        return root;
    }

    @Override
    public void setStatus(CellStatus status,boolean increasing) {
        super.setStatus(status, increasing);
        logger.warning("setting renderer status: " + status + "; increasisng? " + increasing);
    }

    public void layoutUpdated(final float platformWidth, final float platformDepth, final float scale) {

        // Might not need to do this dance; maybe just changing platform is
        // good enough. 
//        root.detachChild(platform);

        logger.warning("about to update platform size to " + platformWidth + "x" + platformDepth);

        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg) {

                platform.setLocalScale(scale);

                ClientContextJME.getWorldManager().addToUpdateList(root);
            }
            },null);

//        slide.setTransform(pdfCell.getLayout()..get(i).getTransform());
    }


//        platform = new Box("platform", Vector3f.ZERO, platformCell.getPlatformWidth(), 0.10f, platformCell.getPlatformDepth());
//        logger.warning("Created new platform box with width: " + platformCell.getPlatformWidth());
//        root.attachChild(platform);
}

