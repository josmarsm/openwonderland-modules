/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.common.simplephysics;

import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author JagWire
 */
@XmlRootElement(name="SimpleRigidBody-Component")
@ServerState
public class SimpleRigidBodyServerState extends CellComponentServerState {

    public SimpleRigidBodyServerState() { }
    
    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.ezscript.server.simplephysics.SimpleRigidBodyMO";
    }
    
}
