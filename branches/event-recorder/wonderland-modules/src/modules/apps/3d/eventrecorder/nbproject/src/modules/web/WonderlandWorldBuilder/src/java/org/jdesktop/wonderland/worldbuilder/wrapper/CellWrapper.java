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
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.worldbuilder.Cell;
import org.jdesktop.wonderland.worldbuilder.CellDimension;
import org.jdesktop.wonderland.worldbuilder.CellLocation;

/**
 *
 * @author jkaplan
 */
@XmlRootElement(name="cell")
public class CellWrapper {
    private Cell cell;
    private URI uri;
    private CellsWrapper children;
    
    public CellWrapper() {
        cell = new Cell();
    }
    
    public CellWrapper(Cell cell, URI uri) {
        this.cell = cell;
        this.uri = uri;
    }
    
    @XmlElement(name="parent")
    public CellRefWrapper getParentRef() {
        if (cell.getParent() == null) {
            return null;
        }
        
        return new CellRefWrapper(cell.getParent(), 
                                  uri.resolve(cell.getParent().getCellID()));
    }
    
    @XmlElement(name="children")
    public CellsWrapper getChildren() {
        if (children == null) {
            children = getChildWrapper();
        }
        
        return children;
    }
    
    public void setChildren(CellsWrapper children) {
        this.children = children;
    }
    
    @XmlElement
    public String getCellID() {
        return cell.getCellID();
    }
   
    public void setCellID(String cellID) {
        cell.setCellID(cellID);
    }
    
    @XmlElement
    public int getVersion() {
        return cell.getVersion();
    }
    
    public void setVersion(int version) {
        cell.setVersion(version);
    }

    @XmlElement
    public String getCellType() {
        return cell.getCellType();
    }

    public void setCellType(String cellType) {
        cell.setCellType(cellType);
    }
    
    @XmlElement
    public String getCellSetupType() {
        return cell.getCellSetupType();
    }
    
    public void setCellSetupType(String cellSetupType) {
        cell.setCellSetupType(cellSetupType);
    }
    
    @XmlElement
    public CellLocation getLocation() {
        return cell.getLocation();
    }
    
    public void setLocation(CellLocation location) {
        cell.setLocation(location);
    }

    @XmlElement
    public CellDimension getSize() {
        return cell.getSize();
    }

    public void setSize(CellDimension size) {
        cell.setSize(size);
    }
    
    @XmlElement
    public int getRotation() {
        return cell.getRotation();
    }
    
    public void setRotation(int rotation) {
        cell.setRotation(rotation);
    }
    
    @XmlElement
    public double getScale() {
        return cell.getScale();
    }
    
    public void setScale(double scale) {
        cell.setScale(scale);
    }
    
    @XmlElement
    public URI getCatalogURI() {
        return cell.getCatalogURI();
    }
    
    public void setCatalogURI(URI catalogURI) {
        cell.setCatalogURI(catalogURI);
    }
    
    @XmlElement
    public Integer getCatalogID() {
        return cell.getCatalogID();
    }
    
    public void setCatalogID(Integer catalogID) {
        cell.setCatalogID(catalogID);
    }
    
    @XmlElement(name="properties")
    public PropertiesWrapper getProperties() {
        return new PropertiesWrapper(cell.getProperties());
    }
    
    public void setProperties(PropertiesWrapper properties) {
        cell.setProperties(properties.getProperties());
    }
    
    @XmlAttribute(name="uri")
    public URI getURI() {
        return uri;
    }
    
    @XmlTransient
    public Cell getCell() {
        return cell;
    }
    
    protected CellsWrapper getChildWrapper() {
        Collection<CellRefWrapper> childList = new ArrayList(cell.getChildren().size());
        for (Cell child : cell.getChildren()) {
            URI childURI = getURI().resolve(child.getCellID());
            childList.add(new CellRefWrapper(child, childURI)); 
        }
        
        return new CellsWrapper(childList);
    }
}
