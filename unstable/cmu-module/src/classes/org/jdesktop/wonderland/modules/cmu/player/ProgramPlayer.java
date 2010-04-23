/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

import edu.cmu.cs.dennisc.alice.ast.AbstractMethod;
import edu.cmu.cs.dennisc.alice.ast.AbstractParameter;
import edu.cmu.cs.dennisc.alice.ast.TypeDeclaredInJava;
import org.jdesktop.wonderland.modules.cmu.player.connections.SceneConnectionHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.alice.apis.moveandturn.Program;
import org.alice.apis.moveandturn.event.MouseButtonListener;
import org.alice.stageide.apis.moveandturn.event.MouseButtonAdapter;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;
import org.jdesktop.wonderland.modules.cmu.common.PersistentSceneData;
import org.jdesktop.wonderland.modules.cmu.common.UnloadSceneReason;
import org.jdesktop.wonderland.modules.cmu.common.events.EventResponseList;
import org.jdesktop.wonderland.modules.cmu.common.events.responses.CMUResponseFunction;
import org.jdesktop.wonderland.modules.cmu.common.events.responses.CMUResponseFunctionTypes;
import org.jdesktop.wonderland.modules.cmu.player.connections.ContentManager;

/**
 * A standard CMU program which can access its scene graph via a
 * SceneConnectionHandler, which in turn forwards scene graph changes
 * to anyone who cares to listen.  Also handles playback speed changes
 * gracefully, and keeps track of its total running time.
 * @author kevin
 */
public class ProgramPlayer extends Program {

    private static final long DEFAULT_ADVANCE_DURATION = 2000;
    private static final Logger logger = Logger.getLogger(ProgramPlayer.class.getName());
    private String sceneName;
    private edu.cmu.cs.dennisc.alice.virtualmachine.VirtualMachine vm;
    private edu.cmu.cs.dennisc.alice.ast.AbstractType sceneType;
    private Object scene;
    private final SceneConnectionHandler sceneConnectionHandler;
    private boolean started = false;        // True once the program has begun to execute.
    private float elapsed = 0;              // The total amount of "time" this program has been executing.
    private float playbackSpeed = 0;        // The current playback speed.
    private long timeOfLastSpeedChange;     // System time at the last speed change (in milliseconds).
    private final Object speedChangeLock = new Object();    // Used to prevent multiple threads from changing the program speed.
    private EventResponseList eventList = null;
    private final Object eventListLock = new Object();

    /**
     * Standard constructor.
     * @param sceneFile A .a3p file representing the scene to load
     * @param sceneName The name of this scene, for use in matching it with persistent data in the content repository
     */
    public ProgramPlayer(File sceneFile, String sceneName) {
        super();
        this.sceneConnectionHandler = new SceneConnectionHandler();
        this.timeOfLastSpeedChange = System.currentTimeMillis();
        this.setFile(sceneFile, sceneName);
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

    public ArrayList<CMUResponseFunction> getAllowedResponses() {
        ArrayList<CMUResponseFunction> allowedResponses = new ArrayList<CMUResponseFunction>();

        for (AbstractMethod method : this.sceneType.getDeclaredMethods()) {
            for (AbstractParameter parameter : method.getParameters()) {
                TypeDeclaredInJava javaType = (TypeDeclaredInJava) parameter.getValueType();
            }

            for (CMUResponseFunction response : CMUResponseFunctionTypes.RESPONSE_FUNCTION_TYPES) {
                if (method.getParameters().size() == response.getArgumentClasses().length) {
                    boolean methodMatchesResponse = true;
                    for (int i = 0; i < method.getParameters().size(); i++) {
                        AbstractParameter parameter = method.getParameters().get(i);
                        if (parameter.getValueType() instanceof TypeDeclaredInJava) {
                            TypeDeclaredInJava javaType = (TypeDeclaredInJava) parameter.getValueType();
                            if (!javaType.getClassReflectionProxy().getReification().equals(response.getArgumentClasses()[i])) {
                                methodMatchesResponse = false;
                            }
                        } else {
                            logger.severe("Invalid parameter type: " + parameter.getValueType());
                            methodMatchesResponse = false;
                        }
                    }

                    if (methodMatchesResponse) {
                        allowedResponses.add(response.createResponse(method.getName()));
                    }
                }
            }
        }

        return allowedResponses;
    }

    public EventResponseList getEventList() {
        synchronized (this.eventListLock) {
            return eventList;
        }
    }

    public void setEventList(EventResponseList eventList) {

        EventResponseList oldList = null;

        synchronized (this.eventListLock) {
            oldList = this.eventList;
            this.eventList = eventList;
        }

        // If a change was made, update the data on the server
        if (eventList != null && !(eventList.equals(oldList))) {
            this.updatePersistentData();
        }
    }

    /**
     * Update the stored data for this scene based on
     * the current data in this object (e.g. the event list).
     */
    protected void updatePersistentData() {
        ContentManager.uploadSceneData(new PersistentSceneData(this.getEventList()),
                this.getPersistentDataFilename());
    }

    /**
     * Simulate a mouse click on a particular node.  Only left-click is
     * supported.
     * @param id ID for the node receiving the click
     */
    public void click(NodeID id) {
        this.sceneConnectionHandler.click(id);
    }

    public void eventResponse(CMUResponseFunction response) {
        this.vm.invokeEntryPoint(this.sceneType.getDeclaredMethod(response.getFunctionName(), response.getArgumentClasses()),
                this.scene, response.getArgumentValues());
    }

    /**
     * Load the given CMU file (.a3p extension), and read any persistent
     * data from the server based on the scene name.
     * @param cmuFile The file to load
     */
    protected void setFile(File cmuFile, String sceneName) {
        this.sceneName = sceneName;

        edu.cmu.cs.dennisc.alice.Project project = edu.cmu.cs.dennisc.alice.io.FileUtilities.readProject(cmuFile);
        edu.cmu.cs.dennisc.alice.ast.AbstractType programType = project.getProgramType();

        this.vm = new edu.cmu.cs.dennisc.alice.virtualmachine.ReleaseVirtualMachine();

        this.sceneType = programType.getDeclaredFields().get(0).getValueType();
        this.scene = this.vm.createInstanceEntryPoint(this.sceneType);

        Object sceneInstance = ((edu.cmu.cs.dennisc.alice.virtualmachine.InstanceInAlice) this.scene).getInstanceInJava();

        vm.registerAnonymousAdapter(MouseButtonListener.class, MouseButtonAdapter.class);

        this.setScene((org.alice.apis.moveandturn.Scene) sceneInstance);

        this.init();

        // Read persistent data for this file, if any
        PersistentSceneData data = ContentManager.downloadSceneData(this.getPersistentDataFilename());
        if (data != null) {
            this.setEventList(data.getEventList());
        } else {
            this.eventList = new EventResponseList();
        }
    }

    public String getPersistentDataFilename() {
        //TODO: find a better way to match CMU files
        return this.sceneName.replace('/', '_') + ".xml";
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
     * @param reason The reason for the disconnection
     */
    public void disconnectProgram(UnloadSceneReason reason) {
        this.sceneConnectionHandler.unloadScene(reason);
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
