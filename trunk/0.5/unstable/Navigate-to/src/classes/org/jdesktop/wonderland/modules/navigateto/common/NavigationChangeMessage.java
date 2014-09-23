/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.navigateto.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;

/**
 *
 * @author Abhishek Upadhyay
 */
public class NavigationChangeMessage extends CellServerComponentMessage{
 
     public NavigationChangeMessage(CellID cellID,NavigateToServerState ntsc) {
        super(cellID,ntsc);
     }
}
