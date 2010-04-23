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
package org.jdesktop.wonderland.modules.connectionsample.client;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState.StateType;
import java.math.BigInteger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.connectionsample.common.ColoredCubeConstants;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;

/**
 * The renderer that creates the colored cube displayed by the
 * ColoredCubeCell.  The renderer updates the color of the cell
 * based on the "color" property in a shared map.
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ColoredCubeRenderer extends BasicRenderer {
    /** The shared state that determines the color of the cube */
    private SharedStateComponent state;

    /** the cube object in the scene graph */
    private TriMesh mesh;

    /**
     * Create a new ColoredCubeRenderer for the given cell.
     * @param cell the cell to create a renderer for.
     * @param state the shared state component to read color data from.
     */
    public ColoredCubeRenderer(Cell cell) {
        super(cell);
    }

    /**
     * Set the SharedStateComponent of the cell renderer.  Register with the
     * SharedStateComponent to receive updates to the cell state.
     * @param state the shared state component
     */
    public void setSharedState(SharedStateComponent state) {
        this.state = state;

        // get the appropriate map from the SharedStateComponent
        SharedMapCli map = state.get(ColoredCubeConstants.MAP_NAME);

        // read the current value of the color variable in the map
        SharedData colorData = map.get(ColoredCubeConstants.COLOR_KEY);
        if (colorData != null) {
            setColor(((SharedInteger) colorData).getValue());
        }

        // register a listener that will change the cube's color any
        // time the "color" variable in the shared map changes
        map.addSharedMapListener(ColoredCubeConstants.COLOR_KEY,
                new SharedMapListenerCli() {

                    public void propertyChanged(SharedMapEventCli arg0) {
                        if (arg0.getNewValue() instanceof SharedInteger) {
                            int color = ((SharedInteger) arg0.getNewValue()).getValue();
                            setColor(color);
                        }
                    }
                });
    }

    /**
     * Set the cube's color to a particular color.
     * @param color the color to set to.
     */
    protected void setColor(int color) {
        System.out.println("[ConnectionSampleRenderer] setColor " + color);
        setColor(color / 255f, 0.0f, 0.0f, 0.0f);
    }

    /**
     * Set the cube's color to the given color.  Note that the actual
     * setting is done in the JME render thread.
     * @param r the red component of the color
     * @param g the green component of the color
     * @param b the blue component of the color
     * @param a the alpha component of the color
     */
    protected void setColor(float r, float g, float b, float a) {
        final ColorRGBA c = new ColorRGBA(r, g, b, a);

        // MT Game requires that if we make any changes to the JME scene
        // graph, we do it in the MT Game commit thread.  This is similar
        // to the Swing requirement to make changes in the AWT event thread.
        // Using the SceneWorker will schedule the given work in the
        // commit thread, just like the SwingWorker schedules things in the
        // AWT event thread.
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                MaterialState ms = (MaterialState) mesh.getRenderState(StateType.Material);
                ms.setDiffuse(c);
            }
        });
    }

    /**
     * Create the scene graph object to represent the cube.
     * @param entity the entity to attach the object to
     * @return the node in the graph representing this object
     */
    protected Node createSceneGraph(Entity entity) {
        /* Fetch the basic info about the cell */
        String name = cell.getCellID().toString();

        /* Create the new mesh for the shape */
        mesh = new Box(name, new Vector3f(), 2, 2, 2);

        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        MaterialState ms = (MaterialState) rm.createRendererState(StateType.Material);
        ms.setDiffuse(new ColorRGBA(1.0f, 0.0f, 0.0f, 0.0f));
        mesh.setRenderState(ms);

        /* Create the scene graph object and set its wireframe state */
        Node node = new Node();
        node.attachChild(mesh);
        node.setModelBound(new BoundingSphere());
        node.updateModelBound();
        node.setName("Cell_" + cell.getCellID() + ":" + cell.getName());

        return node;
    }
}
