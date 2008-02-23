/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.worldbuilder.wrapper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author jkaplan
 */
@XmlRootElement(name="property")
public class PropertyWrapper {
    private String key;
    private String value;
    
    public PropertyWrapper() {
    }
    
    public PropertyWrapper(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    @XmlElement(name="key")
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    @XmlElement(name="value")
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
}
