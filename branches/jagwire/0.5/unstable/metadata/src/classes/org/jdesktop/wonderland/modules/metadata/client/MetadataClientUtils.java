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

import java.util.Iterator;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.metadata.client.plugin.MetadataPlugin;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.MetadataModificationListener;
import org.jdesktop.wonderland.modules.metadata.common.annotations.MetadataType;

/**
 * Utility functions for the client - at the moment, this consists only of an
 * iterator through registered Metadata types and a hook to add/remove listeners..
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

  private static MetadataPlugin metaPlugin;


  public static Iterator<Metadata> getTypesIterator() {
    logger.info("GOT FROM CLIENT UTILS");
    // search annotations
    // note: for now, this only gets types from the primary server
    // in the future, in a client connected to multiple sessions at the same time,
    // items like the global search will have to make it clear what session/server
    // they are searching
    if(MetadataType.class == null){
      logger.info("METATYPE NULL");
    }

    if(Metadata.class == null){
      logger.info("METADATA NULL");
    }
    Iterator<Metadata> it = scl.getAll(MetadataType.class, Metadata.class);
    if(it == null){
      logger.severe("ITR IS NULL");
    }
    return it;
  }

  public static void setPluginRef(MetadataPlugin mp) {
    metaPlugin = mp;
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

  public static void addMetadataModificationListener(MetadataModificationListener l){
    logger.info("[META CLIENT UTILS] register listener " + l);
    metaPlugin.addMetadataModificationListener(l);
  }

  public static void removeMetadataModificationListener(MetadataModificationListener l){
    logger.info("[META CLIENT UTILS] remove listener");
    metaPlugin.removeMetadataModificationListener(l);
  }
}
