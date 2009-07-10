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

import java.util.Date;
import java.util.Iterator;
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
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.modules.metadata.common.MetadataSPI;
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
        // populate context menu
        if(status == CellStatus.INACTIVE){
          //menuComponent.addContextMenuFactory(new SampleContextMenuFactory());
          // gather metadata types that may want to appear in this cell's context menu
//          Iterator<MetadataSPI> it = cl.getAll(MetadataContextMenuItem.class, MetadataSPI.class); //CellFactorySPI.class);
          Iterator<MetadataSPI> it = MetadataClientUtils.getTypesIterator();
          while (it.hasNext()) {
            MetadataSPI metadata = it.next();
            logger.log(Level.INFO, "[METADATA COMPO] using session's loader, scanned type:" + metadata.simpleName());
            if(metadata.contextMenuCheck(cell.getClass())){
              menuComponent.addContextMenuFactory(new MetadataContextMenuFactory(metadata.simpleName(), metadata.getClass()));
            }
          }
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
     * Context menu factory for the metadata menu items
     */
    class MetadataContextMenuFactory implements ContextMenuFactorySPI {
      private String name;
      private Class type;

      /**
       * build a new factory, with the label n, and creates metadata of type t.
       * @param n
       * @param t
       */
      MetadataContextMenuFactory(String n, Class t){
        logger.log(Level.INFO, "[METADATA COMPONENT] context menu elt created for type " + t.getName());
        name = n;
        type = t;
      }
      public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
          return new ContextMenuItem[] {
                      new SimpleContextMenuItem(name, null, new MetadataContextMenuListener(type))
          };
      }
    }

    /**
     * Listener for event when a metadata context menu item is selected
     * Creates a new metadata object of the appropriate type, adds it to cell
     */
    class MetadataContextMenuListener implements ContextMenuActionListener {
      Class type;
      /**
       *
       * @param t the type of metadata to create
       */
      public MetadataContextMenuListener(Class t){
        type = t;
      }

      public void actionPerformed(ContextMenuItemEvent event) {
        // create an object
        logger.log(Level.INFO, "[METADATA COMPONENT] Metadata context menu action performed!");
        Date date = new Date();
        try {
          MetadataSPI m = (MetadataSPI) type.newInstance();
          m.initByClient(LoginManager.getPrimary().getPrimarySession().getUserID());
          addMetadata(m);
        } catch (InstantiationException ex) {
          Logger.getLogger(MetadataComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
          Logger.getLogger(MetadataComponent.class.getName()).log(Level.SEVERE, null, ex);
        } 
      }
    }
    
    public void test(){
      logger.log(Level.INFO, "[METADATA COMPO] test!");
      // System.out.println("metadata - do it!!");
      channel.send(new MetadataMessage());
    }

    public void addMetadata(MetadataSPI meta){
        logger.warning("[METADATA COMPO] add metadata!");
        channel.send(new MetadataMessage(MetadataMessage.Action.ADD, meta));
    }

    public void removeMetadata(MetadataSPI meta){
        logger.warning("[METADATA COMPO] remove metadata!");
        channel.send(new MetadataMessage(MetadataMessage.Action.REMOVE, meta));
    }
}
