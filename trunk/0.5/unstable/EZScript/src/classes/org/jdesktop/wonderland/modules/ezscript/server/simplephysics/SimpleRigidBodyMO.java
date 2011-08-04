/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.server.simplephysics;

import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.ezscript.common.simplephysics.SimpleRigidBodyClientState;
import org.jdesktop.wonderland.modules.ezscript.common.simplephysics.SimpleRigidBodyServerState;
import org.jdesktop.wonderland.modules.ezscript.server.cell.AnotherMovableComponentMO;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author JagWire
 */
public class SimpleRigidBodyMO extends CellComponentMO {

    @UsesCellComponentMO(SharedStateComponentMO.class)
    private ManagedReference<SharedStateComponentMO> sharedStateComponentRef;
    
    @UsesCellComponentMO(AnotherMovableComponentMO.class)
    private ManagedReference<AnotherMovableComponentMO> anotherMovableRef;
    
    public SimpleRigidBodyMO(CellMO cell) {
        super(cell);
    }
    
    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.ezscript.client.simplephysics.SimpleRigidBodyComponent";
    }

    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if(state == null) {
            state = new SimpleRigidBodyClientState();
        }
        
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if(state == null) {
            state = new SimpleRigidBodyServerState();
        }
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);
    }
    
    
    
}
