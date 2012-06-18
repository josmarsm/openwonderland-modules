/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.camera;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.jme.CameraController;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.EventBridgeSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.EventObjectSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.EventBridge;

/**
 *
 * @author JagWire
 */
@EventBridge
public class ScriptedCameraController implements EventBridgeSPI {

    private static final Logger logger = Logger.getLogger(ScriptedCameraController.class.getName());
//    private final ScriptedCameraProcessor camera = new ScriptedCameraProcessor();
    private ScriptEngine engine;
    private Bindings bindings;

    @Override
    public String getBridgeName() {
        return "CameraController";
    }

    @Override
    public EventObjectSPI[] getEventObjects() {
//        return new String[] {"update"};

        return new EventObjectSPI[]{
                    new EventObjectSPI() {

                        public String getEventName() {
                            return "update";
                        }

                        public int getNumberOfArguments() {
                            return 3;
                        }
                    }
                };
    }

    @Override
    public void initialize(ScriptEngine engine, Bindings bindings) {
        this.engine = engine;
        this.bindings = bindings;

        bindings.put("CameraContext", CameraContext.INSTANCE);

        logger.warning("Setting scriptable camera controller: " + getBridgeName());
        CameraContext.INSTANCE.initialize(new ScriptedCameraProcessor());
//        ClientContextJME.getViewManager().setCameraController(camera);
    }

    private void update(Vector3f position, Quaternion direction) {
        try {

//            logger.warning("UPDATE - POSITION: "+position+""
//                    + "\nUPDATE - DIRECTION: "+direction);
            long time = System.currentTimeMillis();
            String positionID = "position" + time;
            String directionID = "direction" + time;
            bindings.put("camera", CameraContext.INSTANCE.getCamera());
            bindings.put(positionID, position);
            bindings.put(directionID, direction);

            String script = getBridgeName() + ".update(camera," + positionID + "," + directionID + ");";
            engine.eval(script, bindings);

//            CameraContext.INSTANCE.getCamera().commit();
        } catch (ScriptException ex) {
            Logger.getLogger(ScriptedCameraController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class ScriptedCameraProcessor implements CameraController {

        private boolean enabled;
        private CameraNode cameraNode;
        private Vector3f avatarPosition;
        private Quaternion avatarRotation;
        private Quaternion newCameraDirection;
        private Vector3f newCameraPosition;

        public ScriptedCameraProcessor() {
            newCameraDirection = new Quaternion();
            newCameraPosition = new Vector3f();
        }

        public void setEnabled(boolean enabled, CameraNode cameraNode) {
            if (this.enabled == enabled) {
                return;
            }

            this.enabled = enabled;
            this.cameraNode = cameraNode;
//                if(enabled) {
//                    ClientContextJME.getInputManager().
//                }
        }

        public void compute() {
            //do nothing for the time being
        }

        public void commit() {
            setCameraPosition(newCameraDirection, newCameraPosition);
            WorldManager.getDefaultWorldManager().addToUpdateList(cameraNode);

        }

        public void viewMoved(CellTransform worldTransform) {
            avatarPosition = worldTransform.getTranslation(avatarPosition);
            avatarRotation = worldTransform.getRotation(avatarRotation);

            update(avatarPosition, avatarRotation);
        }

        private void setCameraPosition(Quaternion rotation,
                Vector3f translation) {
            cameraNode.setLocalRotation(rotation);
            cameraNode.setLocalTranslation(translation);
        }

        public void setNewCameraDirection(Quaternion newCameraDirection) {
            this.newCameraDirection = newCameraDirection;
        }

        public void setNewCameraPosition(Vector3f newCameraPosition) {
            this.newCameraPosition = newCameraPosition;
        }
    }
}
