/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.metadata.common.basetypes;

import com.jme.math.Vector3f;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.metadata.common.MetadataException;
import org.jdesktop.wonderland.modules.metadata.common.MetadataID;
import org.jdesktop.wonderland.modules.metadata.common.annotations.MetadataContextMenuItem;
import org.jdesktop.wonderland.modules.metadata.common.annotations.MetadataType;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.MetadataValue;
import org.jdesktop.wonderland.modules.metadata.common.MetadataValue.Datatype;


/**
 * A default base class for the MetadataSystem, and default implementation
 * of the Metadata interface.
 *
 * @author mabonner
 */
@MetadataType
@MetadataContextMenuItem
public class BaseMetadata implements Serializable, Metadata {
    public final MetadataID id; // unique id
    // TODO does this need to be synchronized in some way?
    // cellregistry didn't seem to sync on cellid's...
    private static int count;

    /**
     * this DateFormat to be used in all default date fields
     */
    public static final DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy h:mm:ss a");
    /**
     * access strings for default values
     */
    public static final String CREATOR_ATTR = "Creator";
    public static final String CREATED_ATTR = "Created";
    public static final String MODIFIER_ATTR = "Modifier";
    public static final String MODIFIED_ATTR = "Modified";

    

    private HashMap<String, MetadataValue> attributes;

    /**
     * Subclasses should include all attributes and any default values
     * by adding them to attributes in their constructor. To work with the
     * default LDAP implementation, attribute names should contain only
     * alpha-numeric and space characters, and should not begin with a numeric
     * character.
     *
     * when a user adds a metadata object in the cell properties panel,
     * a blank object of the appropriate type is created.
     *
     * @return
     */
    public BaseMetadata(){
        attributes = new HashMap<String, MetadataValue>();
        // defaults
        put(CREATOR_ATTR, new MetadataValue(null, false, true, false, Datatype.STRING));
        put(CREATED_ATTR, new MetadataValue(null, false, true, false, Datatype.DATE));
        put(MODIFIER_ATTR, new MetadataValue(null, false, true, false, Datatype.STRING));
        put(MODIFIED_ATTR, new MetadataValue(null, false, true, false, Datatype.DATE));
        id = new MetadataID(count++);
        return;
    }

    public BaseMetadata(String c, String cd, String m, String md){
        attributes = new HashMap<String, MetadataValue>();
        // defaults
        put(CREATOR_ATTR, new MetadataValue(c, false, true, false, Datatype.STRING));
        put(CREATED_ATTR, new MetadataValue(cd, false, true, false, Datatype.DATE));
        put(MODIFIER_ATTR, new MetadataValue(m, false, true, false, Datatype.STRING));
        put(MODIFIED_ATTR, new MetadataValue(md, false, true, false, Datatype.DATE));
        id = new MetadataID(count++);
        return;
    }

    /**
     * Subclasses should overwrite this and provide their own human
     * readable type name.
     * This is displayed on the cell properties, context menus, etc.
     * @return
     */
    public String simpleName(){
        return "Metadata";
    }

    public MetadataValue get(String attr){
        return attributes.get(attr);
    }

    public void put(String attr, MetadataValue val){
      attributes.put(attr, val);
    }

    public void put(String attr, String val) throws MetadataException{
      if(!attributes.containsKey(attr)){
        throw new MetadataException("metadata error: attempt to put new value \""
                + val + "\" in attribute \"" + attr + "\" which has not been bound" +
                "\n use put(String, MetadataValue) first");
      }
      MetadataValue oldVal = attributes.get(attr);
      try {
        oldVal.setVal(val);
      } catch (Exception ex) {
        Logger.getLogger(BaseMetadata.class.getName()).log(Level.SEVERE, null, ex);
      }
      attributes.put(attr, oldVal);
    }


    public void remove(String attr){
      attributes.remove(attr);
    }

    public Set<Map.Entry<String, MetadataValue>> getAttributes(){
        return attributes.entrySet();
    }

    public void initByClient(WonderlandIdentity id) {
      Date date = new Date();
      String created = dateFormat.format(date);
      id.getUsername();
      try {
        put(CREATOR_ATTR, id.getUsername());
        put(CREATED_ATTR, created);
        put(MODIFIER_ATTR, id.getUsername());
        put(MODIFIED_ATTR, created);
      } catch (MetadataException ex) {
        Logger.getLogger(BaseMetadata.class.getName()).log(Level.SEVERE, null, "error creating new" +
                " metadata object via initByClient. Exception:" + ex);
      }
    }

  @Override
  public String toString(){
    String s = "-- ID:" + getID() + " -- " + this.getClass().getName() + " --\n";
    for(Entry<String, MetadataValue> e:getAttributes()){
      s += e.getKey() + ": " + e.getValue().getVal() + "\n";
    }
    return s;
  }

  public MetadataID getID() {
    return id;
  }

  // this type appears only in the sample metadata cell's context menu
  public boolean contextMenuCheck(Class c) {
    if(c.getName().equals("org.jdesktop.wonderland.modules.metadata.client.samplecell.MetadataSampleCell")){
      return true;
    }
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BaseMetadata other = (BaseMetadata) obj;
    if (this.id != other.id) {
      return false;
    }
    if (this.attributes != other.attributes && (this.attributes == null || !this.attributes.equals(other.attributes))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + this.id.hashCode();
    hash = 31 * hash + (this.attributes != null ? this.attributes.hashCode() : 0);
    return hash;
  }

  // some simple access functions to get at each attribute
  public String getCreator(){
    return attributes.get(CREATOR_ATTR).getVal();
  }

  public String getCreated(){
    return attributes.get(CREATED_ATTR).getVal();
  }

  public String getModifier(){
    return attributes.get(MODIFIER_ATTR).getVal();
  }

  public String getModified(){
    return attributes.get(MODIFIED_ATTR).getVal();
  }

  public Vector3f getLocation() {
    return null;
  }

}
