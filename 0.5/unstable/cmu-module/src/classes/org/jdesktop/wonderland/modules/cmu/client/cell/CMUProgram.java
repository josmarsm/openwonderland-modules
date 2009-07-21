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
package org.jdesktop.wonderland.modules.cmu.client.cell;

import java.io.File;
import org.jdesktop.wonderland.modules.cmu.client.cell.jme.cellrenderer.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.alice.apis.moveandturn.Program;
import org.jdesktop.wonderland.modules.cmu.common.cell.PlaybackDefaults;

/**
 * A standard CMU program which provides access to its scene graph
 * as a collection of JME Nodes.
 *
 * @author kevin
 */
public class CMUProgram extends Program {

    private static final long DEFAULT_ADVANCE_DURATION = 10000;
    private edu.cmu.cs.dennisc.alice.virtualmachine.VirtualMachine vm;
    private edu.cmu.cs.dennisc.alice.ast.AbstractType sceneType;
    private Object scene;
    private CMUScene cmuScene;
    private boolean started = false;        // True once the program has begun to execute.
    private float elapsed = 0;              // The total amount of "time" this program has been executing.
    private float playbackSpeed = 0;        // The current playback speed.
    private long timeOfLastSpeedChange;     // System time at the last speed change (in milliseconds).
    private final Object speedChangeLock = new Object();    // Used to prevent multiple threads from changing the program speed.

    public CMUProgram() {
        super();
        this.cmuScene = new CMUScene();
        this.timeOfLastSpeedChange = System.currentTimeMillis();
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

    public CMUScene getCmuScene() {
        return cmuScene;
    }

    /**
     * Load the given CMU file (.a3p extension).
     * @param cmuFile The file to load
     */
    public void setFile(File cmuFile) {
        edu.cmu.cs.dennisc.alice.Project project = edu.cmu.cs.dennisc.alice.io.FileUtilities.readProject(cmuFile);
        edu.cmu.cs.dennisc.alice.ast.AbstractType programType = project.getProgramType();

        this.vm = new edu.cmu.cs.dennisc.alice.virtualmachine.ReleaseVirtualMachine();

        this.sceneType = programType.getDeclaredFields().get(0).getValueType();
        this.scene = this.vm.createInstanceEntryPoint(this.sceneType);

        Object o = ((edu.cmu.cs.dennisc.alice.virtualmachine.InstanceInAlice) this.scene).getInstanceInJava();

        this.setScene((org.alice.apis.moveandturn.Scene) o);

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
     * to reach the specified time.
     * @param time The time (in milliseconds) we should fast-forward to
     * @param duration The length of time (in milliseconds) which the fast-forward should last
     */
    public void advanceToTime(final float time, final long duration) {
        new Thread(new Runnable() {

            public void run() {
                synchronized (CMUProgram.this.speedChangeLock) {
                    try {
                        //CMUProgram.this.setStarted();
                        float timeToAdvance = time - CMUProgram.this.getElapsedTime();
                        float prevPlaybackSpeed = CMUProgram.this.getPlaybackSpeed();
                        if (timeToAdvance > 0) {
                            float speed = timeToAdvance / (float) duration;
                            CMUProgram.this.setPlaybackSpeed(speed);
                            Thread.sleep(duration);
                            CMUProgram.this.setPlaybackSpeed(prevPlaybackSpeed);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(CMUProgram.class.getName()).log(Level.SEVERE, null, ex);
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
     * Play at standard speed.
     */
    public void play() {
        this.setPlaybackSpeed(PlaybackDefaults.DEFAULT_PLAYBACK_SPEED);
    }

    /**
     * Pause playback.
     */
    public void pause() {
        this.setPlaybackSpeed(PlaybackDefaults.PAUSE_SPEED);
    }

    /**
     * Set a particular playback speed and update the total elapsed time.
     * @param speed The speed at which to play
     */
    public void setPlaybackSpeed(float speed) {
        System.out.println("SETTING PLAYBACK SPEED: " + speed);

        long currTime = System.currentTimeMillis();
        this.elapsed += this.playbackSpeed * (currTime - this.timeOfLastSpeedChange);
        this.timeOfLastSpeedChange = currTime;
        this.playbackSpeed = speed;
        this.handleSpeedChange((double) speed);
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

        if (!this.isStarted() && speed != PlaybackDefaults.PAUSE_SPEED) {
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
     * Check whether the program is playing.
     * @return true if the program is playing at any speed
     */
    public boolean isPlaying() {
        return (this.getPlaybackSpeed() != PlaybackDefaults.PAUSE_SPEED);
    }

    /**
     * Start this scene and mark that this has been done; should be
     * used instead of start().
     */
    public void setStarted() {
        //System.out.println("PROGRAM STARTING! with URI: " + this.cmuURI);

        started = true;
        this.start();
        //TODO: figure out how to actually do this.
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(CMUProgram.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.setPlaybackSpeed(this.getPlaybackSpeed());
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
        cmuScene.setScene(sc);
    }
}
