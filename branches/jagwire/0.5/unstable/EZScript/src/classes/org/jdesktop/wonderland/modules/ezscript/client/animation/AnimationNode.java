/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.animation;

import com.jme.animation.AnimationController;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Spatial;
import java.io.IOException;
import java.util.List;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.TimelineScenario;
import org.pushingpixels.trident.callback.TimelineCallback;
import org.pushingpixels.trident.callback.TimelineScenarioCallback;


/**
 * 
 * 
 * @author JagWire
 */
public class AnimationNode {

    private Spatial spatial;
    public Quaternion[] rotations;
    public Vector3f[] translations;
    public TimelineScenario scenario;
    private String name = null;
    private float keyframeTimes[];

    public Vector3f finalTranslation = new Vector3f(0.0f, 0.0f, 0.0f);
    public Quaternion finalRotation = new Quaternion();
    private Vector3f startTranslation;
    private float[] startRotation;
    private int timeMultiplier;
    private int animationStartKeyframe = 0;
    private int animationEndKeyframe = 0;
    private boolean animationSaveTransform = false;
    private boolean animationPlayReverse = false;
    private Timeline timeline;

    private int startFrame = 0;
    private int endFrame = 0;
    private boolean takeAvatar = false;


    public void play() {
        
        scenario.play();
        
        scenario.addCallback(new TimelineScenarioCallback() {

            public void onTimelineScenarioDone() {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

        });
    }

    /**
     * For every keyframe create a new timeline, interpolate the rotations
     * and translations and add to the overarching scenario.
     */
    public void initializeChildNode() {
        
        scenario = new TimelineScenario.Sequence();
        for(int j = startFrame; j < endFrame; j++) {
            
            timeline = new Timeline(this);
            timeline.addPropertyToInterpolate("NodeRotation", rotations[j], rotations[j + 1], new QuaternionInterpolator());
            timeline.addPropertyToInterpolate("NodeTranslation", translations[j], translations[j + 1], new VectorInterpolator());

            timeline.setDuration((long)((keyframeTimes[j + 1] - keyframeTimes[j]) * 1000) * timeMultiplier);
            scenario.addScenarioActor(timeline);
        }
    }

    public void initializePrimaryNode() {

        scenario = new TimelineScenario.Sequence();
// Add each key frame info as an animation and then play the whole

        for(int j = startFrame; j < endFrame; j++)
            {
            timeline = new Timeline(this);
            float[] fromAngles = rotations[j].toAngles(null);
            float[] fromAnglesNext = rotations[j + 1].toAngles(null);

            float[] finalRotationAngles = finalRotation.toAngles(null);

            Quaternion quaternion = new Quaternion();
            quaternion.fromAngles(
                    fromAngles[0] + startRotation[0],
                    fromAngles[1] + finalRotationAngles[1] + startRotation[1],
                    fromAngles[2] + startRotation[2]);
            Quaternion nextQuaternion = new Quaternion();
            nextQuaternion.fromAngles(
                    fromAnglesNext[0] + startRotation[0],
                    fromAnglesNext[1] + finalRotationAngles[1] + startRotation[1],
                    fromAnglesNext[2] + startRotation[2]);

            timeline.addPropertyToInterpolate("NodeRotation",
                                              quaternion,
                                              nextQuaternion,
                                              new QuaternionInterpolator());

            if(finalTranslation.x == 0.0f 
                    && finalTranslation.y == 0.0
                        && finalTranslation.z == 0.0) {
                
                Vector3f translation = new Vector3f(
                    (translations[j].x + startTranslation.x),
                    (translations[j].y + startTranslation.y),
                    (translations[j].z + startTranslation.z));
                Vector3f nextTranslation = new Vector3f(
                    (translations[j + 1].x + startTranslation.x),
                    (translations[j + 1].y + startTranslation.y),
                    (translations[j + 1].z + startTranslation.z));

                timeline.addPropertyToInterpolate("NodeTranslation",
                                                  translation,
                                                  nextTranslation,
                                                  new VectorInterpolator());
            }
            else {
                
                Vector3f tempVec = new Vector3f(
                    translations[j].x + finalTranslation.x + (translations[j].x * (float)Math.sin(finalRotationAngles[1])),
                    translations[j].y + finalTranslation.y,
                    translations[j].z + finalTranslation.z + (translations[j].z * (float)Math.cos(finalRotationAngles[1])));
                Vector3f tempVecPlus = new Vector3f(
                    translations[j + 1].x + finalTranslation.x + (translations[j + 1].x * (float)Math.sin(finalRotationAngles[1])),
                    translations[j + 1].y + finalTranslation.y,
                    translations[j + 1].z + finalTranslation.z + (translations[j + 1].z * (float)Math.cos(finalRotationAngles[1])));
                timeline.addPropertyToInterpolate("NodeTranslation", tempVec, tempVecPlus, new VectorInterpolator());
            }
            if(j == endFrame - 1) {
                timeline.addCallback(new TimelineCallback() {

                    public void onTimelineDone() {
                        //Vector3f v3f = spatial.getLocalTranslation();
                    }

                    public void onTimelineStateChanged(TimelineState oldState, TimelineState newState, float arg2, float arg3) {
                        if(newState == TimelineState.DONE) {
                            if(animationSaveTransform) {
                                finalTranslation = spatial.getLocalTranslation();
                                finalRotation = spatial.getLocalRotation();
                            }
                        }
                    }

                    public void onTimelinePulse(float arg0, float arg1) {

                    }
                });
            }

            timeline.setDuration((long)((keyframeTimes[j + 1] - keyframeTimes[j]) * 1000) * timeMultiplier);
            scenario.addScenarioActor(timeline);
        }
    }
    public void setKeyframeTimes(float[] times) {
        
        keyframeTimes = times;
    }

    public float[] getKeyframeTimes() {
        
        return keyframeTimes;
    }

    public void putStartFrame(int startFrame) {
        
        this.startFrame = startFrame;
    }

    public void putEndFrame(int endFrame) {
        
        this.endFrame = endFrame;
    }

    public void setNodeRotation(Quaternion rotation) {
        
        spatial.setLocalRotation(rotation);
        ClientContextJME.getWorldManager().addToUpdateList(spatial);
    }

    public void setNodeTranslation(Vector3f translation) throws IOException {
        
        spatial.setLocalTranslation(translation);
        ClientContextJME.getWorldManager().addToUpdateList(spatial);
//        if(takeAvatar) {
//
//            ClientContextJME.getClientMain().gotoLocation(null, translation, finalRotation);
//            System.out.println("Avatar vector = " + translation);
//        }
    }

    public AnimationController getAnimationController(int index) {
        return (AnimationController)spatial.getController(index);
    }

    public List<Controller> getControllers() {
        return spatial.getControllers();
    }

    public void setSpatial(Spatial spatial) {
        this.spatial = spatial;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setStartRotation(float[] rotation) {
        this.startRotation = rotation;
    }

    public void setStartTranslation(Vector3f translation) {
        this.startTranslation = translation;
    }

    public void setTimeMultiplier(int multiplier) {
        this.timeMultiplier = multiplier;
    }
}
