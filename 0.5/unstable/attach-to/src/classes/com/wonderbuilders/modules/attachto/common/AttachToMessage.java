/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.attachto.common;

import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * @author Abhishek Upadhyay
 */
public class AttachToMessage extends CellMessage {
 
    private CellTransform transform;

    public AttachToMessage(CellTransform transform) {
        this.transform = transform;
    }
    
    public CellTransform getTransform() {
        return transform;
    }

    public void setTransform(CellTransform transform) {
        this.transform = transform;
    }
    
}
