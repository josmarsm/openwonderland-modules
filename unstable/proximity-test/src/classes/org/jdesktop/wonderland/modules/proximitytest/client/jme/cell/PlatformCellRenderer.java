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
package org.jdesktop.wonderland.modules.proximitytest.client.jme.cell;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import java.awt.Color;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.proximitytest.client.PlatformCell;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class PlatformCellRenderer extends BasicRenderer {

    private static final Logger logger = Logger.getLogger(PlatformCellRenderer.class.getName());
    PlatformCell platformCell;
    TriMesh platform;
    Node root;

    public PlatformCellRenderer(Cell cell) {
        super(cell);

        platformCell = (PlatformCell) cell;
    }

    protected Node createSceneGraph(Entity entity) {
        root = new Node();

        logger.warning("About to create PLATFORM TRIMESH with dimensions: " + platformCell.getPlatformWidth() + "x" + platformCell.getPlatformDepth());
        platform = new Box("platform", Vector3f.ZERO, platformCell.getPlatformWidth(), 0.25f, platformCell.getPlatformDepth());

        root.attachChild(platform);
        logger.warning("Attached platform.");
        root.setModelBound(new BoundingBox(Vector3f.ZERO, platformCell.getPlatformWidth(), 0.25f, platformCell.getPlatformDepth()));
        root.updateModelBound();

        logger.warning("Updated bounds.");
        
        return root;
    }

    public void setColor(Color c) {


        final ColorRGBA color = new ColorRGBA(c.getAlpha(), c.getRed(), c.getGreen(), c.getBlue());
        

        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {

            public void update(Object arg0) {
                logger.warning("Setting color: " + color);
                RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();

                MaterialState matState3 = (MaterialState) rm.createRendererState(RenderState.StateType.Material);
                matState3.setColorMaterial(MaterialState.ColorMaterial.Diffuse);
                matState3.setDiffuse(color);
                matState3.setAmbient(color);
                root.setRenderState(matState3);
                ClientContextJME.getWorldManager().addToUpdateList(root);
            }
            
        }, null
        );
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        logger.warning("setting renderer status: " + status + "; increasisng? " + increasing);
    }
}
