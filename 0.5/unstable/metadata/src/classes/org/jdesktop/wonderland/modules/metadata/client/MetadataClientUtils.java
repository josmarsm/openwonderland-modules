/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.client;

import java.util.Iterator;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSPI;
import org.jdesktop.wonderland.modules.metadata.common.annotations.MetadataType;

/**
 *
 * @author mabonner
 */
public class MetadataClientUtils {

  private static Logger logger = Logger.getLogger(MetadataClientUtils.class.getName());

  private static MetadataClientUtils ref;
  // we know this utils is in 'it's own' session, and could ask for its
  // classloader, but if it was ever further isolated into its own classloader,
  // this would be incorrect, so have the plugin (which will always be rooted
  // in the session) set the class loader here
  private static ScannedClassLoader scl;
  private static final Object lock = new Object();


  public static Iterator<MetadataSPI> getTypesIterator() {
    logger.info("GOT FROM CLIENT UTILS");
    // search annotations
    // note: for now, this only gets types from the primary server
    // in the future, in a client connected to multiple sessions at the same time,
    // items like the global search will have to make it clear what session/server
    // they are searching
    if(MetadataType.class == null){
      logger.info("METATYPE NULL");
    }

    if(MetadataSPI.class == null){
      logger.info("METADATA SPI NULL");
    }
    Iterator<MetadataSPI> it = scl.getAll(MetadataType.class, MetadataSPI.class);
    if(it == null){
      logger.severe("ITR IS NULL");
    }
    return it;
  }

  private MetadataClientUtils(){
    // singleton
  }

  public static void setScannedClassLoader(ScannedClassLoader l){
    logger.info("[META CLIENT UTILS] set class loader");
    scl = l;
  }

  public static MetadataClientUtils getInstance() {
    synchronized(lock){
      if(ref == null) {
         ref = new MetadataClientUtils();
      }
      return ref;
    }
  }
}
