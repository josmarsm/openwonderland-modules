/**
 * Copyright (c) 2013, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.triggergesture.client;

import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.wonderbuilders.modules.capabilitybridge.client.CapabilityBridge;
import com.wonderbuilders.modules.triggergesture.common.TriggerGestureComponentClientState;
import com.wonderbuilders.modules.triggergesture.common.TriggerGestureComponentServerState.Trigger;
import com.wonderbuilders.modules.triggergesture.common.TriggerGestureMessage;
import imi.character.CharacterEyes;
import imi.character.avatar.Avatar;
import imi.character.avatar.AvatarContext;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellStatus;
import static org.jdesktop.wonderland.common.cell.CellStatus.ACTIVE;
import static org.jdesktop.wonderland.common.cell.CellStatus.DISK;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;

/**
 * trigger gesture component
 * this will listen for the events like mouse left & right clicks and proximity enter & exit
 * when any of the above mentioned events occur, it will trigger avatar gesture that user has set.
 * 
 * @author Abhishek Upadhyay.
 */
public class TriggerGestureComponent extends CellComponent implements 
        AvatarImiJME.AvatarChangedListener,CapabilityBridge {
    
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com/wonderbuilders/modules"
            + "/triggergesture/client/resources/Bundle");
    private static final Logger logger = Logger.getLogger(TriggerGestureComponent.class.getName());
    
    @UsesCellComponent
    private ContextMenuComponent contextMenu;
    @UsesCellComponent
    public ProximityComponent proximity;
    private ContextMenuFactorySPI ctxMenuFactory;
    private TriggerGestureContextMenuListener contextMenuListener;
    private TriggerGestureMouseListener mouseListener;
    private TriggerGestureProximityListener proximityListener;
    //map to store all animations of avatar
    private static Map<String, String> gestureMap = new HashMap<String, String>();
    private ChannelComponent channel = null;
    private TriggerGestureMessageReceiver msgReceiver = null;
   
    private Trigger trigger = Trigger.LEFT_CLICK;
    private String gesture = BUNDLE.getString("AnswerCell");
    private String contextMenuName = "Trigger Gesture";
    private int radius = 3;
    
    public TriggerGestureComponent(Cell cell) {
        super(cell);
    }
    
    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
        trigger = ((TriggerGestureComponentClientState)clientState).getTrigger();
        gesture = ((TriggerGestureComponentClientState)clientState).getGesture();
        contextMenuName = ((TriggerGestureComponentClientState)clientState).getContextMenuName();
        radius = ((TriggerGestureComponentClientState)clientState).getRadius();
    }
    
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        System.out.println("status : "+status.name());
        switch (status) {
            
            case ACTIVE:
                break;
            case RENDERING:
                break;
            case VISIBLE:
                //add this class as avatar change listener
                Cell avatarCell = ClientContextJME.getViewManager().getPrimaryViewCell();
                CellRenderer rend = avatarCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                ((AvatarImiJME)rend).addAvatarChangedListener(this);

                //add listeners
                addMouseListener();
                addContenxtMenuItem(contextMenuName);
                addProximityListener(radius);
                
                //register message receiver
                if(msgReceiver==null) {
                    msgReceiver = new TriggerGestureMessageReceiver();
                    channel = cell.getComponent(ChannelComponent.class);
                    channel.addMessageReceiver(TriggerGestureMessage.class, msgReceiver);
                }
                break;
            case DISK:
                
                //remove all listeners & menu item
                removeContextMenuItem();
                removeMouseListener();
                removeProximityListener();
                if(msgReceiver!=null) {
                    channel.removeMessageReceiver(TriggerGestureMessage.class);
                }
                break;
        }
        super.setStatus(status, increasing);
    }
    
    /*
     * add the animation names of avatar in gesture map
     */
    public void initializeGestures() {
        
        if(gestureMap.isEmpty()) {
            
            gestureMap.clear();
            Cell avatarCell = ClientContextJME.getViewManager().getPrimaryViewCell();
            CellRenderer rend = avatarCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
            WlAvatarCharacter myAvatar = ((AvatarImiJME)rend).getAvatarCharacter();

            // Otherwise, figure out which gestures are supported. We want
            // to remove the "Male_" or "Female_" for now.
            for (String action : myAvatar.getAnimationNames()) {
                String name = action;
                if (action.startsWith("Male_") == true) {
                    name = name.substring(5);
                } else if (action.startsWith("Female_") == true) {
                    name = name.substring(7);
                }
                // add to a map of user-friendly names to avatar animations
                // e.g., "Shake Hands" -> "Male_ShakeHands"
                gestureMap.put(BUNDLE.getString(name), action);
            }

            // Add the left and right wink
            if (myAvatar.getCharacterParams().isAnimatingFace()) {
                gestureMap.put(BUNDLE.getString("Wink"), "RightWink");
                gestureMap.put(BUNDLE.getString("Sit"), "Sit");
            }
        }
    }
    
    /*
     * add proximity listener to cell's proximity component
     */
    public void addProximityListener(int radius) {
        if(proximityListener!=null) {
            proximity.removeProximityListener(proximityListener);
        } else {
            proximityListener = new TriggerGestureProximityListener();
        }
        proximity.addProximityListener(proximityListener, new BoundingVolume[] {new BoundingSphere(radius, Vector3f.ZERO)});
//        removeContextMenuItem();
//        removeMouseListener();
    }
    
    /*
     * remove proximity listener to cell's proximity component
     */
    private void removeProximityListener() {
        if(proximityListener!=null) {
            proximity.removeProximityListener(proximityListener);
        }
    }
    
    /*
     * add mouse listener for left click to the entity
     */
    public void addMouseListener() {
        try {
            if(mouseListener == null) {
                BasicRenderer crJME = (BasicRenderer) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                Entity ent = crJME.getEntity();
                mouseListener = new TriggerGestureMouseListener();
                mouseListener.addToEntity(ent);
            }
    //        removeContextMenuItem();
            //removeProximityListener();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /*
     * remove mouse listener
     */
    private void removeMouseListener() {
        if(mouseListener!=null) {
            BasicRenderer crJME = (BasicRenderer) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
            Entity ent = crJME.getEntity();
            mouseListener.removeFromEntity(ent);
        }
    }
    
    /*
     * add an item in context menu
     */
    public void addContenxtMenuItem(final String contextMenuName) {
        if(contextMenuListener!=null) {
            contextMenu.removeContextMenuFactory(ctxMenuFactory);
        } else {
            contextMenuListener = new TriggerGestureContextMenuListener();
        }
        
        ctxMenuFactory = new ContextMenuFactorySPI() {
           public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
               final ContextMenuItem item = new SimpleContextMenuItem(contextMenuName
                    , contextMenuListener);
                return new ContextMenuItem[] { item };
            }
        };
        contextMenu.addContextMenuFactory(ctxMenuFactory);
//        removeMouseListener();
        //removeProximityListener();
    }
    
    /*
     * remove item from context menu
     */
    private void removeContextMenuItem() {
        contextMenu.removeContextMenuFactory(ctxMenuFactory);
    }
    
    /*
     * trigger the animation of avatar
     */
    public void triggerGesture() {
        Cell avatarCell = ClientContextJME.getViewManager().getPrimaryViewCell();
        CellRenderer rend = avatarCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        WlAvatarCharacter myAvatar = ((AvatarImiJME)rend).getAvatarCharacter();

//        for(int i=0;i<gestureMap.size();i++) {
//            System.out.println(gestureMap.keySet().toArray()[i]+" = "+gestureMap.values().toArray()[i]);
//        }
        String action = (String) gestureMap.get(gesture);
        if(action!=null) {
            if (action.equals("Sit")) {
                doSitGesture(myAvatar);
            } else if (action.equals("RightWink")) {
                CharacterEyes eyes = myAvatar.getEyes();
                eyes.wink(false);
            } else {
                myAvatar.playAnimation(action);
            }
        }
    }
    
    /**
     * Invoke the Sit gesture.
     */
    private void doSitGesture(final WlAvatarCharacter avatar) {
        
        // Create a thread that sleeps and tells the sit action to stop.
        final Runnable stopSitRunnable = new Runnable() {

            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    logger.log(Level.WARNING, "Sleep failed.", ex);
                }
                avatar.triggerActionStop(AvatarContext.TriggerNames.SitOnGround);
            }
        };

        // Spawn a thread to start the animation, which then spawns a thread
        // to stop the animation after a small sleep.
        new Thread() {

            @Override
            public void run() {
                avatar.triggerActionStart(AvatarContext.TriggerNames.SitOnGround);
                new Thread(stopSitRunnable).start();
            }
        }.start();
    }

    /*
     * avatar is changes so re-initialized the animations
     */
    public void avatarChanged(Avatar avatar) {
        gestureMap.clear();
        initializeGestures();
    }
    
    public Trigger getTrigger() {
        return trigger;
    }
    
    /*
     * message receiver to add listeners again due to change 
     * in radius or context menu item name
     */
    class TriggerGestureMessageReceiver implements ComponentMessageReceiver {
        
        @Override
        public void messageReceived(CellMessage message) {
            TriggerGestureMessage msg = (TriggerGestureMessage)message;
            logger.warning("message recevied from the server....");
            if (!msg.getSenderID().equals(cell.getCellCache().getSession()
                    .getID())) {
                logger.warning("adding listeners again....");
                addContenxtMenuItem(msg.getContextMenuName());
                addProximityListener(msg.getRadius());
            }
        }
        
    }

    public TriggerGestureMouseListener getMouseEventListener() {
        return mouseListener;
    }

}
