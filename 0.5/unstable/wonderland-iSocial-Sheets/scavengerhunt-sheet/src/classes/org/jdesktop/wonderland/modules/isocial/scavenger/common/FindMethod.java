/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Enumerates method for finding objects within scavenger hunt.
 *
 * @author Vladimir Djurovic
 */
@XmlRootElement (name = "find-method")
public class FindMethod implements Serializable {
    
    public static final int LEFT_CLICK = 0;
    public static final int PROXIMITY = 1;
    public static final int RIGHT_CLICK = 2;
    
    
    
    private String value;
    private String param;
    private String type;
    
    public FindMethod(int type, String value, String param){
        this.type = Integer.toString(type);
        this.value = value;
        this.param = param;
    }
    
    public FindMethod(){
        this(LEFT_CLICK, null,null);
    }

    public String getValue() {
        return value;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getType() {
        return type;
    }
    

    public void setType(String type) {
        this.type = type;
    }
    

    public int getFindType() {
        return Integer.parseInt(type);
    }
    
}
