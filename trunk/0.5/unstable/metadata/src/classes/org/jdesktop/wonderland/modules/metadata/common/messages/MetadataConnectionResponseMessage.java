/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.common.messages;

/**
 *
 * @author mabonner
 */
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * Send an update to this client's permissions
 * @author jkaplan
 */
public class MetadataConnectionResponseMessage extends ResponseMessage {
  private static Logger logger = Logger.getLogger(MetadataConnectionResponseMessage.class.getName());
  private HashMap<CellID, Set<Integer> > results;

  public MetadataConnectionResponseMessage(MessageID id, HashMap<CellID, Set<Integer> > res) {
    super(id);
    results = res;
  }

  public HashMap<CellID, Set<Integer> > getResults(){
    return results;
  }

}