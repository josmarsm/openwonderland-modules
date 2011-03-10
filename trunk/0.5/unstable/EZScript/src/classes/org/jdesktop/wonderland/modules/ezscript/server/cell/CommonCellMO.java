
package org.jdesktop.wonderland.modules.ezscript.server.cell;

import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.modules.ezscript.server.EZScriptComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author JagWire
 */
public class CommonCellMO extends CellMO {


    public CommonCellMO() {
        super();
    }
    @UsesCellComponentMO(EZScriptComponentMO.class)
    ManagedReference<EZScriptComponentMO> ezref;

    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.ezscript.client.cell.CommonCell";
    }

}
