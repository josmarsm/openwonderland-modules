/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.common;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example extension of the default Metadata base class.
 *
 * @author Matt
 */

@MetadataType
@MetadataContextMenuItem
public class SimpleMetadata extends Metadata{
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
