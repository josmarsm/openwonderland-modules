/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.navigateto.client;

import com.jme.math.Vector3f;
import imi.character.avatar.AvatarContext;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.behavior.Task;
import imi.character.behavior.Walk;
import imi.objects.SpatialObject;
import imi.objects.TargetObject;
import imi.character.statemachine.GameContext;
import imi.scene.PSphere;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarCollisionChangeRequestEvent;

/**
 * Turn towards the goal and go to it, if a goal direction is available will
 * turn to it on arrival. Can be set to avoid obstacles on the way.
 *
 * @author Abhishek Upadhyay
 */
public class NewGoTo implements Task {

    private String description = "Go to an object";
    private String status = "Chilling";
    private GameContext context = null;
    private SpatialObject goal = null; // optional
    private Vector3f goalPosition = new Vector3f(Vector3f.ZERO);
    private Vector3f goalDirection = null; // optional
    private boolean bAvoidObstacles = false;
    private float approvedDistanceFromGoal = 1.0f;
    private float directionSensitivity = 0.1f;
    private float currentDistanceFromGoal = 0.0f;
    private Vector3f currentCharacterPosition = new Vector3f();
    private boolean bDone = false;
    private boolean bDoneFacingGoal = false;
    private int precisionCounter = 0;
    private float sampleCounter = 0.0f;
    private float sampleTimeFrame = 0.75f;
    private int samples = 1;
    private Vector3f sampleAvgPos = new Vector3f();
    private Vector3f samplePrevAvgPos = new Vector3f();
    private int sampleStreak = 0;
    private int samplePrevStreak = 0;

    public NewGoTo(Vector3f goalPosition, GameContext context) {
        this.context = context;
        this.goalPosition.set(goalPosition);
        //this.goalPosition.set(goalPosition.x, 0.0f, goalPosition.z);
    }

    public NewGoTo(Vector3f goalPosition, Vector3f directionAtGoal, GameContext context) {
        this(goalPosition, context);
        goalDirection = directionAtGoal;
    }

    public NewGoTo(SpatialObject goal, GameContext context) {
        this.context = context;
        reset(goal);
    }

    public boolean verify() {
        if (bDone) {
            return false;
        }
        return true;
    }

    public void update(float deltaTime) {
        // Update local variables
        Vector3f v3f = context.getController().getPosition();
        currentCharacterPosition.set(v3f.x, goalPosition.y, v3f.z);
//        currentCharacterPosition.set(context.getController().getPosition());
        currentDistanceFromGoal = goalPosition.distance(currentCharacterPosition);

        // Check if we are at the goal
        if (currentDistanceFromGoal <= approvedDistanceFromGoal) {
            triggerRelease(TriggerNames.Move_Forward.ordinal());
            if (goalDirection != null) {
                // Turn to face the proper direction
                bDone = turnToDir(goalDirection);
            } else {
                ClientContext.getInputManager().postEvent(new AvatarCollisionChangeRequestEvent(true, true));
                bDone = true;
            }
            return;
        }

        // First thing is to face the goal
        if (!bDoneFacingGoal) {
            bDoneFacingGoal = turnToPos(goalPosition);
            return;
        }

        // Detect looping
        if (sampleProgress(deltaTime)) {
            return;
        }

        // Walk forward
        triggerPress(TriggerNames.Move_Forward.ordinal());
        triggerRelease(TriggerNames.Move_Back.ordinal());
        if (!(bAvoidObstacles && avoidObstacles())) {
            // Turn while walking
            Vector3f desiredVelocity = goalPosition.subtract(currentCharacterPosition).normalize();
            if (turnToDir(desiredVelocity)) {
                triggerRelease(TriggerNames.Move_Left.ordinal());
                triggerRelease(TriggerNames.Move_Right.ordinal());

                // Once in a while perform on a higher percision
                precisionCounter++;
                if (precisionCounter > 60) {
                    precisionCounter = 0;
                    Vector3f rightVec = context.getController().getRightVector();
                    float dot = desiredVelocity.dot(rightVec);
                    if (dot > Float.MIN_VALUE) {
                        triggerPress(TriggerNames.Move_Right.ordinal());
                        triggerRelease(TriggerNames.Move_Left.ordinal());
                    } else if (dot < -Float.MIN_VALUE) {
                        triggerPress(TriggerNames.Move_Left.ordinal());
                        triggerRelease(TriggerNames.Move_Right.ordinal());
                    }
                }
            }
        }
    }

    // Currently only target object obstacles
    private boolean avoidObstacles() {
        // Is there an imminent obstacle?
        boolean bNeedToAvoid = false;
        imi.character.Character avatar = context.getCharacter();
        SpatialObject obj = null;
        if (avatar.getObjectCollection() != null) {
            obj = avatar.getObjectCollection().findNearestObjectOfType(TargetObject.class, avatar, 2.5f, 0.4f, false); // distance should be scaled by velocity... but at the moment the velocity is pretty constant...
        }
        if (obj != null && obj != goal && currentDistanceFromGoal > 2.0f) {
            bNeedToAvoid = true;
            PSphere obstacleBV = obj.getNearestObstacleSphere(currentCharacterPosition);
            PSphere characterBV = context.getCharacter().getBoundingSphere();
            characterBV.setRadius(characterBV.getRadius() * 0.025f);
//            context.getCharacter().getModelInst().setDebugSphere(obstacleBV, 0);
//            context.getCharacter().getModelInst().setDebugSphere(characterBV, 1);
            if (characterBV.isColliding(obstacleBV)) {
                // Initiate walk back if colliding
                status = "collided with obstacle";
                Task walk = (Task) new Walk("Walking away from an obstacle", 1.0f, false, (AvatarContext) context);
                context.getBehaviorManager().addTaskToTop(walk);
                context.resetTriggersAndActions();

                Vector3f directionToObstacle = obstacleBV.getCenterRef().subtract(currentCharacterPosition).normalize();
                turnToDir(directionToObstacle);
            } else {
                // Turn away to prevent collision
                Vector3f directionToObstacle = obstacleBV.getCenterRef().subtract(currentCharacterPosition).normalize();
                Vector3f desiredVelocity = goalPosition.subtract(currentCharacterPosition).normalize();
                Vector3f direction = directionToObstacle.negate().add(desiredVelocity.mult(2.0f)).mult(0.3333333333f);
                turnToDir(direction);
            }
        }

        return bNeedToAvoid;
    }

    private boolean turnToPos(Vector3f position) {
        Vector3f direction = position.subtract(currentCharacterPosition).normalize();
        return turnToDir(direction);
    }

    private boolean turnToDir(Vector3f direction) {
        status = "turning";
        Vector3f rightVec = context.getController().getRightVector();
        float dot = direction.dot(rightVec);
        if (dot > directionSensitivity) {
            context.triggerPressed(TriggerNames.Move_Right.ordinal());
            context.triggerReleased(TriggerNames.Move_Left.ordinal());
        } else if (dot < -directionSensitivity) {
            context.triggerPressed(TriggerNames.Move_Left.ordinal());
            context.triggerReleased(TriggerNames.Move_Right.ordinal());
        } else if (isBehind(direction)) {
            if (dot > 0) {
                triggerPress(TriggerNames.Move_Right.ordinal());
                triggerRelease(TriggerNames.Move_Left.ordinal());
            } else {
                triggerRelease(TriggerNames.Move_Right.ordinal());
                triggerPress(TriggerNames.Move_Left.ordinal());
            }
        } else {
            return true;
        }
        return false;
    }

    private boolean isBehind(Vector3f direction) {
        // Check if this direction is outside the front half space
        Vector3f fwdVec = context.getController().getForwardVector().mult(-1.0f); // forward is reversed!
        float frontHalfDot = direction.dot(fwdVec);
        return frontHalfDot < 0.0f;
    }

    private void triggerPress(int trigger) {
        if (!context.getTriggerState().isKeyPressed(trigger)) {
            context.triggerPressed(trigger);
        }
    }

    private void triggerRelease(int trigger) {
        if (context.getTriggerState().isKeyPressed(trigger)) {
            context.triggerReleased(trigger);
        }
    }

    // TODO 
    private boolean sampleProgress(float deltaTime) {
        boolean result = false;
        sampleCounter += deltaTime;
        samples++;
        sampleAvgPos.addLocal(currentCharacterPosition);
        if (sampleCounter > sampleTimeFrame) {
            // Sample "tick"
            sampleAvgPos.divideLocal(samples);
            float currentAvgDistance = sampleAvgPos.distanceSquared(goalPosition);
            float previousAvgDistance = samplePrevAvgPos.distanceSquared(goalPosition);

            // which is closer to the goal? the current sample average position or the previous one?
            if (currentAvgDistance > previousAvgDistance) {
                sampleStreak++;
                if (sampleStreak > 3) {
                    samplePrevStreak = sampleStreak;
                    sampleStreak = 0;
                    // we are not closer to the goal after sampleTimeFrame secounds... let's try to get out of this loop
                    //Task walk = (Task) new Walk("Walking away from loop", 0.5f, true, avatarContext);
                    //avatarContext.getSteering().addTaskToTop(walk);
                    context.getController().stop();
                    bDoneFacingGoal = false;

                    //System.out.println("sample tick: stop getting away from the target");
                    status = "loop detected";
                    result = true;
                }
            } else {
                if (samplePrevStreak > 0 && sampleStreak > 0) {
                    //System.out.println("fishy sample tick: stop the loop");    
                    context.getController().stop();
                    bDoneFacingGoal = false;
                }
//                else
//                    System.out.println("sample tick: prev streak " + samplePrevStreak + " current streak " + sampleStreak);

                samplePrevStreak = sampleStreak;
                sampleStreak = 0;
            }

            samplePrevAvgPos.set(sampleAvgPos);
            sampleAvgPos.set(currentCharacterPosition);
            samples = 1;
            sampleCounter = 0.0f;
        }
        return result;
    }

    public void resetSamples() {
        //System.out.println("samples reset");
        Vector3f characterPosition = context.getController().getPosition();

        // Samples
        sampleAvgPos.set(characterPosition);
        samplePrevAvgPos.set(characterPosition);
        samples = 1;
        sampleCounter = 0.0f;
    }

    public void reset(Vector3f goTo) {
        goalPosition.set(goTo);
        goalDirection = null;
        bDone = false;
        //bDoneFacingGoal = false; not desired behavior
        approvedDistanceFromGoal = 1.0f;
    }

    public void reset(Vector3f goTo, Vector3f directionAtGoal) {
        reset(goTo);
        goalDirection = directionAtGoal;
    }

    public void reset(SpatialObject goal) {
        reset(goal.getPositionRef(), goal.getForwardVector());
        this.goal = goal;
        if (goal.getBoundingSphere() != null) {
            approvedDistanceFromGoal = goal.getBoundingSphere().getRadius();
        }
    }

    public float getApprovedDistanceFromGoal() {
        return approvedDistanceFromGoal;
    }

    public void setApprovedDistanceFromGoal(float approvedDistanceFromGoal) {
        this.approvedDistanceFromGoal = approvedDistanceFromGoal;
    }

    public boolean isAvoidObstacles() {
        return bAvoidObstacles;
    }

    public void setAvoidObstacles(boolean bAvoidObstacles) {
        this.bAvoidObstacles = bAvoidObstacles;
    }

    public void onHold() {
        status = "on hold";
    }

    /**
     * May return null
     */
    public SpatialObject getGoal() {
        return goal;
    }

    public void setGoal(SpatialObject obj) {
        goal = obj;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }
}
