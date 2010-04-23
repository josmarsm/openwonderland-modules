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
package org.jdesktop.wonderland.modules.thoughtbubbles.client;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.presentationbase.client.PresentationToolbarManager;
import org.jdesktop.wonderland.modules.thoughtbubbles.client.jme.cell.ThoughtBubbleEntity;
import org.jdesktop.wonderland.modules.thoughtbubbles.common.ThoughtBubblesComponentChangeMessage;
import org.jdesktop.wonderland.modules.thoughtbubbles.common.ThoughtBubblesComponentChangeMessage.ThoughtBubblesAction;
import org.jdesktop.wonderland.modules.thoughtbubbles.common.ThoughtBubblesComponentClientState;
import org.jdesktop.wonderland.modules.thoughtbubbles.common.ThoughtRecord;

public class ThoughtBubblesCellComponent extends CellComponent implements ProximityListener, ActionListener {

    private MouseEventListener listener = null;
    private static final Logger logger =
            Logger.getLogger(ThoughtBubblesCellComponent.class.getName());
    
    @UsesCellComponent
    private ChannelComponent channel;
    @UsesCellComponent
    private ProximityComponent prox;

    private static final String NEW_THOUGHT_ACTION = "new_thought";
    private static JButton createThoughtButton = null;
    private Map<ThoughtRecord, ThoughtBubbleEntity> thoughts = new HashMap<ThoughtRecord, ThoughtBubbleEntity>();
    private String localUsername;
    private HUD mainHUD;
    private HUDComponent thoughtDialog;

    private MouseEventListener mouseListener;

    public ThoughtBubblesCellComponent(Cell cell) {
        super(cell);

        logger.warning("Constructing ThoughtBubblesCellComponent");
        createThoughtButton = new JButton();
        createThoughtButton.setText("Add New Thought");
        createThoughtButton.setActionCommand(NEW_THOUGHT_ACTION);
        createThoughtButton.addActionListener(this);
    }

//    public ThoughtBubblesCellComponent() {
//        logger.warning("Got a call to the no arg constructor of ThoughtBubblesCellComponent.");
//    }

    @Override
    public void setClientState(CellComponentClientState state) {
        super.setClientState(state);

        // Initialize the Map with all the ThoughtRecorsd from the client state.
        // This is a little problematic, because we can't create the entities for
        // each of these yet because the rendering systems aren't ready. So for now,
        // just put a null into the map, and do a pass through the map for null
        // entries and intialize them during setStatus
        for (ThoughtRecord rec : ((ThoughtBubblesComponentClientState) state).getThoughts()) {
            logger.warning("Got thought from server, adding: " + rec);
            thoughts.put(rec, null);
        }

    }

    private ThoughtBubbleEntity createNewThoughtBubbleEntity(ThoughtRecord rec) {
        CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);

        final ThoughtBubbleEntity entity = new ThoughtBubbleEntity(rec, renderer.getEntity());

        // Add the node to the scenegraph.
//        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
//            public void update(Object arg0) {
               getSceneGraphRootEntity().addEntity(entity);

            
//               RenderComponent parentRC = cell.getCellRenderer(RendererType.RENDERER_JME).


//               this.setAttachPoint(parentRC.getSceneRoot());
//               parentRC.getEntity().addEntity(entity);
//               RenderComponent parentRC = this.getComponent(RenderComponent.class);
//                thisRC.setAttachPoint(parentRC.getSceneRoot());
//                this.addEntity(subEntity);

//            }
//        }, null);

        mouseListener.addToEntity(entity);
        this.thoughts.put(rec, entity);

        return entity;
    }

    /**
     * Returns the scene root for the Cell's scene graph
     */
    protected Entity getSceneGraphRootEntity() {

        CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);

        RenderComponent cellRC = (RenderComponent) renderer.getEntity().getComponent(RenderComponent.class);
        return cellRC.getEntity();
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        // get the activation bounds from the cell we are part of
        BoundingVolume[] bounds = new BoundingVolume[]{
            this.cell.getLocalBounds()
        };

        if (status == CellStatus.ACTIVE && increasing) {
            channel.addMessageReceiver(ThoughtBubblesComponentChangeMessage.class, new ThoughtBubblesCellMessageReceiver());
            prox.addProximityListener(this, bounds);
            if(mouseListener==null)
                mouseListener = new MouseEventListener();

            // Do this here - if it happens before the avatar stuff is done, it will fail.
            localUsername = cell.getCellCache().getViewCell().getIdentity().getUsername();
            logger.warning("LocalUsername: " + localUsername);
        } else if (status == CellStatus.RENDERING && increasing) {
            // Initialize any items in the thoughts map that have null entities.
            // Avoid concurrent mod exceptions by cloning.
            // This needs to happen in rendering because in ACTIVE, the renderer
            // objects don't exist yet, so we can't get their entities. 
            Map<ThoughtRecord, ThoughtBubbleEntity> thoughtsClone = new HashMap(this.thoughts);
            for(ThoughtRecord rec : thoughtsClone.keySet()) {
                if(thoughtsClone.get(rec)==null) {
                    // if it's null, construct a new entity and replace
                    // the listing in the original map.
                    this.thoughts.put(rec, createNewThoughtBubbleEntity(rec));
                }
            }

        } else if (status == CellStatus.DISK && !increasing) {
            // remove the listeners from all known entities before going to DISK

            mouseListener = null;

//            listener.removeFromEntity(renderer.getEntity());
//            listener = null;
        } else if (status == CellStatus.RENDERING && !increasing) {
            // As we're falling down the status chain, try removing the listener
            // earlier. It seems to be gone by the time we get to DISK.
            channel.removeMessageReceiver(ThoughtBubblesComponentChangeMessage.class);
            prox.removeProximityListener(this);
        }
    }

    /**
     * Called by the proximity listener.
     * 
     * @param entered
     * @param cell
     * @param viewCellID
     * @param proximityVolume
     * @param proximityIndex
     */
    public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {

        logger.warning(viewCellID + " entered? " + entered);

        // Check to see if the avatar entering/exiting is the local one.
        if (cell.getCellCache().getViewCell().getCellID() == viewCellID) {
            if (entered) {
                // turn on the toolbar
                logger.warning("Adding toolbar button.");
                PresentationToolbarManager.getManager().addToolbarButton(createThoughtButton);
            } else {
                logger.warning("Removing toolbar button.");
                PresentationToolbarManager.getManager().removeToolbarButton(createThoughtButton);
            }
        }
    }

    public void createThought(String text, boolean isQuestion) {
        logger.warning("Got callback to create thought: " + text + " question? " + isQuestion);

//        // get the local view cell so we can figure out where to put this thing.
//        // this is local to the WORLD cell, but we want the translation within OUR cell.
//        CellTransform worldAvatarTransform = cell.getCellCache().getViewCell().getWorldTransform().clone(null);
//        CellTransform cellLocalAvatarTransform = worldAvatarTransform.mul(cell.getLocalToWorldTransform());
//

        CellTransform avatarWorldTransform = cell.getCellCache().getViewCell().getWorldTransform();

        CellTransform cellTransform = cell.getWorldTransform();
        
        cellTransform.invert();
        cellTransform.mul(avatarWorldTransform);

        Vector3f trans = Vector3f.ZERO;
        CellTransform newT = new CellTransform(null, trans, 1.0f);
        cellTransform.mul(newT);

//        logger.warning("cell translation: " + cell.getWorldTransform().getTranslation(Vector3f.ZERO));
//        logger.warning("avatarTranslation: " + worldAvatarTransform.getTranslation(Vector3f.ZERO));
//        logger.warning("Making new thought bubble, with localTransform: " + cellLocalAvatarTransform.getTranslation(Vector3f.ZERO));


//        ThoughtRecord thought = new ThoughtRecord(text, isQuestion, localUsername, new Date(), cellTransform.getTranslation(avatarWorldTransform.getTranslation(Vector3f.ZERO)));
        ThoughtRecord thought = new ThoughtRecord(text, isQuestion, localUsername, new Date(), cellTransform.getTranslation(null));

//        ThoughtRecord thought = new ThoughtRecord(text, isQuestion, localUsername, new Date(), Vector3f.ZERO);

        ThoughtBubblesComponentChangeMessage msg = new ThoughtBubblesComponentChangeMessage(ThoughtBubblesAction.NEW_THOUGHT);
        msg.setThought(thought);

        this.channel.send(msg);
        logger.warning("Message sent to server!");

        // Now add it to the local representation.
        ThoughtBubbleEntity thoughtBubble = createNewThoughtBubbleEntity(thought);


        mainHUD.removeComponent(thoughtDialog);

    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals(NEW_THOUGHT_ACTION)) {
            // Throw up the submit thought dialog box.
            SubmitThoughtDialog d = new SubmitThoughtDialog(this);

            mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

            thoughtDialog = mainHUD.createComponent(d);
            thoughtDialog.setPreferredLocation(Layout.CENTER);
            thoughtDialog.setName("New Thought");
            mainHUD.addComponent(thoughtDialog);

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    thoughtDialog.setVisible(true);
                }
            });
        }
    }

    class ThoughtBubblesCellMessageReceiver implements ComponentMessageReceiver {

        public void messageReceived(CellMessage message) {
            ThoughtBubblesComponentChangeMessage msg = (ThoughtBubblesComponentChangeMessage) message;

            switch (msg.getAction()) {
                case NEW_THOUGHT:
                    thoughts.put(msg.getThought(), createNewThoughtBubbleEntity(msg.getThought()));
                    logger.warning("Got a new thought from another client!");
                    break;
            }
        }
    }

    class MouseEventListener extends EventClassListener {
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseButtonEvent3D.class };
        }

        // Note: we don't override computeEvent because we don't do any computation in this listener.

        @Override
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
            logger.warning("got event! " + mbe.getEntity() + "; " + mbe.getWhen());
            if(mbe.isClicked()) {
                logger.warning("got click!");
                if(mbe.getEntity() instanceof ThoughtBubbleEntity) {
                    logger.warning("found thought bubble entity");
                    ThoughtBubbleEntity e = (ThoughtBubbleEntity)mbe.getEntity();
                    e.showThoughtDialog();
                }
            }
        }
    }

}
