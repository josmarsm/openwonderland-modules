/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.flapper2.client;

/**
 *
 * @author morris
 */

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.jme.ClientContextJME;

/**
 * Rotation Processor for use with Scenario Timing framework
 *
 * @author paulby
 * @author Bernard Horan
 */
public class flapper2RotationAnimation extends AnimationProcessorComponent {
    /**
     * The WorldManager - used for adding to update list
     */
    private WorldManager worldManager = null;
    /**
     * The current degrees of rotation
     */
    private float radians = 0.0f;

    private float startRadians;
    private float endRadians;

    private Vector3f axis;

    /**
     * The rotation matrix to apply to the target
     */
    private Quaternion quaternion = new Quaternion();

    /**
     * The rotation target
     */
    private Spatial target = null;

    /**
     * The constructor
     */
    public flapper2RotationAnimation(Entity entity, Spatial target, float startDegrees, float endDegrees) {
        this(entity, target, startDegrees, endDegrees, new Vector3f(0f,1f,0f));
    }
    public flapper2RotationAnimation(Entity entity, Spatial target, float startDegrees, float endDegrees, Vector3f axis) {
        super(entity);
        this.worldManager = ClientContextJME.getWorldManager();
        this.target = target;
        this.startRadians = (float) Math.toRadians(startDegrees);
        this.endRadians = (float) Math.toRadians(endDegrees);
        this.axis = axis;
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
    public void commit(ProcessorArmingCollection collection) {
//        quaternion.fromAngles(0.0f, radians, 0.0f);
        quaternion.fromAngleAxis(radians, axis);
        target.setLocalRotation(quaternion);
        worldManager.addToUpdateList(target);

    }


    public void timingEvent(float fraction, long totalElapsed) {
        radians = startRadians+(endRadians-startRadians)*fraction;
    }

    public void begin() {
        setArmingCondition(new NewFrameCondition(this));
    }

    public void end() {
        setArmingCondition(null);
    }

    public void pause() {
        setArmingCondition(null);
    }

    public void resume() {
        setArmingCondition(new NewFrameCondition(this));
    }


}
