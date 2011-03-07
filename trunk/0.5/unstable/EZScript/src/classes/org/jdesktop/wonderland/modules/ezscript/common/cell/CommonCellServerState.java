
package org.jdesktop.wonderland.modules.ezscript.common.cell;

import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author JagWire
 */
@XmlRootElement(name="Common-Cell")
@ServerState
public class CommonCellServerState  extends CellServerState {


    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.ezscript.server.cell.CommonCellMO";
    }
}
