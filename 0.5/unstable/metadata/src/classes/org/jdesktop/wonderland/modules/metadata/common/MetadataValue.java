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
    /**
     * whether or not the value may be changed by the user, e.g. in the cell
     * properties pane
     */
    public boolean editable;
    /**
     * whether or not the value may changed at all
     */
    public boolean valIsFinal;
    public final Datatype type;

    public MetadataValue(){
        val = "";
        editable = true;
        displayInProperties = true;
        type = Datatype.STRING;
    }

    /**
     * copy constructor
     * @param v
     */
    public MetadataValue(MetadataValue v){
        val = v.val;
        editable = v.editable;
        displayInProperties = v.displayInProperties;
        type = v.type;
        valIsFinal = false;
    }

    public MetadataValue(String v){
        val = v;
        editable = true;
        displayInProperties = true;
        type = Datatype.STRING;
        valIsFinal = false;
    }

    public MetadataValue(String v, boolean e){
        val = v;
        editable = e;
        displayInProperties = true;
        type = Datatype.STRING;
        valIsFinal = false;
    }

    public MetadataValue(String v, boolean e, boolean d, boolean vf, Datatype t){
        val = v;
        editable = e;
        displayInProperties = d;
        type = t;
        valIsFinal = vf;
    }

    public void setVal(String v) throws MetadataException{
        if(valIsFinal){
            throw (new MetadataException("MetadataValue is final cannot be changed!!"));
        }
        val = v;
    }

    public String getVal(){
        return val;
    }

  /**
   * depends only on value and datatype, does not check boolean settings like
   * editable etc.
   * @param obj
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MetadataValue other = (MetadataValue) obj;
    if ((this.val == null) ? (other.val != null) : !this.val.equals(other.val)) {
      return false;
    }
    if (this.type != other.type) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 11 * hash + (this.val != null ? this.val.hashCode() : 0);
    hash = 11 * hash + this.type.hashCode();
    return hash;
  }


}
