/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.modulator.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.scriptingComponent.client.IntercellEvent;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.TimelineScenario;
import org.pushingpixels.trident.callback.TimelineCallback;

/**
 *
 * @author morrisford
 */
public class ModulatorANode
    {
    public Spatial nodeSpatial;
    public Quaternion[] nodeKeyFrames;
    public Vector3f[] nodeKeyTrans;
    public TimelineScenario nodeResult;
    public String nodeName = null;
    private float Tkft[];

    public Vector3f v3fFinal = new Vector3f(0.0f, 0.0f, 0.0f);
    public Quaternion rotFinal = new Quaternion();
    private Vector3f animationStartTranslate;
    private float[] animationStartRotation;
    private int animationTimeMultiplier;
    private int animationStartKeyframe = 0;
    private int animationEndKeyframe = 0;
    private int animationIceCode = 0;
    private boolean animationSaveTransform = false;
    private boolean animationPlayReverse = false;
    private Timeline t;

    private int startFrame = 0;
    private int endFrame = 0;

    public void play()
        {
        nodeResult.play();
        }

    public void initializeChildNode()
        {
        nodeResult = new TimelineScenario.Sequence();
        for(int j = startFrame; j < endFrame; j++)
            {
            t = new Timeline(this);
            t.addPropertyToInterpolate("NodeQuat", nodeKeyFrames[j], nodeKeyFrames[j + 1], new ModulatorQuaternionInterpolator());
            t.addPropertyToInterpolate("NodeTrans", nodeKeyTrans[j], nodeKeyTrans[j + 1], new ModulatorVectorInterpolator());
//        t.setEase(new Spline(0.4f));
            t.setDuration((long)((Tkft[j + 1] - Tkft[j]) * 1000) * animationTimeMultiplier);
            nodeResult.addScenarioActor(t);
            }
        }

    public void initializePrimaryNode()
        {
        float zRotationInc = 0.0f;
        float xRotationInc = 0.0f;


        Vector3f v3f = nodeSpatial.getLocalTranslation();

        nodeResult = new TimelineScenario.Sequence();
// Add each key frame info as an animation and then play the whole

        for(int j = startFrame; j < endFrame; j++)
            {
            t = new Timeline(this);
            float[] fromAngles = nodeKeyFrames[j].toAngles(null);
            float[] fromAnglesNext = nodeKeyFrames[j + 1].toAngles(null);

            float[] rotFinalAngles = rotFinal.toAngles(null);

            Quaternion quat = new Quaternion();
            quat.fromAngles(
                    fromAngles[0] + animationStartRotation[0],
                    fromAngles[1] + rotFinalAngles[1] + animationStartRotation[1],
                    fromAngles[2] + animationStartRotation[2]);
            Quaternion quatNext = new Quaternion();
            quatNext.fromAngles(
                    fromAnglesNext[0] + animationStartRotation[0],
                    fromAnglesNext[1] + rotFinalAngles[1] + animationStartRotation[1],
                    fromAnglesNext[2] + animationStartRotation[2]);

            t.addPropertyToInterpolate("NodeQuat", quat, quatNext, new ModulatorQuaternionInterpolator());

            zRotationInc = (float)Math.sin(animationStartRotation[1]);
            xRotationInc = (float)Math.cos(animationStartRotation[1]);

            if(v3fFinal.x == 0.0f && v3fFinal.y == 0.0 && v3fFinal.z == 0.0)
                {
                Vector3f tempVec = new Vector3f(
                    (nodeKeyTrans[j].x + animationStartTranslate.x),
                    (nodeKeyTrans[j].y + animationStartTranslate.y),
                    (nodeKeyTrans[j].z + animationStartTranslate.z));
                Vector3f tempVecPlus = new Vector3f(
                    (nodeKeyTrans[j + 1].x + animationStartTranslate.x),
                    (nodeKeyTrans[j + 1].y + animationStartTranslate.y),
                    (nodeKeyTrans[j + 1].z + animationStartTranslate.z));
                t.addPropertyToInterpolate("NodeTrans", tempVec, tempVecPlus, new ModulatorVectorInterpolator());
                }
            else
                {
                Vector3f tempVec = new Vector3f(
                    nodeKeyTrans[j].x + v3fFinal.x + (nodeKeyTrans[j].x * (float)Math.sin(rotFinalAngles[1])),
                    nodeKeyTrans[j].y + v3fFinal.y,
                    nodeKeyTrans[j].z + v3fFinal.z + (nodeKeyTrans[j].z * (float)Math.cos(rotFinalAngles[1])));
                Vector3f tempVecPlus = new Vector3f(
                    nodeKeyTrans[j + 1].x + v3fFinal.x + (nodeKeyTrans[j + 1].x * (float)Math.sin(rotFinalAngles[1])),
                    nodeKeyTrans[j + 1].y + v3fFinal.y,
                    nodeKeyTrans[j + 1].z + v3fFinal.z + (nodeKeyTrans[j + 1].z * (float)Math.cos(rotFinalAngles[1])));
                t.addPropertyToInterpolate("NodeTrans", tempVec, tempVecPlus, new ModulatorVectorInterpolator());
                }
            if(j == endFrame - 1)
                {
                t.addCallback(new TimelineCallback()
                    {

                    public void onTimelineDone()
                        {
                        Vector3f v3f = nodeSpatial.getLocalTranslation();
                        }

                    public void onTimelineStateChanged(TimelineState oldState, TimelineState newState, float arg2, float arg3)
                        {
                        if(newState == TimelineState.DONE)
                            {
                            if(animationSaveTransform)
                                {
                                v3fFinal = nodeSpatial.getLocalTranslation();
                                rotFinal = nodeSpatial.getLocalRotation();
                                }
                            ClientContext.getInputManager().postEvent(new IntercellEvent("F", animationIceCode));
                            }
                        }

                    public void onTimelinePulse(float arg0, float arg1)
                        {

                        }
                    });
                }

            t.setDuration((long)((Tkft[j + 1] - Tkft[j]) * 1000) * animationTimeMultiplier);
            nodeResult.addScenarioActor(t);
            }
        }
    public void putTkft(float[] theArray)
        {
        Tkft = theArray;
        }

    public float[] getTkft()
        {
        return Tkft;
        }

    public void putStartFrame(int startFrame)
        {
        this.startFrame = startFrame;
        }

    public void putEndFrame(int endFrame)
        {
        this.endFrame = endFrame;
        }

    public void setNodeQuat(Quaternion newQuat)
        {
        nodeSpatial.setLocalRotation(newQuat);
        ClientContextJME.getWorldManager().addToUpdateList(nodeSpatial);
        }

    public void setNodeTrans(Vector3f newTrans)
        {
        nodeSpatial.setLocalTranslation(newTrans);
        ClientContextJME.getWorldManager().addToUpdateList(nodeSpatial);
        }

    public void setAnimationStartTranslate(Vector3f startTranslate)
        {
        animationStartTranslate = startTranslate;
        }

    public void setAnimationStartRotation(float[] startRotation)
        {
        animationStartRotation = startRotation;
        }

    public void setAnimationTimeMultiplier(int timeMultiplier)
        {
        animationTimeMultiplier = timeMultiplier;
        }
    public void setAnimationStartKeyframe(int start)
        {
        animationStartKeyframe = start;
        }

    public void setAnimationEndKeyframe(int end)
        {
        animationEndKeyframe = end;
        }

    public void setAnimationIceCode(int code)
        {
        animationIceCode = code;
        }

    public void setAnimationSaveTransform(boolean save)
        {
        animationSaveTransform = save;
        }

    public void setAnimationPlayReverse(boolean reverse)
        {
        animationPlayReverse = reverse;
        }

    }

