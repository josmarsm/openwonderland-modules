/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.bulletphysics;

import org.jdesktop.wonderland.client.cell.registry.annotation.CellComponentFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.ezscript.common.simplephysics.SimpleRigidBodyServerState;

/**
 *
 * @author JagWire
 */
@CellComponentFactory
public class SimpleRigidBodyFactory implements CellComponentFactorySPI {

    public <T extends CellComponentServerState> T getDefaultCellComponentServerState() {
        return (T) new SimpleRigidBodyServerState();
    }

    public String getDisplayName() {
       return "Rigidy Body (SP)";
    }

    public String getDescription() {
       return "SimplePhysics Rigid Body attached to a cell.";
    }
    
}
