/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 *
 * @author Vladimir Djurovic
 */
public class ScavengerHuntComponentClientState extends CellComponentClientState {
    
    private ScavengerHuntItem item;
    private String sheetId;
    
    public ScavengerHuntComponentClientState(){
        
    }

    public void setItem(ScavengerHuntItem item) {
        this.item = item;
    }

    public ScavengerHuntItem getItem() {
        return item;
    }

    public void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }

    public String getSheetId() {
        return sheetId;
    }

    
}
