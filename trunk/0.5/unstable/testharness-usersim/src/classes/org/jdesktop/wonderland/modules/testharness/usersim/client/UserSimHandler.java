/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.testharness.usersim.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.ViewManager.ViewManagerListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.testharness.common.UserSimRequest;

/**
 * Receives UserSimRequest messages from the test harness (via WebstartClientWrapper)
 * and simulates a user using the requested actions.
 *
 * @author paulby
 */
public class UserSimHandler implements ClientPlugin {

    private TestRequestHandler requestHandler = null;
    private UserSimulator userSim = null;
    private final Object userSimLock = new Object();
    private LinkedList<UserSimRequest> pendingRequests = new LinkedList();

    public void initialize(ServerSessionManager loginInfo) {
        if (System.getProperty("testharness.enabled")==null) {
            return;
        }

        // The 'headless' api in mtgame is a hack to support this simulator, so
        // it's not exposed in the public api. Use introspection to get to it.
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        Method setHeadless;
        try {
            setHeadless = rm.getClass().getDeclaredMethod("setWlTestHarness", Boolean.TYPE);
            setHeadless.setAccessible(true);
            setHeadless.invoke(rm, Boolean.TRUE);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(UserSimHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(UserSimHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(UserSimHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(UserSimHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(UserSimHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        ClientContextJME.getViewManager().addViewManagerListener(new ViewManagerListener() {

            public void primaryViewCellChanged(ViewCell oldViewCell, ViewCell newViewCell) {
                synchronized(userSimLock) {
                    if (userSim!=null)
                        userSim.quit();

                    if (newViewCell==null)
                        return;

                    userSim = new UserSimulator(((CellClientSession)newViewCell.getCellCache().getSession()).getLocalAvatar());
                    userSim.start();

                    for(UserSimRequest r : pendingRequests)
                        processRequest(r);
                    pendingRequests.clear();
                }
            }
        });

        requestHandler = new TestRequestHandler();
        requestHandler.start();
    }

    public void cleanup() {
        if (requestHandler!=null)
            requestHandler.quit();
        if (userSim!=null)
            userSim.quit();
    }

    private void processRequest(UserSimRequest request) {
        synchronized(userSimLock) {
            if (userSim==null) {
                System.err.println("Queueing WALK");
                pendingRequests.add(request);
            } else {
                switch(request.getAction()) {
                    case WALK :
                        if (request.getDesiredLocations()==null)
                            userSim.stopWalking();
                        else
                            userSim.walkLoop(request.getDesiredLocations(), new Vector3f(1f, 0f, 0f), request.getSpeed(), request.getLoopCount());
                        break;
                }
            }
        }
    }

    class TestRequestHandler extends Thread {

        private Socket socket;
        private boolean quit = false;

        public TestRequestHandler() {
            setDaemon(true);
            int port = Integer.valueOf(System.getProperty("testharness.port"));
            try {
                socket = new Socket("localhost", port);
            } catch (IOException ex) {
                Logger.getLogger(UserSimHandler.class.getName()).log(Level.SEVERE, "ServerSocket creation failed", ex);
            }
        }

        @Override
        public void run() {
            System.err.println("------> TestRequestHandler running");
            try {
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                while(!quit) {
                    UserSimRequest request;
                    try {
                        System.err.println("---> WAITING FOR REQUEST");
                        request = (UserSimRequest) in.readObject();
                        System.err.println("----> GOT REQUEST "+request);
                        processRequest(request);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(UserSimHandler.class.getName()).log(Level.SEVERE, "Bad TestRequest Class ", ex);
                    }
                }

                out.close();
                in.close();
                socket.close();

            } catch (IOException ex) {
                System.err.println("Quiting...");
                Logger.getLogger(UserSimHandler.class.getName()).log(Level.SEVERE, null, ex);
                userSim.quit();
                ClientContextJME.getWorldManager().shutdown();
                // Force quit
                System.exit(0);
            }

        }

        void quit() {
            quit = true;
        }
    }

    /**
     * A very basic UserSimulator, this really needs a lot of attention.....
     */
    class UserSimulator extends Thread {

        private Vector3f currentLocation = new Vector3f();
        private Vector3f[] desiredLocations;
        private int locationIndex;
        private Vector3f step = null;
        private float speed;
        private Quaternion orientation = new Quaternion();
        private LocalAvatar avatar;
        private boolean quit = false;
        private boolean walking = false;
        private long sleepTime = 50; // Time between steps (in ms)
        private int currentLoopCount = 0;
        private int desiredLoopCount;
        private Semaphore semaphore;
        private AvatarImiJME rend;

        public UserSimulator(LocalAvatar avatar) {
            super("UserSimulator");
            this.avatar = avatar;
            semaphore = new Semaphore(0);
            rend = (AvatarImiJME) avatar.getViewCell().getCellRenderer(RendererType.RENDERER_JME);
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
            rend.triggerGoto(currentLocation, orientation);

            while (!quit) {
                try {
                    semaphore.acquire();
                } catch (InterruptedException ex) {
                    Logger.getLogger(UserSimulator.class.getName()).log(Level.SEVERE, null, ex);
                }

                while (!quit && walking) {
                    if (currentLocation.subtract(desiredLocations[locationIndex]).lengthSquared() < 0.5) {   // Need epsilonEquals
                        if (locationIndex < desiredLocations.length - 1) {
                            locationIndex++;
                            
                            setStep();
                        } else if (locationIndex == desiredLocations.length - 1 && desiredLoopCount != currentLoopCount) {
                            currentLoopCount++;
                            locationIndex = 0;

                            setStep();
                        } else {
                            walking = false;
                        }

                        
                    }

                    if (walking) {
                        //System.err.println("Walk " + currentLocation);
                        currentLocation.addLocal(step);
                        rend.triggerGoto(currentLocation, orientation);
//                        messageTimer.messageSent(new CellTransform(orientation, currentLocation));
                    }

                    try {
                        sleep(sleepTime);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(UserSimulator.class.getName()).log(Level.SEVERE, null, ex);
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
                        System.err.println("STARTING WALK");
            this.speed = speed;
            locationIndex = 0;
            desiredLocations = locations;
            desiredLoopCount = loopCount;
            currentLoopCount = 0;

            setStep();

            walking = true;
            semaphore.release();
        }

        void setStep() {
            float dist = desiredLocations[locationIndex].distance(currentLocation);
            float steps = (dist / speed) * (1000f / sleepTime);

            step = desiredLocations[locationIndex].subtract(currentLocation);
            step.multLocal(1f / steps);

            System.err.println("Set step: " + step + " target (" +
                               locationIndex + "): " +
                               desiredLocations[locationIndex] + " dist: " + dist +
                               " steps: " + steps + " time " +
                               (steps / (1000f / sleepTime) + " seconds."));
        }

        void stopWalking() {
            walking = false;
        }

        /**
         * Send audio data to the server
         *
         * TODO implement
         */
        public void talk() {
        }
    }
}
