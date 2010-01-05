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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;

import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellStatus;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.metadata.client.cache.CacheEventListener;
import org.jdesktop.wonderland.modules.metadata.client.cache.MetadataCache;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.MetadataID;
import org.jdesktop.wonderland.modules.metadata.common.basetypes.SimpleMetadata;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataModMessage;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataModResponseMessage;

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

    private MetadataCache metadataCache = null;

    public MetadataComponent(Cell cell) {
        super(cell);
        metadataCache = new MetadataCache(this);
        logger.info("[METADATA COMPONENT] compo created");
    }

    public ChannelComponent getChannel() {
      return channel;
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        
        // cell will only hit inactive when it is first created, so this only happens once
        // populate context menu
        if(status == CellStatus.INACTIVE){
          if(increasing){
            //menuComponent.addContextMenuFactory(new SampleContextMenuFactory());
            // gather metadata types that may want to appear in this cell's context menu
  //          Iterator<Metadata> it = cl.getAll(MetadataContextMenuItem.class, Metadata.class); //CellFactorySPI.class);
            Iterator<Metadata> it = MetadataClientUtils.getTypesIterator();
            while (it.hasNext()) {
              Metadata m = it.next();
              logger.info("[METADATA COMPO] using session's loader, scanned type:" + m.simpleName());
              if(m.contextMenuCheck(cell.getClass())){
                menuComponent.addContextMenuFactory(new MetadataContextMenuFactory(m.simpleName(), m.getClass()));
              }
            }

            logger.info("[METADATA COMPO] adding receiver");
            // receive responses to metadata cache requests
            channel.addMessageReceiver(MetadataModResponseMessage.class, new ModResponseMessageReceiver());


            // TODO add test context items
//            menuComponent.addContextMenuFactory(new TestContextMenuFactory(0));
//            menuComponent.addContextMenuFactory(new TestContextMenuFactory(1));
          }
          else{
//            channel.removeMessageReceiver(MetadataCacheResponse.class);
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
        logger.info("[METADATA COMPONENT] context menu elt created for type " + t.getName());
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
        logger.info("[METADATA COMPONENT] Metadata context menu action performed!");
        Date date = new Date();
        try {
          Metadata m = (Metadata) type.newInstance();
          m.initByClient(LoginManager.getPrimary().getPrimarySession().getUserID());
          addMetadata(m);
        } catch (InstantiationException ex) {
          Logger.getLogger(MetadataComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
          Logger.getLogger(MetadataComponent.class.getName()).log(Level.SEVERE, null, ex);
        } 
      }
    }

    /**
     * Context menu factory for activating test functions
     */
    class TestContextMenuFactory implements ContextMenuFactorySPI {
      int type;

      TestContextMenuFactory(int t){
        logger.info("[METADATA COMPONENT] context menu test ");
        type = t;
      }
      public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
        if(type == 0){
          return new ContextMenuItem[] {
                      new SimpleContextMenuItem("get all meta", null, new TestContextMenuListener1())
          };
        }
        return new ContextMenuItem[] {
                      new SimpleContextMenuItem("get simple meta", null, new TestContextMenuListener2())
          };
      }
    }

    /**
     * Listener for test context menu item - get all metadata
     */
    class TestContextMenuListener1 implements ContextMenuActionListener {

      public void actionPerformed(ContextMenuItemEvent event) {
        // create an object
        logger.info("[METADATA COMPONENT] test context menu get all");
        Collection<Metadata> meta = getAllMetadata();
        if(meta == null){
          logger.info("EMPTY!");
          return;
        }
        for(Metadata m:meta){
          logger.info("Piece:" + m);
        }
        
      }
    }

    /**
     * Listener for test context menu item - get metadata
     */
    class TestContextMenuListener2 implements ContextMenuActionListener {

      public void actionPerformed(ContextMenuItemEvent event) {
        // create an object
        logger.info("[METADATA COMPONENT] test context get class: simple");
        ArrayList<SimpleMetadata> meta = getMetadataOfType(SimpleMetadata.class);
        if(meta == null){
          logger.info("EMPTY!");
          return;
        }
        for(Metadata m:meta){
          logger.info("Piece:" + m);
        }

      }
    }
    
    public void test(){
      logger.info("[METADATA COMPO] test!");
      // System.out.println("metadata - do it!!");
      channel.send(new MetadataModMessage());
    }

    public void addMetadata(Metadata meta){
        logger.info("[METADATA COMPO] add metadata!");
        channel.send(new MetadataModMessage(MetadataModMessage.Action.ADD, meta));
    }

    public void removeMetadata(Metadata meta){
        logger.info("[METADATA COMPO] remove metadata!");
        channel.send(new MetadataModMessage(MetadataModMessage.Action.REMOVE, meta));
    }
    
    public void modifyMetadata(Metadata meta){
        logger.info("[METADATA COMPO] modify metadata!");
        logger.info("new metadata will be: " + meta);
        channel.send(new MetadataModMessage(MetadataModMessage.Action.MODIFY, meta));
    }


    /**
     * Requests all of a cell's metadata from the cache
     * @return
     */
    public Collection<Metadata> getAllMetadata(){
      return metadataCache.getAllMetadata();
    }

    /**
     * Requests a specific piece of metadata from the cache.
     * @param mid MetadataID of metadata piece to fetch
     * @return
     */
    public Metadata getMetadata(MetadataID mid){
      return metadataCache.getMetadataByID(mid);
    }

    /**
     * Requests all of a cell's metadata of a certain type from the cache
     * @param c class of metadata to fetch
     * @return array list of matching metadata, null if none of this class has been added
     */
    public <T extends Metadata> ArrayList<T> getMetadataOfType(Class<T> c){
      return metadataCache.getMetadataByClass(c);
    }


    /**
     * recieves the MO's responses to cache requests and changes
     * from the properties pane (invalidates cache)
     */
    class ModResponseMessageReceiver implements ComponentMessageReceiver {
     public void messageReceived(CellMessage message) {
        // if cache is valid or non-null, then something has requested the metadata from
        // this compo. Keep the caches updated once they have been created.
        if (message instanceof MetadataModResponseMessage) {
          logger.info("[META COMPO] received update message ");
          MetadataModResponseMessage msg = (MetadataModResponseMessage) message;
          metadataCache.cacheModified(msg);
        }
      }
    }

    

    /**
     * add a cache listener.. will cause cell to validate cache
     */
    public void addCacheListener(CacheEventListener l){
      metadataCache.addListener(l);
    }
}
