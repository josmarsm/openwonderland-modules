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

package org.jdesktop.wonderland.modules.pdfspreader.client.jme.cell;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.RenderState.StateType;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.pdfspreader.client.PDFSpreaderCell;

public class PDFSpreaderCellRenderer extends BasicRenderer {
    private Node node = null;

    private PDFSpreaderCell pdfCell;

    private static final Logger logger =
            Logger.getLogger(PDFSpreaderCellRenderer.class.getName());

    public PDFSpreaderCellRenderer(Cell cell) {
        super(cell);
        this.pdfCell = (PDFSpreaderCell) cell;
    }

    @Override
    protected Node createSceneGraph(Entity entity) {

        logger.info("Creating scene graph for entity: " + entity);

        String name = cell.getCellID().toString();

        TriMesh mesh = new Box(cell.getCellID().toString(), new Vector3f(), 4, 4, 4f);

//        TriMesh mesh = new Tube(name, 1, 1,1, 50, 50);
       
        if (mesh == null) {
          node = new Node();
          return node;
        }

        node = new Node();
        node.attachChild(mesh);
        node.setModelBound(new BoundingBox());
        node.updateModelBound();
        node.setName("Cell_"+cell.getCellID()+":"+cell.getName());

        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();

        ColorRGBA color = new ColorRGBA(0.0f, 0.3f, 0.7f, 0.5f);

        mesh.setSolidColor(color);
        MaterialState matState3 = (MaterialState)
        rm.createRendererState(RenderState.StateType.Material);
        matState3.setDiffuse(color);
        mesh.setRenderState(matState3);

        BlendState alphaState = (BlendState)rm.createRendererState(StateType.Blend);
        alphaState.setBlendEnabled(true);
        alphaState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        alphaState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        alphaState.setTestEnabled(true);
        alphaState.setTestFunction(BlendState.TestFunction.GreaterThan);
        alphaState.setEnabled(true);
        mesh.setRenderState(alphaState);

        CullState cullState = (CullState)rm.createRendererState(StateType.Cull);
        cullState.setCullFace(CullState.Face.Back);
        mesh.setRenderState(cullState);

        node.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);


        return node;
    }
}