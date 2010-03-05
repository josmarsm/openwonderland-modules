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

package org.jdesktop.wonderland.modules.metadata.server;

import com.sun.sgs.app.AppContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.metadata.common.MetadataComponentServerState;
import org.jdesktop.wonderland.modules.metadata.common.MetadataConnectionType;
import org.jdesktop.wonderland.modules.metadata.common.MetadataID;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataCellInfo;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataConnectionMessage;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataConnectionModMessage;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataConnectionResponseMessage;
import org.jdesktop.wonderland.modules.metadata.server.service.MetadataManager;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author mabonner
 */
public class MetadataConnectionHandler implements ClientConnectionHandler, Serializable {

  private static final Logger logger =
            Logger.getLogger(MetadataConnectionHandler.class.getName());
            
  /**
  * used to send a message to a particular client
  */
  HashMap <WonderlandClientID, WonderlandClientSender> senders = new HashMap <WonderlandClientID, WonderlandClientSender>();

  public MetadataConnectionHandler() {
  }

  public ConnectionType getConnectionType() {
    return MetadataConnectionType.CONN_TYPE;
  }

  public void registered(WonderlandClientSender sender) {
    //    logger.info("[metaconnhandler] sender registered");
  }

  public void clientConnected(WonderlandClientSender sender, WonderlandClientID clientID, Properties properties) {
   logger.info("[metaconnhandler] client connected");
   senders.put(clientID, sender);
  }

  public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, Message message) {
    MetadataConnectionMessage m = (MetadataConnectionMessage) message;
    // for now, the only message type is to search, tell service to search based on contents
    MetadataManager metaService = AppContext.getManager(MetadataManager.class);
    MetadataConnectionResponseMessage response;
    HashMap<CellID, Set<MetadataID> > results;
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
        // for every cell in results entry set,
        // get metadata from cell server state
        // build cellInfo object with cid, metadata, and hits
        ArrayList<MetadataCellInfo> cellInfo = new ArrayList<MetadataCellInfo>();
        for(Entry<CellID, Set<MetadataID>> e : results.entrySet()){
          CellID cid = e.getKey();
          // CellManager -> CellMO -> ComponentMO -> Metadata info!
          // CellMO also has the cell name
          CellMO cell = CellManagerMO.getCell(cid);
          if(cell == null){
            logger.severe("cell is null!!!");
          }
          if(MetadataComponentMO.class == null){
            logger.severe("compo class is null!?");
          }
          CellComponentMO compoMO = cell.getComponent(MetadataComponentMO.class);
          logger.info("made it");
          MetadataComponentServerState state =
                  (MetadataComponentServerState) compoMO.getServerState(null);
          // build cell info object
          cellInfo.add(new MetadataCellInfo(cid, state.getAllMetadata(),
                  e.getValue(), cell.getName(), cell.getWorldBounds().getCenter()));

        }

        // create a response message
        response = new MetadataConnectionResponseMessage(message.getMessageID(), cellInfo);
        logger.info("[MCH] results size " + results.size());
        sender.send(clientID, response);
        break;
      case ADD_MOD_LISTENER:
        metaService.addClientMetadataModificationListener(m.getModListener(), clientID);
        break;
      case REMOVE_MOD_LISTENER:
        metaService.removeClientMetadataModificationListener(m.getModListener(), clientID);
        break;
    }
    // appcontext.getmanager

    // build response msg, send back
  }

  public void clientDisconnected(WonderlandClientSender sender, WonderlandClientID clientID) {
   logger.info("[metaconnhandler] client disc");
   senders.remove(clientID);
  }

  public void sendClientMessage(WonderlandClientID wlcid, MetadataConnectionModMessage msg) {
    if(!senders.containsKey(wlcid)){
      logger.severe("trying to send a message to clientID that has not connected: cid " + wlcid);
      return;
    }

    senders.get(wlcid).send(wlcid, msg);
  }

}
