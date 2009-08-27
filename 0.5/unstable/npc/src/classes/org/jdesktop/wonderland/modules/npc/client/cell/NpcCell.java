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
package org.jdesktop.wonderland.modules.npc.client.cell;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import imi.character.CharacterMotionListener;
import imi.character.behavior.CharacterBehaviorManager;
import imi.character.behavior.GoTo;
import imi.character.statemachine.GameContext;
import imi.scene.PMatrix;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.MovableAvatarComponent;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.jme.AvatarRenderManager.RendererUnavailable;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.cellrenderer.AvatarJME;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.npc.common.NpcCellChangeMessage;

/**
 *
 * @author paulby
 * @author david <dmaroto@it.uc3m.es> UC3M - "Project España Virtual"
 */
public class NpcCell extends Cell {

    private final JMenuItem menuItem;
    boolean menuAdded = false;
    private AvatarImiJME renderer;
    @UsesCellComponent
    private ProximityComponent proximityComp;
    @UsesCellComponent
    private MovableAvatarComponent movableAvatar;
    private NPCProximityListener listenerProx;
    private Vector3f npcPosition;
    private GoTo myGoTo;

    public NpcCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        
        // Create a menu item to control the NPC
        menuItem = new JMenuItem("NPC " + cellID + " controls...");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                NpcControllerFrame ncf = new NpcControllerFrame(NpcCell.this,
                        renderer.getAvatarCharacter());
                ncf.setVisible(true);
            }
        });

        // Create a proximity listener that will be added in setStatus()
        listenerProx = new NPCProximityListener();
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        
        // If the Cell is being made active and increasing, then add the menu
        // item. Also add the proximity listener
        if (status == CellStatus.ACTIVE && increasing == true) {
            JmeClientMain.getFrame().addToEditMenu(menuItem, -1);
            BoundingVolume bv[] = new BoundingVolume[] { getLocalBounds() };
            proximityComp.addProximityListener(listenerProx, bv);
            return;
        }

        // if the Cell is being brought back down through the ACTIVE state,
        // then remove the menu item
        if (status == CellStatus.ACTIVE && increasing == false) {
            JmeClientMain.getFrame().removeFromEditMenu(menuItem);
            return;
        }
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        CellRenderer ret = null;
        switch (rendererType) {
            case RENDERER_2D:
                // No 2D Renderer yet
                break;
            case RENDERER_JME:
                try {
                    ServerSessionManager session = getCellCache().getSession().getSessionManager();
                    ret = ClientContextJME.getAvatarRenderManager().createRenderer(session, this);

                    if (ret instanceof AvatarImiJME) {
                        renderer = (AvatarImiJME) ret;
                    }
                } catch (RendererUnavailable ex) {
                    Logger.getLogger(NpcCell.class.getName()).log(Level.SEVERE, null, ex);
                    ret = new AvatarJME(this);
                }
                break;
        }

        return ret;
    }

    public void move(int x, int y, int z) {
        npcPosition = new Vector3f(x, y, z);

        goTo();

        CellTransform transform = new CellTransform(null, npcPosition, null);
        NpcCellChangeMessage msg = new NpcCellChangeMessage(getCellID(), transform);
        sendCellMessage(msg);

        CharacterMotionListener motionListener = new CharacterMotionListener() {

            public void transformUpdate(Vector3f translation, PMatrix rotation) {
                //Check if NPC has reached his destination
                if (!myGoTo.verify()) {
                    CellTransform transform = new CellTransform(rotation.getRotationJME(), translation);
                    movableAvatar.localMoveRequest(transform, 0, false, null, null);
                }
            }
        };

        renderer.getAvatarCharacter().getController().addCharacterMotionListener(motionListener);
    }

    public void goTo() {
        GameContext context = renderer.getAvatarCharacter().getContext();
        CharacterBehaviorManager helm = context.getBehaviorManager();
        myGoTo = new GoTo(npcPosition, context);
        helm.clearTasks();
        helm.setEnable(true);
        helm.addTaskToTop(myGoTo);

    }

    private class NpcCellMessageReceiver implements ComponentMessageReceiver {

        public void messageReceived(CellMessage message) {
            NpcCellChangeMessage sccm = (NpcCellChangeMessage) message;
            if (!sccm.getSenderID().equals(getCellCache().getSession().getID())) {
                //npcPosition = sccm.getNpcPosition();
                npcPosition = sccm.getCellTransform().getTranslation(null);
                goTo();
            }
        }
    }

    /**
     * A class that notifies when avatars have moved within proximity of the
     * NPC Cell.
     */
    private class NPCProximityListener implements ProximityListener {

        /**
         * {@inheritDoc}
         */
        public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID,
                BoundingVolume proximityVolume, int proximityIndex) {
            if (entered) {
                //Do here whatever you want
                System.out.println("*****IN");
            } else {
                //Do here whatever you want
                System.out.println("************OUT");
            }

        }
    }
}
