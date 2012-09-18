
package org.jdesktop.wonderland.modules.clock2.client;

import com.jme.animation.AnimationController;
import com.jme.animation.BoneAnimation;
import com.jme.animation.BoneTransform;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Spatial;
//import com.sun.scenario.animation.Animation;
//import com.sun.scenario.animation.Clip;
//import com.sun.scenario.animation.Clip.RepeatBehavior;
//import com.sun.scenario.animation.Timeline;
import java.util.ArrayList;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.clock2.common.clock2CellClientState;
import org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingActionClass;
import org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingComponent;
import org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingRunnable;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.TimelineScenario;

public class clock2Cell extends Cell
    {
//    private Set<Animation> animations = new HashSet<Animation>();

    private clock2Renderer renderer;
    public String modelURI = null;
    @UsesCellComponent
    private ScriptingComponent scriptingComponent;
    private Spatial theSpatial = null;
    private int aniSpeedMultiplier = 2;
    private Quaternion minuteStart = null;
    private Quaternion minuteEnd = null;
    private Quaternion hourStart = null;
    private Quaternion hourEnd = null;
    private Quaternion hourIncrement = new Quaternion();
    private Quaternion minuteIncrement = new Quaternion();
    private Spatial hourSpatial = null;
    private Spatial minuteSpatial = null;
    private Quaternion hourFinal = new Quaternion();
    private Quaternion minuteFinal = new Quaternion();
    private clock2SingleNode msn = null;
    private clock2ThreeNodes m3n = null;

    public clock2Cell(CellID cellID, CellCache cellCache)
        {
        super(cellID, cellCache);
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClientState(CellClientState state)
        {
        super.setClientState(state);
        this.modelURI = ((clock2CellClientState)state).getModelURI();
        }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType)
        {
        if (rendererType == RendererType.RENDERER_JME)
            {
            this.renderer = new clock2Renderer(this);
            return this.renderer;
            }
        else
            {
            return super.createCellRenderer(rendererType);
            }
        }

    @Override
    protected void setStatus(CellStatus status, boolean increasing)
        {
        super.setStatus(status, increasing);
        System.out.println("clock2 - status = " + status + " - increasing = " + increasing);
        
        if (increasing && status == CellStatus.ACTIVE)
            {
            //posterCellLogger.severe("active and increasing");

            ScriptingActionClass sac = new ScriptingActionClass();
            sac.setName("clock2");
            sac.insertCmdMap("testit", testitRun);
            sac.insertCmdMap("testAnimate", testAnimateRun);
            sac.insertCmdMap("animateNode", animateNodeRun);
            sac.insertCmdMap("getHourValues", getHourValuesRun);
            sac.insertCmdMap("getMinuteValues", getMinuteValuesRun);
            sac.insertCmdMap("setTime", setTimeRun);
            sac.insertCmdMap("animate3Nodes", animate3NodesRun);

            scriptingComponent.putActionObject(sac);
            }

        if (status == CellStatus.RENDERING && increasing == true)
            {
            // Initialize the render with the current poster text
            //posterCellLogger.severe("rendering and increasing");

            }
        if (!increasing && status == CellStatus.DISK)
            {
            // Remove the listener for changes to the shared map
            }
        }

    public void testit(float x, float y, float z)
        {
        System.out.println("testit x = " + x + " y = " + y + " z = " + z);
        }

    ScriptingRunnable testitRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            testit(x, y, z);
            System.out.println("ScriptingActionClass - enter testit");
            }
        };

    public void getHourValues(String hourNodeName)
        {
        System.out.println("getHourValues");
        hourSpatial = renderer.getNode(hourNodeName);

        System.out.println("hourSpatial = " + hourSpatial);
        ArrayList al = hourSpatial.getControllers();

        Controller Tco = hourSpatial.getController(0);

        AnimationController Tac = (AnimationController)Tco;
        BoneAnimation Tani = Tac.getAnimation(0);
        float Tkft[] = Tani.getKeyFrameTimes();

// Make storage for the transforms at each keyframe

        float[] transX = new float[Tkft.length];
        float[] transY = new float[Tkft.length];
        float[] transZ = new float[Tkft.length];
        float[] angleX = new float[Tkft.length];
        float[] angleY = new float[Tkft.length];
        float[] angleZ = new float[Tkft.length];
// Make storage for the Quats that will hold the rotates at each keyframe
        Quaternion[] keyFrames = new Quaternion[Tkft.length];
// and the vectors that will hold the translates
        Vector3f[] keyTrans = new Vector3f[Tkft.length];

// Step through the animation controllers to pick up the changes at each key frame
        for(int k = 0; k < al.size(); k++)
            {
            System.out.println("Controller = " + al.get(k).toString());

// Get the controller and then cast it to an animation controller
            Controller co = hourSpatial.getController(k);
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
            System.out.println(" ********************** final angles " + angleX[j] + " - " + angleY[j] + " - " + angleZ[j]);
            keyFrames[j] = temp;
            keyTrans[j] = new Vector3f(transX[j], transY[j], transZ[j]);
            }
        float[] endAngles = keyFrames[1].toAngles(null);
        float[] startAngles = keyFrames[0].toAngles(null);
        hourIncrement.fromAngles(endAngles[0] - startAngles[0],
                                 endAngles[1] - startAngles[1],
                                 endAngles[2] - startAngles[2]);
        hourStart = keyFrames[0];
        hourEnd = keyFrames[1];
        System.out.println("Hour values = start -> " + hourStart + " : end -> " + hourEnd + " : increment -> " + hourIncrement);
        }

    ScriptingRunnable getHourValuesRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            getHourValues(avatar);
            }
        };

    public void getMinuteValues(String minuteNodeName)
        {
        System.out.println("getMinuteValues");
        minuteSpatial = renderer.getNode(minuteNodeName);

        System.out.println("minuteSpatial = " + minuteSpatial);
        ArrayList al = minuteSpatial.getControllers();

        Controller Tco = minuteSpatial.getController(0);

        AnimationController Tac = (AnimationController)Tco;
        BoneAnimation Tani = Tac.getAnimation(0);
        float Tkft[] = Tani.getKeyFrameTimes();
        System.out.println("Tkft size = " + Tkft.length);

// Make storage for the transforms at each keyframe

        float[] transX = new float[Tkft.length];
        float[] transY = new float[Tkft.length];
        float[] transZ = new float[Tkft.length];
        float[] angleX = new float[Tkft.length];
        float[] angleY = new float[Tkft.length];
        float[] angleZ = new float[Tkft.length];
// Make storage for the Quats that will hold the rotates at each keyframe
        Quaternion[] keyFrames = new Quaternion[Tkft.length];
// and the vectors that will hold the translates
        Vector3f[] keyTrans = new Vector3f[Tkft.length];

// Step through the animation controllers to pick up the changes at each key frame
        for(int k = 0; k < al.size(); k++)
            {
            System.out.println("Controller = " + al.get(k).toString());

// Get the controller and then cast it to an animation controller
            Controller co = minuteSpatial.getController(k);
            AnimationController ac = (AnimationController)co;
// Get the animations for this controller
            ArrayList bans = ac.getAnimations();
            System.out.println("bans size = " + bans.size());

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
            System.out.println(" ********************** final angles " + angleX[j] + " - " + angleY[j] + " - " + angleZ[j]);
            keyFrames[j] = temp;
            keyTrans[j] = new Vector3f(transX[j], transY[j], transZ[j]);
            System.out.println(" *************** final translations " + transX[j] + " - " + transY[j] + " - " + transZ[j]);
            }
        float[] endAngles = keyFrames[1].toAngles(null);
        float[] startAngles = keyFrames[0].toAngles(null);
        minuteIncrement.fromAngles((endAngles[0] - startAngles[0]) / 15,
                                   (endAngles[1] - startAngles[1]) / 15,
                                   (endAngles[2] - startAngles[2]) / 15);
        System.out.println("IN minutes - end angles - 0 -> " + endAngles[0] + ":" + endAngles[1] + ":" + endAngles[2]);
        System.out.println("IN minutes - start angles - 0 -> " + startAngles[0] + ":" + startAngles[1] + ":" + startAngles[2]);

        minuteStart = keyFrames[0];
        minuteEnd = keyFrames[1];
        System.out.println("Minute values = start -> " + minuteStart + " : end -> " + minuteEnd + ": increment -> " + minuteIncrement);
        }

    ScriptingRunnable getMinuteValuesRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            getMinuteValues(avatar);
            }
        };

    public void setTime(float hour, float minute, float second)
        {
        System.out.println("Enter setTime - " + hour + ":" + minute + ":" + second);
        float[] hourStartAngles = hourStart.toAngles(null);
        float[] hourIncrementAngles = hourIncrement.toAngles(null);

        hourFinal.fromAngles(hourStartAngles[0] + hourIncrementAngles[0] * hour + hourIncrementAngles[0] * minute / 60,
                             hourStartAngles[1] + hourIncrementAngles[1] * hour + hourIncrementAngles[1] * minute / 60,
                             hourStartAngles[2] + hourIncrementAngles[2] * hour + hourIncrementAngles[2] * minute / 60);
        hourSpatial.setLocalRotation(hourFinal);
        ClientContextJME.getWorldManager().addToUpdateList(hourSpatial);
        float[] minuteStartAngles = minuteStart.toAngles(null);
        float[] minuteIncrementAngles = minuteIncrement.toAngles(null);

        minuteFinal.fromAngles(minuteStartAngles[0] + minuteIncrementAngles[0] * minute,
                             minuteStartAngles[1] + minuteIncrementAngles[1] * minute,
                             minuteStartAngles[2] + minuteIncrementAngles[2] * minute);
        minuteSpatial.setLocalRotation(minuteFinal);
        ClientContextJME.getWorldManager().addToUpdateList(minuteSpatial);
        }

    ScriptingRunnable setTimeRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            setTime(x, y, z);
            }
        };

    public void testAnimate(String theName)
        {
        System.out.println("testAnimate");
        Spatial theSpatial = renderer.getNode(theName);
        System.out.println("theSpatial = " + theSpatial);
        clock2RotationAnimation theRotProc = new clock2RotationAnimation(renderer.getTheEntity(), theSpatial, 0f, 360, new Vector3f(0f,1f,0f));
//        Clip clip = Clip.create(3000, Clip.INDEFINITE, theRotProc);
//        clip.setRepeatBehavior(RepeatBehavior.LOOP);
//        clip.start();
        }

    ScriptingRunnable testAnimateRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            testAnimate(avatar);
            System.out.println("ScriptingActionClass - enter testAnimate");
            }
        };
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

    ScriptingRunnable animateNodeRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            if(msn == null)
                {
                msn = new clock2SingleNode(renderer, avatar, 2);
                }
            msn.animateNode();
            System.out.println("ScriptingActionClass - enter animateNode");
            }
        };

    ScriptingRunnable animate3NodesRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            if(m3n == null)
                {
                m3n = new clock2ThreeNodes(renderer, string1, string2, string3, 2);
                }
            m3n.animateNode();
            System.out.println("ScriptingActionClass - enter animate3Nodes");
            }
        };
    }
