package org.jdesktop.wonderland.modules.webcaster.common;

import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

@XmlRootElement(name="webcaster-cell")
@ServerState
public class WebcasterCellServerState extends CellServerState
{
    public WebcasterCellServerState(){
    }

    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.webcaster.server.WebcasterCellMO";
    }
}