/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author Vladimir Djurovic
 */
@XmlRootElement (name = "scavenger-hunt-component")
@ServerState
public class ScavengerHuntComponentServerState extends CellComponentServerState {
    
    private ScavengerHuntItem item;
    private String sheetId;
    
    public ScavengerHuntComponentServerState(){
        
    }

    public ScavengerHuntItem getItem() {
        return item;
    }

    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.isocial.scavenger.server.ScavengerHuntComponentMO";
    }

    public void setItem(ScavengerHuntItem item) {
        this.item = item;
    }

    public void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }

    public String getSheetId() {
        return sheetId;
    }
    
}
