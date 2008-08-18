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

import com.sun.ws.rest.impl.json.JSONJAXBContext;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.worldbuilder.Cell;

/**
 *
 * @author jkaplan
 */
@XmlRootElement(name="cell")
public class TreeCellWrapper extends CellWrapper {
    private static final Logger logger =
            Logger.getLogger(TreeCellWrapper.class.getName());
   
    private int maxDepth;
    private boolean topLevel;
    
    public TreeCellWrapper() {
        super ();
    }
    
    public TreeCellWrapper(Cell cell, URI uri, int maxDepth, boolean topLevel) {
        super (cell, uri);
        
        this.maxDepth = maxDepth;
        this.topLevel = topLevel;
    }
    
    @XmlTransient
    public int getMaxDepth() {
        return maxDepth;
    }

    @Override
    public CellRefWrapper getParentRef() {
        if (!topLevel) {
            return null;
        }
        
        return super.getParentRef();
    }
    
    @Override
    protected CellsWrapper getChildWrapper() {
        if (getMaxDepth() == 0) {
            return super.getChildWrapper();
        }
        
        Collection<CellWrapper> childList = new ArrayList(getCell().getChildren().size());
        for (Cell child : getCell().getChildren()) {
            URI childURI = getURI().resolve(child.getCellID());
            childList.add(new TreeCellWrapper(child, childURI, 
                                              getMaxDepth() - 1, false)); 
        }
        
        return new CellsWrapper(childList, true);
    }
    
    @Provider
    public static class JAXBContextResolver implements ContextResolver<JAXBContext> {
        private JAXBContext context;
        private Class[] types = { TreeCellWrapper.class };

        public JAXBContextResolver() throws Exception {
            Map<String, Object> props = new HashMap<String, Object>();
            props.put(JSONJAXBContext.JSON_NOTATION, "BADGERFISH");
            props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.FALSE);
            this.context = new JSONJAXBContext(types, props);
        }

        public JAXBContext getContext(Class<?> objectType) {
            return (types[0].equals(objectType)) ? context : null;
        }
    }
}
