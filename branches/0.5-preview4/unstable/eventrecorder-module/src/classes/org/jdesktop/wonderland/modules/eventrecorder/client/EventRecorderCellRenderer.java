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


package org.jdesktop.wonderland.modules.eventrecorder.client;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Pyramid;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.RenderState.StateType;
import com.sun.scenario.animation.Animation;
import com.sun.scenario.animation.Clip;
import com.sun.scenario.animation.Clip.RepeatBehavior;
import java.util.HashSet;
import java.util.Set;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;

/**
 *
 * @author bh37721
 */
public class EventRecorderCellRenderer extends BasicRenderer {

    private static final float WIDTH = 1f; //x-extent
    private static final float HEIGHT = WIDTH * 3 / 2 ; //y-extent was 0.3f
    private static final ColorRGBA RECORD_BUTTON_DEFAULT = new ColorRGBA(0.2f, 0, 0, 0.5f);
    private static final ColorRGBA RECORD_BUTTON_SELECTED = ColorRGBA.red.clone();
    private static MaterialState LOWERPYRAMID_MATERIALSTATE;
    private static MaterialState RECORDBUTTON_DEFAULT_MATERIALSTATE;
    private static MaterialState RECORDBUTTON_SELECTED_MATERIALSTATE;

    private boolean isRecording = false;
    private Pyramid buttonPyramid;
    private Set<Animation> animations = new HashSet<Animation>();

    static {
        LOWERPYRAMID_MATERIALSTATE = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Material);
        LOWERPYRAMID_MATERIALSTATE.setAmbient(ColorRGBA.blue);
        LOWERPYRAMID_MATERIALSTATE.setDiffuse(ColorRGBA.blue);
        LOWERPYRAMID_MATERIALSTATE.setSpecular(ColorRGBA.blue);
        LOWERPYRAMID_MATERIALSTATE.setEmissive(new ColorRGBA(0.0f, 0.0f, 0.0f, 1f));

        RECORDBUTTON_DEFAULT_MATERIALSTATE = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Material);
        RECORDBUTTON_DEFAULT_MATERIALSTATE.setAmbient(RECORD_BUTTON_DEFAULT);
        RECORDBUTTON_DEFAULT_MATERIALSTATE.setDiffuse(RECORD_BUTTON_DEFAULT);
        RECORDBUTTON_DEFAULT_MATERIALSTATE.setSpecular(RECORD_BUTTON_DEFAULT);
        RECORDBUTTON_DEFAULT_MATERIALSTATE.setEmissive(new ColorRGBA(0.0f, 0.0f, 0.0f, 1f));

        RECORDBUTTON_SELECTED_MATERIALSTATE = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Material);
        RECORDBUTTON_SELECTED_MATERIALSTATE.setAmbient(RECORD_BUTTON_SELECTED);
        RECORDBUTTON_SELECTED_MATERIALSTATE.setDiffuse(RECORD_BUTTON_SELECTED);
        RECORDBUTTON_SELECTED_MATERIALSTATE.setSpecular(RECORD_BUTTON_SELECTED);
        RECORDBUTTON_SELECTED_MATERIALSTATE.setEmissive(new ColorRGBA(0.0f, 0.0f, 0.0f, 1f));
    }
    


    public EventRecorderCellRenderer(Cell cell) {
        super(cell);
    }

    protected Node createSceneGraph(Entity entity) {
        /* Create the scene graph object*/
        Node root = new Node();
        attachRecordingDevice(root, entity);
        root.setModelBound(new BoundingBox());
        root.updateModelBound();
        //Set the name of the root node
        root.setName("Cell_" + cell.getCellID() + ":" + cell.getName());
        //Set the state of the button
        isRecording = ((EventRecorderCell)cell).isRecording();
        setRecording(isRecording);
        return root;
    }

    private void addPlinth(Node aNode) {
        float boxWidth = WIDTH /2;
        //The square of the hypotenuse...
        boxWidth = (float) Math.sqrt(2 * boxWidth * boxWidth);
        Box b = new Box("Plinth", new Vector3f(0, 0, 0), boxWidth, HEIGHT /16, boxWidth); //x, y, z
        b.setModelBound(new BoundingBox());
        b.updateModelBound();
        b.setRenderState(LOWERPYRAMID_MATERIALSTATE);
        aNode.attachChild(b);
    }

    private void attachRecordingDevice(Node device, Entity entity) {
        addLowerPyramid(device);
        addPlinth(device);
        addUpperPyramid(device, entity);
    }

    private void addLowerPyramid(Node aNode) {
        Pyramid p = new Pyramid("lower pyramid", WIDTH, HEIGHT);
        p.setModelBound(new BoundingSphere());
        p.updateModelBound();
        p.setRenderState(LOWERPYRAMID_MATERIALSTATE);
        p.setLocalTranslation(0, 0 - HEIGHT/2, 0);
        Matrix3f rot = new Matrix3f();
        rot.fromAngleAxis((float) Math.PI, new Vector3f(1f, 0f, 0f));
        p.setLocalRotation(rot);
        aNode.attachChild(p);
    }

    private void addUpperPyramid(Node aNode, Entity entity) {
        Node pyramidRoot = new Node();
        buttonPyramid = new Pyramid("upper pyramid", WIDTH, HEIGHT);
        Entity pyramidEntity = new Entity("button");
        
        buttonPyramid.setRenderState(RECORDBUTTON_DEFAULT_MATERIALSTATE);
        buttonPyramid.setLocalTranslation(0, HEIGHT/2, 0);
        pyramidRoot.attachChild(buttonPyramid);
        pyramidRoot.setModelBound(new BoundingSphere());
        pyramidRoot.updateModelBound();
        aNode.attachChild(pyramidRoot);
        RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(pyramidRoot);
        pyramidEntity.addComponent(RenderComponent.class, rc);

        RotationAnimationProcessor spinner = new RotationAnimationProcessor(pyramidEntity, pyramidRoot, 0f, 360, new Vector3f(0f,1f,0f));
        Clip clip = Clip.create(1000, Clip.INDEFINITE, spinner);
        clip.setRepeatBehavior(RepeatBehavior.LOOP);
        clip.start();
        animations.add(clip);
        makeEntityPickable(pyramidEntity, pyramidRoot);
        ButtonListener listener = new ButtonListener();
        listener.addToEntity(pyramidEntity);
        entity.addEntity(pyramidEntity);
    }

    private void setRenderState(Pyramid aPyramid, RenderState aRenderState) {
        aPyramid.setRenderState(aRenderState);
        ClientContextJME.getWorldManager().addToUpdateList(aPyramid);
    }


    void setRecording(boolean b) {
        isRecording = b;
        if (isRecording) {
            setRenderState(buttonPyramid, RECORDBUTTON_SELECTED_MATERIALSTATE);
        } else {
            setRenderState(buttonPyramid, RECORDBUTTON_DEFAULT_MATERIALSTATE);
        }
        enableAnimations(isRecording);
    }

    

    private void enableAnimations(boolean b) {
        for (Animation anim : animations) {
            if (b) {
                anim.resume();
            } else {
                anim.pause();
            }
        }
    }

    // Make this pyramidEntity pickable by adding a collision component to it
    protected void makeEntityPickable(Entity entity, Node node) {
        JMECollisionSystem collisionSystem = (JMECollisionSystem) ClientContextJME.getWorldManager().getCollisionManager().
                loadCollisionSystem(JMECollisionSystem.class);

        CollisionComponent cc = collisionSystem.createCollisionComponent(node);
        entity.addComponent(CollisionComponent.class, cc);
    }

    

    class ButtonListener extends EventClassListener {

        ButtonListener() {
            super();
        }

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseButtonEvent3D.class};
        }

        // Note: we don't override computeEvent because we don't do any computation in this listener.
        @Override
        public void commitEvent(Event event) {
            logger.info("commit " + event + " for " + this);
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            if (!mbe.isClicked()) {
                return;
            }
            //ignore any mouse button that isn't the left one
            if (mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }
            if (!isRecording) {
                logger.info("startRecording");
                ((EventRecorderCell)cell).startRecording();
            } else {
                logger.info("stop recording");
                ((EventRecorderCell)cell).stop();
            }



        }


    }

}
