/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.animation;

import com.jme.animation.AnimationController;
import com.jme.animation.BoneAnimation;
import com.jme.animation.BoneTransform;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Spatial;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.utils.ScenegraphUtils;
import org.pushingpixels.trident.Timeline;

/**
 *
 * @author JagWire
 */
public class AnimationFactory {
    private BasicRenderer renderer;
    private float[] xTranslations;
    private float[] yTranslations;
    private float[] zTranslations;
    private float[] xAngles;
    private float[] yAngles;
    private float[] zAngles;
    private List controllers;
    private AnimationController animationController;
    private BoneAnimation animation;
    private Controller controller;
    private Timeline timeline;
    private Vector3f startTranslation;
    private float[] startRotation;
    private int timeMultiplier = 0;
    private int initialKeyframe = 0;
    private int endKeyframe = 0;
    final boolean traceLoad = false;
    private int animationIceCode = 0;
    private boolean animationSaveTransform = false;
    private boolean animationPlayReverse = false;
    private int nodeCount = 0;

    private List nodeList;

    private int NodeFrame = 0;
    private int NodeLast = 0;
    private boolean takeAvatar = false;

    public AnimationFactory(BasicRenderer Renderer, List nodeList) {
        this.renderer = Renderer;
        this.nodeList = nodeList;
    }

    public void processAllNodes() {

        Iterator<AnimationNode> iterator = nodeList.iterator();
        while (iterator.hasNext()) {

            AnimationNode animationNode = iterator.next();
            System.out.println("Element = " + animationNode.getName());
            processNode(animationNode);
        }
    }

    private void processNode(AnimationNode node) {
        
        node.setStartRotation(new Quaternion().toAngles(null));
        node.setStartTranslation(new Vector3f());
        node.setTimeMultiplier(timeMultiplier);
       
        node.finalRotation.fromAngles(0.0f, 0.0f, 0.0f);

        Spatial spatial = ScenegraphUtils.findNamedNode(renderer.getSceneRoot(),
                                                        node.getName());

        node.setSpatial(spatial);
        controllers = node.getControllers();

        animationController = node.getAnimationController(0);
        animation = animationController.getAnimation(0);
        node.setKeyframeTimes(animation.getKeyFrameTimes());
        int numberOfKeyframeTimes = node.getKeyframeTimes().length;

        if(initialKeyframe == 0 && endKeyframe == 0) {
            node.putStartFrame(0);
            node.putEndFrame(numberOfKeyframeTimes - 1);
        }
        else {
            node.putStartFrame(initialKeyframe);
            node.putEndFrame(endKeyframe - 1);
        }

// Make storage for the transforms at each keyframe

        xTranslations = new float[numberOfKeyframeTimes];
        yTranslations = new float[numberOfKeyframeTimes];
        zTranslations = new float[numberOfKeyframeTimes];
        xAngles = new float[numberOfKeyframeTimes];
        yAngles = new float[numberOfKeyframeTimes];
        zAngles = new float[numberOfKeyframeTimes];

// Make storage for the rotations and translations of each keyframe

        node.rotations = new Quaternion[numberOfKeyframeTimes];
        node.translations = new Vector3f[numberOfKeyframeTimes];

// There wil be multiple animations for the same node. Each animation will express one (?) of the
// directions of rotation or one of the directions of translation, or combinations. All the rotations
// for a key frame combined together will be the full rotation. All the translations for a key frame
// summed together will be the total translation for the key frame. Scaling should have the same structure
// when I get it added.
// The result of all this will be a series of Quaternions, one for each key frame start and one to finish
// that contain the rotations for each keyframe. Also, there will be a set of vectors that contain
// the translations.

// Step through the animation controllers to pick up the changes at each key frame
        for(int index = 0; index < controllers.size(); index++) {
            

// Get the controller
            AnimationController currentController = node.getAnimationController(index);

// Step through the animations
            for(int i = 0; i < currentController.getAnimations().size(); i++) {
// Get this animation
                BoneAnimation currentAnimation = currentController.getAnimation(i);

                List<BoneTransform> transforms = currentAnimation.getBoneTransforms();

                Quaternion rotations[] = transforms.get(0).getRotations();

                for(int j = 0; j < rotations.length; j++) {

                    float[] fromAngles = rotations[j].toAngles(null);

                    xAngles[j] = xAngles[j] + fromAngles[0];
                    yAngles[j] = yAngles[j] + fromAngles[1];
                    zAngles[j] = zAngles[j] + fromAngles[2];
                }

                Vector3f translations[] = transforms.get(0).getTranslations();

                for(int j = 0; j < translations.length; j++) {
                    
                    xTranslations[j] += translations[j].x;
                    yTranslations[j] += translations[j].y;
                    zTranslations[j] += translations[j].z;
                }
            }
        }
        // Build the final key frame rotations and translations
        for(int index = 0; index < numberOfKeyframeTimes; index++) {
            
            Quaternion rotation = new Quaternion();
            rotation.fromAngles(xAngles[index], yAngles[index], zAngles[index]);

            node.rotations[index] = rotation;
            node.translations[index] = new Vector3f(xTranslations[index], yTranslations[index], zTranslations[index]);

        }
    }


    public void setStartTranslation(Vector3f startTranslation) {        
        this.startTranslation = startTranslation;
    }

    public void setStartRotation(float[] startRotation) {        
        this.startRotation = startRotation;
    }

    public void setTimeMultiplier(int timeMultiplier) {        
        this.timeMultiplier = timeMultiplier;
    }
    public void setInitialKeyframe(int start) {
        initialKeyframe = start;
    }

    public void setEndKeyframe(int end) {        
        endKeyframe = end;
    }


    //
    //
    // wtf?
    //
    //
    public void setAnimationSaveTransform(boolean save) {
        animationSaveTransform = save;
    }

    public void setAnimationPlayReverse(boolean reverse) {
        animationPlayReverse = reverse;
    }

    public void setTakeAvatar(boolean take) {
        takeAvatar = take;
    }

    public void initializeNodes() {

        Iterator<AnimationNode> iterator = nodeList.iterator();

        while(iterator.hasNext()) {
            
            AnimationNode element = iterator.next();
            if(element.getName().equals("main")) {
                
                element.initializePrimaryNode();
                //element.setTakeAvatar(takeAvatar);

            } else {
                
                element.initializeChildNode();
            }
        }
    }

    public void animateNodes() {
        
        Iterator<AnimationNode> iterator = nodeList.iterator();
        while(iterator.hasNext()) {
            AnimationNode animationNode = iterator.next();
            animationNode.play();
        }
    }
}
