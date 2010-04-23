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

import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.AppContext;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;

import org.jdesktop.wonderland.modules.metadata.common.MetadataComponentServerState;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.MetadataID;
import org.jdesktop.wonderland.modules.metadata.common.ModifyCacheAction;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataCacheMessage;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataCacheResponseMessage;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataModMessage;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataModResponseMessage;
import org.jdesktop.wonderland.modules.metadata.server.service.MetadataManager;

import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * Metadata component
 *
 * @author mabonner
 */
public class MetadataComponentMO extends CellComponentMO {

    private static Logger logger = Logger.getLogger(MetadataComponentMO.class.getName());
    private String info = null;
    private MetadataComponentServerState mcss;
    
    /** the channel component */
    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelRef;
    

    // @UsesCellComponentMO(SampleCellSubComponentMO.class)
    // private ManagedReference<SampleCellSubComponentMO> subComponentRef;
    
    public MetadataComponentMO(CellMO cell) {
        super(cell);
        mcss = new MetadataComponentServerState();
        MetadataManager metaService = AppContext.getManager(MetadataManager.class);
        metaService.addCell(this.cellID);
        logger.info("[METADATA COMPONENT] MO created");
    }
    

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.metadata.client.MetadataComponent";
    }
    // 
    @Override
    protected void setLive(boolean live) {
        logger.info("[MetadataComponentMO] setLive: " + live);

        super.setLive(live);

        if (live) {
            logger.info("[METADATA COMPONENT] register mod receiver");
            channelRef.getForUpdate().addMessageReceiver(MetadataModMessage.class,
                                                  new ModMessageReceiver(cellRef.get(), this, channelRef));
            channelRef.getForUpdate().addMessageReceiver(MetadataCacheMessage.class,
                                                  new CacheMessageReceiver(cellRef.get(), this, channelRef));
        } else {
            // unregister 

            channelRef.getForUpdate().removeMessageReceiver(MetadataModMessage.class);
            channelRef.getForUpdate().removeMessageReceiver(MetadataCacheMessage.class);
        }
    }
    
    /**
     * Message receiver to handle adds, removes, modifys, and metadata requests.
     */
    static final class ModMessageReceiver extends AbstractComponentMessageReceiver {
        // private ManagedReference<MetadataComponentMO> componentRef;
        private ManagedReference<MetadataComponentMO> componentRef;
        private ManagedReference<ChannelComponentMO> channelComponentRef;

        public ModMessageReceiver(CellMO cellMO, MetadataComponentMO component,
                ManagedReference<ChannelComponentMO>channel) {
            super (cellMO);
            componentRef = AppContext.getDataManager().createReference(component);
            channelComponentRef = channel;
        }
        
        @Override
        public void messageReceived(WonderlandClientSender sender,
                                    WonderlandClientID clientID,
                                    CellMessage message)
        {
            logger.info("[METADATA COMPONENT MO] mod message received... ");
            MetadataModMessage msg = (MetadataModMessage) message;

            if(msg.action != null){
              // call corresponding methods on component, and send appropriate
              // response to all clients
              MetadataModResponseMessage response = new MetadataModResponseMessage(msg);
              switch (msg.action){
                  case ADD:
                      logger.info("[METADATA COMPONENT MO] add metadata ");
                      componentRef.get().add(msg.metadata);
                      break;
                  case REMOVE:
                      logger.info("[METADATA COMPONENT MO] remove metadata... ");
                      componentRef.get().remove(msg.metadata);
                      break;
                  case MODIFY:
                      logger.info("[METADATA COMPONENT MO] mod metadata... ");
                      componentRef.get().modify(msg.metadata);
                      break;
              }
              channelComponentRef.getForUpdate().sendAll(clientID, response);
            }
//            MetadataManager metaService = AppContext.getManager(MetadataManager.class);
            // metaService.test();
            // componentRef.get().sendUserPermissions(sender, clientID,
            //                                                    message.getMessageID());
        }
    }

    /**
     * Message receiver to handle adds, removes, modifys, and metadata requests.
     */
    static final class CacheMessageReceiver extends AbstractComponentMessageReceiver {
        // private ManagedReference<MetadataComponentMO> componentRef;
        private ManagedReference<MetadataComponentMO> componentRef;
        private ManagedReference<ChannelComponentMO> channelComponentRef;

        public CacheMessageReceiver(CellMO cellMO, MetadataComponentMO component,
                ManagedReference<ChannelComponentMO>channel) {
            super (cellMO);
            componentRef = AppContext.getDataManager().createReference(component);
            channelComponentRef = channel;
        }

        @Override
        public void messageReceived(WonderlandClientSender sender,
                                    WonderlandClientID clientID,
                                    CellMessage message)
        {
          logger.info("[METADATA COMPONENT MO] cache message received... ");
          MetadataCacheMessage msg = (MetadataCacheMessage) message;


          MetadataManager metaService = AppContext.getManager(MetadataManager.class);

          // create a response message
          MetadataComponentServerState mcss =
                  (MetadataComponentServerState) componentRef.get().getServerState(null);
          LinkedHashMap<MetadataID, Metadata> metadata = mcss.getAllMetadataMap();
          MetadataCacheResponseMessage response = 
                  new MetadataCacheResponseMessage(message.getMessageID(), metadata);
          logger.info("[MCH] returning " + metadata.values().size() + " pieces of metadata");
          sender.send(clientID, response);
          logger.info("[MCH] sent!");
        }
    }


    // @Override
    // public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
    //     if (state == null) {
    //         state = new MetadataComponentClientState();
    //     }
    //     
    //     return super.getClientState(state, clientID, capabilities);
    // }
    
    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if (state == null) {
            state = new MetadataComponentServerState();
        }
        state = mcss;
        return super.getServerState(state);
    }

    /**
     * Obliterates old server state (and accompanying MetadataService information
     * on this cell), replacing it with a new state. If you only wish to add a single
     * new piece of metadata, or remove/modify an existing piece, use the
     * add/remove/modify functions instead.
     *
     * Notifies all client components to invalidate their caches.
     *
     * @param state the component's new server state.
     */
    @Override
    public void setServerState(CellComponentServerState state) {
      //      TODO
      // in the future, could diff past and present state
      // for now, just erase everything under the cell in search DB
      // and replace with new data
      super.setServerState(state);
      MetadataComponentServerState s = (MetadataComponentServerState) state;
      MetadataComponentServerState s0 = (MetadataComponentServerState) getServerState(null);
      mcss = (MetadataComponentServerState) state;
      logger.info("[METADATA COMPONENT MO] set server state.. count was  " + s0.metaCount() + " and is now " + s.metaCount());

      // notify service to re-populate cell
      MetadataManager metaService = AppContext.getManager(MetadataManager.class);
      metaService.setCellMetadata(this.cellID, mcss.getAllMetadata());

      // notify components to invalidate caches
      // first argument null = sent from server

      // notify client-side component of cache change, will pass along message
      // to any cache listeners
      if(channelRef != null){
        channelRef.getForUpdate().sendAll(null,
              new MetadataModResponseMessage(ModifyCacheAction.INVALIDATE, null));
      }
    }


    /**
     * add a piece of metadata to the cell. Preserve it in the server state and
     * communicate the change to the metadata service.
     * @param meta
     */
    public void add(Metadata meta){
      // add component to server state
      logger.info("[METADATA COMPONENT MO] add metadata, passed in metadata is:\n" + meta);
      MetadataComponentServerState state = (MetadataComponentServerState) getServerState(null);
      logger.info("current # of metadata in server state --- " + state.metaCount());
      state.addMetadata(meta);
      mcss = state;
      logger.info("new # --- " + state.metaCount());

      // notify service
      MetadataManager metaService = AppContext.getManager(MetadataManager.class);
      metaService.addMetadata(this.cellID, meta);

      // notify client-side component of cache change, will pass along message
      // to any cache listeners
      channelRef.getForUpdate().sendAll(null,
            new MetadataModResponseMessage(ModifyCacheAction.ADD, meta));
      
    }

    /**
     * remove a piece of metadata to the cell. Preserve it in the server state and
     * communicate the change to the metadata service.
     * @param meta
     */
    public void remove(Metadata meta){
      // remove component from server state
      logger.info("[METADATA COMPONENT MO] remove metadata, passed in metadata is:\n" + meta);
      MetadataComponentServerState state = (MetadataComponentServerState) getServerState(null);
      logger.info("current # of metadata in server state --- " + state.metaCount());
      state.removeMetadata(meta);
      mcss = state;
      logger.info("new # --- " + state.metaCount());

      // notify service
      MetadataManager metaService = AppContext.getManager(MetadataManager.class);
      metaService.removeMetadata(this.cellID, meta.getID(), meta);

      // notify client-side component of cache change, will pass along message
      // to any cache listeners
      channelRef.getForUpdate().sendAll(null,
            new MetadataModResponseMessage(ModifyCacheAction.REMOVE, meta));
    }

    /**
     * modify a piece of metadata on the cell. Preserve it in the server state and
     * communicate the change to the metadata service.
     *
     * actually works by removing the old cell and adding the new cell.
     * @param meta the modified piece of metadata
     */
    public void modify(Metadata meta){
      logger.info("[METADATA COMPONENT MO] modify metadata, passed in metadata is:\n" + meta);
      // check if this piece of metadata is contained
      if(!mcss.contains(meta.getID())){
        logger.severe("[METADATA COMPO MO] Error: attempting to modify metadata with" +
                "id " + meta.getID() + " which is not present in cell with id" + cellID);
      }

      // if so, remove the old one from compo state and service
//      MetadataComponentServerState state = (MetadataComponentServerState) getServerState(null);
      MetadataManager metaService = AppContext.getManager(MetadataManager.class);
      logger.info("current # of metadata in server state --- " + mcss.metaCount());
      mcss.removeMetadata(meta);
      // and add the new
      mcss.addMetadata(meta);
      metaService.modifyMetadata(cellID, meta.getID(), meta);

      // notify client-side component of cache change, will pass along message
      // to any cache listeners
      channelRef.getForUpdate().sendAll(null,
            new MetadataModResponseMessage(ModifyCacheAction.MODIFY, meta));
    }
}
