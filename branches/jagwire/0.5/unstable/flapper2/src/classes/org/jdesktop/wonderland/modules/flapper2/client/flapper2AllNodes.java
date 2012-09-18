/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.flapper2.client;

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
public class flapper2AllNodes {
    private flapper2Renderer renderer;
    private float Tkft[];
    private float[] transX;
    private float[] transY;
    private float[] transZ;
    private float[] angleX;
    private float[] angleY;
    private float[] angleZ;
    private ArrayList al;
    private AnimationController Tac;
    private BoneAnimation Tani;
    private Controller Tco;
    private Timeline t;
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

    private ArrayList NodeList;

    private int NodeFrame = 0;
    private int NodeLast = 0;

    public flapper2AllNodes(flapper2Renderer Renderer)
        {
        this.renderer = Renderer;
        aNode theNode = new aNode();

        doTheDisassembly(theNode);
        }

    private void doTheDisassembly(aNode theNode)
        {
        rotFinal.fromAngles(0.0f, 0.0f, 0.0f);

        Spatial spat = theNode.nodeSpatial;

        System.out.println("node1theSpatial = " + spat);
        al = spat.getControllers();

        Tco = spat.getController(0);

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
        theNode.nodeKeyFrames = new Quaternion[Tkft.length];
// and the vectors that will hold the translates
        theNode.nodeKeyTrans = new Vector3f[Tkft.length];

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
            Controller co = spat.getController(k);
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
            theNode.nodeKeyFrames[j] = temp;
            theNode.nodeKeyTrans[j] = new Vector3f(transX[j], transY[j], transZ[j]);
            if(traceLoad)
                System.out.println(" *************** final translations " + transX[j] + " - " + transY[j] + " - " + transZ[j]);
            }

        }

    class aNode
        {
        public Spatial nodeSpatial;
        Quaternion[] nodeKeyFrames;
        Vector3f[] nodeKeyTrans;
        TimelineScenario nodeResult;
        String nodeName = null;
        }

    public void setTrans(Spatial theSpatial, Vector3f newTrans)
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

    public void setAnimationSaveTransform(boolean save)
        {
        animationSaveTransform = save;
        }

    public void setAnimationPlayReverse(boolean reverse)
        {
        animationPlayReverse = reverse;
        }

}
