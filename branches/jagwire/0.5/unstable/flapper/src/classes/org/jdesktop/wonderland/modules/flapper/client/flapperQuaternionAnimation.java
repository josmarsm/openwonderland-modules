/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.flapper.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.jme.ClientContextJME;

/**
 *
 * @author morris
 */
public class flapperQuaternionAnimation extends AnimationProcessorComponent {
    /**
     * The WorldManager - used for adding to update list
     */
    private WorldManager worldManager = null;
    /**
     * The current transformation
     */
    private Quaternion newQuat = null;
    private Vector3f newTranslation = null;

    private Quaternion startQuat;
    private Quaternion endQuat;
    private Vector3f startTranslation;
    private Vector3f endTranslation;
    /**
     * The transform target
     */
    private Spatial target = null;

    /**
     * The constructor
     */
    public flapperQuaternionAnimation(Entity entity, Spatial target, Quaternion startQuat, Quaternion endQuat, Vector3f startTranslation, Vector3f endTranslation)
        {
        super(entity);
        this.worldManager = ClientContextJME.getWorldManager();
        this.target = target;
        this.startQuat = startQuat;
        this.endQuat = endQuat;
        this.startTranslation = startTranslation;
        this.endTranslation = endTranslation;
        float[] fromAngles = startQuat.toAngles(null);
        float[] toAngles = endQuat.toAngles(null);

        System.out.println("from = " + fromAngles[0] + "-" + fromAngles[1] + "-" + fromAngles[2] + " -- to = " + toAngles[0] + "-" + toAngles[1] + "-" + toAngles[2]);

        }

    /**
     * The initialize method
     */
    public void initialize() {
    }

    /**
     * The Calculate method
     */
    public void compute(ProcessorArmingCollection collection) {
    }

    /**
     * The commit method
     */
    public void commit(ProcessorArmingCollection collection)
        {
        target.setLocalRotation(newQuat);
        target.setLocalTranslation(newTranslation);

        worldManager.addToUpdateList(target);
        }

    public void timingEvent(float fraction, long totalElapsed)
        {
        Vector3f fromAxis = null;
        Vector3f toAxis = null;
        newQuat = new Quaternion();

        float[] fromAngles = startQuat.toAngles(null);
        float[] toAngles = endQuat.toAngles(null);

//        newQuat.fromAngleAxis(fromAngle +((toAngle - fromAngle) * fraction), new Vector3f(0f, 1f, 0f));
        newQuat.fromAngles(fromAngles[0] +(toAngles[0] - fromAngles[0]) * fraction,
                           fromAngles[1] +(toAngles[1] - fromAngles[1]) * fraction,
                           fromAngles[2] +(toAngles[2] - fromAngles[2]) * fraction);

        float[] newAngles = newQuat.toAngles(null);

        System.out.println("new = " + newAngles[0] + "-" + newAngles[1] + "-" + newAngles[2] + " -- fraction = " + fraction);
/*
        newQuat.fromAngleAxis(fromAngle +((toAngle - fromAngle) * fraction),
                new Vector3f(fromAxis.x +((toAxis.x - fromAxis.x) * fraction),
                             fromAxis.y +((toAxis.y - fromAxis.y) * fraction),
                             fromAxis.z +((toAxis.z - fromAxis.z) * fraction)));
*/

//                final Vector3f v3fn;

        float FX = startTranslation.x;
        float FY = startTranslation.y;
        float FZ = startTranslation.z;

        float TX = endTranslation.x;
        float TY = endTranslation.y;
        float TZ = endTranslation.z;

        newTranslation = new Vector3f(FX + (TX - FX) * fraction, FY + (TY - FY) * fraction, FZ + (TZ - FZ) * fraction);
        }

    public void begin() {
        setArmingCondition(new NewFrameCondition(this));
    }

    public void end() {
        setArmingCondition(null);
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++ End animation");
    }

    public void pause() {
        setArmingCondition(null);
    }

    public void resume() {
        setArmingCondition(new NewFrameCondition(this));
    }
}
