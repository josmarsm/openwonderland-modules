/*
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

package org.jdesktop.wonderland.modules.annotations.common;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.modules.metadata.common.MetadataValue;
import org.jdesktop.wonderland.modules.metadata.common.annotations.MetadataContextMenuItem;
import org.jdesktop.wonderland.modules.metadata.common.annotations.MetadataType;
import org.jdesktop.wonderland.modules.metadata.common.basetypes.SimpleMetadata;

/**
 * Metadata system provides backend for Annotations, providing search access
 * and persistence.
 *
 * @author mabonner
 */

@MetadataType
@MetadataContextMenuItem
public class Annotation extends SimpleMetadata{
  // All members in an Annotation will be preserved
  // To make information searchable via the Metadata system as well, add it
  // to this object as TEXT_ATTR and SUBJ_ATTR are using put, and match with a
  // MetadataValue

  // graphical settings
  AnnotationSettings settings = null;
  // position in world
  Vector3f translation = null;

  public static final String REPLY_ATTR = "Reply To";
  public static final String SUBJ_ATTR = "Subject";

  public Annotation(){
    super();
    put(TEXT_ATTR, new MetadataValue("No Text"));
    put(SUBJ_ATTR, new MetadataValue("No Subject"));
  }

  public Annotation(String t, String s){
    super();
    if(t == null || t.length() == 0){
      t = "no text";
    }
    put(TEXT_ATTR, new MetadataValue(t));
    if(s == null || s.length() == 0){
      s = "No Subject";
    }
    put(SUBJ_ATTR, new MetadataValue(s));
  }

  @Override
  public String simpleName(){
      return "Annotation Metadata";
  }

  public String getSubject(){
    return get(SUBJ_ATTR).getVal();
  }

  public void setSubject(String s){
    put(SUBJ_ATTR, new MetadataValue(s));
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

  public AnnotationSettings getSettings() {
    return settings;
  }

  public Vector3f getTranslation() {
    return translation;
  }

  public void setSettings(AnnotationSettings settings) {
    this.settings = settings;
  }

  public void setTranslation(Vector3f translation) {
    this.translation = translation;
  }

  /**
    * The in-world location associated with this metadata. The default implementation
    * returns null, which will cause the search's goto button to use the parent
    * cell's location.
    * @return location
    */
  @Override
  public Vector3f getLocation() {
    return this.translation;
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