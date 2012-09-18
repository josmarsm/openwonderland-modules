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
import java.util.Iterator;
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
public class RoundEllie4AllNodes {
    private RoundEllie4Renderer renderer;
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
    final boolean traceLoad = false;
    private int animationIceCode = 0;
    private boolean animationSaveTransform = false;
    private boolean animationPlayReverse = false;
    private int nodeCount = 0;

    private ArrayList nodeList;

    private int NodeFrame = 0;
    private int NodeLast = 0;
    private boolean takeAvatar = false;

    public RoundEllie4AllNodes(RoundEllie4Renderer Renderer, ArrayList nodeList)
        {
        this.renderer = Renderer;
        this.nodeList = nodeList;
        }

    public void allNodesDisassembly()
        {
        Iterator<RoundEllie4ANode> itr = nodeList.iterator();
        while (itr.hasNext())
            {
            RoundEllie4ANode element = itr.next();
            System.out.println("Element = " + element.nodeName);
            doTheDisassembly(element);
            }
        }

    private void doTheDisassembly(RoundEllie4ANode theNode)
        {
        theNode.setAnimationStartRotation(animationStartRotation);
        theNode.setAnimationStartTranslate(animationStartTranslate);
        theNode.setAnimationTimeMultiplier(animationTimeMultiplier);

        if(traceLoad)
            System.out.println("######## - Node name = :" + theNode.nodeName + ":");
        theNode.rotFinal.fromAngles(0.0f, 0.0f, 0.0f);

        theNode.nodeSpatial = renderer.getNode(theNode.nodeName);

        if(traceLoad)
            System.out.println("theSpatial = " + theNode.nodeSpatial);
        al = theNode.nodeSpatial.getControllers();

        Tco = theNode.nodeSpatial.getController(0);

        Tac = (AnimationController)Tco;
        Tani = Tac.getAnimation(0);
        theNode.putTkft(Tani.getKeyFrameTimes());
        int tkftSize = theNode.getTkft().length;

        if(animationStartKeyframe == 0 && animationEndKeyframe == 0)
            {
            theNode.putStartFrame(0);
            theNode.putEndFrame(tkftSize - 1);
            }
        else
            {
            theNode.putStartFrame(animationStartKeyframe);
            theNode.putEndFrame(animationEndKeyframe - 1);
            }

        if(traceLoad)
            System.out.println("Node " + theNode.nodeName + " - Tkft size = " + tkftSize);

// Make storage for the transforms at each keyframe

        transX = new float[tkftSize];
        transY = new float[tkftSize];
        transZ = new float[tkftSize];
        angleX = new float[tkftSize];
        angleY = new float[tkftSize];
        angleZ = new float[tkftSize];
// Make storage for the Quats that will hold the rotates at each keyframe
        theNode.nodeKeyFrames = new Quaternion[tkftSize];
// and the vectors that will hold the translates
        theNode.nodeKeyTrans = new Vector3f[tkftSize];

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
            Controller co = theNode.nodeSpatial.getController(k);
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
        for(int j = 0; j < tkftSize; j++)
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

    public void setTakeAvatar(boolean take)
        {
        takeAvatar = take;
        }

    public void initializeNodes()
        {
        System.out.println("animateNodes");

        Iterator<RoundEllie4ANode> itr = nodeList.iterator();

        while(itr.hasNext())
            {
            RoundEllie4ANode element = itr.next();
            if(element.nodeName.equals("main"))
                {
                element.initializePrimaryNode();
                element.setTakeAvatar(takeAvatar);
                }
            else
                {
                element.initializeChildNode();
                }
            }
        }

    public void animateNodes()
        {
        Iterator<RoundEllie4ANode> itr = nodeList.iterator();

        while(itr.hasNext())
            {
            RoundEllie4ANode element = itr.next();
            element.play();
            }
        }
}
