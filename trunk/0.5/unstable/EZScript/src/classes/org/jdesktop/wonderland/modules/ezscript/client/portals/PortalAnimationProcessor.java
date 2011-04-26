/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.portals;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.Portal;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author JagWire
 */

    public class PortalAnimationProcessor extends ProcessorComponent {

        /**
         * The arming conditions for this processor
         */
        private ProcessorArmingCollection collection = null;

        /**
         * States for movement
         */
        private static final int STOPPED = 0;
        private static final int RUNNING = 1;

        /**
         * Our current state
         */
        private int state = STOPPED;

        /**
         * The Portals to modify
         */
        private Portal p = null;

        /**
         * The WorldManager
         */
        private WorldManager worldManager = null;

        /**
         * The set of positions, directions, ups, and times
         */
        private Vector3f position = null;
        private Quaternion rotation = null;
        private float[] times = null;

        /**
         * The set of current variables
         */
        private float penetration = 0.0f;
        private long startTime = 0;
        private long totalTime = 0;
        
        private Object processLock = new Object();

        //this is a dependency!
        private boolean animating = false;
        /**
         * The default constructor
         */
        public PortalAnimationProcessor(WorldManager wm, Entity myEntity, Vector3f positions,
                Quaternion rots, float[] times) {
            worldManager = wm;

            this.position = positions;
            this.rotation = rots;
            this.times = times;
            state = STOPPED;

            collection = new ProcessorArmingCollection(this);
            collection.addCondition(new NewFrameCondition(this));
        }

        public void initialize() {
            setArmingCondition(collection);

        }

        public void compute(ProcessorArmingCollection collection) {
            synchronized (processLock) {
            if (state == RUNNING) {
                totalTime = System.nanoTime() - startTime;
                float flTime = totalTime / 1000000000.0f;

                // Find the right interval
                int startIndex = 0;
                int endIndex = 0;

                for (int i = 0; i < times.length - 1; i++) {
                    if (flTime > times[i] && flTime < times[i + 1]) {
                        startIndex = i;
                        endIndex = i + 1;
                        break;
                    }
                }

                if (startIndex != endIndex) {
                    // Find the percentage
                    float timeSpread = times[endIndex] - times[startIndex];
                    float alpha = flTime - times[startIndex];
                    penetration = alpha / timeSpread;
                } else {
                    animating = false;
                }
            }
            }
        }

        public void start() {
            synchronized (processLock) {
                startTime = System.nanoTime();
                state = RUNNING;
            }
        }

        public void stop() {
            synchronized (processLock) {
                state = STOPPED;
            }
        }

        public void reset(Portal p, Vector3f newPosition, Quaternion newRotation) {
            synchronized (processLock) {
                state = STOPPED;
                this.p = p;
                this.position = newPosition;
                this.rotation = newRotation;
                penetration = 0.0f;
            }
        }

        /**
         * The commit methods
         */
        public void commit(ProcessorArmingCollection collection) {
            if (state == RUNNING && p != null) {
                p.getGeometry().setLocalTranslation(position.x, position.y, position.z);
                p.getGeometry().setLocalRotation(rotation.clone());
                p.getGeometry().setLocalScale(penetration);
                worldManager.addToUpdateList(p.getGeometry());
            }
        }
    }
