package org.jdesktop.wonderland.modules.annotations.server;

/**
 *
 * @author mabonner
 */
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

import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.AppContext;
import java.util.logging.Logger;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;

import org.jdesktop.wonderland.modules.annotations.common.AnnotationComponentServerState;

import org.jdesktop.wonderland.modules.metadata.server.MetadataComponentMO;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.annotation.DependsOnCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

@DependsOnCellComponentMO(MetadataComponentMO.class)

/**
 * Annotations component
 *
 * @author mabonner
 */
public class AnnotationComponentMO extends CellComponentMO {

    private static Logger logger = Logger.getLogger(AnnotationComponentMO.class.getName());
    private String info = null;
    private AnnotationComponentServerState acss;

    /** the channel component */
    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelRef;


    // @UsesCellComponentMO(SampleCellSubComponentMO.class)
    // private ManagedReference<SampleCellSubComponentMO> subComponentRef;

    public AnnotationComponentMO(CellMO cell) {
        super(cell);
        acss = new AnnotationComponentServerState();
        logger.info("[ANNO COMPONENT] MO created");
    }


    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.annotations.client.AnnotationComponent";
    }
    //
    @Override
    protected void setLive(boolean live) {
        logger.info("[AnnotationComponentMO] setLive: " + live);

        super.setLive(live);

        if (live) {
//            channelRef.getForUpdate().addMessageReceiver(MetadataMessage.class,
//                                                  new MessageReceiver(cellRef.get(), this));
        } else {
            // unregister

//            channelRef.getForUpdate().removeMessageReceiver(MetadataMessage.class);
        }
    }

    /**
     * Message receiver to handle permission change requests
     */
    public static final class MessageReceiver extends AbstractComponentMessageReceiver {
        // private ManagedReference<MetadataComponentMO> componentRef;
        private ManagedReference<AnnotationComponentMO> componentRef;

        public MessageReceiver(CellMO cellMO, AnnotationComponentMO component) {
            super (cellMO);

            componentRef = AppContext.getDataManager().createReference(component);
        }

        @Override
        public void messageReceived(WonderlandClientSender sender,
                                    WonderlandClientID clientID,
                                    CellMessage message)
        {
            logger.info("[ANNO COMPONENT] message received... ");
//            MetadataMessage msg = (MetadataMessage) message;

//            if(msg.action != null){
//                switch (msg.action){
//                    case ADD:
//                        logger.info("[METADATA COMPONENT MO] add metadata ");
//                        componentRef.get().add(msg.metadata);
//                        break;
//                    case REMOVE:
//                        logger.info("[METADATA COMPONENT MO] remove metadata... ");
//                        componentRef.get().remove(msg.metadata);
//                        break;
//                    case MODIFY:
//                        logger.info("[METADATA COMPONENT MO] mod metadata... ");
//                        break;
//                }
//            }
        }
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if (state == null) {
            state = new AnnotationComponentServerState();
        }
        state = acss;
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
       AnnotationComponentServerState s = (AnnotationComponentServerState) state;
       AnnotationComponentServerState s0 = (AnnotationComponentServerState) getServerState(null);
       acss = (AnnotationComponentServerState) state;
       logger.info("[ANNO COMPONENT MO] set server state");
     }

}

