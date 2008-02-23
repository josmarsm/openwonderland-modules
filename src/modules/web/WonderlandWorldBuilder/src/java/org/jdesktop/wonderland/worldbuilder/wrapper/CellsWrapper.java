/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.worldbuilder.wrapper;

import java.util.Collection;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author jkaplan
 */
@XmlRootElement(name="cells")
public class CellsWrapper {
    private Collection<CellWrapper> cells;
    private Collection<CellRefWrapper> cellRefs;
    
    public CellsWrapper() {
    }
   
    public CellsWrapper(Collection<CellRefWrapper> cellRefs) {
        this.cellRefs = cellRefs;
    }
    
    public CellsWrapper(Collection<CellWrapper> cells, boolean isTree) {
        this.cells = cells;
    }
    
    @XmlElement(name="cellRef")
    public Collection<CellRefWrapper> getCellRefs() {
        return cellRefs;
    }
      
    public void setCellRefs(Collection<CellRefWrapper> cellRefs) {
        this.cellRefs = cellRefs;
    }
    
    @XmlElement(name="cell")
    public Collection<CellWrapper> getCells() {
        return cells;
    }
    
    public void setCells(Collection<CellWrapper> cells) {
        this.cells = cells;
    }
}
