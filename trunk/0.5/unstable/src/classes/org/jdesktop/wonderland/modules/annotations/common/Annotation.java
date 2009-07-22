/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.annotations.common;

/**
 *
 * @author mabonner
 */
import org.jdesktop.wonderland.modules.metadata.common.MetadataValue;
import org.jdesktop.wonderland.modules.metadata.common.annotations.MetadataContextMenuItem;
import org.jdesktop.wonderland.modules.metadata.common.annotations.MetadataType;
import org.jdesktop.wonderland.modules.metadata.common.basetypes.SimpleMetadata;

/**
 * Example extension of the default Metadata base class.
 *
 * @author Matt
 */

@MetadataType
@MetadataContextMenuItem
public class Annotation extends SimpleMetadata{
  public static final String REPLY_ATTR = "Reply To";

  public Annotation(){
    super();
    put(TEXT_ATTR, new MetadataValue(""));
  }

  public Annotation(String t){
      super();
//      put(TEXT_ATTR, new MetadataValue(t));
  }

  @Override
  public String simpleName(){
      return "Annotation Metadata";
  }

  /**
   * return true (appear in context menu) for any cell
   * @param c cell in question
   * @return
   */
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