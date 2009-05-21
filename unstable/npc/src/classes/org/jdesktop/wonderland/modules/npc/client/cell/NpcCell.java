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
import imi.character.CharacterSteeringHelm;
import imi.character.statemachine.GameContext;
import imi.character.steering.GoTo;
import imi.scene.PMatrix;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
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
import org.jdesktop.wonderland.common.cell.state.CellClientState;
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
    private ProximityComponent proximity;
    
    @UsesCellComponent
    private MovableAvatarComponent movableAvatar;

    private proximityListener1 listenerProx;
  
    private BoundingVolume[] boundingVolume;
    private Vector3f npcPosition;

    public NpcCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);

        menuItem = new JMenuItem("NPC " + cellID + " controls...");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                NpcControllerFrame ncf = new NpcControllerFrame(NpcCell.this,
                                                                renderer.getAvatarCharacter());
                ncf.setVisible(true);
            }
        });
    }

    @Override
    public boolean setStatus(CellStatus status) {
        boolean res = super.setStatus(status);
        ChannelComponent channel = getComponent(ChannelComponent.class);
        switch (status) {
            case BOUNDS:
                if (!menuAdded) {
                    JmeClientMain.getFrame().addToEditMenu(menuItem, Integer.MAX_VALUE);
                    menuAdded = true;
                }
                break;
            case DISK:
                if (menuAdded) {
                    JmeClientMain.getFrame().removeFromEditMenu(menuItem);
                    menuAdded = false;
                }
                break;
            case ACTIVE:
                if (listenerProx == null) {
                    boundingVolume = new BoundingVolume[1];
                    boundingVolume[0] = this.getLocalBounds();
                    listenerProx= new proximityListener1();

                    proximity.addProximityListener(listenerProx, boundingVolume);

                }
                channel.addMessageReceiver(NpcCellChangeMessage.class, new NpcCellMessageReceiver());
                break;
        }

        return res;
    }



    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        CellRenderer ret = null;
        switch(rendererType) {
            case RENDERER_2D :
                // No 2D Renderer yet
                break;
            case RENDERER_JME :
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
    
    public void move(int x, int y, int z){
        npcPosition = new Vector3f(x, y, z);
        
        goTo();
        
        NpcCellChangeMessage msg = new NpcCellChangeMessage(getCellID(), npcPosition);
        sendCellMessage(msg);    
    }
    
    public void goTo(){
    
        CharacterMotionListener motionListener = new CharacterMotionListener() {
            public void transformUpdate(Vector3f translation, PMatrix rotation) {
                        
            }
        };
        
        renderer.getAvatarCharacter().getController().addCharacterMotionListener(motionListener);
        motionListener.transformUpdate(renderer.getAvatarCharacter().getController().getPosition(), null);
           
        GameContext context = renderer.getAvatarCharacter().getContext() ;
      
        CharacterSteeringHelm helm = context.getSteering();
        //Vector3f myvector = new Vector3f(x,y,z);
        GoTo myGoTo=new GoTo(npcPosition, context);
        helm.addTaskToTop(myGoTo);
        helm.setEnable(true);
        
        
        proximity.removeProximityListener(listenerProx);
        
        listenerProx = null;
        boundingVolume = null;
        
        CellTransform transform = new CellTransform(null, npcPosition, null);
        movableAvatar.localMoveRequest(transform);
        
        
        boundingVolume = new BoundingVolume[1];
        boundingVolume[0] = this.getWorldBounds();
       
        listenerProx = new proximityListener1();
        proximity.addProximityListener(listenerProx, boundingVolume);
            
    }
    
    class NpcCellMessageReceiver implements ComponentMessageReceiver {
        
        public void messageReceived(CellMessage message) {
            NpcCellChangeMessage sccm = (NpcCellChangeMessage)message;
            if (!sccm.getSenderID().equals(getCellCache().getSession().getID())) {
                npcPosition = sccm.getNpcPosition();
                goTo();
            }
        }
    }

    
    class proximityListener1 implements ProximityListener{
        public void viewEnterExit( boolean entered,
                Cell cell,  CellID viewCellID,  com.jme.bounding.BoundingVolume proximityVolume, int proximityIndex)
            {
            if(entered){
                //Do here whatever you want
                System.out.println("*****IN");
            }
            else{
                //Do here whatever you want
                System.out.println("************OUT");
            }
                    
            }
        }
}
