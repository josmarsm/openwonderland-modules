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

package org.jdesktop.wonderland.modules.metadata.client.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.metadata.client.MetadataComponent;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.MetadataID;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataCacheMessage;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataCacheResponseMessage;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataModResponseMessage;

/**
 * Cache for cell metadata... MetadataComponent delegates to this object in order
 * to provide client-side access to its metadata. A cache uses the channel of
 * its parent MetadataComponent to request metadata from the server.
 *
 * All pieces of metadata are placed into the main metadataid - metadata map.
 * Access is also provided to class-specific subsets, and to a simple list for
 * fetching all metadata
 *
 * The cache is inactive (and ignores all adjustments to cache via cacheModified)
 * until metadata has been requested using one of the getMetadata... functions.
 *
 * Note that the cache can still register and notify listeners of changes
 * without becoming active or actually storing data. Only a get request will
 * activate the caching.
 *
 * @author mabonner
 */
public class MetadataCache {
  private static Logger logger = Logger.getLogger(MetadataCache.class.getName());

  /**
   * The MetadataComponent this cache is associated with. A MetadataCache uses
   * its parent component's channel to request metadata from the server.
   */
  private MetadataComponent component;

  /**
   * main cache, used to access all metadata or metadata by ID
   */
  LinkedHashMap<MetadataID, Metadata> cache;

  /**
   * cache, access any piece of metadata on this cell by class and then id.
   */
  private HashMap<Class, HashMap<MetadataID, Metadata>> classCache = null;

  
  /**
   * an invalid cache must be validated before metadata may be returned
   */
  private boolean valid = false;

  /**
   * The cache is inactive (and ignores all adjustments to cache) until metadata
   * has been requested using one of the getMetadata... functions.
   *
   * Note that the cache can still register and notify listeners of changes
   * without becoming active or actually storing data. Only a get request will
   * activate the caching.
   */
  private boolean active = false;

  /**
   * listeners to notify when the cache is modified
   */
  ArrayList<CacheEventListener> listeners = new ArrayList<CacheEventListener>();

  /**
   *
   * @param parent the associated component this cache stores metadata for
   */
  public MetadataCache(MetadataComponent parent){
    component = parent;
  }

  /**
   * inform cache to update itself based on passed message
   * @param msg
   */
  public void cacheModified(MetadataModResponseMessage msg){
    logger.info("[META CACHE] cacheModified");

    // only bother updating cache if it has been activated
    if(!active){
      logger.info("[META CACHE] inactive, ignore  ");
      // listeners are always notified
      notifyCacheListeners(msg);
      return;
    }
    switch (msg.action){
        case ADD:
            logger.info("[META CACHE] add metadata:\n" + msg.metadata);
            addToCache(msg.metadata);
            break;
        case REMOVE:
            logger.info("[META CACHE] remove metadata:\n" + msg.metadata);
            removeFromCache(msg.metadata);
            break;
        case MODIFY:
            logger.info("[META CACHE] mod metadata:\n" + msg.metadata);
            modifyInCache(msg.metadata);
            break;
        case INVALIDATE:
            logger.info("[META CACHE] invalidate metadata... ");
            invalidateCache();
            break;
    }

    // dont notify until AFTER cache has been modified!
    notifyCacheListeners(msg);
  }

  /**
   * completely rebuild cache and access maps
   * @param metadata initial metadata for cache
   */
  private void setCache(LinkedHashMap<MetadataID, Metadata> metadata) {
    cache = new LinkedHashMap<MetadataID, Metadata>();
    classCache = new HashMap<Class, HashMap<MetadataID, Metadata>>();
    for(Metadata m:metadata.values()){
      cacheMetadata(m);
    }
  }

  /**
   * Used when building or updating the components metadata caches. Adds the
   * piece of metadata to the appropriate general and class-specific maps.
   *
   * @param m piece of metadata to cache
   */
  private void cacheMetadata(Metadata m) {
    // all pieces of metadata accessible from the main and ID caches
    cache.put(m.getID(), m);

    // also keep class-specific caches
    Class mClass = m.getClass();
    if(!classCache.containsKey(mClass)){
      // create maps for new classes
      classCache.put(mClass, new HashMap<MetadataID, Metadata>());
    }
    // add to appropriate class cache
    classCache.get(mClass).put(m.getID(), m);
  }

  // Note: All get methods should validate the cache before returning results!!
  /**
   * Requests all of the cell's metadata. Validates Cache.
   * @return the full, main cache
   */
  public Collection<Metadata> getAllMetadata(){
    logger.info("[META CACHE] get all  ");
    // actively maintain cache until an invalidate message
    active = true;
    validateCache();
    return cache.values();
  }

  /**
   * Requests a specific piece of metadata. Validates cache.
   * @param mid MetadataID of metadata piece to fetch
   * @return
   */
  public Metadata getMetadataByID(MetadataID mid){
    logger.info("[META CACHE] get id: " + mid);
    // actively maintain cache until an invalidate message
    active = true;
    validateCache();
    return cache.get(mid);
  }

  /**
   * Requests all of a cell's metadata of a certain type. Validates cache.
   * @param c class of metadata to fetch
   * @return array list of matching metadata, null if none of this class has been added
   */
  public <T extends Metadata> ArrayList<T> getMetadataByClass(Class<T> c){
    logger.info("[META CACHE] get for type: " + c.getName());
    // actively maintain cache until an invalidate message
    active = true;
    validateCache();
//    c.
    HashMap<MetadataID, Metadata> thisClassCache = classCache.get(c);
    ArrayList<T> res = new ArrayList<T>();
    if(thisClassCache == null || thisClassCache.isEmpty()){
      return res;
    }
    
    for(Metadata m:thisClassCache.values()){
      res.add((T) m);
    }
    logger.info("[META CACHE] returning " + res.size() + " pieces");
    return res;
  }


  /**
   * Called in response to a metadata change on the server that has not been
   * immediately corrected in this component. This happens when a cell's metadata
   * is completely replaced (e.g., altered in the properties pane).
   *
   * Caching is also deactivated, the next getMetadata... call will cause it to
   * be completely rebuilt and reactivated.
   */
  private void invalidateCache(){
    active = false;
    valid = false;
  }

  /**
   * Checks and validates cache if necessary. Cache will be valid when this
   * returns.
   */
  private void validateCache(){
    if(!valid){
      logger.info("[META CACHE] invalid ");
      ResponseMessage rm = null;
      try {
        rm = component.getChannel().sendAndWait(new MetadataCacheMessage());
      } catch (InterruptedException ex) {
        Logger.getLogger(MetadataCache.class.getName()).log(Level.SEVERE, null, ex);
      }
      if (rm instanceof MetadataCacheResponseMessage) {
        logger.info("[META CACHE] got response for revalidation ");
        MetadataCacheResponseMessage m = (MetadataCacheResponseMessage) rm;
        setCache(m.getMetadata());
        valid = true;
      }
      else{
        logger.severe("[META CACHE] got invalid type of message" + rm.getClass().getName());
      }
    }
  }

  /**
   * Revalidate the cache by adding this piece of metadata
   * @param metadata piece of metadata to add
   */
  private void addToCache(Metadata m) {
    logger.info("[META CACHE] add to cache");
    cacheMetadata(m);
    valid = true;
  }

  /**
   * Revalidate the cache by removing this piece of metadata
   * @param metadata piece of metadata to remove
   */
  private void removeFromCache(Metadata m) {
    logger.info("[META CACHE] remove from cache");
    cache.remove(m.getID());
    HashMap<MetadataID, Metadata> theClassCache = classCache.get(m.getClass());
    if(theClassCache != null){
      theClassCache.remove(m.getID());
    }
    valid = true;
  }

  /**
   * Revalidate the cache by modifying this piece of metadata
   * @param metadata piece of metadata to add
   */
  private void modifyInCache(Metadata m) {
    logger.info("[META CACHE] mod in cache");
    // this will simply overwrite the old value
    cacheMetadata(m);
    valid = true;
  }

  /**
   * add a cache listener, notified when this cache changes
   */
  public void addListener(CacheEventListener l) {
    listeners.add(l);
  }

  /**
   * remove a cache listener
   */
  public void removeListener(CacheEventListener l) {
    listeners.remove(l);
  }

  /**
   * Notify all listeners on this cache that it has been modified
   * @param msg
   */
  private void notifyCacheListeners(MetadataModResponseMessage msg) {
    CacheEvent evt = new CacheEvent(this, msg);
    if(msg.metadata == null){
      logger.info("[META CACHE] no metadata in MMRM!");
    }
    for(CacheEventListener l:listeners){
      logger.info("[META CACHE] notifying listener..");
      l.cacheEventOccurred(evt);
    }
  }


}
