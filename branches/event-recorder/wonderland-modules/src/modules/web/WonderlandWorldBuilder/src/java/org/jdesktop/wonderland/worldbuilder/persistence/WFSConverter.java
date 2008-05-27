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
package org.jdesktop.wonderland.worldbuilder.persistence;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.CellGLOSetup;
import org.jdesktop.lg3d.wonderland.wfs.WFSCell;
import org.jdesktop.wonderland.worldbuilder.Cell;
import org.jdesktop.wonderland.worldbuilder.CellDimension;
import org.jdesktop.wonderland.worldbuilder.CellLocation;

/**
 * Convert to and from WFS
 * @author jkaplan
 */
public class WFSConverter {
    private static final Logger logger = 
            Logger.getLogger(WFSConverter.class.getName());
    
    private static final Pattern setupPattern = Pattern.compile("(.*)\\{(.*)\\}");
    
    /**
     * Translate a cell to wfs
     * @param cell the cell to translate
     * @return the corresponding WFS cell
     */
    public BasicCellGLOSetup toWFS(Cell cell) {
        BasicCellGLOSetup setup;
        
        try {
            setup = createCellGLOSetup(cell);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to convert " + cell, ex);
        }
        
        return setup;
    }
    
    protected BasicCellGLOSetup createCellGLOSetup(Cell cell) 
        throws ClassNotFoundException, InstantiationException,
               IllegalAccessException
    {
        BasicCellGLOSetup out;
        
        String setupStr = cell.getCellSetupType();
        Class<?> gloSetupClass;
        Class<?> setupClass;
        
        Matcher m = setupPattern.matcher(setupStr);
        if (m.matches()) {  
            gloSetupClass = Class.forName(m.group(1));
            setupClass = Class.forName(m.group(2));
        } else {
            throw new IllegalArgumentException("Bad cell setup type: " + 
                                               cell.getCellSetupType());
        }
        
        out = (BasicCellGLOSetup) gloSetupClass.newInstance();
        out.setCellGLOClassName(cell.getCellType());
        out.setCellSetup((CellSetup) setupClass.newInstance());
        
        out.setBoundsType(getBoundsType(cell.getSize()));
        out.setBoundsRadius(getBoundsRadius(cell.getSize()));
        out.setOrigin(toOrigin(cell.getLocation()));
        out.setRotation(toRotation(cell.getRotation()));
        out.setScale(cell.getScale());
        
        setProperties(out, cell.getProperties());
        setMetadata(out, cell);
        return out;
    }
    
    /**
     * Translate a wfs cell into a cell
     * @param wfsCell the wfs cell to translate
     * @return the corresponding cell
     */
    public Cell fromWFS(WFSCell wfsCell) {
        Cell cell = new Cell(wfsCell.getCellName());
        
        // get the cell glo setup from this cell
        try {
            setupCell(cell, wfsCell.getCellSetup()); 
        } catch (Exception ex) {
            throw new RuntimeException("Error converting cell " + 
                                       wfsCell.getCanonicalName(), ex);
        }
        
        return cell;
    }
    
    protected void setupCell(Cell cell, BasicCellGLOSetup setup) {
        cell.setCellType(setup.getCellGLOClassName());
        cell.setCellSetupType(toCellSetupType(setup));
        cell.setSize(toSize(setup.getBoundsType(), setup.getBoundsRadius()));
        cell.setLocation(toLocation(setup.getOrigin()));
        cell.setRotation(toRotation(setup.getRotation()));
        cell.setScale(setup.getScale());    
        cell.setProperties(toProperties(setup));
        
        setCatalog(cell, setup.getMetadata());
    }
    
    protected String toCellSetupType(BasicCellGLOSetup setup) {
        String type = setup.getClass().getName();
        String setupType = setup.getCellSetup().getClass().getName();
        type += "{" + setupType + "}";
        
        return type;
    }
    
    protected CellDimension toSize(String boundsType, double boundsRadius) {
        if (boundsType.equals("BOX")) {
            return new CellDimension((int) Math.round(boundsRadius), 
                                     (int) Math.round(boundsRadius));
        } else {
            throw new IllegalArgumentException("Unsupported bounds type: " +
                                               boundsType);
        }
    }
    
    protected String getBoundsType(CellDimension dimension) {
        return "BOX";
    }
    
    protected double getBoundsRadius(CellDimension dimension) {
        return dimension.getWidth();
    }
    
    protected CellLocation toLocation(double[] origin) {
        return new CellLocation((int) Math.round(origin[0]),
                                (int) Math.round(origin[2]));
    }
    
    protected double[] toOrigin(CellLocation location) {
        return new double[] { location.getX(), 0, location.getY() };
    }
    
    protected int toRotation(double[] rotation) {
        return (int) Math.toDegrees(rotation[3]);
    }
    
    protected double[] toRotation(int rotation) {
        return new double[] { 0, 1, 0, Math.toRadians(rotation) };
    }
    
    protected Map<String, String> toProperties(BasicCellGLOSetup setup) {
        Map<String, String> properties = new HashMap<String, String>();
        
        CellSetup cellSetup     = setup.getCellSetup();
        Class<?> cellSetupClazz = cellSetup.getClass();
        
        try {
            BeanInfo info = Introspector.getBeanInfo(cellSetupClazz);
            PropertyDescriptor[] pds = info.getPropertyDescriptors();
            for (PropertyDescriptor desc : pds) {
                if (String.class.isAssignableFrom(desc.getPropertyType())) {
                    Method readMethod = desc.getReadMethod();
                    
                    try {
                        String value = (String) readMethod.invoke(cellSetup);
                        if (value != null) {
                            properties.put(desc.getName(), value);
                        }
                    } catch (Exception ex) {
                        logger.log(Level.WARNING, "Error reading property " + 
                                   desc.getName(), ex);
                    }
                }
            }
        
        } catch (IntrospectionException ie) {
            throw new RuntimeException("Error introspecting bean: " + cellSetup, 
                                       ie);
        }
        
        return properties;
    }
    
    protected void setProperties(BasicCellGLOSetup setup, 
                                 Map<String, String> props)
    {
        CellSetup cellSetup     = setup.getCellSetup();
        Class<?> cellSetupClazz = cellSetup.getClass();
        
        try {
            BeanInfo info = Introspector.getBeanInfo(cellSetupClazz);
            for (Map.Entry<String, String> me : props.entrySet()) {
                PropertyDescriptor desc = 
                        findDescriptor(info.getPropertyDescriptors(), me.getKey());
                
                if (desc == null) {
                    logger.log(Level.WARNING, "No property for " + me.getKey());
                    continue;
                }
                
                if (String.class.isAssignableFrom(desc.getPropertyType())) {
                    try {
                        Method writeMethod = desc.getWriteMethod();
                        writeMethod.invoke(cellSetup, me.getValue());
                    } catch (Exception ex) {
                        logger.log(Level.WARNING, "Error writing property " + 
                                   desc.getName(), ex);
                    }
                }
            }
        } catch (IntrospectionException ie) {
            throw new RuntimeException("Error introspecting bean: " + cellSetup, 
                                       ie);
        }
    }
    
    private PropertyDescriptor findDescriptor(PropertyDescriptor[] pds,
                                              String name)
    {
        for (PropertyDescriptor desc : pds) {
            if (desc.getName().equals(name)) {
                return desc;
            }
        }
        
        return null;
    }
    
    protected void setCatalog(Cell cell, Map<String, Object> metadata) {
        String uriStr = (String) metadata.get("WorldBuilder.CatalogURI");
        if (uriStr != null) {
            try {
                cell.setCatalogURI(new URI(uriStr));   
            } catch (URISyntaxException use) {
                logger.log(Level.WARNING, "Error setting URI: " + uriStr, use);
            }
        }
        
        String idStr  = (String) metadata.get("WorldBuilder.CatalogID");
        if (idStr != null) {
            cell.setCatalogID(Integer.parseInt(idStr));
        }
    }
    
    protected void setMetadata(BasicCellGLOSetup out, Cell cell) {
        Map<String, Object> metadata = new HashMap<String, Object>();
        
        if (cell.getCatalogURI() != null) {
            metadata.put("WorldBuilder.CatalogURI", cell.getCatalogURI().toString());
        }
        
        if (cell.getCatalogID() != null) {
            metadata.put("WorldBuilder.CatalogID", String.valueOf(cell.getCatalogID()));
        }
        
        out.setMetadata(metadata);
    }
}
