/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.common.messages;

/**
 * Stores information about a cell that was matched in a metadata search. Stores
 * the CellID, positional information for gotos, the cell's metadata,
 * and the id's of which metadata in this cell were matches
 *
 * Send a MetadataCellInfo for each cell result in a search. This saves sending
 * many messages requesting their information one by one
 *
 * for the search.
 * @author mabonner
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSPI;


public class MetadataCellInfo implements Serializable{
  private static Logger logger = Logger.getLogger(MetadataCellInfo.class.getName());
  // TODO
  // good style to make them public since they are final? or should I still have getters?
  private final CellID cid;
  private final ArrayList<MetadataSPI> metadata;
  private final Set<Integer> hits;
  private final String name;


  public MetadataCellInfo(CellID c, ArrayList<MetadataSPI> m, Set<Integer> h, String n){
    // TODO store position as well
    cid = c;
    metadata = m;
    hits = h;
    name = n;
  }

  public CellID getCellID(){
    return cid;
  }

  public ArrayList<MetadataSPI> getMetadata(){
    return metadata;
  }

  public Set<Integer> getHits(){
    return hits;
  }

  public String getName(){
    return name;
  }

}
