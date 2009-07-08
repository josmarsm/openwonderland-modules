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
import java.util.logging.Logger;
import java.util.logging.Level;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;

import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.MetadataComponentServerState;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSPI;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataMessage;
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
        logger.log(Level.INFO, "[METADATA COMPONENT] MO created");
    }
    

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.metadata.client.MetadataComponent";
    }
    // 
    @Override
    protected void setLive(boolean live) {
        logger.log(Level.INFO, "[MetadataComponentMO] setLive: " + live);

        super.setLive(live);

        if (live) {
            channelRef.getForUpdate().addMessageReceiver(MetadataMessage.class,
                                                  new MessageReceiver(cellRef.get(), this));
        } else {
            // unregister 

            channelRef.getForUpdate().removeMessageReceiver(MetadataMessage.class);
        }
    }
    
    /**
     * Message receiver to handle permission change requests
     */
    public static final class MessageReceiver extends AbstractComponentMessageReceiver {
        // private ManagedReference<MetadataComponentMO> componentRef;
        private ManagedReference<MetadataComponentMO> componentRef;

        public MessageReceiver(CellMO cellMO, MetadataComponentMO component) {
            super (cellMO);
            
            componentRef = AppContext.getDataManager().createReference(component);
        }
        
        @Override
        public void messageReceived(WonderlandClientSender sender,
                                    WonderlandClientID clientID,
                                    CellMessage message)
        {
            logger.log(Level.INFO, "[METADATA COMPONENT] message received... ");
            MetadataMessage msg = (MetadataMessage) message;

            if(msg.action != null){
                switch (msg.action){
                    case ADD:
                        logger.log(Level.INFO, "[METADATA COMPONENT MO] add metadata ");
                        componentRef.get().add(msg.metadata);
                        break;
                    case REMOVE:
                        logger.log(Level.INFO, "[METADATA COMPONENT MO] remove metadata... ");
                        componentRef.get().remove(msg.metadata);
                        break;
                    case MODIFY:
                        logger.log(Level.INFO, "[METADATA COMPONENT MO] mod metadata... ");
                        break;
                }
            }
            MetadataManager metaService = AppContext.getManager(MetadataManager.class);
            // metaService.test();
            // componentRef.get().sendUserPermissions(sender, clientID,
            //                                                    message.getMessageID());
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
     
     @Override
     public void setServerState(CellComponentServerState state) {
       // TODO
       // in the future, could diff past and present state, or include
       // 'add remove modify' in server state, and be more efficient here
       // for now, just erase everything under the cell in search DB
       // and replace with new data
       super.setServerState(state);
       MetadataComponentServerState s = (MetadataComponentServerState) state;
       MetadataComponentServerState s0 = (MetadataComponentServerState) getServerState(null);
       mcss = (MetadataComponentServerState) state;
       logger.log(Level.INFO, "[METADATA COMPONENT MO] set server state.. count was  " + s0.metaCount() + " and is now " + s.metaCount());

       MetadataManager metaService = AppContext.getManager(MetadataManager.class);
//       metaService.setCellMetadata(cellID, mcss.getMetadata());
       metaService.setCellMetadata(this.cellID, mcss.getMetadata());
     }

    // Metadata functions

    public void add(MetadataSPI meta){
        logger.log(Level.INFO, "[METADATA COMPONENT MO] add metadata fn");
        logger.log(Level.INFO, "Author --- " + meta.get("Creator"));
        MetadataComponentServerState state = (MetadataComponentServerState) getServerState(null);
        logger.log(Level.INFO, "current # of metadata in server state --- " + state.metaCount());
        state.addMetadata(meta);
        setServerState(state);
        logger.log(Level.INFO, "new # --- " + state.metaCount());
    }

    public void remove(MetadataSPI meta){
        MetadataComponentServerState state = (MetadataComponentServerState) getServerState(null);
        state.addMetadata(meta);
        setServerState(state);
    }
}
