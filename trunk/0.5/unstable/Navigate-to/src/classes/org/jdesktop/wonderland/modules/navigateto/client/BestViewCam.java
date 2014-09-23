/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.navigateto.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import com.wonderbuilders.modules.capabilitybridge.client.CapabilityBridge;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.utils.CellPlacementUtils;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.CameraController;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseWheelEvent3D;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.bestview.client.BestViewComponent;

/**
 *
 * @author nilang shah
 */
public class BestViewCam extends EventClassFocusListener implements CameraController {

    private final WorldManager wm;
    private final CellTransform start;
    private final CellTransform target;
    private final float distance;
    private final CameraController prevCam;
    private long startTime;
    private final long moveTime;
    private boolean doneMoving = false;
    private boolean dirty = false;
    private CellTransform transform;
    private CameraNode cameraNode;
    int i = 0;
    private float zoom = 0;
    boolean bestView = false;
    private Event genEvent;
    private Cell cell;
    private List compListener;

    public BestViewCam(CellTransform start, CellTransform target,
            float distance, long moveTime, CameraController prevCam, Event genEvent, Cell cell) {
        this.start = start;
        this.target = target;
        this.distance = distance;
        this.moveTime = moveTime;
        this.prevCam = prevCam;
        this.wm = WorldManager.getDefaultWorldManager();
        this.genEvent = genEvent;
        this.cell = cell;
    }

    public void setEnabled(boolean enabled, CameraNode cameraNode) {
        if (enabled) {
            setCameraNode(cameraNode);
            setStartTime(System.currentTimeMillis());
            ClientContextJME.getInputManager().addGlobalEventListener(this);
        } else {
            setCameraNode(null);
            ClientContextJME.getInputManager().removeGlobalEventListener(this);
        }
    }

    public void compute() {
        bestView = true;
        if (doneMoving) {
            //enable all mouse listeners
            if(genEvent!=null) {
                for (CellComponent comp : cell.getComponents()) {
                    if (comp instanceof CapabilityBridge) {
                        CapabilityBridge bridge = (CapabilityBridge) comp;
                        EventClassListener listener = bridge.getMouseEventListener();
                        if(listener!=null) {
                            listener.setEnabled(true);
                            if(!(comp instanceof BestViewComponent)) {
                                listener.commitEvent(genEvent);
                            }
                        }
                    }
                }
                if(cell.getParent()!=null) {
                    for (CellComponent comp : cell.getParent().getComponents()) {
                        if (comp instanceof CapabilityBridge) {
                            CapabilityBridge bridge = (CapabilityBridge) comp;
                            EventClassListener listener = bridge.getMouseEventListener();
                            if(listener!=null) {
                                listener.setEnabled(true);
                                if(!(comp instanceof BestViewComponent)) {
                                    listener.commitEvent(genEvent);
                                }
                            }
                        }
                    }
                }
            }
            genEvent = null;
            return;
        }

        // get the current time and location
        long relativeTime = System.currentTimeMillis() - getStartTime();
        float amt = (float) relativeTime / (float) moveTime;
        if (amt >= 1.0) {
            amt = 1.0f;
            doneMoving = true;
        }

        Quaternion t = target.getRotation(null);
        Vector3f distVec = CellPlacementUtils.getLookDirection(t, null);
        distVec.multLocal(distance);
        Vector3f origin = target.getTranslation(null);
        origin.subtractLocal(distVec);
        Vector3f st = start.getTranslation(null);
        st.interpolate(origin, amt);
        Quaternion sq = start.getRotation(null);
        sq.slerp(t, amt);
        transform = new CellTransform(sq, st);
        dirty = true;
    }

    @Override
    public Class[] eventClassesToConsume() {
        return new Class[]{MouseWheelEvent3D.class};
    }

    @Override
    public void commitEvent(Event event) {
        MouseWheelEvent me = (MouseWheelEvent) ((MouseEvent3D) event).getAwtEvent();
        int clicks = me.getWheelRotation();
        zoom -= clicks * 0.2f;
    }

    public void commit() {
        if (dirty && transform != null) {
            CameraNode camera = getCameraNode();
            // apply zoom
            Vector3f loc = transform.getTranslation(null);
            Quaternion look = transform.getRotation(null);
            Vector3f z = look.mult(new Vector3f(0, 0, zoom));
            loc.addLocal(z);
            camera.setLocalRotation(look);
            camera.setLocalTranslation(loc);
            wm.addToUpdateList(camera);
        }
    }

    public void viewMoved(CellTransform worldTransform) {
        if (doneMoving) {
            ClientContextJME.getViewManager().setCameraController(prevCam);
        }
    }

    public CameraController getPrevCam() {
        return prevCam;
    }

    private synchronized void setCameraNode(CameraNode cameraNode) {
        this.cameraNode = cameraNode;
    }

    private synchronized CameraNode getCameraNode() {
        return cameraNode;
    }

    private synchronized void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    private synchronized long getStartTime() {
        return startTime;
    }
}
