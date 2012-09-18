/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.clock2.client;

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
public class clock2ThreeNodes
    {
    private clock2Renderer renderer;
    private Spatial node1theSpatial;
    private Spatial node2theSpatial;
    private Spatial node3theSpatial;
    private float Tkft[];
    private float[] transX;
    private float[] transY;
    private float[] transZ;
    private float[] angleX;
    private float[] angleY;
    private float[] angleZ;
    private Quaternion[] node1keyFrames;
    private Vector3f[] node1keyTrans;
    private Quaternion[] node2keyFrames;
    private Vector3f[] node2keyTrans;
    private Quaternion[] node3keyFrames;
    private Vector3f[] node3keyTrans;
    private int aniSpeedMultiplier;
    private ArrayList al;
    private AnimationController Tac;
    private BoneAnimation Tani;
    private Controller Tco;
    private Timeline t;
    private TimelineScenario node1result;
    private TimelineScenario node2result;
    private TimelineScenario node3result;

    public clock2ThreeNodes(clock2Renderer Renderer, String node1Name, String node2Name, String node3Name, int speedMultiplier)
        {
        renderer = Renderer;
        aniSpeedMultiplier = speedMultiplier;

        node1theSpatial = renderer.getNode(node1Name);

        System.out.println("node1theSpatial = " + node1theSpatial);
        al = node1theSpatial.getControllers();

        Tco = node1theSpatial.getController(0);
//        float max = Tco.getMaxTime();
//        float min = Tco.getMinTime();

        Tac = (AnimationController)Tco;
        Tani = Tac.getAnimation(0);
        Tkft = Tani.getKeyFrameTimes();
        System.out.println("node1Tkft size = " + Tkft.length);

// Make storage for the transforms at each keyframe

        transX = new float[Tkft.length];
        transY = new float[Tkft.length];
        transZ = new float[Tkft.length];
        angleX = new float[Tkft.length];
        angleY = new float[Tkft.length];
        angleZ = new float[Tkft.length];
// Make storage for the Quats that will hold the rotates at each keyframe
        node1keyFrames = new Quaternion[Tkft.length];
// and the vectors that will hold the translates
        node1keyTrans = new Vector3f[Tkft.length];

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
            Controller co = node1theSpatial.getController(k);
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
            node1keyFrames[j] = temp;
            node1keyTrans[j] = new Vector3f(transX[j], transY[j], transZ[j]);
            System.out.println(" *************** final translations " + transX[j] + " - " + transY[j] + " - " + transZ[j]);
            }

// *************************************************************************************************
// *************************************************************************************************

        node2theSpatial = renderer.getNode(node2Name);

        System.out.println("theSpatial = " + node2theSpatial);
        al = node2theSpatial.getControllers();

        Tco = node2theSpatial.getController(0);
//        float max = Tco.getMaxTime();
//        float min = Tco.getMinTime();

        Tac = (AnimationController)Tco;
        Tani = Tac.getAnimation(0);
        Tkft = Tani.getKeyFrameTimes();
        System.out.println("node1Tkft size = " + Tkft.length);

// Make storage for the transforms at each keyframe

        transX = new float[Tkft.length];
        transY = new float[Tkft.length];
        transZ = new float[Tkft.length];
        angleX = new float[Tkft.length];
        angleY = new float[Tkft.length];
        angleZ = new float[Tkft.length];
// Make storage for the Quats that will hold the rotates at each keyframe
        node2keyFrames = new Quaternion[Tkft.length];
// and the vectors that will hold the translates
        node2keyTrans = new Vector3f[Tkft.length];

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
            Controller co = node2theSpatial.getController(k);
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
            node2keyFrames[j] = temp;
            node2keyTrans[j] = new Vector3f(transX[j], transY[j], transZ[j]);
            System.out.println(" *************** final translations " + transX[j] + " - " + transY[j] + " - " + transZ[j]);
            }

// *************************************************************************************************
// *************************************************************************************************

        node3theSpatial = renderer.getNode(node3Name);

        System.out.println("node3theSpatial = " + node3theSpatial);
        al = node3theSpatial.getControllers();

        Tco = node3theSpatial.getController(0);
//        float max = Tco.getMaxTime();
//        float min = Tco.getMinTime();

        Tac = (AnimationController)Tco;
        Tani = Tac.getAnimation(0);
        Tkft = Tani.getKeyFrameTimes();
        System.out.println("node1Tkft size = " + Tkft.length);

// Make storage for the transforms at each keyframe

        transX = new float[Tkft.length];
        transY = new float[Tkft.length];
        transZ = new float[Tkft.length];
        angleX = new float[Tkft.length];
        angleY = new float[Tkft.length];
        angleZ = new float[Tkft.length];
// Make storage for the Quats that will hold the rotates at each keyframe
        node3keyFrames = new Quaternion[Tkft.length];
// and the vectors that will hold the translates
        node3keyTrans = new Vector3f[Tkft.length];

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
            Controller co = node3theSpatial.getController(k);
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
            node3keyFrames[j] = temp;
            node3keyTrans[j] = new Vector3f(transX[j], transY[j], transZ[j]);
            System.out.println(" *************** final translations " + transX[j] + " - " + transY[j] + " - " + transZ[j]);
            }
        }

    public void animateNode()
        {
        System.out.println("animateNode");

        node1result = new TimelineScenario.Sequence();
// Add each key frame info as an animation and then play the whole
        for(int j = 0; j < Tkft.length - 1; j++)
            {
            t = new Timeline(this);
            t.addPropertyToInterpolate("Node1Quat", node1keyFrames[j], node1keyFrames[j + 1], new clock2QuaternionInterpolator());
            t.addPropertyToInterpolate("Node1Trans", node1keyTrans[j], node1keyTrans[j + 1], new clock2VectorInterpolator());
//        t.setEase(new Spline(0.4f));
            t.setDuration((long)(Tkft[j + 1] - Tkft[j]) * 1000 * aniSpeedMultiplier);
            node1result.addScenarioActor(t);
            }

        node2result = new TimelineScenario.Sequence();
// Add each key frame info as an animation and then play the whole
        for(int j = 0; j < Tkft.length - 1; j++)
            {
            t = new Timeline(this);
            t.addPropertyToInterpolate("Node2Quat", node2keyFrames[j], node2keyFrames[j + 1], new clock2QuaternionInterpolator());
            t.addPropertyToInterpolate("Node2Trans", node2keyTrans[j], node2keyTrans[j + 1], new clock2VectorInterpolator());
//        t.setEase(new Spline(0.4f));
            t.setDuration((long)(Tkft[j + 1] - Tkft[j]) * 1000 * aniSpeedMultiplier);
            node2result.addScenarioActor(t);
            }

        node3result = new TimelineScenario.Sequence();
// Add each key frame info as an animation and then play the whole
        for(int j = 0; j < Tkft.length - 1; j++)
            {
            t = new Timeline(this);
            t.addPropertyToInterpolate("Node3Quat", node3keyFrames[j], node3keyFrames[j + 1], new clock2QuaternionInterpolator());
            t.addPropertyToInterpolate("Node3Trans", node3keyTrans[j], node3keyTrans[j + 1], new clock2VectorInterpolator());
//        t.setEase(new Spline(0.4f));
            t.setDuration((long)(Tkft[j + 1] - Tkft[j]) * 1000 * aniSpeedMultiplier);
            node3result.addScenarioActor(t);
            }

        node1result.play();
        node2result.play();
        node3result.play();
        }

    public void setNode1Quat(Quaternion newQuat)
        {
        node1theSpatial.setLocalRotation(newQuat);
        ClientContextJME.getWorldManager().addToUpdateList(node1theSpatial);
        }
    public void setNode1Trans(Vector3f newTrans)
        {
        node1theSpatial.setLocalTranslation(newTrans);
        ClientContextJME.getWorldManager().addToUpdateList(node1theSpatial);
        }

    public void setNode2Quat(Quaternion newQuat)
        {
        node2theSpatial.setLocalRotation(newQuat);
        ClientContextJME.getWorldManager().addToUpdateList(node2theSpatial);
        }
    public void setNode2Trans(Vector3f newTrans)
        {
        node2theSpatial.setLocalTranslation(newTrans);
        ClientContextJME.getWorldManager().addToUpdateList(node2theSpatial);
        }

    public void setNode3Quat(Quaternion newQuat)
        {
        node3theSpatial.setLocalRotation(newQuat);
        ClientContextJME.getWorldManager().addToUpdateList(node3theSpatial);
        }
    public void setNode3Trans(Vector3f newTrans)
        {
        node3theSpatial.setLocalTranslation(newTrans);
        ClientContextJME.getWorldManager().addToUpdateList(node3theSpatial);
        }

    }
