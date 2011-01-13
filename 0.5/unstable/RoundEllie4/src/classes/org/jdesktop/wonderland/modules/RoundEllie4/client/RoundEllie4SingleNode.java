/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.RoundEllie4.client;

import com.jme.animation.AnimationController;
import com.jme.animation.BoneAnimation;
import com.jme.animation.BoneTransform;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Spatial;
import java.util.ArrayList;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.TimelineScenario;

/**
 *
 * @author morrisford
 */
public class RoundEllie4SingleNode 
    {
    private RoundEllie4Renderer renderer;
    private Spatial theSpatial;
    private float Tkft[];
    private float[] transX;
    private float[] transY;
    private float[] transZ;
    private float[] angleX;
    private float[] angleY;
    private float[] angleZ;
    private Quaternion[] keyFrames;
    private Vector3f[] keyTrans;
    private Vector3f animationStartTranslate;
    private float[] animationStartRotation;
    private int animationTimeMultiplier;
    private int animationStartKeyframe = 0;
    private int animationEndKeyframe = 0;
    private int animationIceCode = 0;
    
    public RoundEllie4SingleNode(RoundEllie4Renderer Renderer, String nodeName)
        {
        renderer = Renderer;

        theSpatial = renderer.getNode(nodeName);

        System.out.println("theSpatial = " + theSpatial);
        ArrayList al = theSpatial.getControllers();

        Controller Tco = theSpatial.getController(0);
        float max = Tco.getMaxTime();
        float min = Tco.getMinTime();

        AnimationController Tac = (AnimationController)Tco;
        BoneAnimation Tani = Tac.getAnimation(0);
        float floatTemp[] = Tani.getKeyFrameTimes();
        Tkft = floatTemp;
        System.out.println("Tkft size = " + Tkft.length);

// Make storage for the transforms at each keyframe

        transX = new float[Tkft.length];
        transY = new float[Tkft.length];
        transZ = new float[Tkft.length];
        angleX = new float[Tkft.length];
        angleY = new float[Tkft.length];
        angleZ = new float[Tkft.length];
// Make storage for the Quats that will hold the rotates at each keyframe
        keyFrames = new Quaternion[Tkft.length];
// and the vectors that will hold the translates
        keyTrans = new Vector3f[Tkft.length];

// There wil be multiple animations for the same node. Each animation will express one (?) of the
// directions of rotation or one of the directions of translation, or combinations. All the rotations
// for a key frame combined together will be the full rotation. All the translations for a key frame
// summed together will be the total translation for the key frame. Scaling should have the same structure
// when I get it added.
// The result of all this will be a series of Quaternions, one for each key frame start and one to finish
// that contain the rotations for each keyframe. Also, there will be a set of vectors that contain
// the translations.

// Step through the animation controllers to pick up the changes at each key frame
        for(int k = 0; k < al.size(); k++)
            {
            System.out.println("Controller = " + al.get(k).toString());

// Get the controller and then cast it to an animation controller
            Controller co = theSpatial.getController(k);
            AnimationController ac = (AnimationController)co;
// Get the animations for this controller
            ArrayList bans = ac.getAnimations();
// Step through the animations
            for(int i = 0; i < bans.size(); i++)
                {
// Get this animation
                BoneAnimation ani = ac.getAnimation(i);
// Get the frame times for this animation
                float kft[] = ani.getKeyFrameTimes();
                System.out.println("kft size = " + kft.length);

                for(int j = 0; j < kft.length; j++)
                    {
                    System.out.println("kft - " + kft[j]);
                    }
                ArrayList<BoneTransform> bt = ani.getBoneTransforms();
                System.out.println("bt size = " + bt.size());
                for(int j = 0; j < bt.size(); j++)
                    {
                    System.out.println("bt = " + bt.get(j).toString());
                    }
                Quaternion qt[] = bt.get(0).getRotations();
                System.out.println("bt - rotations = " + qt.length);

                for(int j = 0; j < qt.length; j++)
                    {
                    System.out.println("rots = " + qt[j]);
                    float[] fromAngles = qt[j].toAngles(null);
                    System.out.println("Angles - " + fromAngles[0] + " - " + fromAngles[1] + " - " + fromAngles[2]);

                    angleX[j] = angleX[j] + fromAngles[0];
                    angleY[j] = angleY[j] + fromAngles[1];
                    angleZ[j] = angleZ[j] + fromAngles[2];
                    }
                System.out.println(" start frame = " + ani.getStartFrame() + " - end frame = " + ani.getEndFrame());
                Vector3f v3f[] = bt.get(0).getTranslations();
                System.out.println("bt - translations = " + v3f.length);

                for(int j = 0; j < v3f.length; j++)
                    {
                    System.out.println("trans = " + v3f[j]);
                    transX[j] = transX[j] + v3f[j].x;
                    transY[j] = transY[j] + v3f[j].y;
                    transZ[j] = transZ[j] + v3f[j].z;
                    }
                }
            }
        // Build the final key frame Quats from the extracted angles and the Vectors from the extracted translates
        for(int j = 0; j < Tkft.length; j++)
            {
            Quaternion temp = null;
            temp = new Quaternion();
            temp.fromAngles(angleX[j], angleY[j], angleZ[j]);
            System.out.println(" ********************** final angles " + angleX[j] + "--" + angleY[j] + "--" + angleZ[j]);
            keyFrames[j] = temp;
            keyTrans[j] = new Vector3f(transX[j], transY[j], transZ[j]);
            System.out.println(" *************** final translations " + transX[j] + " - " + transY[j] + " - " + transZ[j]);
            }
        }

    public void animateNode()
        {
        System.out.println("animateNode");
        TimelineScenario result = new TimelineScenario.Sequence();
// Add each key frame info as an animation and then play the whole
        for(int j = 0; j < Tkft.length - 1; j++)
            {
            Timeline t = new Timeline(this);
            t.addPropertyToInterpolate("Quat", keyFrames[j], keyFrames[j + 1], new RoundEllie4QuaternionInterpolator());

            Vector3f tempVec = new Vector3f(keyTrans[j].x + animationStartTranslate.x, keyTrans[j].y + animationStartTranslate.y, keyTrans[j].z + animationStartTranslate.z);
            Vector3f tempVecPlus = new Vector3f(keyTrans[j + 1].x + animationStartTranslate.x, keyTrans[j + 1].y + animationStartTranslate.y, keyTrans[j + 1].z + animationStartTranslate.z);
            t.addPropertyToInterpolate("Node1Trans", tempVec, tempVecPlus, new RoundEllie4VectorInterpolator());
            t.addPropertyToInterpolate("Trans", keyTrans[j], keyTrans[j + 1], new RoundEllie4VectorInterpolator());
//        t.setEase(new Spline(0.4f));
            t.setDuration(((long)(Tkft[j + 1] - Tkft[j]) * 1000) * animationTimeMultiplier);
            result.addScenarioActor(t);
            }

//        timeline.addPropertyToInterpolate(Timeline.<Quaternion> property("Quat").on(this.theQuat).from(keyFrames[0]).to(keyFrames[3]));

        result.play();

//            RoundEllie4QuaternionAnimation theQuatProc = new RoundEllie4QuaternionAnimation(renderer.getTheEntity(), theSpatial, keyFrames[0], keyFrames[5], keyTrans[0], keyTrans[5]);
//            Clip clip = Clip.create(2000, 1, theQuatProc);
//            clip.start();

/*
        Timeline tl = new Timeline();
        Clip[] clips = new Clip[Tkft.length];

        for(int j = 0; j < Tkft.length - 1; j++)
            {
            System.out.println("kft - " + Tkft[j]);

                RoundEllie4QuaternionAnimation theQuatProc = new RoundEllie4QuaternionAnimation(renderer.getTheEntity(), theSpatial, keyFrames[j], keyFrames[j + 1], keyTrans[j], keyTrans[j + 1]);
//        Clip clip = Clip.create(5000, Clip.INDEFINITE, theQuatProc);
                clips[j] = Clip.create(4000, 1, theQuatProc);
//        clip.setRepeatBehavior(RepeatBehavior.LOOP);
                tl.schedule(clips[j], 4000 * j);
//                clip.start();
                }
          tl.start();

 */
        }

    public void setQuat(Quaternion newQuat)
        {
        theSpatial.setLocalRotation(newQuat);
        ClientContextJME.getWorldManager().addToUpdateList(theSpatial);
        }
    public void setTrans(Vector3f newTrans)
        {
        theSpatial.setLocalTranslation(newTrans);
        ClientContextJME.getWorldManager().addToUpdateList(theSpatial);
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
    }
