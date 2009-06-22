/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.common;

import java.io.Serializable;

/**
 *
 * @author Matt
 */
public class MetadataValue implements Serializable{
    private String val;
    public boolean displayInProperties;
    public boolean editable;

    public MetadataValue(){
        val = "";
        editable = true;
        displayInProperties = true;

    }

    public MetadataValue(String v){
        val = v;
        editable = true;
        displayInProperties = true;
    }

    public MetadataValue(String v, boolean e){
        val = v;
        editable = e;
        displayInProperties = true;
    }

    public MetadataValue(String v, boolean e, boolean d){
        val = v;
        editable = e;
        displayInProperties = d;
    }

    public void setVal(String v) throws Exception{
        if(!editable){
            throw (new Exception("MetadataValue is not editable!"));
        }
        val = v;
    }

    public String getVal(){
        return val;
    }
}
