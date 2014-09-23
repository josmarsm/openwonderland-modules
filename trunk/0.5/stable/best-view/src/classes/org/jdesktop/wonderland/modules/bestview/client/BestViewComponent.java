/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.bestview.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import com.wonderbuilders.modules.capabilitybridge.client.CapabilityBridge;
import imi.character.behavior.CharacterBehaviorManager;
import imi.character.behavior.GoSit;
import imi.character.statemachine.GameContext;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.*;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseWheelEvent3D;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellStatus;
import static org.jdesktop.wonderland.common.cell.CellStatus.DISK;
import static org.jdesktop.wonderland.common.cell.CellStatus.RENDERING;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;
import org.jdesktop.wonderland.modules.bestview.common.BestViewChangeMessage;
import org.jdesktop.wonderland.modules.bestview.common.BestViewClientState;
import org.jdesktop.wonderland.modules.bestview.common.BestViewServerState;
import org.jdesktop.wonderland.modules.sitting.client.SittingCellComponent;

/**
 * Cell component for best view
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 * @author Abhishek Upadhyay
 */
public class BestViewComponent extends CellComponent
        implements ContextMenuActionListener, CapabilityBridge {

    private static final Logger LOGGER =
            Logger.getLogger(BestViewComponent.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org.jdesktop.wonderland.modules.bestview.client.Bundle");
    // import the context menu component
    @UsesCellComponent
    private ContextMenuComponent contextMenu;
    // context menu item factory
    private final ContextMenuFactorySPI ctxMenuFactory;
    //variable made public for using in navigate-to capability
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public float lookDirX;
    public float lookDirY;
    public float lookDirZ;
    public float lookDirW;
    public float zoom;
    public int trigger = 1;
    private BestViewComponent.MouseEventListener listener = null;
    private BestViewComponent.AltKeyListener altKeyListener = null;
    private boolean isAltPressed = false;
    public CameraController prev;
    public float oldObjPosX;
    public float oldObjPosY = 999;
    public float oldObjPosZ;
    private ChannelComponent channelComp;
    private ChannelComponent.ComponentMessageReceiver msgReceiver;
    private Event genEvent;
    private ScheduledExecutorService exec = null;

    public BestViewComponent(Cell cell) {
        super(cell);
        // create the context menu item and register it
        final ContextMenuItem item =
                new SimpleContextMenuItem(BUNDLE.getString("Best_View"), this);
        ctxMenuFactory = new ContextMenuFactorySPI() {
            public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                return new ContextMenuItem[]{item};
            }
        };
    }

    /**
     * Configure the component based on the client state that was passed in.
     */
    @Override
    public void setClientState(CellComponentClientState clientState) {

        // allow the superclass to do any configuration necessary
        super.setClientState(clientState);
        offsetX = ((BestViewClientState) clientState).getOffsetX();
        offsetY = ((BestViewClientState) clientState).getOffsetY();
        offsetZ = ((BestViewClientState) clientState).getOffsetZ();
        lookDirX = ((BestViewClientState) clientState).getLookDirX();
        lookDirY = ((BestViewClientState) clientState).getLookDirY();
        lookDirZ = ((BestViewClientState) clientState).getLookDirZ();
        lookDirW = ((BestViewClientState) clientState).getLookDirW();
        zoom = ((BestViewClientState) clientState).getZoom();
        trigger = ((BestViewClientState) clientState).getTrigger();
        oldObjPosX = ((BestViewClientState) clientState).getOldObjPosX();
        oldObjPosY = ((BestViewClientState) clientState).getOldObjPosY();
        oldObjPosZ = ((BestViewClientState) clientState).getOldObjPosZ();
    }

    /**
     * Set the status of the component
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {

        switch (status) {
            case DISK:
                channelComp.removeMessageReceiver(BestViewChangeMessage.class);
                if (listener != null) {
                    CellRenderer cellRenderer =
                            cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                    CellRendererJME renderer = (CellRendererJME) cellRenderer;
                    Entity entity = renderer.getEntity();
                    listener.removeFromEntity(entity);
                    listener = null;
                }

                //remove global alt key listener
                if (altKeyListener != null) {
                    ClientContextJME.getInputManager().removeGlobalEventListener(altKeyListener);
                }

                // remove context menu item
                contextMenu.removeContextMenuFactory(ctxMenuFactory);
                break;
            case RENDERING:
                if (listener == null) {
                    try {
                        //Attach a click listener
                        CellRenderer cellRenderer =
                                cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                        CellRendererJME renderer =
                                (CellRendererJME) cellRenderer;
                        Entity entity = renderer.getEntity();
                        listener = new BestViewComponent.MouseEventListener();
                        listener.addToEntity(entity);

                    } catch (NullPointerException npe) {
                        LOGGER.log(Level.WARNING,
                                "could not attach click listener", npe);
                    }
                }
                //add global alt key listener if not null
                if (altKeyListener == null) {
                    altKeyListener = new BestViewComponent.AltKeyListener();
                    ClientContextJME.getInputManager().addGlobalEventListener(altKeyListener);
                }
                break;

            case VISIBLE:

                if (increasing) {
                    // add context menu item
                    contextMenu.addContextMenuFactory(ctxMenuFactory);
                    if (msgReceiver == null) {
                        msgReceiver = new ChannelComponent.ComponentMessageReceiver() {
                            public void messageReceived(CellMessage message) {
                                receive(message);
                            }
                        };
                        channelComp = cell.getComponent(ChannelComponent.class);
                        channelComp.addMessageReceiver(BestViewChangeMessage.class, msgReceiver);
                    }

                    //store initial values.    
                    Vector3f cellPos = cell.getWorldTransform().getTranslation(null);
                    if (offsetX == 0 && offsetY == 0 && offsetZ == 0 && lookDirX == 0 && lookDirY == 0 && lookDirZ == 0 && lookDirW == 0) {
                        CellTransform xform = BestViewUtils.getBestView(cell);

                        offsetX = cellPos.x - xform.getTranslation(null).x;
                        offsetY = cellPos.y - xform.getTranslation(null).y;
                        offsetZ = cellPos.z - xform.getTranslation(null).z;
                        lookDirX = xform.getRotation(null).x;
                        lookDirY = xform.getRotation(null).y;
                        lookDirZ = xform.getRotation(null).z;
                        lookDirW = xform.getRotation(null).w;
                        BestViewServerState bvss = new BestViewServerState();
                        bvss.setLookDirW(lookDirW);
                        bvss.setLookDirX(lookDirX);
                        bvss.setLookDirY(lookDirY);
                        bvss.setLookDirZ(lookDirZ);
                        bvss.setOffsetX(offsetX);
                        bvss.setOffsetY(offsetY);
                        bvss.setOffsetZ(offsetZ);
                        bvss.setOldObjPosX(oldObjPosX);
                        bvss.setOldObjPosY(oldObjPosY);
                        bvss.setOldObjPosZ(oldObjPosZ);
                        bvss.setTrigger(trigger);
                        bvss.setZoom(zoom);
                        BestViewChangeMessage msg = new BestViewChangeMessage(cell.getCellID(), bvss);
                        cell.sendCellMessage(msg);
                    }
                }
                break;
            default:
                break;
        }
        super.setStatus(status, increasing);
    }

    private void receive(CellMessage message) {
        if (message instanceof CellServerComponentMessage) {
        }
    }

    /**
     * Called when the context menu is clicked
     */
    public void actionPerformed(ContextMenuItemEvent event) {
        //if trigger set to right click
        if (trigger == 1) {
            //store the current camera
            genEvent=null;
            if (!(ViewManager.getViewManager().getCameraController() instanceof BestViewCam)) {
                prev = ViewManager.getViewManager().getCameraController();
            } else {
                BestViewCam bvcc = (BestViewCam) ViewManager.getViewManager().getCameraController();
                prev = bvcc.getPrevCam();
            }
            
            //set camera to best view
            listener.setBestView(prev,
                    ViewManager.getViewManager().getPrimaryViewCell().getWorldTransform());
        }
    }

    /**
     * A camera controller that puts the camera at the best view position, and
     * allows zooming in or out from there.
     */
    public class BestViewCameraController extends EventClassFocusListener
            implements CameraController {

        private final Vector3f location;
        private final Quaternion look;
        private final CameraController prevCamera;
        private final CellTransform viewLocation;
        private boolean needsUpdate = true;
        private CameraNode cameraNode;
        private float zoom = 0;

        public BestViewCameraController(Vector3f location, Quaternion look,
                CameraController prevCamera,
                CellTransform viewLocation) {
            this.location = location;
            this.look = look;
            this.prevCamera = prevCamera;
            this.viewLocation = viewLocation;
        }

        public void setEnabled(boolean enabled, CameraNode cameraNode) {
            if (enabled) {
                this.cameraNode = cameraNode;
                ClientContextJME.getInputManager().addGlobalEventListener(this);
            } else {
                this.cameraNode = null;
                ClientContextJME.getInputManager().removeGlobalEventListener(this);
            }
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
            needsUpdate = true;
        }

        public void compute() {}

        public void commit() {
            if (cameraNode != null && needsUpdate) {
                Vector3f loc = location.clone();
                Vector3f z = look.mult(new Vector3f(0, 0, zoom));
                loc.addLocal(z);
                cameraNode.setLocalTranslation(loc);
                cameraNode.setLocalRotation(look);
                ClientContextJME.getWorldManager().addToUpdateList(cameraNode);
                needsUpdate = false;
            }
        }

        public void viewMoved(CellTransform worldTransform) {
            // if the avatar moves, go back to the original camera
            if (!viewLocation.equals(worldTransform)) {
                ClientContextJME.getViewManager().setCameraController(prevCamera);
            }
        }
    }

    public class MouseEventListener extends EventClassListener {

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseButtonEvent3D.class};
        }

        @Override
        public boolean propagatesToParent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            if (trigger == 0) {
                if (mbe.isClicked() == true && mbe.getButton() == MouseEvent3D.ButtonId.BUTTON1) {
                    return false;
                }
                return true;
            } else {
                return true;
            }
        }

        @Override
        public boolean consumesEvent(final Event event) {

            LOGGER.info("BestView : consumesEvent" + event);
            //if left click then check if other capabilities are triggered on lift click or not.
            //if yes then temporary disable their listener.
            if (event instanceof MouseButtonEvent3D) {
                MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
                if (mbe.isClicked() == true && mbe.getButton() == MouseEvent3D.ButtonId.BUTTON1) {
                    LOGGER.info("BestView : consumesEvent");

                    if (trigger == 0) {
                        SittingCellComponent sittingComp = cell.getComponent(SittingCellComponent.class);
                        if (sittingComp != null) {
                            Cell avatarCell = ClientContextJME.getViewManager().getPrimaryViewCell();
                            CellRenderer rend = avatarCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                            WlAvatarCharacter myAvatar = ((AvatarImiJME) rend).getAvatarCharacter();
                            GameContext context = myAvatar.getContext();
                            CharacterBehaviorManager helm = context.getBehaviorManager();
                            if (helm.getCurrentTask() instanceof GoSit) {
                                if (exec != null) {
                                    exec.shutdown();
                                }
                                exec = Executors.newSingleThreadScheduledExecutor();
                                exec.scheduleAtFixedRate(new Runnable() {
                                    public void run() {
                                        Cell avatarCell = ClientContextJME.getViewManager().getPrimaryViewCell();
                                        CellRenderer rend = avatarCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                                        WlAvatarCharacter myAvatar = ((AvatarImiJME) rend).getAvatarCharacter();
                                        GameContext context = myAvatar.getContext();
                                        CharacterBehaviorManager helm = context.getBehaviorManager();
                                        if (helm.getCurrentTask() == null) {
                                            commitEvent(event);
                                            exec.shutdown();
                                        }
                                    }
                                }, 0, 1, TimeUnit.SECONDS);
                            }
                            return false;
                        }
                        //Iterate all components of this cell and see if any one implements the CapabilityBridge.
                        //Disable the mouse listener if the component is a CapabilityBridge
                        //Then do the same for the parent cell
                        for(CellComponent comp : cell.getComponents()) {
                            if (comp instanceof CapabilityBridge && !(comp instanceof BestViewComponent)) {
                                CapabilityBridge bridge = (CapabilityBridge) comp;
                                EventClassListener listener = bridge.getMouseEventListener();
                                if(listener!=null) {
                                    listener.setEnabled(false);
                                }
                            }
                        }
                        //for parent cell
                        if(cell.getParent()!=null) {
                            for(CellComponent comp : cell.getParent().getComponents()) {
                                if (comp instanceof CapabilityBridge && !(comp instanceof BestViewComponent)) {
                                    CapabilityBridge bridge = (CapabilityBridge) comp;
                                    EventClassListener listener = bridge.getMouseEventListener();
                                    if(listener!=null) {
                                        listener.setEnabled(false);
                                    }
                                }
                            }
                        }
                        genEvent = event;
                    }
                }
            }
            return super.consumesEvent(event);
        }

        @Override
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;

            //Make sure its a click!
            if (mbe.isClicked() == true && mbe.getButton() == MouseEvent3D.ButtonId.BUTTON1) {
                //if trigger is set to left click
                if (trigger == 0) {
                    //check if user tries to use ez move functionality or not
                    //so check if alt is already pressed
                    //if pressed then dont set bestview camera
                    if (!isAltPressed()) {

                        //store current camera as previous camera
                        if (!(ViewManager.getViewManager().getCameraController() instanceof BestViewCam)) {
                            prev = ViewManager.getViewManager().getCameraController();
                        } else {
                            BestViewCam bvcc = (BestViewCam) ViewManager.getViewManager().getCameraController();
                            prev = bvcc.getPrevCam();
                        }
                        setBestView(prev,
                                ViewManager.getViewManager().getPrimaryViewCell().getWorldTransform());
                    }
                }
            }
        }

        public void setBestView(CameraController prev, CellTransform viewLocation) {
            //trigger best view camera

            Vector3f cellPos = cell.getWorldTransform().getTranslation(null);

            //if initial values
            if (offsetX == 0 && offsetY == 0 && offsetZ == 0 && lookDirX == 0 && lookDirY == 0 && lookDirZ == 0 && lookDirW == 0) {
                CellTransform xform = BestViewUtils.getBestView(cell);
                Quaternion quat = cell.getWorldTransform().getRotation(null);
                float ff[] = new float[3];
                quat.toAngles(ff);
                oldObjPosX = (float) Math.toDegrees(ff[0]);
                oldObjPosY = (float) Math.toDegrees(ff[1]);
                oldObjPosZ = (float) Math.toDegrees(ff[2]);
                offsetX = cellPos.x - xform.getTranslation(null).x;
                offsetY = cellPos.y - xform.getTranslation(null).y;
                offsetZ = cellPos.z - xform.getTranslation(null).z;
                lookDirX = xform.getRotation(null).x;
                lookDirY = xform.getRotation(null).y;
                lookDirZ = xform.getRotation(null).z;
                lookDirW = xform.getRotation(null).w;
            }

            Vector3f cameraPos = new Vector3f((cellPos.x - offsetX), cellPos.y - offsetY, (cellPos.z - offsetZ));
            Quaternion cameraRot = new Quaternion(lookDirX, lookDirY, lookDirZ, lookDirW);

            if (cameraPos.equals(ViewManager.getViewManager().getCameraTransform().getTranslation(null))
                    && cameraRot.equals(ViewManager.getViewManager().getCameraTransform().getRotation(null))) {
                enableCapabilityListeners();
                return;
            }

            //code for changing offset after object rotation    
            Quaternion cellQ = cell.getWorldTransform().getRotation(null);
            float angles[] = new float[3];
            cellQ.toAngles(angles);
            angles[0] = (float) Math.toDegrees(angles[0]);
            angles[1] = (float) Math.toDegrees(angles[1]);
            angles[2] = (float) Math.toDegrees(angles[2]);
            Vector3f offsetVec = new Vector3f(offsetX, offsetY, offsetZ);
            float diffX = 0;
            float diff = 0;
            float diffZ = 0;

            if (oldObjPosY != 999) {
                diffX = oldObjPosX - angles[0];
                diff = oldObjPosY - angles[1];
                diffZ = oldObjPosZ - angles[2];
            } else {
                Quaternion quat = cell.getWorldTransform().getRotation(null);
                float[] f = new float[3];
                quat.toAngles(f);
                oldObjPosX = (float) Math.toDegrees(f[0]);
                oldObjPosY = (float) Math.toDegrees(f[1]);
                oldObjPosZ = (float) Math.toDegrees(f[2]);
            }

            if (Math.abs(diff) > 0.1) {
                Quaternion q = new Quaternion(new float[]{(float) Math.toRadians(-diffX), (float) Math.toRadians(-diff), (float) Math.toRadians(diffZ)});
                offsetVec = q.mult(offsetVec);
                cameraPos.setX((cellPos.getX() - offsetVec.getX()));
                cameraPos.setY(cellPos.getY() - offsetVec.getY());
                cameraPos.setZ((cellPos.getZ() - offsetVec.getZ()));
            }
            Quaternion q2 = null;
            if (Math.abs(diff) > 0.1) {
                float ang[] = new float[3];
                cameraRot.toAngles(ang);
                float angleX = (float) Math.toDegrees(ang[0]) - diffX;
                float angle = (float) Math.toDegrees(ang[1]) - diff;
                float angleZ = (float) Math.toDegrees(ang[2]) - diffZ;
                q2 = new Quaternion(new float[]{(float) Math.toRadians(angleX), (float) Math.toRadians(angle), (float) Math.toRadians(angleZ)});
                float[] f1 = new float[3];
                q2.toAngles(f1);
                Quaternion my1 = new Quaternion();
                my1.fromAngles(f1);
                cameraRot = my1;
            }

            if (Math.abs(diff) > 0.1) {

                //change values and send msg to server
                oldObjPosX = angles[0];
                oldObjPosY = angles[1];
                oldObjPosZ = angles[2];
                lookDirX = q2.x;
                lookDirY = q2.y;
                lookDirZ = q2.z;
                lookDirW = q2.w;
                offsetX = offsetVec.getX();
                offsetY = offsetVec.getY();
                offsetZ = offsetVec.getZ();
                BestViewServerState bvss = new BestViewServerState();
                bvss.setLookDirW(lookDirW);
                bvss.setLookDirX(lookDirX);
                bvss.setLookDirY(lookDirY);
                bvss.setLookDirZ(lookDirZ);
                bvss.setOffsetX(offsetX);
                bvss.setOffsetY(offsetY);
                bvss.setOffsetZ(offsetZ);
                bvss.setOldObjPosX(oldObjPosX);
                bvss.setOldObjPosY(oldObjPosY);
                bvss.setOldObjPosZ(oldObjPosZ);
                bvss.setTrigger(trigger);
                bvss.setZoom(zoom);
                BestViewChangeMessage msg = new BestViewChangeMessage(cell.getCellID(), bvss);
                cell.sendCellMessage(msg);
            }
            //create best view camera
            CameraController camera = new BestViewCam(ViewManager.getViewManager().getCameraTransform(), new CellTransform(cameraRot, cameraPos),
                    0f, 1500, prev, genEvent, cell);
            ClientContextJME.getViewManager().setCameraController(camera);
        }
    }

    /**
     * Enable the listeners of other capabilities.
     */
    private void enableCapabilityListeners() {

        //done the work of bestview
        //so enable all the other components
        if(genEvent!=null) {
            for (CellComponent comp : cell.getComponents()) {
                if (comp instanceof CapabilityBridge && !(comp instanceof BestViewComponent)) {
                    CapabilityBridge bridge = (CapabilityBridge) comp;
                    EventClassListener listener = bridge.getMouseEventListener();
                    if(listener!=null) {
                        listener.setEnabled(true);
                        listener.commitEvent(genEvent);
                    }
                }
            }
            //for parent cell
            if(cell.getParent()!=null) {
                for (CellComponent comp : cell.getParent().getComponents()) {
                    if (comp instanceof CapabilityBridge && !(comp instanceof BestViewComponent)) {
                        CapabilityBridge bridge = (CapabilityBridge) comp;
                        EventClassListener listener = bridge.getMouseEventListener();
                        if(listener!=null) {
                            listener.setEnabled(true);
                            listener.commitEvent(genEvent);
                        }
                    }
                }
            }
        }
        genEvent = null;
    }

    class AltKeyListener extends EventClassListener {

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{KeyEvent3D.class};
        }

        @Override
        public void commitEvent(Event event) {
            if (event instanceof KeyEvent3D) {
                KeyEvent3D e = (KeyEvent3D) event;
                if (e.getKeyCode() == KeyEvent.VK_ALT && e.isPressed()) {
                    setIsAltPressed(true);
                } else if (e.getKeyCode() == KeyEvent.VK_ALT && e.isReleased()) {
                    setIsAltPressed(false);
                }
            }
        }
    }

    public void setIsAltPressed(boolean isAltPressed) {
        this.isAltPressed = isAltPressed;
    }

    public boolean isAltPressed() {
        return isAltPressed;
    }

    public EventClassListener getMouseEventListener() {
        return listener;
    }
}
