/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.Vector3f;
import java.awt.event.MouseEvent;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.PickDetails;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseMovedEvent3D;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class ClickPickMethod implements ReturnableScriptMethodSPI {

    private MouseClickPickEventListener listener;
    private PickDetailsWrapper pickPosition;
    private Semaphore lock = new Semaphore(0);
    private static final Logger logger = Logger.getLogger(ClickPickMethod.class.getName());

    public String getDescription() {
        return "Returns the position (Vector3f) of the object the mouse clicked on.\n"
                + "-- Usage: var vec3 = ClickPick();";
    }

    public String getFunctionName() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return "ClickPick";
    }

    public String getCategory() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return "utilities";
    }

    public void setArguments(Object[] args) {
        //no arguments
        //listener = new MouseClickPickEventListener();
        listener = new MouseClickPickEventListener();
    }

    public Object returns() {
        try {
            lock.acquire();
            InputManager.inputManager().removeGlobalEventListener(listener);
            listener = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pickPosition;
    }

    public void run() {
        new Thread(new Runnable() {
            public void run() {
                InputManager.inputManager().addGlobalEventListener(listener);


            }
        }).start();
    }

    class MouseClickPickEventListener extends EventClassListener {

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseButtonEvent3D.class};
        }

        @Override
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            if (mbe.isClicked()) {

                MouseEvent me = (MouseEvent)mbe.getAwtEvent();
                if(SwingUtilities.isRightMouseButton(me)) {
                    return;
                }
                
                if (mbe.getPickDetails() == null) {
                    lock.release();
                    logger.warning("Pick(): received null pick details!");
                    return;
                }

                pickPosition = new PickDetailsWrapper(mbe.getPickDetails());//mbe.getPickDetails().getPosition();

                logger.warning("Pick(): received pickPosition " + pickPosition);
                lock.release();
            }
        }
    }

    public static class PickDetailsWrapper extends Vector3f {
//        public float x;
//        public float y;
//        public float z;

        public Vector3f entityPosition;
        public Vector3f normal;
        private final PickDetails details;

        public PickDetailsWrapper(PickDetails details) {
            super();

            this.details = details;

            x = details.getPosition().x;
            y = details.getPosition().y;
            z = details.getPosition().z;
            normal = getNormalFromPickDetails(details);

            entityPosition = rootPositionOfEntity(details.getEntity());


        }

        private Vector3f rootPositionOfEntity(Entity entity) {
            Vector3f position = null;

            RenderComponent rc = entity.getComponent(RenderComponent.class);
            if (rc == null) {
                logger.warning("NO RENDER COMPONENT!");
                //we have problems!
                return null;
            }

            position = rc.getSceneRoot().getWorldTranslation();
            return position;


        }

        private Vector3f getNormalFromPickDetails(PickDetails details) {
            Vector3f triData[] = new Vector3f[3];

            details.getTriMesh().getTriangle(details.getTriIndex(), triData);
            triData[0] = details.getTriMesh().localToWorld(triData[0], null);
            triData[1] = details.getTriMesh().localToWorld(triData[1], null);
            triData[2] = details.getTriMesh().localToWorld(triData[2], null);

            Vector3f v1 = new Vector3f();
            Vector3f v2 = new Vector3f();
            Vector3f _normal = new Vector3f();

            triData[0].subtract(triData[1], v1);
            triData[2].subtract(triData[1], v2);

            v1.normalizeLocal();
            v2.normalizeLocal();

            v2.cross(v1, _normal);
            _normal.normalizeLocal();

            return _normal;
        }
    }

    class MouseMovePickEventListener extends EventClassFocusListener {

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseMovedEvent3D.class};
        }

        @Override
        public void commitEvent(Event event) {

            MouseMovedEvent3D m = (MouseMovedEvent3D) event;
            if (m.getPickDetails() == null) {
                lock.release();
                logger.warning("Pick(): received null pick details!");
                return;
            }

//            pickPosition = m.getPickDetails().getPosition();
            logger.warning("Pick(): received pickPosition " + pickPosition);
            lock.release();
        }
    }
}
