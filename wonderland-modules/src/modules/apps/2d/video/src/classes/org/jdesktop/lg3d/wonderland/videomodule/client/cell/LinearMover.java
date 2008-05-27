/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.lg3d.wonderland.videomodule.client.cell;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import javax.vecmath.Point3f;

public class LinearMover {
    private static final Logger logger =
            Logger.getLogger(LinearMover.class.getName());
    
    /** update rate */
    public static int UPDATE_RATE = 1;
    /** minimum animation distance */
    private static float MIN_DISTANCE = 0.05f;
    /** maximum speed */
    private static float MAX_SPEED = 0.008f;
    
    /** current position */
    private Point3f pos;
    /** goal position */
    private Point3f goal;
    /** the mover, containing the move(Point3f) method */
    private MoveListener listener;
    /** a Timer to use for updates */
    private Timer timer;
    /** a TimerTask */
    private MoveTask moveTask;

    /**
     * Creates a new instance of LinearMover
     * @param startPos the initial position
     * @param timer the motion timer
     * @param listener the MoveListener
     */
    public LinearMover(Point3f startPos, Timer timer,
            MoveListener listener) {
        this.pos = this.goal = startPos;
        this.listener = listener;
        this.timer = timer;

        listener.move(startPos);
    }

    /**
     * Cancels the timer task.
     */
    private synchronized void cancelTask() {
        if (moveTask != null) {
            moveTask.cancel();
        }
        moveTask = null;
        listener.moveComplete();
    }

    /**
     * Gets the current position
     */
    public synchronized Point3f getCurrentPosition() {
        return pos;
    }

    /**
     * Move smoothly to the specified position
     */
    public synchronized void moveTo(Point3f goal) {
        logger.fine("moving from: " + pos + " to: " + goal);
        if (this.goal == goal) {
            return;
        }
        this.goal = goal;
        if (moveTask == null) {
            moveTask = new MoveTask();
            timer.schedule(moveTask, 0, UPDATE_RATE);
        }
    }

    class MoveTask extends TimerTask {

        public void run() {
            // calculate distance to from current position to goal
            logger.finest("current: " + pos);
            logger.finest("goal: " + goal);
            float sx = pos.getX();
            float sy = pos.getY();
            float dx = goal.getX();
            float dy = goal.getY();

            float distance = (float) Math.sqrt(((dx - sx) * (dx - sx)) + ((dy - sy) * (dy - sy)));

            if (distance < MIN_DISTANCE) {
                logger.finest("distance is: " + distance + " so ending move");
                listener.move(goal);
                cancelTask();
            } else {
                float speed = MAX_SPEED;
                float time = distance / speed;      // time to reach destination
                float steps = time / UPDATE_RATE;   // number of animation steps to get there
                float xPerStep = (dx - sx) / steps; // how far to move in x per step
                float yPerStep = (dy - sy) / steps; // how far to move in y per step
                
                // calculate next position
                pos.set(pos.getX() + xPerStep, pos.getY() + yPerStep, pos.getZ());

                logger.finest("distance: " + distance);
                logger.finest("speed: " + speed);
                logger.finest("time: " + time);
                logger.finest("steps: " + steps);
                logger.finest("x per step: " + xPerStep);
                logger.finest("y per step: " + yPerStep);
                logger.finest("next position: " + pos);

                listener.move(pos);
            }
        }
    }
}
