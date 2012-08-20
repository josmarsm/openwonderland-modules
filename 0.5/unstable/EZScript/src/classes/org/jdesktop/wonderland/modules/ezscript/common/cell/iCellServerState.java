/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.common.cell;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author Ryan
 */
@XmlRootElement(name="iCell")
@ServerState
public class iCellServerState extends CellServerState {
    
    @XmlElement(name="cell-class")
    public String cellClassName;
    
    @XmlElement(name="renderer-class")
    public String rendererClassName;
    
    
    
    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.ezscript.server.cell.iCellMO";
    }

    @XmlTransient
    public String getRendererClassName() {
        return rendererClassName;
    }

    public void setRendererClassName(String rendererClassName) {
        this.rendererClassName = rendererClassName;
    }

    @XmlTransient
    public String getCellClassName() {
        return cellClassName;
    }

    public void setCellClassName(String cellClassName) {
        this.cellClassName = cellClassName;
    }
    
    
    
    
}
