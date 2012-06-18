/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.camera;

import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.jme.CameraController;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.ThirdPersonCameraProcessor;
import org.jdesktop.wonderland.modules.ezscript.client.camera.ScriptedCameraController.ScriptedCameraProcessor;

/**
 *
 * @author JagWire
 */
public enum CameraContext {
    INSTANCE;
    
    private CameraController previousController = null;
    private ScriptedCameraProcessor camera = null;
    private boolean initialized = false;
    
    public void initialize(ScriptedCameraProcessor camera) {
        this.camera = camera;
        this.initialized = true;
    }
    
    public void startScriptedCamera() {
        
        SceneWorker.addWorker(new WorkCommit() {

            public void commit() {
                if (initialized) {
                    /*
                     * Temporary fix camera issues with iSocial last lesson.
                     */
//                    previousController = ClientContextJME.getViewManager().getCameraController();

                    ClientContextJME.getViewManager().setCameraController(camera);
                }
            }
        });

    }
    
    public void stopScriptedCamera() {
        SceneWorker.addWorker(new WorkCommit() {

            public void commit() {
                if (initialized) {
                    if (previousController != null) {

                        ClientContextJME.getViewManager().setCameraController(previousController);
                    } else {
                        ClientContextJME.getViewManager().setCameraController(new ThirdPersonCameraProcessor());
                    }
                }
            }
        });
    }
    
    public ScriptedCameraProcessor getCamera() {
        if(initialized) {
            return camera;
        } 
        return null;
    }
}
