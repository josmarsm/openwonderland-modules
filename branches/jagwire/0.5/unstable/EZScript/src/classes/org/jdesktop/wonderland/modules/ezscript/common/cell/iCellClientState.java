/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.common.cell;

import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 *
 * @author Ryan
 */
public class iCellClientState extends CellClientState {
    
    private String rendererClassName;
    public iCellClientState() {
        super();
    }

    public String getRendererClassName() {
        return rendererClassName;
    }

    public void setRendererClassName(String rendererClassName) {
        this.rendererClassName = rendererClassName;
    }
    
    
}
