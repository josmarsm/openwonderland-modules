/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.mynpc.client;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.jme.AvatarRenderManager.RendererUnavailable;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.AvatarJME;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingComponent;
import imi.character.avatar.AvatarContext.TriggerNames;

/**
 *
 * @author morrisford
 */
public class mynpcCell extends Cell {
    @UsesCellComponent
    private ScriptingComponent scriptingComp;

    private AvatarImiJME renderer;

    public mynpcCell(CellID cellID, CellCache cellCache)
        {
        super(cellID, cellCache);
        }
    
   @Override
    public boolean setStatus(CellStatus status) {
        boolean res = super.setStatus(status);

        switch (status) {
            case BOUNDS:
                break;
            case DISK:
                break;
            case ACTIVE:
                System.out.println("Inside ACTIVE in mynpc");
                scriptingComp.setAvatarRenderer(renderer);
                scriptingComp.setScriptClump("npc");
                break;
        }

        return res;
    }

   @Override
    public void setClientState(CellClientState state)
        {
        super.setClientState(state);
        }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType)
        {
        CellRenderer ret = null;
        switch(rendererType)
            {
            case RENDERER_2D :
                // No 2D Renderer yet
                break;
            case RENDERER_JME :
                try {
                    ServerSessionManager session = getCellCache().getSession().getSessionManager();
                    ret = ClientContextJME.getAvatarRenderManager().createRenderer(session, this);

                    if (ret instanceof AvatarImiJME)
                        {
                        renderer = (AvatarImiJME) ret;
                        System.out.println("In mynpc renderer = " + renderer);
                        }
                    }
                catch (RendererUnavailable ex)
                    {
                    Logger.getLogger(mynpcCell.class.getName()).log(Level.SEVERE, null, ex);
                    ret = new AvatarJME(this);
                    }
                break;
            }

        return ret;
        }
}
