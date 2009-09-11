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
package org.jdesktop.wonderland.modules.cmu.player;

import org.jdesktop.wonderland.modules.cmu.player.connections.SceneConnectionHandler;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.alice.apis.moveandturn.Program;
import org.alice.apis.moveandturn.event.MouseButtonListener;
import org.alice.stageide.apis.moveandturn.event.MouseButtonAdapter;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;

/**
 * A standard CMU program which can access its scene graph via a
 * SceneConnectionHandler, which in turn forwards scene graph changes
 * to anyone who cares to listen.  Also handles playback speed changes
 * gracefully, and keeps track of its total running time.
 * @author kevin
 */
public class ProgramPlayer extends Program {

    private static final long DEFAULT_ADVANCE_DURATION = 2000;
    private edu.cmu.cs.dennisc.alice.virtualmachine.VirtualMachine vm;
    private edu.cmu.cs.dennisc.alice.ast.AbstractType sceneType;
    private Object scene;
    private final SceneConnectionHandler sceneConnectionHandler;
    private boolean started = false;        // True once the program has begun to execute.
    private float elapsed = 0;              // The total amount of "time" this program has been executing.
    private float playbackSpeed = 0;        // The current playback speed.
    private long timeOfLastSpeedChange;     // System time at the last speed change (in milliseconds).
    private final Object speedChangeLock = new Object();    // Used to prevent multiple threads from changing the program speed.

    /**
     * Standard constructor.
     * @param sceneFile A .a3p file representing the scene to load.
     */
    public ProgramPlayer(File sceneFile) {
        super();
        this.sceneConnectionHandler = new SceneConnectionHandler();
        this.timeOfLastSpeedChange = System.currentTimeMillis();
        this.setFile(sceneFile);
    }

    /**
     * Get the current playback speed.
     * @return The current playback speed.
     */
    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    /**
     * Get the local elapsed "time", scaled by playback speed.
     * @return Total elapsed program time
     */
    public float getElapsedTime() {
        return elapsed + ((System.currentTimeMillis() - this.timeOfLastSpeedChange) * this.getPlaybackSpeed());
    }

    /**
     * Get the port designated by the SceneConnectionHandler for this program.
     * @return The port used to connect to this program
     */
    public int getPort() {
        return this.sceneConnectionHandler.getPort();
    }

    /**
     * Get the server designated by the SceneConnectionHandler for this program.
     * @return The server used to connect to this program
     */
    public String getHostname() {
        return this.sceneConnectionHandler.getHostname();
    }

    /**
     * Simulate a mouse click on a particular node.  Only left-click is
     * supported.
     * @param id ID for the node receiving the click
     */
    public void click(NodeID id) {
        this.sceneConnectionHandler.click(id);
    }

    /**
     * Load the given CMU file (.a3p extension).
     * @param cmuFile The file to load
     */
    protected void setFile(File cmuFile) {
        edu.cmu.cs.dennisc.alice.Project project = edu.cmu.cs.dennisc.alice.io.FileUtilities.readProject(cmuFile);
        edu.cmu.cs.dennisc.alice.ast.AbstractType programType = project.getProgramType();

        this.vm = new edu.cmu.cs.dennisc.alice.virtualmachine.ReleaseVirtualMachine();
        this.sceneType = programType.getDeclaredFields().get(0).getValueType();
        this.scene = this.vm.createInstanceEntryPoint(this.sceneType);

        Object sceneInstance = ((edu.cmu.cs.dennisc.alice.virtualmachine.InstanceInAlice) this.scene).getInstanceInJava();

        vm.registerAnonymousAdapter(MouseButtonListener.class, MouseButtonAdapter.class);

        this.setScene((org.alice.apis.moveandturn.Scene) sceneInstance);

        this.init();
    }

    /**
     * If this program's elapsed time is shorter than time, "fast-forward"
     * to reach the specified time.  The fast-forward will last for a default
     * amount of time.
     * @param time The time (in milliseconds) we should fast-forward to
     */
    public void advanceToTime(float time) {
        advanceToTime(time, DEFAULT_ADVANCE_DURATION);
    }

    /**
     * If this program's elapsed time is shorter than time, "fast-forward"
     * to reach the specified time.  Note that this method is not thread-correct,
     * in the sense that program speed can still be freely changed during the
     * "fast-forward" period.
     * @param time The time (in milliseconds) we should fast-forward to
     * @param duration The length of time (in milliseconds) which the fast-forward should last
     */
    public void advanceToTime(final float time, final long duration) {
        new Thread(new Runnable() {

            public void run() {
                synchronized (ProgramPlayer.this.speedChangeLock) {
                    try {
                        float timeToAdvance = time - ProgramPlayer.this.getElapsedTime();
                        float prevPlaybackSpeed = ProgramPlayer.this.getPlaybackSpeed();
                        if (timeToAdvance > 0) {
                            float speed = timeToAdvance / (float) duration;
                            ProgramPlayer.this.setPlaybackSpeed(speed);
                            Thread.sleep(duration);
                            ProgramPlayer.this.setPlaybackSpeed(prevPlaybackSpeed);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ProgramPlayer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
    }

    /**
     * Sever ties with the loaded scene, and destroy the player.
     */
    @Override
    public void destroy() {
        this.sceneConnectionHandler.unloadScene();
        super.destroy();
    }

    /**
     * Set a particular playback speed and update the total elapsed time.
     * @param speed The speed at which to play
     */
    public void setPlaybackSpeed(float speed) {
        synchronized (speedChangeLock) {
            long currTime = System.currentTimeMillis();
            this.elapsed += this.playbackSpeed * (currTime - this.timeOfLastSpeedChange);
            this.timeOfLastSpeedChange = currTime;
            this.playbackSpeed = speed;
            this.handleSpeedChange((double) speed);
        }
    }

    /**
     * Change the playback speed.
     * @param speed The speed at which to play
     */
    @Override
    protected void handleSpeedChange(double speed) {
        //TODO: Deal better with startup at non-default playback speed (currently
        //initial speed can only be 1 or 0, since
        //handleSpeedChange doesn't have any effect until after the program thread is running,
        //i.e. a little while after start is called).

        if (!this.isStarted() && speed != 0.0f) {
            this.setStarted();
        }

        synchronized (this.speedChangeLock) {
            super.handleSpeedChange(speed);
        }
    }

    /**
     * Check whether the program has started executing.
     * @return true if the program has started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Start this scene and mark that this has been done; should be
     * used instead of start().
     */
    public void setStarted() {
        started = true;
        this.start();
    }

    /**
     * Start this scene in a VM.
     */
    @Override
    public void run() {
        this.vm.invokeEntryPoint(this.sceneType.getDeclaredMethod("run"), this.scene);
    }

    /**
     * Set the program's scene graph, and parse it as a jME graph.
     * @param sc The CMU scene graph.
     */
    @Override
    public void setScene(org.alice.apis.moveandturn.Scene sc) {
        super.setScene(sc);
        sceneConnectionHandler.setScene(sc);
    }
}
