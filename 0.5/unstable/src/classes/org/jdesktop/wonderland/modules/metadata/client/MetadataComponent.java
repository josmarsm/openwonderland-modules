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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;

import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.SimpleMetadata;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataMessage;
// import org.jdesktop.wonderland.modules.sample.common.SampleCellComponentClientState;

/**
 * Client-side metadata cell component
 * 
 * @author Matt Bonner
 */
public class MetadataComponent extends CellComponent {

    private static Logger logger = Logger.getLogger(MetadataComponent.class.getName());
    private String info = null;
    
    /** 
     * The channel to listen for messages over
     */
    @UsesCellComponent private ChannelComponent channel;
    /**
     * Add items to the right click menu - test, then simple tag and annotate
     */
    @UsesCellComponent ContextMenuComponent menuComponent;
    
    

    public MetadataComponent(Cell cell) {
        super(cell);
        logger.log(Level.INFO, "[METADATA COMPONENT] compo created");
    }

    // @Override
    // public void setClientState(CellComponentClientState clientState) {
    //     super.setClientState(clientState);
    //     info = ((SampleCellComponentClientState)clientState).getInfo();
    // }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        
        // cell will only hit inactive when it is first created, so this only happens once
        if(status == CellStatus.INACTIVE){
          menuComponent.addContextMenuFactory(new SampleContextMenuFactory());
        }

        // switch (status) {
        //     case ACTIVE:
        //         if (increasing) {
        //             if (receiver == null) {
        //                 receiver = new SecurityMessageReceiver();
        //                 channel.addMessageReceiver(PermissionsChangedMessage.class,
        //                                            receiver);
        //             }
        //         } else {
        //             channel.removeMessageReceiver(PermissionsChangedMessage.class);
        //             receiver = null;
        //         }
        //         break;
        //     case DISK:
        //         break;
        // }
    }
    
    /**
     * Context menu factory for the Sample menu item
     */
    class SampleContextMenuFactory implements ContextMenuFactorySPI {
        SampleContextMenuFactory(){
          logger.log(Level.INFO, "[METADATA COMPONENT] context menu elt created");
        }
        public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
            return new ContextMenuItem[] {
                        new SimpleContextMenuItem("MetaSample", null,
                                new SampleContextMenuListener())
            };
        }
    }

    /**
     * Listener for event when the Sample context menu item is selected
     */
    class SampleContextMenuListener implements ContextMenuActionListener {

        public void actionPerformed(ContextMenuItemEvent event) {
            logger.warning("Metadata Sample context menu action performed!");
//            test();
            DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy h:mm:ss a");
            Date date = new Date();
            addMetadata(new SimpleMetadata("Matt", dateFormat.format(date)));
        }
    }
    
    public void test(){
      logger.log(Level.INFO, "[METADATA COMPO] test!");
      // System.out.println("metadata - do it!!");
      channel.send(new MetadataMessage());
    }

    public void addMetadata(Metadata meta){
        logger.warning("[METADATA COMPO] add metadata!");
        channel.send(new MetadataMessage(MetadataMessage.Action.ADD, meta));
    }

    public void removeMetadata(Metadata meta){
        logger.warning("[METADATA COMPO] remove metadata!");
        channel.send(new MetadataMessage(MetadataMessage.Action.REMOVE, meta));
    }
}
