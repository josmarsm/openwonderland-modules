/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.headless;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar;

/**
 *
 * @author ryan
 */
    class UserSimulator extends Thread {

        private Vector3f currentLocation = new Vector3f();
        private Vector3f[] desiredLocations;
        private int locationIndex;
        private Vector3f step = null;
        private float speed;
        private Quaternion orientation = null;
        private LocalAvatar avatar;
        private boolean quit = false;
        private boolean walking = false;
        private long sleepTime = 500; // Time between steps (in ms)
        private int currentLoopCount = 0;
        private int desiredLoopCount;
        private Semaphore semaphore;

        public UserSimulator(LocalAvatar avatar) {
            super("UserSimulator");
            this.avatar = avatar;
            semaphore = new Semaphore(0);
        }

        public synchronized boolean isQuit() {
            return quit;
        }

        public synchronized void quit() {
            this.quit = true;
        }

        @Override
        public void run() {
            // Set initial position
            avatar.localMoveRequest(currentLocation, orientation);

            while (!quit) {
                try {
                    semaphore.acquire();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Client3DSim.class.getName()).log(Level.SEVERE, null, ex);
                }

                while (!quit && walking) {
                    if (currentLocation.subtract(desiredLocations[locationIndex]).lengthSquared() < 0.1) {   // Need epsilonEquals
                        if (locationIndex < desiredLocations.length - 1) {
                            locationIndex++;

                            step = desiredLocations[locationIndex].subtract(currentLocation);
                            step.multLocal(speed / (1000f / sleepTime));

                        } else if (locationIndex == desiredLocations.length - 1 && desiredLoopCount != currentLoopCount) {
                            currentLoopCount++;
                            locationIndex = 0;

                            step = desiredLocations[locationIndex].subtract(currentLocation);
                            step.multLocal(speed / (1000f / sleepTime));
                        } else {
                            walking = false;
                        }
                    }

                    if (walking) {
                        currentLocation.addLocal(step);
                        avatar.localMoveRequest(currentLocation, orientation);
//                        messageTimer.messageSent(new CellTransform(orientation, currentLocation));
                    }

                    try {
                        sleep(sleepTime);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Client3DSim.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        /**
         * Walk  from the current location to the new location specified, and
         * orient to the give look direction
         * @param location
         * @param lookDirection
         * @param speed in meters/second
         */
        void walkLoop(Vector3f[] locations, Vector3f lookDirection, float speed, int loopCount) {
            this.speed = speed;
            locationIndex = 0;
            desiredLocations = locations;
            desiredLoopCount = loopCount;
            currentLoopCount = 0;

            step = new Vector3f(desiredLocations[0]);
            step.subtractLocal(currentLocation);
            step.multLocal(speed / (1000f / sleepTime));

            walking = true;
            semaphore.release();
        }

        /**
         * Send audio data to the server
         *
         * TODO implement
         */
        public void talk() {
        }
    }

