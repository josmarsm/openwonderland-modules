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
    public enum Datatype { DATE, STRING, INTEGER }
    private String val;
    public boolean displayInProperties;
    public boolean editable;
    public final Datatype type;

    public MetadataValue(){
        val = "";
        editable = true;
        displayInProperties = true;
        type = Datatype.STRING;
    }

    public MetadataValue(String v){
        val = v;
        editable = true;
        displayInProperties = true;
        type = Datatype.STRING;
    }

    public MetadataValue(String v, boolean e){
        val = v;
        editable = e;
        displayInProperties = true;
        type = Datatype.STRING;
    }

    public MetadataValue(String v, boolean e, boolean d, Datatype t){
        val = v;
        editable = e;
        displayInProperties = d;
        type = t;
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
