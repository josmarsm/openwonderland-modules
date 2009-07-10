/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.server;

import com.sun.sgs.app.AppContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.metadata.common.MetadataConnectionType;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataConnectionMessage;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataConnectionResponseMessage;
import org.jdesktop.wonderland.modules.metadata.server.service.MetadataManager;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author mabonner
 */
class MetadataConnectionHandler implements ClientConnectionHandler, Serializable {

  private static final Logger logger =
            Logger.getLogger(MetadataConnectionHandler.class.getName());

  public MetadataConnectionHandler() {
  }

  public ConnectionType getConnectionType() {
    return MetadataConnectionType.CONN_TYPE;
  }

  public void registered(WonderlandClientSender sender) {
    //    logger.info("[metaconnhandler] sender registered");
  }

  public void clientConnected(WonderlandClientSender sender, WonderlandClientID clientID, Properties properties) {
//    logger.info("[metaconnhandler] client connected");
  }

  public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, Message message) {
    MetadataConnectionMessage m = (MetadataConnectionMessage) message;
    // for now, the only message type is to search, tell service to search based on contents
    MetadataManager metaService = AppContext.getManager(MetadataManager.class);
    MetadataConnectionResponseMessage response;
    HashMap<CellID, Set<Integer> > results;
    switch(m.getAction()){
      case SEARCH:
        // do search
        if(m.getCellScope() == null){
          // world-wide search
          logger.info("[MCH] global search with " + m.getFilters().filterCount() + " filters");
          results = metaService.searchMetadata(m.getFilters());
        }
        else{
          results = metaService.searchMetadata(m.getFilters(), m.getCellScope());
        }

        response = new MetadataConnectionResponseMessage(message.getMessageID(), results);
        logger.info("[MCH] results size " + results.size());
        sender.send(clientID, response);
        break;
    }
    // appcontext.getmanager

    // build response msg, send back
  }

  public void clientDisconnected(WonderlandClientSender sender, WonderlandClientID clientID) {
//    logger.info("[metaconnhandler] client disc");
  }

}
