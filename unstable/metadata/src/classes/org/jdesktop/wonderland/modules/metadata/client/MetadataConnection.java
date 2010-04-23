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

package org.jdesktop.wonderland.modules.metadata.client;

import java.util.HashMap;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.metadata.common.MetadataConnectionType;
import org.jdesktop.wonderland.modules.metadata.common.MetadataModificationListener;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSearchFilters;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataCellInfo;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataConnectionMessage;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataConnectionModMessage;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataConnectionResponseMessage;

/**
 * A connection for clients to message the MetadataService. This is currently
 * used only to make search requests.
 *
 * @author mabonner
 */
public class MetadataConnection extends BaseConnection{
  private static Logger logger = Logger.getLogger(MetadataConnection.class.getName());

  private static MetadataConnection ref;
  private static final Object lock = new Object();

  /**
   * Handle a message from the server. Currnetly, this will only be a
   * MetadataConnModMessage, as the result of previously registered mod listeners
   * from this client firing.
   * @param message
   */
  @Override
  public void handleMessage(Message message) {
    logger.info("[META CONN] handle message");
    MetadataConnectionModMessage msg = (MetadataConnectionModMessage) message;
    for(MetadataModificationListener l : msg.getListenerHits()){
      logger.info("listener hit!");
      // note: this fires using the metadata type, not the actual changed metadata
      // (as currently happens on the server).
      // this could be changed by adding the actual metadata pieces to the MetadataConnectionModMessage
      logger.info("cid " + l.getCellID() + " meta type: " + l.getMetadataFilter());
      l.fireAlert(l.getCellID(), l.getMetadataFilter());
    }
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
  public HashMap<CellID, MetadataCellInfo> search(MetadataSearchFilters filters) {
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

  public void addMetadataModificationListener(MetadataModificationListener l) {
    MetadataConnectionMessage msg = new MetadataConnectionMessage(l,
            MetadataConnectionMessage.Action.ADD_MOD_LISTENER);
    send(msg);
  }

  public void removeMetadataModificationListener(MetadataModificationListener l) {
    MetadataConnectionMessage msg = new MetadataConnectionMessage(l,
            MetadataConnectionMessage.Action.REMOVE_MOD_LISTENER);
    send(msg);
  }

}
