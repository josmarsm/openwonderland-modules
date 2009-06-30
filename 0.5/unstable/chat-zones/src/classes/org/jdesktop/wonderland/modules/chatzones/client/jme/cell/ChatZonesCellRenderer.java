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

package org.jdesktop.wonderland.modules.chatzones.client.jme.cell;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingCapsule;
import com.jme.math.LineSegment;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Tube;
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
import org.jdesktop.wonderland.modules.chatzones.client.ChatZonesCell;

public class ChatZonesCellRenderer extends BasicRenderer {
    private Node node = null;

    private ChatZonesCell chatZoneCell;

    private static final Logger logger =
            Logger.getLogger(ChatZonesCellRenderer.class.getName());

    public ChatZonesCellRenderer(Cell cell) {
        super(cell);

        this.chatZoneCell = (ChatZonesCell) cell;
    }

    @Override
    protected Node createSceneGraph(Entity entity) {

        logger.info("Creating scene graph for entity: " + entity);

        String name = cell.getCellID().toString();

//        TriMesh mesh = new Box(cell.getCellID().toString(), new Vector3f(), 4, 4, 4f);

        TriMesh mesh = new Tube(name, 1, 0, 3, 50, 50);

//        float outerRadius = 1.0 + 0.2 *
//
//
//        Tube t = new Tube(name, outerRadius, innerRadius, thickness, 50, 50);
//        t.setSolidColor(color);
//
//        // Create the main node and set the material state on the node so the
//        // color shows up. Attach the tube to the node.
//        Node n = new Node();
//        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
//        MaterialState matState3 = (MaterialState)
//                rm.createRendererState(RenderState.StateType.Material);
//        matState3.setDiffuse(color);
//        n.setRenderState(matState3);
//        n.attachChild(t);
//
//        // Set the bound on the tube and update it
//        t.setModelBound(new BoundingSphere());
//        t.updateModelBound();


//        final BlendState alphaState = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
//        alphaState.setBlendEnabled(true);
//        alphaState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
//        alphaState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
//        alphaState.setTestEnabled(true);
//        alphaState.setTestFunction(BlendState.TestFunction.GreaterThan);
//        alphaState.setEnabled(true);
//
//        mesh.setRenderState(alphaState);
//        mesh.updateRenderState();

        // IMPORTANT: since the sphere will be transparent, place it
        // in the transparent render queue!
//        mesh.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);

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
        node.setRenderState(matState3);

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
        node.setRenderState(cullState);


        return node;
    }


}