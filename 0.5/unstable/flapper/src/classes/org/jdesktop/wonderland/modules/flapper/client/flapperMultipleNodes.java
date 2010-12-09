/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.flapper.client;

import com.jme.animation.AnimationController;
import com.jme.animation.BoneAnimation;
import com.jme.animation.BoneTransform;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Spatial;
import java.util.ArrayList;
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
public class flapperMultipleNodes
    {
    private flapperRenderer renderer;
    private Spatial node1theSpatial;
    private Spatial node2theSpatial;
    private Spatial node3theSpatial;
    private Spatial node4theSpatial;
    private Spatial node5theSpatial;
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
    private Quaternion[] node4keyFrames;
    private Vector3f[] node4keyTrans;
    private Quaternion[] node5keyFrames;
    private Vector3f[] node5keyTrans;
    private ArrayList al;
    private AnimationController Tac;
    private BoneAnimation Tani;
    private Controller Tco;
    private Timeline t;
    private TimelineScenario node1result;
    private TimelineScenario node2result;
    private TimelineScenario node3result;
    private TimelineScenario node4result;
    private TimelineScenario node5result;
    private Vector3f animationStartTranslate;
    private float[] animationStartRotation;
    private int animationTimeMultiplier;
    private int animationStartKeyframe = 0;
    private int animationEndKeyframe = 0;
    private Vector3f v3fFinal = new Vector3f(0.0f, 0.0f, 0.0f);
    private Quaternion rotFinal = new Quaternion();
    final boolean traceLoad = false;
    private int animationIceCode = 0;
    private boolean animationSaveTransform = false;
    private boolean animationPlayReverse = false;
    private int nodeCount = 0;
    private String node1Name = null;
    private String node2Name = null;
    private String node3Name = null;
    private String node4Name = null;
    private String node5Name = null;

    public flapperMultipleNodes(flapperRenderer Renderer, String node1Name)
        {
        nodeCount = 1;
        this.node1Name = node1Name;
        this.renderer = Renderer;

        doTheDisassembly();
        }

    public flapperMultipleNodes(flapperRenderer Renderer, String node1Name, String node2Name)
        {
        nodeCount = 2;
        this.node1Name = node1Name;
        this.node2Name = node2Name;
        this.renderer = Renderer;

        doTheDisassembly();
        }

    public flapperMultipleNodes(flapperRenderer Renderer, String node1Name, String node2Name, String node3Name)
        {
        nodeCount = 3;
        this.node1Name = node1Name;
        this.node2Name = node2Name;
        this.node3Name = node3Name;
        this.renderer = Renderer;

        doTheDisassembly();
        }

    public flapperMultipleNodes(flapperRenderer Renderer, String node1Name, String node2Name, String node3Name, String node4Name)
        {
        nodeCount = 4;
        this.node1Name = node1Name;
        this.node2Name = node2Name;
        this.node3Name = node3Name;
        this.node4Name = node4Name;
        this.renderer = Renderer;

        doTheDisassembly();
        }

    public flapperMultipleNodes(flapperRenderer Renderer, String node1Name, String node2Name, String node3Name, String node4Name, String node5Name)
        {
        nodeCount = 5;
        this.node1Name = node1Name;
        this.node2Name = node2Name;
        this.node3Name = node3Name;
        this.node4Name = node4Name;
        this.node5Name = node5Name;
        this.renderer = Renderer;

        doTheDisassembly();
        }

    private void doTheDisassembly()
        {
        rotFinal.fromAngles(0.0f, 0.0f, 0.0f);

        node1theSpatial = renderer.getNode(node1Name);

        System.out.println("node1theSpatial = " + node1theSpatial);
        al = node1theSpatial.getControllers();

        Tco = node1theSpatial.getController(0);

        Tac = (AnimationController)Tco;
        Tani = Tac.getAnimation(0);
        Tkft = Tani.getKeyFrameTimes();
        if(traceLoad)
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
            if(traceLoad)
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
                if(traceLoad)
                    {
                    System.out.println("kft size = " + kft.length);

                    for(int j = 0; j < kft.length; j++)
                        {
                        System.out.println("kft - " + kft[j]);
                        }
                    }
                ArrayList<BoneTransform> bt = ani.getBoneTransforms();
                if(traceLoad)
                    {
                    System.out.println("bt size = " + bt.size());
                    for(int j = 0; j < bt.size(); j++)
                        {
                        System.out.println("bt = " + bt.get(j).toString());
                        }
                    }
                Quaternion qt[] = bt.get(0).getRotations();
                if(traceLoad)
                    System.out.println("bt - rotations = " + qt.length);

                for(int j = 0; j < qt.length; j++)
                    {
                    if(traceLoad)
                        System.out.println("rots = " + qt[j]);
                    float[] fromAngles = qt[j].toAngles(null);
                    if(traceLoad)
                        System.out.println("Angles - " + fromAngles[0] + " - " + fromAngles[1] + " - " + fromAngles[2]);

                    angleX[j] = angleX[j] + fromAngles[0];
                    angleY[j] = angleY[j] + fromAngles[1];
                    angleZ[j] = angleZ[j] + fromAngles[2];
                    }
                if(traceLoad)
                    System.out.println(" start frame = " + ani.getStartFrame() + " - end frame = " + ani.getEndFrame());
                Vector3f v3f[] = bt.get(0).getTranslations();
                if(traceLoad)
                    System.out.println("bt - translations = " + v3f.length);

                for(int j = 0; j < v3f.length; j++)
                    {
                    if(traceLoad)
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
            if(traceLoad)
                System.out.println(" ********************** final angles " + angleX[j] + "--" + angleY[j] + "--" + angleZ[j]);
            node1keyFrames[j] = temp;
            node1keyTrans[j] = new Vector3f(transX[j], transY[j], transZ[j]);
            if(traceLoad)
                System.out.println(" *************** final translations " + transX[j] + " - " + transY[j] + " - " + transZ[j]);
            }

// *************************************************************************************************
// *************************************************************************************************

        if(nodeCount > 1)
            {
            node2theSpatial = renderer.getNode(node2Name);

            System.out.println("theSpatial = " + node2theSpatial);
            al = node2theSpatial.getControllers();

            Tco = node2theSpatial.getController(0);
//        float max = Tco.getMaxTime();
//        float min = Tco.getMinTime();

            Tac = (AnimationController)Tco;
            Tani = Tac.getAnimation(0);
            Tkft = Tani.getKeyFrameTimes();
            if(traceLoad)
                System.out.println("node2Tkft size = " + Tkft.length);

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
                if(traceLoad)
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
                    if(traceLoad)
                        {
                        System.out.println("kft size = " + kft.length);

                        for(int j = 0; j < kft.length; j++)
                            {
                            System.out.println("kft - " + kft[j]);
                            }
                        }
                    ArrayList<BoneTransform> bt = ani.getBoneTransforms();
                    if(traceLoad)
                        {
                        System.out.println("bt size = " + bt.size());
                        for(int j = 0; j < bt.size(); j++)
                            {
                            System.out.println("bt = " + bt.get(j).toString());
                            }
                        }
                    Quaternion qt[] = bt.get(0).getRotations();
                    if(traceLoad)
                        System.out.println("bt - rotations = " + qt.length);

                    for(int j = 0; j < qt.length; j++)
                        {
                        if(traceLoad)
                            System.out.println("rots = " + qt[j]);
                        float[] fromAngles = qt[j].toAngles(null);
                        if(traceLoad)
                            System.out.println("Angles - " + fromAngles[0] + " - " + fromAngles[1] + " - " + fromAngles[2]);

                        angleX[j] = angleX[j] + fromAngles[0];
                        angleY[j] = angleY[j] + fromAngles[1];
                        angleZ[j] = angleZ[j] + fromAngles[2];
                        }
                    if(traceLoad)
                        System.out.println(" start frame = " + ani.getStartFrame() + " - end frame = " + ani.getEndFrame());
                    Vector3f v3f[] = bt.get(0).getTranslations();
                    if(traceLoad)
                        System.out.println("bt - translations = " + v3f.length);

                    for(int j = 0; j < v3f.length; j++)
                        {
                        if(traceLoad)
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
                if(traceLoad)
                    System.out.println(" ********************** final angles " + angleX[j] + "--" + angleY[j] + "--" + angleZ[j]);
                node2keyFrames[j] = temp;
                node2keyTrans[j] = new Vector3f(transX[j], transY[j], transZ[j]);
                if(traceLoad)
                    System.out.println(" *************** final translations " + transX[j] + " - " + transY[j] + " - " + transZ[j]);
                }
            }

// *************************************************************************************************
// *************************************************************************************************

        if(nodeCount > 2)
            {
            node3theSpatial = renderer.getNode(node3Name);

            System.out.println("node3theSpatial = " + node3theSpatial);
            al = node3theSpatial.getControllers();

            Tco = node3theSpatial.getController(0);

            Tac = (AnimationController)Tco;
            Tani = Tac.getAnimation(0);
            Tkft = Tani.getKeyFrameTimes();
            if(traceLoad)
                System.out.println("node3Tkft size = " + Tkft.length);

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
                if(traceLoad)
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
                    if(traceLoad)
                        {
                        System.out.println("kft size = " + kft.length);

                        for(int j = 0; j < kft.length; j++)
                            {
                            System.out.println("kft - " + kft[j]);
                            }
                        }
                    ArrayList<BoneTransform> bt = ani.getBoneTransforms();
                    if(traceLoad)
                        {
                        System.out.println("bt size = " + bt.size());
                        for(int j = 0; j < bt.size(); j++)
                            {
                            System.out.println("bt = " + bt.get(j).toString());
                            }
                        }
                    Quaternion qt[] = bt.get(0).getRotations();
                    if(traceLoad)
                        System.out.println("bt - rotations = " + qt.length);

                    for(int j = 0; j < qt.length; j++)
                        {
                        if(traceLoad)
                            System.out.println("rots = " + qt[j]);
                        float[] fromAngles = qt[j].toAngles(null);
                        if(traceLoad)
                            System.out.println("Angles - " + fromAngles[0] + " - " + fromAngles[1] + " - " + fromAngles[2]);

                        angleX[j] = angleX[j] + fromAngles[0];
                        angleY[j] = angleY[j] + fromAngles[1];
                        angleZ[j] = angleZ[j] + fromAngles[2];
                        }
                    if(traceLoad)
                        System.out.println(" start frame = " + ani.getStartFrame() + " - end frame = " + ani.getEndFrame());
                    Vector3f v3f[] = bt.get(0).getTranslations();
                    if(traceLoad)
                        System.out.println("bt - translations = " + v3f.length);

                    for(int j = 0; j < v3f.length; j++)
                        {
                        if(traceLoad)
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
                if(traceLoad)
                    System.out.println(" ********************** final angles " + angleX[j] + "--" + angleY[j] + "--" + angleZ[j]);
                node3keyFrames[j] = temp;
                node3keyTrans[j] = new Vector3f(transX[j], transY[j], transZ[j]);
                if(traceLoad)
                    System.out.println(" *************** final translations " + transX[j] + " - " + transY[j] + " - " + transZ[j]);
                }
            }

// *************************************************************************************************
// *************************************************************************************************

        if(nodeCount > 3)
            {
            node4theSpatial = renderer.getNode(node4Name);

            System.out.println("node4theSpatial = " + node4theSpatial);
            al = node4theSpatial.getControllers();

            Tco = node4theSpatial.getController(0);

            Tac = (AnimationController)Tco;
            Tani = Tac.getAnimation(0);
            Tkft = Tani.getKeyFrameTimes();
            if(traceLoad)
                System.out.println("node4Tkft size = " + Tkft.length);

// Make storage for the transforms at each keyframe

            transX = new float[Tkft.length];
            transY = new float[Tkft.length];
            transZ = new float[Tkft.length];
            angleX = new float[Tkft.length];
            angleY = new float[Tkft.length];
            angleZ = new float[Tkft.length];
// Make storage for the Quats that will hold the rotates at each keyframe
            node4keyFrames = new Quaternion[Tkft.length];
// and the vectors that will hold the translates
            node4keyTrans = new Vector3f[Tkft.length];

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
                if(traceLoad)
                    System.out.println("Controller = " + al.get(k).toString());

// Get the controller and then cast it to an animation controller
                Controller co = node4theSpatial.getController(k);
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
                    if(traceLoad)
                        {
                        System.out.println("kft size = " + kft.length);

                        for(int j = 0; j < kft.length; j++)
                            {
                            System.out.println("kft - " + kft[j]);
                            }
                        }
                    ArrayList<BoneTransform> bt = ani.getBoneTransforms();
                    if(traceLoad)
                        {
                        System.out.println("bt size = " + bt.size());
                        for(int j = 0; j < bt.size(); j++)
                            {
                            System.out.println("bt = " + bt.get(j).toString());
                            }
                        }
                    Quaternion qt[] = bt.get(0).getRotations();
                    if(traceLoad)
                        System.out.println("bt - rotations = " + qt.length);

                    for(int j = 0; j < qt.length; j++)
                        {
                        if(traceLoad)
                            System.out.println("rots = " + qt[j]);
                        float[] fromAngles = qt[j].toAngles(null);
                        if(traceLoad)
                            System.out.println("Angles - " + fromAngles[0] + " - " + fromAngles[1] + " - " + fromAngles[2]);

                        angleX[j] = angleX[j] + fromAngles[0];
                        angleY[j] = angleY[j] + fromAngles[1];
                        angleZ[j] = angleZ[j] + fromAngles[2];
                        }
                    if(traceLoad)
                        System.out.println(" start frame = " + ani.getStartFrame() + " - end frame = " + ani.getEndFrame());
                    Vector3f v3f[] = bt.get(0).getTranslations();
                    if(traceLoad)
                        System.out.println("bt - translations = " + v3f.length);

                    for(int j = 0; j < v3f.length; j++)
                        {
                        if(traceLoad)
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
                if(traceLoad)
                    System.out.println(" ********************** final angles " + angleX[j] + "--" + angleY[j] + "--" + angleZ[j]);
                node4keyFrames[j] = temp;
                node4keyTrans[j] = new Vector3f(transX[j], transY[j], transZ[j]);
                if(traceLoad)
                    System.out.println(" *************** final translations " + transX[j] + " - " + transY[j] + " - " + transZ[j]);
                }
            }
// *************************************************************************************************
// *************************************************************************************************

        if(nodeCount > 4)
            {
            node5theSpatial = renderer.getNode(node5Name);

            System.out.println("node5theSpatial = " + node5theSpatial);
            al = node5theSpatial.getControllers();

            Tco = node5theSpatial.getController(0);

            Tac = (AnimationController)Tco;
            Tani = Tac.getAnimation(0);
            Tkft = Tani.getKeyFrameTimes();
            if(traceLoad)
                System.out.println("node5Tkft size = " + Tkft.length);

// Make storage for the transforms at each keyframe

            transX = new float[Tkft.length];
            transY = new float[Tkft.length];
            transZ = new float[Tkft.length];
            angleX = new float[Tkft.length];
            angleY = new float[Tkft.length];
            angleZ = new float[Tkft.length];
// Make storage for the Quats that will hold the rotates at each keyframe
            node5keyFrames = new Quaternion[Tkft.length];
// and the vectors that will hold the translates
            node5keyTrans = new Vector3f[Tkft.length];

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
                if(traceLoad)
                    System.out.println("Controller = " + al.get(k).toString());

// Get the controller and then cast it to an animation controller
                Controller co = node5theSpatial.getController(k);
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
                    if(traceLoad)
                        {
                        System.out.println("kft size = " + kft.length);

                        for(int j = 0; j < kft.length; j++)
                            {
                            System.out.println("kft - " + kft[j]);
                            }
                        }
                    ArrayList<BoneTransform> bt = ani.getBoneTransforms();
                    if(traceLoad)
                        {
                        System.out.println("bt size = " + bt.size());
                        for(int j = 0; j < bt.size(); j++)
                            {
                            System.out.println("bt = " + bt.get(j).toString());
                            }
                        }
                    Quaternion qt[] = bt.get(0).getRotations();
                    if(traceLoad)
                        System.out.println("bt - rotations = " + qt.length);

                    for(int j = 0; j < qt.length; j++)
                        {
                        if(traceLoad)
                            System.out.println("rots = " + qt[j]);
                        float[] fromAngles = qt[j].toAngles(null);
                        if(traceLoad)
                            System.out.println("Angles - " + fromAngles[0] + " - " + fromAngles[1] + " - " + fromAngles[2]);

                        angleX[j] = angleX[j] + fromAngles[0];
                        angleY[j] = angleY[j] + fromAngles[1];
                        angleZ[j] = angleZ[j] + fromAngles[2];
                        }
                    if(traceLoad)
                        System.out.println(" start frame = " + ani.getStartFrame() + " - end frame = " + ani.getEndFrame());
                    Vector3f v3f[] = bt.get(0).getTranslations();
                    if(traceLoad)
                        System.out.println("bt - translations = " + v3f.length);

                    for(int j = 0; j < v3f.length; j++)
                        {
                        if(traceLoad)
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
                if(traceLoad)
                    System.out.println(" ********************** final angles " + angleX[j] + "--" + angleY[j] + "--" + angleZ[j]);
                node5keyFrames[j] = temp;
                node5keyTrans[j] = new Vector3f(transX[j], transY[j], transZ[j]);
                if(traceLoad)
                    System.out.println(" *************** final translations " + transX[j] + " - " + transY[j] + " - " + transZ[j]);
                }
            }

        }

    public void animateNode()
        {
        Vector3f v3f;

        int startFrame = 0;
        int endFrame = 0;
        float zRotationInc = 0.0f;
        float xRotationInc = 0.0f;

        System.out.println("animateNode");

        v3f = node1theSpatial.getLocalTranslation();
        System.out.println("Before animation - " + v3f + " - After last run = " + v3fFinal);

        node1result = new TimelineScenario.Sequence();
// Add each key frame info as an animation and then play the whole
        if(animationStartKeyframe == 0 && animationEndKeyframe == 0)
            {
            startFrame = 0;
            endFrame = Tkft.length - 1;
            }
        else
            {
            startFrame = animationStartKeyframe;
            endFrame = animationEndKeyframe - 1;
            }

        for(int j = startFrame; j < endFrame; j++)
            {
            t = new Timeline(this);
// *******************************************  Node 1
            float[] fromAngles = node1keyFrames[j].toAngles(null);
            System.out.println("Ahead of suspected problem j = " + j);
            float[] fromAnglesNext = node1keyFrames[j + 1].toAngles(null);

            float[] rotFinalAngles = rotFinal.toAngles(null);

            Quaternion quat = new Quaternion();
            quat.fromAngles(
//                    fromAngles[0] + rotFinalAngles[0] + animationStartRotation[0],
//                    fromAngles[1] + rotFinalAngles[1] + animationStartRotation[1],
//                    fromAngles[2] + rotFinalAngles[2] + animationStartRotation[2]);
                    fromAngles[0] + animationStartRotation[0],
                    fromAngles[1] + rotFinalAngles[1] + animationStartRotation[1],
                    fromAngles[2] + animationStartRotation[2]);
            Quaternion quatNext = new Quaternion();
            quatNext.fromAngles(
//                    fromAnglesNext[0] + rotFinalAngles[0] + animationStartRotation[0],
//                    fromAnglesNext[1] + rotFinalAngles[1] + animationStartRotation[1],
//                    fromAnglesNext[2] + rotFinalAngles[2] + animationStartRotation[2]);
                    fromAnglesNext[0] + animationStartRotation[0],
                    fromAnglesNext[1] + rotFinalAngles[1] + animationStartRotation[1],
                    fromAnglesNext[2] + animationStartRotation[2]);
//            System.out.println("rots - x = " +
//                    (fromAngles[0] + rotFinalAngles[0] + animationStartRotation[0]) + " - y = " +
//                    (fromAngles[1] + rotFinalAngles[1] + animationStartRotation[1]) + " - z = " +
//                    (fromAngles[2] + rotFinalAngles[2] + animationStartRotation[2]));

            t.addPropertyToInterpolate("Node1Quat", quat, quatNext, new flapperQuaternionInterpolator());

            ////            t.addPropertyToInterpolate("Node1Quat", node1keyFrames[j], node1keyFrames[j + 1], new flapperQuaternionInterpolator());

            zRotationInc = (float)Math.sin(animationStartRotation[1]);
            xRotationInc = (float)Math.cos(animationStartRotation[1]);

            if(v3fFinal.x == 0.0f && v3fFinal.y == 0.0 && v3fFinal.z == 0.0)
                {
                Vector3f tempVec = new Vector3f(
                    (node1keyTrans[j].x + animationStartTranslate.x),
                    (node1keyTrans[j].y + animationStartTranslate.y),
                    (node1keyTrans[j].z + animationStartTranslate.z));
                Vector3f tempVecPlus = new Vector3f(
                    (node1keyTrans[j + 1].x + animationStartTranslate.x),
                    (node1keyTrans[j + 1].y + animationStartTranslate.y),
                    (node1keyTrans[j + 1].z + animationStartTranslate.z));
//                System.out.println("x = " +
//                        (node1keyTrans[j].x + animationStartTranslate.x) + " - y = " +
//                        (node1keyTrans[j].y + animationStartTranslate.y) + " - z = " +
//                        (node1keyTrans[j].z + animationStartTranslate.z));
                t.addPropertyToInterpolate("Node1Trans", tempVec, tempVecPlus, new flapperVectorInterpolator());
                }
            else
                {
                Vector3f tempVec = new Vector3f(
                    node1keyTrans[j].x + v3fFinal.x + (node1keyTrans[j].x * (float)Math.sin(rotFinalAngles[1])),
                    node1keyTrans[j].y + v3fFinal.y,
                    node1keyTrans[j].z + v3fFinal.z + (node1keyTrans[j].z * (float)Math.cos(rotFinalAngles[1])));
                Vector3f tempVecPlus = new Vector3f(
                    node1keyTrans[j + 1].x + v3fFinal.x + (node1keyTrans[j + 1].x * (float)Math.sin(rotFinalAngles[1])),
                    node1keyTrans[j + 1].y + v3fFinal.y,
                    node1keyTrans[j + 1].z + v3fFinal.z + (node1keyTrans[j + 1].z * (float)Math.cos(rotFinalAngles[1])));
//                System.out.println("final - x = " +
//                        (node1keyTrans[j].x + v3fFinal.x + (node1keyTrans[j].x * (float)Math.sin(rotFinalAngles[1]))) + " - y = " +
//                        (node1keyTrans[j].y + v3fFinal.y) + " - z = " +
//                        (node1keyTrans[j].z + v3fFinal.z + (node1keyTrans[j].z * (float)Math.cos(rotFinalAngles[1]))));
                t.addPropertyToInterpolate("Node1Trans", tempVec, tempVecPlus, new flapperVectorInterpolator());
                }
            if(j == endFrame - 1)
                {
                t.addCallback(new TimelineCallback()
                    {

                    public void onTimelineDone()
                        {
                Vector3f v3f = node1theSpatial.getLocalTranslation();
                System.out.println("After animation from timeline done - " + v3f);
                        }

                    public void onTimelineStateChanged(TimelineState oldState, TimelineState newState, float arg2, float arg3)
                        {
                        if(newState == TimelineState.DONE)
                            {
                            if(animationSaveTransform)
                                {
                                v3fFinal = node1theSpatial.getLocalTranslation();
                                rotFinal = node1theSpatial.getLocalRotation();
                                }
                            System.out.println("After animation - " + v3fFinal);
                            ClientContext.getInputManager().postEvent(new IntercellEvent("F", animationIceCode));
                            }
                        }

                    public void onTimelinePulse(float arg0, float arg1)
                        {

                        }
                    });
                }

////            t.addPropertyToInterpolate("Node1Trans", node1keyTrans[j], node1keyTrans[j + 1], new flapperVectorInterpolator());
//        t.setEase(new Spline(0.4f));
            t.setDuration((long)((Tkft[j + 1] - Tkft[j]) * 1000) * animationTimeMultiplier);
            node1result.addScenarioActor(t);
            }

        if(nodeCount > 1)
            {
            node2result = new TimelineScenario.Sequence();
// ***************************************** Node 2
// Add each key frame info as an animation and then play the whole
            for(int j = startFrame; j < endFrame; j++)
                {
                t = new Timeline(this);
                t.addPropertyToInterpolate("Node2Quat", node2keyFrames[j], node2keyFrames[j + 1], new flapperQuaternionInterpolator());

///            Vector3f tempVec = new Vector3f(node2keyTrans[j].x + animationStartTranslate.x, node2keyTrans[j].y + animationStartTranslate.y, node2keyTrans[j].z + animationStartTranslate.z);
///            Vector3f tempVecPlus = new Vector3f(node2keyTrans[j + 1].x + animationStartTranslate.x, node2keyTrans[j + 1].y + animationStartTranslate.y, node2keyTrans[j + 1].z + animationStartTranslate.z);
///            t.addPropertyToInterpolate("Node2Trans", tempVec, tempVecPlus, new flapperVectorInterpolator());
///System.out.println("node2 - orig y = " + node2keyTrans[j].y + " - shift - " + tempVec.y);
                t.addPropertyToInterpolate("Node2Trans", node2keyTrans[j], node2keyTrans[j + 1], new flapperVectorInterpolator());
//        t.setEase(new Spline(0.4f));
                t.setDuration((long)((Tkft[j + 1] - Tkft[j]) * 1000) * animationTimeMultiplier);
                node2result.addScenarioActor(t);
                }
            }

        if(nodeCount > 2)
            {
            node3result = new TimelineScenario.Sequence();
// *******************************************************  Node 3
// Add each key frame info as an animation and then play the whole
            for(int j = startFrame; j < endFrame; j++)
                {
                t = new Timeline(this);
                t.addPropertyToInterpolate("Node3Quat", node3keyFrames[j], node3keyFrames[j + 1], new flapperQuaternionInterpolator());

///            Vector3f tempVec = new Vector3f(node3keyTrans[j].x + animationStartTranslate.x, node3keyTrans[j].y + animationStartTranslate.y, node3keyTrans[j].z + animationStartTranslate.z);
///            Vector3f tempVecPlus = new Vector3f(node3keyTrans[j + 1].x + animationStartTranslate.x, node3keyTrans[j + 1].y + animationStartTranslate.y, node3keyTrans[j + 1].z + animationStartTranslate.z);
///            t.addPropertyToInterpolate("Node3Trans", tempVec, tempVecPlus, new flapperVectorInterpolator());
///System.out.println("node3 - orig y = " + node3keyTrans[j].y + " - shift - " + tempVec.y);
                t.addPropertyToInterpolate("Node3Trans", node3keyTrans[j], node3keyTrans[j + 1], new flapperVectorInterpolator());
//        t.setEase(new Spline(0.4f));
                t.setDuration((long)((Tkft[j + 1] - Tkft[j]) * 1000) * animationTimeMultiplier);
                node3result.addScenarioActor(t);
                }
            }

        if(nodeCount > 3)
            {
            node4result = new TimelineScenario.Sequence();
// *******************************************************  Node 3
// Add each key frame info as an animation and then play the whole
            for(int j = startFrame; j < endFrame; j++)
                {
                t = new Timeline(this);
                t.addPropertyToInterpolate("Node4Quat", node4keyFrames[j], node4keyFrames[j + 1], new flapperQuaternionInterpolator());
                t.addPropertyToInterpolate("Node4Trans", node4keyTrans[j], node4keyTrans[j + 1], new flapperVectorInterpolator());
                t.setDuration((long)((Tkft[j + 1] - Tkft[j]) * 1000) * animationTimeMultiplier);
                node4result.addScenarioActor(t);
                }
            }

        if(nodeCount > 4)
            {
            node5result = new TimelineScenario.Sequence();
            for(int j = startFrame; j < endFrame; j++)
                {
                t = new Timeline(this);
                t.addPropertyToInterpolate("Node5Quat", node5keyFrames[j], node5keyFrames[j + 1], new flapperQuaternionInterpolator());
                t.addPropertyToInterpolate("Node5Trans", node5keyTrans[j], node5keyTrans[j + 1], new flapperVectorInterpolator());
                t.setDuration((long)((Tkft[j + 1] - Tkft[j]) * 1000) * animationTimeMultiplier);
                node5result.addScenarioActor(t);
                }
            }

        node1result.play();
        if(nodeCount > 1)
            node2result.play();
        if(nodeCount > 2)
            node3result.play();
        if(nodeCount > 3)
            node4result.play();
        if(nodeCount > 4)
            node5result.play();
        }

    public void setTrans(Spatial theSpatial, Vector3f newTrans)
        {
        theSpatial.setLocalTranslation(newTrans);
        ClientContextJME.getWorldManager().addToUpdateList(theSpatial);
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

    public void setNode4Quat(Quaternion newQuat)
        {
        node4theSpatial.setLocalRotation(newQuat);
        ClientContextJME.getWorldManager().addToUpdateList(node4theSpatial);
        }
    public void setNode4Trans(Vector3f newTrans)
        {
        node4theSpatial.setLocalTranslation(newTrans);
        ClientContextJME.getWorldManager().addToUpdateList(node4theSpatial);
        }

    public void setNode5Quat(Quaternion newQuat)
        {
        node5theSpatial.setLocalRotation(newQuat);
        ClientContextJME.getWorldManager().addToUpdateList(node5theSpatial);
        }
    public void setNode5Trans(Vector3f newTrans)
        {
        node5theSpatial.setLocalTranslation(newTrans);
        ClientContextJME.getWorldManager().addToUpdateList(node5theSpatial);
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
