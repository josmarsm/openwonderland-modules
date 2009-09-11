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
import org.jdesktop.wonderland.modules.metadata.common.MetadataValue;
import org.jdesktop.wonderland.modules.metadata.common.annotations.MetadataContextMenuItem;
import org.jdesktop.wonderland.modules.metadata.common.annotations.MetadataType;

/**
 * Example extension of the default Metadata base class.
 *
 * @author mabonner
 */

@MetadataType
@MetadataContextMenuItem
public class SimpleMetadata extends BaseMetadata{
  public static final String TEXT_ATTR = "Text";
  
  public SimpleMetadata(){
    super();
    put(TEXT_ATTR, new MetadataValue(""));
  }

  public SimpleMetadata(String t){
      super();
      put(TEXT_ATTR, new MetadataValue(t));
  }

  @Override
  public String simpleName(){
      return "Simple Metadata";
  }

  // unlike its parent, this type appears in every cell's context menu
  @Override
  public boolean contextMenuCheck(Class c) {
    return true;
  }

  // simple getter/setter for text
  public String getText(){
    return get(TEXT_ATTR).getVal();
  }

  // simple getter/setter for text
  public void setText(String s){
    put(TEXT_ATTR, new MetadataValue(s));
  }

  // a metadata subclass that wishes to report an associated location would
  // need to overwrite this. The goto functionality in SearchResultsForm will
  // send users to the parent cell's location if this returns null.
//  @Override
//  public Vector3f getLocation() {
//    return null;
//  }

//  @Override
//  public boolean contextMenuCheck(Cell c) {
//    return true;
//  }

  // a metadata subclass that needs to populate fields with default fields
  // on a client init should override initByClient to do so. Most subclasses
  // should begin by calling their super class's initByClient
//  @Override
//  public void initByClient(WonderlandIdentity id) {
//    super.initByClient(id);
//    // init subtype here
//  }

}
