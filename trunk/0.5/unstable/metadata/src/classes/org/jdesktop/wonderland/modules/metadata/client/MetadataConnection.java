/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.client;

import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.metadata.common.MetadataConnectionType;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSearchFilters;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataConnectionMessage;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataConnectionResponseMessage;

/**
 *
 * @author Matt
 */
public class MetadataConnection extends BaseConnection{
  private static Logger logger = Logger.getLogger(MetadataConnection.class.getName());

  private static MetadataConnection ref;
  private static final Object lock = new Object();

  @Override
  public void handleMessage(Message message) {
    logger.log(Level.INFO, "[META CONN] handle message");
    // need to give results to some listener here..
    // per cellcacheconnection, message needs to store pointer to what should be notified?
    // or things attach as listeners to connection..
  }

  public ConnectionType getConnectionType() {
    return MetadataConnectionType.CONN_TYPE;
  }

  private MetadataConnection(){
    // singleton, so any module can ask for instance to get the connection
  }

  public static MetadataConnection getInstance() {
    synchronized(lock){
      if(ref == null) {
         ref = new MetadataConnection();
      }
      return ref;
    }
  }

  /**
   * Build a search message, tell service to search, wait for results.
   * @param filters
   * @return
   */
  public HashMap<CellID, Set<Integer>> search(MetadataSearchFilters filters) {
    MetadataConnectionMessage msg = new MetadataConnectionMessage(filters,
            MetadataConnectionMessage.Action.SEARCH);
    MetadataConnectionResponseMessage res = null;
    try {
      res = (MetadataConnectionResponseMessage) sendAndWait(msg);
    } catch (InterruptedException ex) {
      logger.severe("[META CONN] search interrupted - disconnected");
    }
    return res.getResults();
  }

}
