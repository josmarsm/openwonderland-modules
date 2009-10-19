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

package org.jdesktop.wonderland.modules.metadata.common.messages;

/**
 * Stores information about a cell that was matched in a metadata search. Stores
 * the CellID, positional information for gotos (TODO), the cell's metadata,
 * and the id's of which metadata in this cell were matches
 *
 * Send a MetadataCellInfo for each cell result in a search. This saves sending
 * many messages requesting their information one by one
 *
 * @author mabonner
 */

import com.jme.math.Vector3f;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.MetadataID;


public class MetadataCellInfo implements Serializable{
  private static Logger logger = Logger.getLogger(MetadataCellInfo.class.getName());
  // TODO
  // good style to make them public since they are final? or should I still have getters?
  private final CellID cid;
  private final ArrayList<Metadata> metadata;
  private final Set<MetadataID> hits;
  private final String name;
  private Vector3f cellLocation;


  public MetadataCellInfo(CellID c, ArrayList<Metadata> m, Set<MetadataID> h, String n, Vector3f loc){
    // TODO store position as well
    cid = c;
    metadata = m;
    hits = h;
    name = n;
    cellLocation = loc;
  }

  public CellID getCellID(){
    return cid;
  }

  public ArrayList<Metadata> getMetadata(){
    return metadata;
  }

  public Set<MetadataID> getHits(){
    return hits;
  }

  public String getName(){
    return name;
  }

  public Vector3f getCellLocation() {
    return cellLocation;
  }
}
