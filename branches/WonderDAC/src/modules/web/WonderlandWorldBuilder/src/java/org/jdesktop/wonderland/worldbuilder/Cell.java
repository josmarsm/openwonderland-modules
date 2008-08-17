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
package org.jdesktop.wonderland.worldbuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author jkaplan
 */
@XmlRootElement
public class Cell {
    private static final Logger logger =
            Logger.getLogger(Cell.class.getName());
    
    private String cellID;
     
    private Cell parent;
    private Set<Cell> children;
    
    private int version;
    
    private String cellType;
    private String cellSetupType;
    private CellLocation location;
    private CellDimension size;
    private int rotation;
    private double scale;
    
    private URI catalogURI;
    private Integer catalogID;
    
    private Map<String, String> properties;
    
    public Cell() {
        this (null);
    }
    
    public Cell(String cellID) {
        this.cellID = cellID;
        this.children = new HashSet<Cell>();
        this.properties = new HashMap<String, String>();
        
        this.size = new CellDimension(1, 1);
        this.location = new CellLocation(0, 0);
        this.scale = 1.0;
    }

    public String getCellID() {
        return cellID;
    }

    public void setCellID(String cellID) {
        this.cellID = cellID;
    }
    
    public Cell getParent() {
        return parent;
    }
    
    protected void setParent(Cell parent) {
        this.parent = parent;
    }
    
    /**
     * Return a list of children sorted by cellID.  This list is
     * independent of the cell's internal storage of children, so modifying
     * this list does not add or remove children.  use the
     * <code>addChild()</code> and <code>removeChild</code> methods to
     * actually remove children.
     * @return the sorted list of children.
     */
    public List<Cell> getChildren() {
        // sort the children by id
        List<Cell> sorted = new ArrayList(children);
        Collections.sort(sorted, new Comparator<Cell>() {
            public int compare(Cell cell0, Cell cell1) {
                return cell0.getCellID().compareTo(cell1.getCellID());
            }
        });
        
        return sorted;
    }

    public void addChild(Cell child) {
        if (child.getParent() != null) {
            child.getParent().removeChild(child);
        }
        
        // replace the child
        children.remove(child);
        children.add(child);
        child.setParent(this);
    }
    
    public void removeChild(Cell child) {
        if (!child.getParent().equals(this)) {
            throw new RuntimeException(this + " is not the parent of " + child);
        }
        
        child.setParent(null);
        children.remove(child);
    }
    
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCellType() {
        return cellType;
    }

    public void setCellType(String cellType) {
        this.cellType = cellType;
    }

    public String getCellSetupType() {
        return cellSetupType;
    }
    
    public void setCellSetupType(String cellSetupType) {
        this.cellSetupType = cellSetupType;
    }
    
    public CellLocation getLocation() {
        return location;
    }
    
    public void setLocation(CellLocation location) {
        this.location = location;
    }
    
    public CellDimension getSize() {
        return size;
    }

    public void setSize(CellDimension size) {
        this.size = size;
    }
   
    public int getRotation() {
        return rotation;
    }
    
    public void setRotation(int rotation) {
        this.rotation = rotation;
    }
    
    public double getScale() {
        return scale;
    }
    
    public void setScale(double scale) {
        this.scale = scale;
    }
    
    public URI getCatalogURI() {
        return catalogURI;
    }
    
    public void setCatalogURI(URI catalogURI) {
        this.catalogURI = catalogURI;
    }
    
    public Integer getCatalogID() {
        return catalogID;
    }
    
    public void setCatalogID(Integer catalogID) {
        this.catalogID = catalogID;
    }
    
    public Map<String, String> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    
    public String getProperty(String key) {
        return properties.get(key);
    }
    
    public void setProperty(String key, String value) {
        properties.put(key, value);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Cell other = (Cell) obj;
        if (this.getCellID() != other.getCellID() && (this.getCellID() == null || !this.cellID.equals(other.cellID))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.getCellID() != null ? this.getCellID().hashCode() : 0);
        return hash;
    }
}
