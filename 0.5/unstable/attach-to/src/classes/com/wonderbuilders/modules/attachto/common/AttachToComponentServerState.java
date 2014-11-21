/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.attachto.common;

import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author Abhishek Upadhyay
 */
@XmlRootElement(name = "attach-to-component")
@ServerState
public class AttachToComponentServerState extends CellComponentServerState {

    private String cellName = "";
    private String nodeName = "";

    public String getCellName() {
        return cellName;
    }

    public void setCellName(String cellName) {
        this.cellName = cellName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public String getServerComponentClassName() {
        return "com.wonderbuilders.modules.attachto.server.AttachToComponentMO";
    }
}
