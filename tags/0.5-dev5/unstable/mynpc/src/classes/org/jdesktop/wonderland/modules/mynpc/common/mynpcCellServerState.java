/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.mynpc.common;

import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author morrisford
 */
@XmlRootElement(name="mynpc-cell")
@ServerState
public class mynpcCellServerState extends CellServerState {

//    @XmlElement(name="shape-type")
//    private String shapeType = "BOX";

    public mynpcCellServerState() {
    }

//    @XmlTransient public String getShapeType() { return this.shapeType; }
//    public void setShapeType(String shapeType) { this.shapeType = shapeType; }

    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.mynpc.server.mynpcCellMO";
    }
}
