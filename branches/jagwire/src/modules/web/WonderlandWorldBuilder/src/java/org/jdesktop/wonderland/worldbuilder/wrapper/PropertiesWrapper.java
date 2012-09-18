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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author jkaplan
 */
@XmlRootElement(name="properties")
public class PropertiesWrapper {
    private Collection<PropertyWrapper> properties;
    
    public PropertiesWrapper() {
        properties = new ArrayList();
    }
    
    public PropertiesWrapper(Map<String, String> propMap) {
        properties = new ArrayList();
        
        for (Map.Entry<String, String> me : propMap.entrySet()) {
            properties.add(new PropertyWrapper(me.getKey(), me.getValue()));
        }
    }
    
    @XmlElement(name="property")
    public Collection<PropertyWrapper> getPropertyWrappers() {
        return properties;
    }
    
    public void setPropertyWrappers(Collection<PropertyWrapper> properties) {
        this.properties = properties;
    }
    
    @XmlTransient
    public Map<String, String> getProperties() {
        Map<String, String> out = new HashMap();
        for (PropertyWrapper pr : getPropertyWrappers()) {
            out.put(pr.getKey(), pr.getValue());
        }
        return out;
    }
}
