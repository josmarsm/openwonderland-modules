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

package org.jdesktop.wonderland.modules.eventplayer.client;

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
 * The renderer for the event player cell
 * @author Bernard Horan
 */
public class EventPlayerCellRenderer extends BasicRenderer {
    
    private static final float WIDTH = 1f; //x-extent
    private static final float HEIGHT = WIDTH * 3 / 2 ; //y-extent was 0.3f
    private static final ColorRGBA PLAY_BUTTON_DEFAULT = new ColorRGBA(0, 0.5f, 0.2f, 1f);
    private static final ColorRGBA PLAY_BUTTON_SELECTED = ColorRGBA.green.clone();
    private static MaterialState LOWERPYRAMID_MATERIALSTATE;
    private static MaterialState PLAYBUTTON_DEFAULT_MATERIALSTATE;
    private static MaterialState PLAYBUTTON_SELECTED_MATERIALSTATE;
    private static final float MINIMUM_SCALE = 0.1f;

    private Pyramid buttonPyramid;
    private Set<Animation> animations = new HashSet<Animation>();

    static {
        LOWERPYRAMID_MATERIALSTATE = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Material);
        LOWERPYRAMID_MATERIALSTATE.setAmbient(ColorRGBA.blue);
        LOWERPYRAMID_MATERIALSTATE.setDiffuse(ColorRGBA.blue);
        LOWERPYRAMID_MATERIALSTATE.setSpecular(ColorRGBA.blue);
        LOWERPYRAMID_MATERIALSTATE.setEmissive(new ColorRGBA(0.0f, 0.0f, 0.0f, 1f));

        PLAYBUTTON_DEFAULT_MATERIALSTATE = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Material);
        PLAYBUTTON_DEFAULT_MATERIALSTATE.setAmbient(PLAY_BUTTON_DEFAULT);
        PLAYBUTTON_DEFAULT_MATERIALSTATE.setDiffuse(PLAY_BUTTON_DEFAULT);
        PLAYBUTTON_DEFAULT_MATERIALSTATE.setSpecular(PLAY_BUTTON_DEFAULT);
        PLAYBUTTON_DEFAULT_MATERIALSTATE.setEmissive(new ColorRGBA(0.0f, 0.0f, 0.0f, 1f));

        PLAYBUTTON_SELECTED_MATERIALSTATE = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Material);
        PLAYBUTTON_SELECTED_MATERIALSTATE.setAmbient(PLAY_BUTTON_SELECTED);
        PLAYBUTTON_SELECTED_MATERIALSTATE.setDiffuse(PLAY_BUTTON_SELECTED);
        PLAYBUTTON_SELECTED_MATERIALSTATE.setSpecular(PLAY_BUTTON_SELECTED);
        PLAYBUTTON_SELECTED_MATERIALSTATE.setEmissive(new ColorRGBA(0.0f, 0.0f, 0.0f, 1f));
    }
    private Node pyramidRoot;
    private Entity pyramidEntity;

    public EventPlayerCellRenderer(Cell cell) {
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
        //Get the state of the recording
        boolean isLoaded = ((EventPlayerCell)cell).isRecordingLoaded();
        if (isLoaded) {
            enlargeButtonPyramid();
        }
        //Set the state of the button
        boolean isPlaying = ((EventPlayerCell)cell).isPlayingTape();
        setPlaying(isPlaying);
        //enableAnimations(isPlayingTape);
        return root;
    }

    void enlargeButtonPyramid() {
        ScaleAnimationProcessor enlarger = new ScaleAnimationProcessor(pyramidEntity, pyramidRoot, MINIMUM_SCALE, 1.0f);
        Clip enlargingClip = Clip.create(1000, 1, enlarger);
        //reducingClip = Clip.create(1000, Clip.INDEFINITE, reducer);
        //reducingClip.setRepeatBehavior(RepeatBehavior.REVERSE);
        

        TranslationAnimationProcessor translator = new TranslationAnimationProcessor(pyramidEntity, pyramidRoot, new Vector3f(0f,HEIGHT/8,0f) , new Vector3f(0f,HEIGHT/2,0f));
        Clip translatorClip = Clip.create(1000, 1, translator);
        //translatorClip.setRepeatBehavior(RepeatBehavior.REVERSE);
        enlargingClip.start();
        //enlargingClip.resume();
        translatorClip.start();
        //translatorClip.resume();
    }

    void reduceButtonPyramid() {
        ScaleAnimationProcessor reducer = new ScaleAnimationProcessor(pyramidEntity, pyramidRoot, 1.0f, MINIMUM_SCALE);
        Clip reducingClip = Clip.create(1000, 1, reducer);
        //reducingClip = Clip.create(1000, Clip.INDEFINITE, reducer);
        //reducingClip.setRepeatBehavior(RepeatBehavior.REVERSE);

        TranslationAnimationProcessor translator = new TranslationAnimationProcessor(pyramidEntity, pyramidRoot , new Vector3f(0f,HEIGHT/2,0f), new Vector3f(0f,0f,0f));
        Clip translatorClip = Clip.create(1000, 1, translator);
        //translatorClip.setRepeatBehavior(RepeatBehavior.REVERSE);
        reducingClip.start();
        //reducingClip.resume();
        translatorClip.start();
        //translatorClip.resume();
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

    private void setRenderState(Pyramid aPyramid, RenderState aRenderState) {
        aPyramid.setRenderState(aRenderState);
        ClientContextJME.getWorldManager().addToUpdateList(aPyramid);
    }

    void setPlaying(boolean b) {
        if (b) {
            setRenderState(buttonPyramid, PLAYBUTTON_SELECTED_MATERIALSTATE);
        } else {
            setRenderState(buttonPyramid, PLAYBUTTON_DEFAULT_MATERIALSTATE);
        }
        enableAnimations(b);
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
        pyramidRoot = new Node();
        buttonPyramid = new Pyramid("upper pyramid", WIDTH, HEIGHT);
        pyramidEntity = new Entity("button");

        buttonPyramid.setRenderState(PLAYBUTTON_DEFAULT_MATERIALSTATE);
        //buttonPyramid.setLocalTranslation(0, HEIGHT/2, 0);
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

        pyramidRoot.setLocalScale(MINIMUM_SCALE);
        

        makeEntityPickable(pyramidEntity, pyramidRoot);
        ButtonListener listener = new ButtonListener();
        listener.addToEntity(pyramidEntity);
        entity.addEntity(pyramidEntity);
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

    // Make this buttonEntity pickable by adding a collision component to it
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
            //ignore any mouse button that isn't the left one
            if (mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }
            if (mbe.isClicked() == false) {
                return;
            }

            logger.info("toggle playing");
            ((EventPlayerCell)cell).togglePlaying();
            
        }

        
    }

    
}
