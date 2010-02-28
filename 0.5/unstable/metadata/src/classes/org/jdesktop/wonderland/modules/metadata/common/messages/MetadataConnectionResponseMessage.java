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
 * MetadataService's response to a client on the MetadataConnection. Currently
 * used to return search results.
 * @author mabonner
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * Response to a MetadataConnectionMessage (thus far, only searches)
 * @author mabonner
 */
public class MetadataConnectionResponseMessage extends ResponseMessage {
  private static Logger logger = Logger.getLogger(MetadataConnectionResponseMessage.class.getName());
  private HashMap<CellID, MetadataCellInfo> results = new HashMap<CellID, MetadataCellInfo>();

  /**
   * Constructor
   * @param id message id for super
   * @param res list of metadata cell info results
   */
  public MetadataConnectionResponseMessage(MessageID id, ArrayList<MetadataCellInfo> res) {
    super(id);
    for(MetadataCellInfo i:res){
      results.put(i.getCellID(), i);
    }
  }

  /**
   * Return results for cell with CellID id
   * @param id
   * @return
   */
  public MetadataCellInfo getCellResults(CellID id){
    return results.get(id);
  }

  /**
   * Return all results
   * @return
   */
  public HashMap<CellID, MetadataCellInfo> getResults(){
    return results;
  }

}