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
package org.jdesktop.wonderland.modules.navigateto.client;

import com.jme.bounding.BoundingBox;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.wonderbuilders.modules.capabilitybridge.client.CapabilityBridge;
import imi.character.avatar.AvatarContext;
import imi.character.behavior.CharacterBehaviorManager;
import imi.character.statemachine.GameContext;
import java.awt.Component;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.*;
import org.jdesktop.wonderland.client.cell.TransformChangeListener.ChangeSource;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.utils.CellPlacementUtils;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.*;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.*;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarCollisionChangeRequestEvent;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;
import org.jdesktop.wonderland.modules.bestview.client.BestViewComponent;
import org.jdesktop.wonderland.modules.bestview.common.BestViewChangeMessage;
import org.jdesktop.wonderland.modules.bestview.common.BestViewServerState;
import org.jdesktop.wonderland.modules.navigateto.common.NavigateToClientState;
import org.jdesktop.wonderland.modules.navigateto.common.NavigateToServerState;
import org.jdesktop.wonderland.modules.navigateto.common.NavigationChangeMessage;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;

/**
 * Cell component for best view
 *
 * @author nilang shah
 * @author Abhishek Upadhyay
 */
public class NavigateToComponent extends CellComponent
        implements ContextMenuActionListener {

    private static final Logger LOGGER
            = Logger.getLogger(NavigateToComponent.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org.jdesktop.wonderland.modules.navigateto.client.Bundle");
    // import the context menu component
    @UsesCellComponent
    private ContextMenuComponent contextMenu;
    // context menu item factory
    private final ContextMenuFactorySPI ctxMenuFactory;
    @UsesCellComponent
    public SharedStateComponent sharedState;
    public SharedMapCli propertyMap;
    private int trigger = 0;
    public float offsetX = 1f;
    public float offsetY = 2f;
    public float offsetZ = 1f;
    public float lookDirX;
    public float lookDirY;
    private float lookDirZ;
    private boolean bestview = true;
    private Cell parentCell;
    private CellTransform ggoalPos;
    private static CameraController prevCC;
    private boolean isAltPressed = false;
    private NewGoTo gt;
    private TurnTo tt;
    private boolean isMoving = false;
    private static CellID prevNavigateCell;
    private BestViewCam bestViewCam = null;
    private int status = 0;
    private MouseEventListener listener = null;
    private AltKeyListener altKeyListener = null;
    private AvatarTransformChangeListener1 atcl1 = null;
    // Menu items for the collision & gravity check boxes
    private JCheckBoxMenuItem collisionResponseEnabledMI = null;
    private JCheckBoxMenuItem gravityEnabledMI = null;
    private boolean collision;
    private boolean gravity;
    float nvx = 0;
    float nvz = 0;
    private ChannelComponent channelComp;
    private ChannelComponent.ComponentMessageReceiver msgReceiver;
    private CharacterBehaviorManager helm;
    private Vector3f myvec;
    private CellTransform cellTrans;
    private Event genEvent;
    boolean goToRunning = false;
    private float prevScale = -1;
    private Quaternion prevRot = null;
    private ScaleRotationChangeListener srChangeListener = null;
    private ScheduledExecutorService ses1 = null;
    private ScheduledExecutorService ses2 = null;
    private ScheduledExecutorService ses3 = null;
    private ScheduledExecutorService ses4 = null;
    private ScheduledExecutorService ses5 = null;
    private ScheduledExecutorService ses6 = null;

    public NavigateToComponent(Cell cell) {
        super(cell);
        this.parentCell = cell;
        final ContextMenuItem item
                = new SimpleContextMenuItem(BUNDLE.getString("Navigate_To"), this);
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
        trigger = ((NavigateToClientState) clientState).getTrigger();
        offsetX = ((NavigateToClientState) clientState).getOffsetX();
        offsetY = ((NavigateToClientState) clientState).getOffsetY();
        offsetZ = ((NavigateToClientState) clientState).getOffsetZ();
        lookDirX = ((NavigateToClientState) clientState).getLookDirX();
        lookDirY = ((NavigateToClientState) clientState).getLookDirY();
        lookDirZ = ((NavigateToClientState) clientState).getLookDirZ();
        bestview = ((NavigateToClientState) clientState).getBestView();

    }

    /**
     * Set the status of the component
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        if (status == CellStatus.ACTIVE && increasing) {
            // add context menu item
            contextMenu.addContextMenuFactory(ctxMenuFactory);
        } else if (status == CellStatus.INACTIVE && !increasing) {
            // remove context menu item
            contextMenu.removeContextMenuFactory(ctxMenuFactory);
        }

        switch (status) {
            case DISK:
                channelComp.removeMessageReceiver(NavigationChangeMessage.class);
                //remove mouse listener
                if (listener != null) {
                    CellRenderer cellRenderer
                            = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                    CellRendererJME renderer = (CellRendererJME) cellRenderer;
                    Entity entity = renderer.getEntity();
                    listener.removeFromEntity(entity);
                    listener = null;
                }
                //remove alt key listener
                if (altKeyListener != null) {
                    CellRenderer cellRenderer
                            = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                    CellRendererJME renderer = (CellRendererJME) cellRenderer;
                    Entity entity = renderer.getEntity();
                    altKeyListener.removeFromEntity(entity);
                    altKeyListener = null;
                }
                prevScale = -1;
                prevRot = null;
                if (srChangeListener != null) {
                    cell.removeTransformChangeListener(srChangeListener);
                }
                break;

            case VISIBLE:
                if (increasing) {
                    if (listener == null) {
                        try {
                            //Attach a click listener
                            CellRenderer cellRenderer
                                    = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                            CellRendererJME renderer
                                    = (CellRendererJME) cellRenderer;
                            Entity entity = renderer.getEntity();
                            listener = new MouseEventListener();
                            listener.addToEntity(entity);
                            //LOGGER.warning("LISTENER ADDED...");
                        } catch (NullPointerException npe) {
                            //LOGGER.warning("LISTENER NOT ADDED...");
                            LOGGER.log(Level.INFO,
                                    "could not attach click listener", npe);
                        }
                    }

                    if (altKeyListener == null) {
                        //Attach a alt click listener
                        CellRenderer cellRenderer
                                = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                        CellRendererJME renderer
                                = (CellRendererJME) cellRenderer;
                        Entity entity = renderer.getEntity();
                        altKeyListener = new AltKeyListener(this);
                        altKeyListener.addToEntity(entity);
                    }

                    //object rotation
                    Quaternion objq = cell.getWorldTransform().getRotation(null);
                    float oangles[] = new float[3];
                    objq.toAngles(oangles);
                    oangles[0] = (float) Math.toDegrees(oangles[0]);
                    oangles[1] = (float) Math.toDegrees(oangles[1]);
                    oangles[2] = (float) Math.toDegrees(oangles[2]);

                    // When we import object using .wlexport, the rotation get changed. 
                    // So update the offset values.
                    if (lookDirX != oangles[1] && lookDirX != 999) {
                        Quaternion prevQ = new Quaternion(new float[]{0, (float) Math.toRadians(lookDirX), 0});
                        rotationChanged(prevQ, objq);
                        updateValues();
                    }
                    lookDirX = oangles[1];

                    if (msgReceiver == null) {
                        msgReceiver = new ChannelComponent.ComponentMessageReceiver() {
                            public void messageReceived(CellMessage message) {
                                receive(message);
                            }
                        };
                        channelComp = cell.getComponent(ChannelComponent.class);
                        channelComp.addMessageReceiver(NavigationChangeMessage.class, msgReceiver);
                    }
                    //add transform change listener for ditacting scale up/down
                    prevScale = cell.getWorldTransform().getScaling();
                    prevRot = cell.getWorldTransform().getRotation(null);
                    if (srChangeListener == null) {
                        srChangeListener = new ScaleRotationChangeListener();
                    }
                    cell.addTransformChangeListener(srChangeListener);
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

        //trigger is set to right click
        if (trigger == 1) {
            //check if avatar is already in Navigate-To state
            if (prevNavigateCell != null) {
                if (cell.getCellID().equals(prevNavigateCell)) {
                    if ((ViewManager.getViewManager().getCameraController() instanceof BestViewCam)) {
                        return;
                    }
                }
            }
            if (!goToRunning) {
                isMoving = true;
                prevCC = ViewManager.getViewManager().getCameraController();
                cellTrans = cell.getWorldTransform();
                genEvent = null;
                //move avatar
                moveAvatar(event.getCell().getWorldTransform().getTranslation(null));
            }
        }

    }

    private void moveAvatar(Vector3f objPos) {

        //calculate goal position
        Vector3f goalPos = new Vector3f();
        goalPos.setX((objPos.getX() - offsetX));
        goalPos.setY(objPos.getY() - offsetY);
        goalPos.setZ((objPos.getZ() - offsetZ));
        float diff = 0;
        ggoalPos = new CellTransform();
        ggoalPos.setTranslation(goalPos);
        //convert angle into vector from degree
        float angle = lookDirY - diff;
        Quaternion q2 = new Quaternion(new float[]{0, (float) Math.toRadians(angle), 0});
        float[] f1 = new float[3];
        q2.toAngles(f1);
        Quaternion my1 = new Quaternion();
        my1.fromAngles(f1);
        myvec = CellPlacementUtils.getLookDirection(my1, null);
        ggoalPos.setRotation(q2);

        Cell avatarCell = ClientContextJME.getViewManager().getPrimaryViewCell();
        CellRenderer rend = avatarCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        WlAvatarCharacter myAvatar = ((AvatarImiJME) rend).getAvatarCharacter();
        GameContext context = myAvatar.getContext();
        helm = context.getBehaviorManager();

        //check if avatar is already on goal position then just change the camera to best view
        Vector3f v1 = goalPos;
        Vector3f v2 = avatarCell.getWorldTransform().getTranslation(null);
        float currentDistanceFromGoal = new Vector3f(v1.x, 0f, v1.z).distance(new Vector3f(v2.x, 0f, v2.z));
        float approvedDistanceFromGoal = 1.0f;
        int flg = 0;
        float directionSensitivity = 0.1f;
        Vector3f rightVec = context.getController().getRightVector();
        float dot = myvec.dot(rightVec);

        if (dot > directionSensitivity) {
            flg = 1;
        } else if (dot < -directionSensitivity) {
            flg = 1;
        } else if (isBehind(myvec)) {
            flg = 1;
        } else {
            flg = 0;
        }
        if ((currentDistanceFromGoal <= approvedDistanceFromGoal) && flg == 0) {
            try {
                ClientContextJME.getClientMain().gotoLocation(null, new Vector3f(ggoalPos.getTranslation(null).x, avatarCell.getWorldTransform().getTranslation(null).y, ggoalPos.getTranslation(null).z), ggoalPos.getRotation(null));
            } catch (IOException ex) {
                Logger.getLogger(NavigateToComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
            //if best view is enable, switch camera to best view
            if (bestview) {
                BestViewComponent bvc = parentCell.getComponent(BestViewComponent.class);
                if (bvc != null) {
                    Vector3f cellPos = cellTrans.getTranslation(null);

                    if (bvc.offsetX == 0 && bvc.offsetY == 0 && bvc.offsetZ == 0 && bvc.lookDirX == 0 && bvc.lookDirY == 0
                            && bvc.lookDirZ == 0 && bvc.lookDirW == 0) {
                        CellTransform xform = BestViewUtils.getBestView(parentCell, cellTrans);
                        Quaternion quat = cellTrans.getRotation(null);
                        float ff[] = new float[3];
                        quat.toAngles(ff);

                        bvc.oldObjPosX = (float) Math.toDegrees(ff[0]);
                        bvc.oldObjPosY = (float) Math.toDegrees(ff[1]);
                        bvc.oldObjPosZ = (float) Math.toDegrees(ff[2]);

                        bvc.offsetX = cellPos.x - xform.getTranslation(null).x;
                        bvc.offsetY = cellPos.y - xform.getTranslation(null).y;
                        bvc.offsetZ = cellPos.z - xform.getTranslation(null).z;

                        bvc.lookDirX = xform.getRotation(null).x;
                        bvc.lookDirY = xform.getRotation(null).y;
                        bvc.lookDirZ = xform.getRotation(null).z;
                        bvc.lookDirW = xform.getRotation(null).w;
                    }

                    Vector3f camarePos = new Vector3f((cellPos.x - bvc.offsetX), cellPos.y - bvc.offsetY, (cellPos.z - bvc.offsetZ));
                    Quaternion cameraRot = new Quaternion(bvc.lookDirX, bvc.lookDirY, bvc.lookDirZ, bvc.lookDirW);

                    //code for changing offset after object rotation    
                    Quaternion cellQ1 = cellTrans.getRotation(null);
                    float angles1[] = new float[3];
                    cellQ1.toAngles(angles1);
                    angles1[0] = (float) Math.toDegrees(angles1[0]);
                    angles1[1] = (float) Math.toDegrees(angles1[1]);
                    angles1[2] = (float) Math.toDegrees(angles1[2]);
                    Vector3f offsetVec1 = new Vector3f(bvc.offsetX, bvc.offsetY, bvc.offsetZ);

                    float diffX = 0;
                    float diffY = 0;
                    float diffZ = 0;
                    if (bvc.oldObjPosY != 999) {
                        diffX = bvc.oldObjPosX - angles1[0];
                        diffY = bvc.oldObjPosY - angles1[1];
                        diffZ = bvc.oldObjPosZ - angles1[2];
                    }

                    if (Math.abs(diffY) > 0.1) {
                        Quaternion q = new Quaternion(new float[]{(float) Math.toRadians(-diffX), (float) Math.toRadians(-diffY), (float) Math.toRadians(-diffZ)});
                        offsetVec1 = q.mult(offsetVec1);
                        camarePos.setX((cellPos.getX() - offsetVec1.getX()));
                        camarePos.setY(cellPos.getY() - offsetVec1.getY());
                        camarePos.setZ((cellPos.getZ() - offsetVec1.getZ()));
                    }

                    Quaternion q21 = null;
                    if (Math.abs(diffY) > 0.1) {
                        float ang[] = new float[3];
                        cameraRot.toAngles(ang);
                        float angleX = (float) Math.toDegrees(ang[0]) - diffX;
                        float angleY = (float) Math.toDegrees(ang[1]) - diffY;
                        float angleZ = (float) Math.toDegrees(ang[2]) - diffZ;
                        q21 = new Quaternion(new float[]{(float) Math.toRadians(angleX), (float) Math.toRadians(angleY), (float) Math.toRadians(angleZ)});
                        float[] f11 = new float[3];
                        q21.toAngles(f11);
                        Quaternion my11 = new Quaternion();
                        my11.fromAngles(f11);
                        cameraRot = my11;

                    }

                    if (Math.abs(diffY) > 0.1) {

                        //change values and send msg to server
                        bvc.oldObjPosX = angles1[0];
                        bvc.oldObjPosY = angles1[1];
                        bvc.oldObjPosZ = angles1[2];
                        bvc.lookDirX = q2.x;
                        bvc.lookDirY = q2.y;
                        bvc.lookDirZ = q2.z;
                        bvc.lookDirW = q2.w;
                        bvc.offsetX = offsetVec1.getX();
                        bvc.offsetY = offsetVec1.getY();
                        bvc.offsetZ = offsetVec1.getZ();
                        BestViewServerState bvss = new BestViewServerState();
                        bvss.setLookDirW(bvc.lookDirW);
                        bvss.setLookDirX(bvc.lookDirX);
                        bvss.setLookDirY(bvc.lookDirY);
                        bvss.setLookDirZ(bvc.lookDirZ);
                        bvss.setOffsetX(bvc.offsetX);
                        bvss.setOffsetY(bvc.offsetY);
                        bvss.setOffsetZ(bvc.offsetZ);
                        bvss.setOldObjPosX(bvc.oldObjPosX);
                        bvss.setOldObjPosY(bvc.oldObjPosY);
                        bvss.setOldObjPosZ(bvc.oldObjPosZ);
                        bvss.setTrigger(bvc.trigger);
                        bvss.setZoom(bvc.zoom);
                        BestViewChangeMessage msg = new BestViewChangeMessage(cell.getCellID(), bvss);
                        cell.sendCellMessage(msg);
                    }

                    //create best view cam
                    bestViewCam = new BestViewCam(ViewManager.getViewManager().getCameraTransform(), new CellTransform(cameraRot, camarePos),
                            0f, 1500, prevCC, genEvent, cell);
                }
                ClientContextJME.getViewManager().setCameraController(bestViewCam);
                status = 0;
                prevNavigateCell = cell.getCellID();
                goToRunning = false;
                return;
            } else {
                enableCapabilityListeners();
                goToRunning = false;
                return;
            }
        }

        if (!bestview) {
            //add avatar transfor listener to enable collision
            atcl1 = new AvatarTransformChangeListener1(cell);
            avatarCell.addTransformChangeListener(atcl1);
        }

        //disable collision
        MainFrameImpl frame = (MainFrameImpl) JmeClientMain.getFrame();
        JRootPane jrp = (JRootPane) frame.getComponent(0);
        JMenuBar jmb = jrp.getJMenuBar();
        JMenu jm = null;
        int index = findMenu(jmb, "Tools");
        if (index != -1) {
            jm = jmb.getMenu(index);
            LOGGER.log(Level.INFO, "{0} index {1}", new Object[]{jm.getText(), index});

            index = findMenu(jm, "Collision Enabled");
            if (index != -1) {
                collisionResponseEnabledMI = (JCheckBoxMenuItem) jm.getSubElements()[0].getSubElements()[index];
                //collisionResponseEnabledMI = (JCheckBoxMenuItem) jm.getComponent(index);
                LOGGER.log(Level.INFO, "{0} index {1}", new Object[]{collisionResponseEnabledMI.getText(), index});
            }

            index = findMenu(jm, "Gravity Enabled");
            if (index != -1) {
                gravityEnabledMI = (JCheckBoxMenuItem) jm.getSubElements()[0].getSubElements()[index];
                //gravityEnabledMI = (JCheckBoxMenuItem) jm.getComponent(index);
                LOGGER.log(Level.INFO, "{0} index {1}", new Object[]{gravityEnabledMI.getText(), index});
            }
        }
        /**
         * If the "Tools" menu is not present in the frame.
         */
        if (collisionResponseEnabledMI != null) {
            collision = collisionResponseEnabledMI.isSelected();
            gravity = gravityEnabledMI.isSelected();
            ClientContext.getInputManager().postEvent(new AvatarCollisionChangeRequestEvent(false, false));
            collisionResponseEnabledMI.setSelected(false);
            gravityEnabledMI.setSelected(false);
        }
        helm.setEnable(true);
        //create GoTo task
        gt = new NewGoTo(goalPos, null, (AvatarContext) context);

        helm.addTaskToTop(gt);

        //store cell id on which navigate to is performed
        prevNavigateCell = cell.getCellID();

        if ((currentDistanceFromGoal <= approvedDistanceFromGoal)) {
            ses3 = Executors.newSingleThreadScheduledExecutor();
            ses3.scheduleAtFixedRate(new Runnable3(), 0, 1000, TimeUnit.MILLISECONDS);
        } else {
            ses1 = Executors.newSingleThreadScheduledExecutor();
            ses1.scheduleAtFixedRate(new Runnable1(), 0, 100, TimeUnit.MILLISECONDS);
        }

    }

    /**
     * @param jBar
     * @param menuText
     * @return index of the menu; -1 if menu is not found.
     */
    private int findMenu(Object jComponent, String menuText) {

        if (jComponent instanceof JMenuBar) {
            JMenuBar jBar = (JMenuBar) jComponent;
            JMenu jm;
            for (int i = 0; i < jBar.getMenuCount(); i++) {
                jm = jBar.getMenu(i);
                if (jm.getText().equals(menuText)) {
                    LOGGER.log(Level.INFO, "{0} index {1}", new Object[]{menuText, i});
                    return i;
                }
            }
        } else if (jComponent instanceof JMenu) {
            JMenu jMenu = (JMenu) jComponent;
            JMenuItem jMenuItem;
            for (int i = 0; i < jMenu.getMenuComponentCount(); i++) {
                jMenuItem = jMenu.getItem(i);
                if (jMenuItem.getText().equals(menuText)) {
                    LOGGER.log(Level.INFO, "{0} index {1}", new Object[]{menuText, i});
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean isBehind(Vector3f direction) {
        // Check if this direction is outside the front half space
        Cell avatarCell = ClientContextJME.getViewManager().getPrimaryViewCell();
        CellRenderer rend = avatarCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        WlAvatarCharacter myAvatar = ((AvatarImiJME) rend).getAvatarCharacter();
        GameContext context = myAvatar.getContext();
        Vector3f fwdVec = context.getController().getForwardVector().mult(-1.0f); // forward is reversed!
        float frontHalfDot = direction.dot(fwdVec);
        return frontHalfDot < 0.0f;
    }

    public void doAfterAvatarReaches() {
        prevCC = ViewManager.getViewManager().getCameraController();
        Cell avatarCell1 = ClientContextJME.getViewManager().getPrimaryViewCell();
        CellRenderer rend = avatarCell1.getCellRenderer(Cell.RendererType.RENDERER_JME);
        WlAvatarCharacter myAvatar = ((AvatarImiJME) rend).getAvatarCharacter();
        AvatarContext context = (AvatarContext) myAvatar.getContext();

        context.triggerReleased(AvatarContext.TriggerNames.Move_Right.ordinal());
        context.triggerReleased(AvatarContext.TriggerNames.Move_Left.ordinal());

        Vector3f cellPos = cellTrans.getTranslation(null);
        BestViewComponent bvc = parentCell.getComponent(BestViewComponent.class);
        if (bvc != null) {

            //if initial values
            if (bvc.offsetX == 0 && bvc.offsetY == 0 && bvc.offsetZ == 0 && bvc.lookDirX == 0 && bvc.lookDirY == 0
                    && bvc.lookDirZ == 0 && bvc.lookDirW == 0) {
                CellTransform xform = BestViewUtils.getBestView(parentCell, cellTrans);
                Quaternion quat = cellTrans.getRotation(null);
                float ff[] = new float[3];
                quat.toAngles(ff);
                bvc.oldObjPosX = (float) Math.toDegrees(ff[0]);
                bvc.oldObjPosY = (float) Math.toDegrees(ff[1]);
                bvc.oldObjPosZ = (float) Math.toDegrees(ff[2]);
                bvc.offsetX = cellPos.x - xform.getTranslation(null).x;
                bvc.offsetY = cellPos.y - xform.getTranslation(null).y;
                bvc.offsetZ = cellPos.z - xform.getTranslation(null).z;
                bvc.lookDirX = xform.getRotation(null).x;
                bvc.lookDirY = xform.getRotation(null).y;
                bvc.lookDirZ = xform.getRotation(null).z;
                bvc.lookDirW = xform.getRotation(null).w;
            }

            Vector3f camarePos = new Vector3f((cellPos.x - bvc.offsetX), cellPos.y - bvc.offsetY, (cellPos.z - bvc.offsetZ));
            Quaternion cameraRot = new Quaternion(bvc.lookDirX, bvc.lookDirY, bvc.lookDirZ, bvc.lookDirW);

            //code for changing offset after object rotation    
            Quaternion cellQ = cellTrans.getRotation(null);
            float angles[] = new float[3];
            cellQ.toAngles(angles);
            angles[0] = (float) Math.toDegrees(angles[0]);
            angles[1] = (float) Math.toDegrees(angles[1]);
            angles[2] = (float) Math.toDegrees(angles[2]);
            Vector3f offsetVec = new Vector3f(bvc.offsetX, bvc.offsetY, bvc.offsetZ);
            float diffX = 0;
            float diff = 0;
            float diffZ = 0;

            if (bvc.oldObjPosY != 999) {
                diffX = bvc.oldObjPosX - angles[0];
                diff = bvc.oldObjPosY - angles[1];
                diffZ = bvc.oldObjPosZ - angles[2];
            }
            if (Math.abs(diff) > 0.1) {
                Quaternion q = new Quaternion(new float[]{(float) Math.toRadians(-diffX), (float) Math.toRadians(-diff), (float) Math.toRadians(-diffZ)});
                offsetVec = q.mult(offsetVec);
                camarePos.setX((cellPos.getX() - offsetVec.getX()));
                camarePos.setY(cellPos.getY() - offsetVec.getY());
                camarePos.setZ((cellPos.getZ() - offsetVec.getZ()));
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
                bvc.oldObjPosX = angles[0];
                bvc.oldObjPosY = angles[1];
                bvc.oldObjPosZ = angles[2];
                bvc.lookDirX = q2.x;
                bvc.lookDirY = q2.y;
                bvc.lookDirZ = q2.z;
                bvc.lookDirW = q2.w;
                bvc.offsetX = offsetVec.getX();
                bvc.offsetY = offsetVec.getY();
                bvc.offsetZ = offsetVec.getZ();
                BestViewServerState bvss = new BestViewServerState();
                bvss.setLookDirW(bvc.lookDirW);
                bvss.setLookDirX(bvc.lookDirX);
                bvss.setLookDirY(bvc.lookDirY);
                bvss.setLookDirZ(bvc.lookDirZ);
                bvss.setOffsetX(bvc.offsetX);
                bvss.setOffsetY(bvc.offsetY);
                bvss.setOffsetZ(bvc.offsetZ);
                bvss.setOldObjPosX(bvc.oldObjPosX);
                bvss.setOldObjPosY(bvc.oldObjPosY);
                bvss.setOldObjPosZ(bvc.oldObjPosZ);
                bvss.setTrigger(bvc.trigger);
                bvss.setZoom(bvc.zoom);
                BestViewChangeMessage msg = new BestViewChangeMessage(cell.getCellID(), bvss);
                cell.sendCellMessage(msg);

            }

            //create best view camera
            bestViewCam = new BestViewCam(ViewManager.getViewManager().getCameraTransform(), new CellTransform(cameraRot, camarePos),
                    0f, 1500, prevCC, genEvent, cell);

            //setting previous values of collision and gravity
            MainFrameImpl frame = (MainFrameImpl) JmeClientMain.getFrame();
            Component[] comps = frame.getComponents();
            JRootPane jrp = (JRootPane) frame.getComponent(0);
            JMenuBar jmb = jrp.getJMenuBar();
            //JMenu jm = jmb.getMenu(5);
            JMenu jm = null;
            int index = findMenu(jmb, "Tools");
            if (index != -1) {
                jm = jmb.getMenu(index);
                LOGGER.log(Level.INFO, "{0} index {1}", new Object[]{jm.getText(), index});
                index = findMenu(jm, "Collision Enabled");
                if (index != -1) {
                    collisionResponseEnabledMI = (JCheckBoxMenuItem) jm.getSubElements()[0].getSubElements()[index];
                    LOGGER.log(Level.INFO, "{0} index {1}", new Object[]{collisionResponseEnabledMI.getText(), index});
                }
                index = findMenu(jm, "Gravity Enabled");
                if (index != -1) {
                    gravityEnabledMI = (JCheckBoxMenuItem) jm.getSubElements()[0].getSubElements()[index];
                    LOGGER.log(Level.INFO, "{0} index {1}", new Object[]{gravityEnabledMI.getText(), index});
                }
            }

            /**
             * If the "Tools" menu is not present in the frame.
             */
            if (collisionResponseEnabledMI != null) {
                ClientContext.getInputManager().postEvent(new AvatarCollisionChangeRequestEvent(collision, gravity));
                collisionResponseEnabledMI.setSelected(collision);
                gravityEnabledMI.setSelected(gravity);
            }

            
            //switch camera to best view
            if (bestview) {
                new Thread(new Runnable() {
                    public void run() {
                        LOGGER.info("Switch camera to best view...");
                        ClientContextJME.getViewManager().setCameraController(bestViewCam);
                    }
                }).start();
            } else {
                enableCapabilityListeners();
            }
            goToRunning = false;
        }
    }

    /**
     * Enable the listeners of other capabilities.
     */
    private void enableCapabilityListeners() {

        //if there is best view component in cell then pass
        //to it to enable all other components
        BestViewComponent bvc = cell.getComponent(BestViewComponent.class);
        if (bvc != null && bestview) {
           bvc.getMouseEventListener().setEnabled(true);
           bvc.getMouseEventListener().commitEvent(genEvent);
           genEvent = null;
        } else {
            if (genEvent != null) {
                for (CellComponent comp : cell.getComponents()) {
                    if (comp instanceof CapabilityBridge) {
                        CapabilityBridge bridge = (CapabilityBridge) comp;
                        EventClassListener listener = bridge.getMouseEventListener();
                        if (listener != null) {
                            listener.setEnabled(true);
                            listener.commitEvent(genEvent);
                        }
                    }
                }
                if(cell.getParent()!=null) {
                    for (CellComponent comp : cell.getParent().getComponents()) {
                        if (comp instanceof CapabilityBridge) {
                            CapabilityBridge bridge = (CapabilityBridge) comp;
                            EventClassListener listener = bridge.getMouseEventListener();
                            if (listener != null) {
                                listener.setEnabled(true);
                                listener.commitEvent(genEvent);
                            }
                        }
                    }
                }
            }
            genEvent = null;
        }
    }

    public class AvatarTransformChangeListener1 implements TransformChangeListener {

        Cell parentCell;

        public AvatarTransformChangeListener1(Cell parentCell) {
            this.parentCell = parentCell;
        }

        public void transformChanged(Cell cell, ChangeSource source) {
            if (isMoving) {
                if (gt.verify() == false) {

                    Cell avatarCell1 = ClientContextJME.getViewManager().getPrimaryViewCell();
                    CellRenderer rend = avatarCell1.getCellRenderer(Cell.RendererType.RENDERER_JME);
                    WlAvatarCharacter myAvatar = ((AvatarImiJME) rend).getAvatarCharacter();
                    AvatarContext context = (AvatarContext) myAvatar.getContext();
                    context.triggerReleased(AvatarContext.TriggerNames.Move_Right.ordinal());
                    context.triggerReleased(AvatarContext.TriggerNames.Move_Left.ordinal());

                    //enable collision
                    MainFrameImpl frame = (MainFrameImpl) JmeClientMain.getFrame();
                    Component[] comps = frame.getComponents();
                    JRootPane jrp = (JRootPane) frame.getComponent(0);
                    JMenuBar jmb = jrp.getJMenuBar();
                    JMenu jm = jmb.getMenu(5);
                    collisionResponseEnabledMI = (JCheckBoxMenuItem) jm.getSubElements()[0].getSubElements()[2];
                    gravityEnabledMI = (JCheckBoxMenuItem) jm.getSubElements()[0].getSubElements()[3];
                    ClientContext.getInputManager().postEvent(new AvatarCollisionChangeRequestEvent(collision, gravity));
                    collisionResponseEnabledMI.setSelected(collision);
                    gravityEnabledMI.setSelected(gravity);

                    //enableCapabilityListeners();
                    Cell avatarCell = ClientContextJME.getViewManager().getPrimaryViewCell();
                    avatarCell.removeTransformChangeListener(this);
                }
            }
        }
    }

    class MouseEventListener extends EventClassListener {

        @Override
        public Class[] eventClassesToConsume() {

            return new Class[]{MouseButtonEvent3D.class};
        }

        @Override
        public boolean consumesEvent(Event event) {

            //if left click then check if other capabilities are triggered on lift click or not.
            //if yes then temporary disable their listener.
            if (event instanceof MouseButtonEvent3D) {
                MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
                if (mbe.isClicked() == true && mbe.getButton() == MouseEvent3D.ButtonId.BUTTON1) {
                    if (trigger == 0) {
                        if (prevNavigateCell != null) {
                            if (cell.getCellID().equals(prevNavigateCell)) {
                                if ((ViewManager.getViewManager().getCameraController() instanceof BestViewCam)) {
                                    return false;
                                }
                            }
                        }

                        //iterate to all components to this cell and see if any one
                        //is of type CapabilityBridge. If it is then disable its
                        //mouse listener.
                        for (CellComponent comp : cell.getComponents()) {
                            if (comp instanceof CapabilityBridge) {
                                CapabilityBridge bridge = (CapabilityBridge) comp;
                                EventClassListener listener = bridge.getMouseEventListener();
                                if (listener != null) {
                                    listener.setEnabled(false);
                                }
                            }
                        }
                        if(cell.getParent()!=null) {
                            for (CellComponent comp : cell.getParent().getComponents()) {
                                if (comp instanceof CapabilityBridge) {
                                    CapabilityBridge bridge = (CapabilityBridge) comp;
                                    EventClassListener listener = bridge.getMouseEventListener();
                                    if (listener != null) {
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
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;

            //Make sure its a click!
            if (mbe.isClicked() == true && mbe.getButton() == MouseEvent3D.ButtonId.BUTTON1) {
                //if trigger is set to left click
                if (trigger == 0) {
                    LOGGER.info("TRIGGER IS ON LEFT CLICK...");
                    //check if user tries to use ez move functionality or not
                    //so check if alt is already pressed
                    //if pressed then dont set bestview camera
                    if (!isAltPressed()) {
                        if (prevNavigateCell != null) {
                            if (cell.getCellID().equals(prevNavigateCell)) {
                                if ((ViewManager.getViewManager().getCameraController() instanceof BestViewCam)) {
                                    return;
                                }
                            }
                        }
                        if (!goToRunning) {
                            //move avatar
                            goToRunning = true;
                            isMoving = true;
                            Entity ee = event.getEntity();
                            CellRefComponent crc = ee.getComponent(CellRefComponent.class);
                            Cell cell = crc.getCell();
                            prevCC = ViewManager.getViewManager().getCameraController();
                            cellTrans = parentCell.getWorldTransform();
                            moveAvatar(cell.getWorldTransform().getTranslation(null));
                        }
                    } else {
                        setIsAltPressed(false);
                    }
                }
            }
        }
    }

    public class Runnable1 implements Runnable {

        public void run() {
            Cell avatarCell = ClientContextJME.getViewManager().getPrimaryViewCell();
            CellRenderer rend = avatarCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
            WlAvatarCharacter myAvatar = ((AvatarImiJME) rend).getAvatarCharacter();
            GameContext context = myAvatar.getContext();
            CharacterBehaviorManager helm = context.getBehaviorManager();

            if (helm.getCurrentTask() instanceof NewGoTo) {
                LOGGER.info("GoTo Task started...");
                //LOGGER.warning("GoTo Task started...");
                ses2 = Executors.newSingleThreadScheduledExecutor();
                ses2.scheduleAtFixedRate(new Runnable2(), 0, 1, TimeUnit.SECONDS);
                ses1.shutdown();
            }
        }
    }

    public class Runnable2 implements Runnable {

        public void run() {
            Cell avatarCell = ClientContextJME.getViewManager().getPrimaryViewCell();
            CellRenderer rend = avatarCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
            WlAvatarCharacter myAvatar = ((AvatarImiJME) rend).getAvatarCharacter();
            GameContext context = myAvatar.getContext();
            final CharacterBehaviorManager helm = context.getBehaviorManager();
            if (helm.getCurrentTask() == null) {
                LOGGER.info("Avatar reached to goal postion...");
                ses3 = Executors.newSingleThreadScheduledExecutor();
                ses3.scheduleAtFixedRate(new Runnable3(), 500, 1000, TimeUnit.MILLISECONDS);
                ses2.shutdown();
            }
        }
    }

    public class Runnable3 implements Runnable {

        public void run() {
            LOGGER.info("Initialize Turn To started......");
            //LOGGER.warning("Initialize Turn To started......");
            Cell avatarCell = ClientContextJME.getViewManager().getPrimaryViewCell();
            CellRenderer rend = avatarCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
            WlAvatarCharacter myAvatar = ((AvatarImiJME) rend).getAvatarCharacter();
            GameContext context = myAvatar.getContext();
            final CharacterBehaviorManager helm = context.getBehaviorManager();
            tt = new TurnTo(myvec);
            helm.addTaskToTop(tt);
            ses4 = Executors.newSingleThreadScheduledExecutor();
            ses4.scheduleAtFixedRate(new Runnable4(), 0, 1, TimeUnit.SECONDS);
            ses3.shutdown();
        }
    }

    public class Runnable4 implements Runnable {

        public void run() {
            Cell avatarCell = ClientContextJME.getViewManager().getPrimaryViewCell();
            CellRenderer rend = avatarCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
            WlAvatarCharacter myAvatar = ((AvatarImiJME) rend).getAvatarCharacter();
            GameContext context = myAvatar.getContext();
            final CharacterBehaviorManager helm = context.getBehaviorManager();

            if (helm.getCurrentTask() == null && (!tt.verify())) {
                LOGGER.info("Avatar reached to goal direction...");
                //LOGGER.warning("Avatar reached to goal direction...");
                LOGGER.info("Correct the position...");
                try {

                    ClientContextJME.getClientMain().gotoLocation(null, new Vector3f(ggoalPos.getTranslation(null).x, avatarCell.getWorldTransform().getTranslation(null).y, ggoalPos.getTranslation(null).z), ggoalPos.getRotation(null));
                } catch (IOException ex) {
                    Logger.getLogger(NavigateToComponent.class.getName()).log(Level.SEVERE, null, ex);
                }
                //doAfterAvatarReaches();
                ses6 = Executors.newSingleThreadScheduledExecutor();
                ses6.scheduleAtFixedRate(new Runnable5(), 500, 1000, TimeUnit.MILLISECONDS);
                ses5.shutdown();
            }
        }
    }

    public class Runnable5 implements Runnable {

        public void run() {
            LOGGER.info("doAfterAvatarReaches...");
            doAfterAvatarReaches();
            ses6.shutdown();
        }
    }

    /*
     * listener for changing offset values when rotation/scaling change.
     */
    private class ScaleRotationChangeListener implements TransformChangeListener {

        public void transformChanged(Cell cell, ChangeSource source) {
            boolean needUpdate = false;
            float prevS = prevScale;
            float currS = cell.getWorldTransform().getScaling();
            Quaternion prevR = prevRot;
            Quaternion currR = cell.getWorldTransform().getRotation(null);
            //check if rotation changed
            if (!prevR.equals(currR)) {
                needUpdate = true;
                rotationChanged(prevR, currR);
            }
            //check if scaling changed
            if (prevS != currS && currS != 0) {
                needUpdate = true;
                scaleChanged(currS);
            }
            if (needUpdate) {
                updateValues();
            }
        }
    }

    private void updateValues() {
        //update the values
        NavigateToServerState ntcs = new NavigateToServerState();
        ntcs.setBestView(bestview);
        ntcs.setLookDirX(lookDirX);
        ntcs.setLookDirY(lookDirY);
        ntcs.setLookDirZ(lookDirZ);
        ntcs.setOffsetX(offsetX);
        ntcs.setOffsetY(offsetY);
        ntcs.setOffsetZ(offsetZ);
        ntcs.setTrigger(trigger);
        NavigationChangeMessage msg = new NavigationChangeMessage(cell.getCellID(), ntcs);
        cell.sendCellMessage(msg);
    }

    private void scaleChanged(float currS) {
        DecimalFormat df = new DecimalFormat("#.00");
        LOGGER.warning("Scaling changed. Update the offset values.");
        LOGGER.log(Level.INFO, "prevScale : {0}", prevScale);
        LOGGER.log(Level.INFO, "currScale : {0}", df.format(cell.getWorldTransform().getScaling()));

        if (cell.getWorldBounds() instanceof BoundingBox) {
            LOGGER.log(Level.INFO, "BOX...");
            LOGGER.log(Level.INFO, "prevXOff < {0}", offsetX);
            LOGGER.log(Level.INFO, "prevZOff < {0}", offsetZ);
            offsetX = (offsetX / prevScale) * cell.getWorldTransform().getScaling();
            offsetZ = (offsetZ / prevScale) * cell.getWorldTransform().getScaling();
            LOGGER.log(Level.INFO, "newXOff < {0}", offsetX);
            LOGGER.log(Level.INFO, "newZOff < {0}", offsetZ);
        } else {
            LOGGER.log(Level.INFO, "SPHERE...");
            LOGGER.log(Level.INFO, "prevXOff < {0}", offsetX);
            LOGGER.log(Level.INFO, "prevZOff < {0}", offsetZ);
            offsetX = (offsetX / prevScale) * cell.getWorldTransform().getScaling();
            LOGGER.log(Level.INFO, "newXOff < {0}", offsetX);
            LOGGER.log(Level.INFO, "newZOff < {0}", offsetZ);
        }
        prevScale = currS;
    }

    private void rotationChanged(Quaternion prevR, Quaternion currR) {

        LOGGER.warning("Rotation changed. Update the offset values.");
        LOGGER.log(Level.INFO, "prevR : {0}", prevRot);
        LOGGER.log(Level.INFO, "currR : {0}", currR);

        float[] oldAngles = new float[3];
        float[] newAngles = new float[3];
        prevR.toAngles(oldAngles);
        currR.toAngles(newAngles);
        oldAngles[0] = (float) Math.toDegrees(oldAngles[0]);
        oldAngles[1] = (float) Math.toDegrees(oldAngles[1]);
        oldAngles[2] = (float) Math.toDegrees(oldAngles[2]);
        newAngles[0] = (float) Math.toDegrees(newAngles[0]);
        newAngles[1] = (float) Math.toDegrees(newAngles[1]);
        newAngles[2] = (float) Math.toDegrees(newAngles[2]);
        LOGGER.log(Level.INFO, "oldAngles : {0}", oldAngles[1]);
        LOGGER.log(Level.INFO, "newAngles : {0}", newAngles[1]);
        float diff = oldAngles[1] - newAngles[1];
        lookDirY = lookDirY - diff;
        LOGGER.log(Level.INFO, "lookDirY : {0}", lookDirY);
        LOGGER.log(Level.INFO, "diff : {0}", diff);
        //calculate new offset
        Vector3f oldOffsets = new Vector3f(offsetX, offsetY, offsetZ);
        Quaternion q = new Quaternion(new float[]{0f, (float) Math.toRadians(-diff), 0f});
        oldOffsets = q.mult(oldOffsets);
        offsetX = oldOffsets.getX();
        offsetY = oldOffsets.getY();
        offsetZ = oldOffsets.getZ();
        lookDirX = newAngles[1];
        prevRot = currR;
    }

    public void setIsAltPressed(boolean isAltPressed) {
        this.isAltPressed = isAltPressed;
    }

    public boolean isAltPressed() {
        return isAltPressed;
    }
}
