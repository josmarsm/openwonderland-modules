/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.attachto.common;

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 *
 * @author Abhishek Upadhyay
 */
public class AttachToComponentClientState extends CellComponentClientState {

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
}
