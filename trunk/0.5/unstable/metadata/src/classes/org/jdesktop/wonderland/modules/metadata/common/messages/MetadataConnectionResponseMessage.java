/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.common.messages;

/**
 *
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

  // cell id to..
  // server state Set<Integer>
  // hits

  public MetadataConnectionResponseMessage(MessageID id, ArrayList<MetadataCellInfo> res) {
    super(id);
    for(MetadataCellInfo i:res){
      results.put(i.getCellID(), i);
    }
  }

  public MetadataCellInfo getCellResults(CellID id){
    return results.get(id);
  }

  public HashMap<CellID, MetadataCellInfo> getResults(){
    return results;
  }

}