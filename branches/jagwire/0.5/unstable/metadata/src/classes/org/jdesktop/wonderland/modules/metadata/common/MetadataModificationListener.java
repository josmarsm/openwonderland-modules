/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import java.io.Serializable;

/**
 * Listen to metadata service, be informed every time a like piece of metadata
 * is added/removed/modified. Set cell ID to null to match for any cell. Set metadata
 * to null to match for any metadata.
 *
 * Important Note: metadata fields are currently not examined, only the type
 * of metadata must match.
 * @param cellid CellID (null for any)
 * @param meta metadata type (null for any)
 * @author mabonner
 */
abstract public class MetadataModificationListener implements Serializable {
  private CellID cid;
  private Metadata meta;

  public MetadataModificationListener(CellID cellid, Metadata metadata) {
    cid = cellid;
    meta = metadata;
  }

  public boolean match(CellID cellid, Metadata metadata) {
    if((cid == null || cid == cellid) &&
            (meta == null || meta.getClass() == metadata.getClass())){
      return true;
    }
    return false;
  }

  @Override
  public boolean equals(Object obj){
    if (getClass() != obj.getClass()) {
      return false;
    }

    MetadataModificationListener l = (MetadataModificationListener) obj;

    if(this.cid != l.cid){
      return false;
    }

    if(this.meta != l.meta){
      return false;
    }

    return true;
  }

  @Override
  public String toString(){
    String s = "MetaModListener cid: " + cid + " metadata: " + meta;
    return s;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 37 * hash + (this.cid != null ? this.cid.hashCode() : 0);
    hash = 37 * hash + (this.meta != null ? this.meta.hashCode() : 0);
    return hash;
  }

  public CellID getCellID() {
    return cid;
  }

  /**
   * Note: this returns the matching metadata criteria (in the future, could include
   * regular expressions, though at the moment this only matches by type). Does
   * NOT return the actual, changed metadata.
   * @return
   */
  public Metadata getMetadataFilter() {
    return meta;
  }



  abstract public void fireAlert(CellID cid, Metadata metadata);

}
