/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.worldbuilder.wrapper;

import java.net.URI;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.worldbuilder.Cell;
import org.jdesktop.wonderland.worldbuilder.persistence.CellPersistence;

/**
 *
 * @author jkaplan
 */
@XmlRootElement
public class CellRefWrapper {
    private Cell cell;
    private URI uri;
    
    public CellRefWrapper() {    
        cell = new Cell();
    }
    
    public CellRefWrapper(Cell cell, URI uri) {
        this.cell = cell;
        this.uri = uri;
    }
    
    @XmlElement
    public String getCellID() {
        return cell.getCellID();
    }
    
    public void setCellID(String cellID) {  
        cell.setCellID(cellID);
    }
    
    @XmlAttribute(name="uri")
    public URI getURI() {
        return uri;
    }
    
    public void setURI(URI uri) {
        this.uri = uri;
    }
    
    @XmlTransient
    public Cell getCell() {
        return cell;
    }
}
