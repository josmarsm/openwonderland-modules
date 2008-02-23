/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
