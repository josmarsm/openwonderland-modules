/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.mynpc.server;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.AvatarConfigComponentServerState;
import org.jdesktop.wonderland.modules.mynpc.common.mynpcCellClientState;
import org.jdesktop.wonderland.modules.mynpc.common.mynpcCellServerState;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.MovableAvatarComponentMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.AvatarConfigComponentServerState;
import org.jdesktop.wonderland.modules.avatarbase.server.cell.AvatarConfigComponentMO;
import org.jdesktop.wonderland.modules.scriptingComponent.server.ScriptingComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;

/**
 *
 * @author morrisford
 */
public class mynpcCellMO extends CellMO
    {
//    @UsesCellComponentMO(ScriptingComponentMO.class)
//    private ManagedReference<ScriptingComponentMO> scriptingCompRef;

    public mynpcCellMO(String avatarConfigURL, CellTransform transform)
        {
        super(new BoundingBox(new Vector3f(0,1,0), 1,2,1), transform);

        AvatarConfigComponentMO avatarConfig = new AvatarConfigComponentMO(this);
        AvatarConfigComponentServerState state = new AvatarConfigComponentServerState();
        state.setAvatarConfigURL(avatarConfigURL);
        avatarConfig.setServerState(state);
        addComponent(avatarConfig);
        System.out.println("mynpcCellMO - in constructor with parms");
//        addComponent(new ScriptingComponentMO(this), ScriptingComponentMO.class);
        }

    public mynpcCellMO()
        {
        addComponent(new ChannelComponentMO(this));
        addComponent(new MovableAvatarComponentMO(this), MovableComponentMO.class);
        addComponent(new ScriptingComponentMO(this), ScriptingComponentMO.class);
        System.out.println("mynpcCellMO - in constructor without parms");

        }

    @Override
    public String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.mynpc.client.mynpcCell";
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
//        this.shapeType = ((ShapeCellServerState)state).getShapeType();
    }

  @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new mynpcCellServerState();
        }
//        ((mynpcCellServerState)state).setShapeType(shapeType);
        return super.getServerState(state);
    }

    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new mynpcCellClientState();
        }
//        ((ShapeCellClientState)cellClientState).setShapeType(shapeType);
        return super.getClientState(cellClientState, clientID, capabilities);
    }
}
