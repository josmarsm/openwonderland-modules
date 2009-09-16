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
package org.jdesktop.wonderland.modules.eventplayer.client.npcplayer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.cell.view.AvatarCell.AvatarActionTrigger;
import org.jdesktop.wonderland.client.jme.AvatarRenderManager.RendererUnavailable;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.AvatarJME;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.eventplayer.common.npcplayer.NpcPlayerCellClientState;

/**
 * Cell that represents an NPC in the event player. Adapted from the NPC module.
 * @author paulby
 * @author david <dmaroto@it.uc3m.es> UC3M - "Project España Virtual"
 * @author Bernard Horan
 */
public class NpcPlayerCell extends AvatarCell {

    @UsesCellComponent
    private MovableNpcPlayerComponent movableNPC;

    private WonderlandIdentity identity;

    public NpcPlayerCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);        
    }
    
    @Override
    public void setClientState(CellClientState cellClientState) {
        String userName = ((NpcPlayerCellClientState)cellClientState).getUserName();
        this.identity = new WonderlandIdentity(userName, null, null);
        super.setClientState(cellClientState);
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

                } catch (RendererUnavailable ex) {
                    Logger.getLogger(NpcPlayerCell.class.getName()).log(Level.SEVERE, null, ex);
                    //Create Teapot
                    ret = new AvatarJME(this);
                }
                break;
        }

        return ret;
    }

    /**
     * TODO this is a temporary interface for handling avatar actions, need
     * to uplevel
     * @param trigger
     * @param pressed
     */
    public void triggerAction(int trigger, boolean pressed, String animationName) {
        if (ClientContext.getRendererType()==RendererType.RENDERER_JME) {
            CellRenderer rend = getCellRenderer(RendererType.RENDERER_JME);
            if (rend instanceof AvatarActionTrigger) {
                ((AvatarActionTrigger)rend).trigger(trigger, pressed, animationName);
            }
        }
    }

    @Override
    public WonderlandIdentity getIdentity() {
        return identity;
    }

    
}
