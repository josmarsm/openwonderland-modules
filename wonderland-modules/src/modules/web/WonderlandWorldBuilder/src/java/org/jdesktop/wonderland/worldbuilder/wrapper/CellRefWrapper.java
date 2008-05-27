/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.worldbuilder.wrapper;

import java.net.URI;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.worldbuilder.Cell;

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
