/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.client;

import java.util.Iterator;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSPI;
import org.jdesktop.wonderland.modules.metadata.common.MetadataType;

/**
 *
 * @author mabonner
 */
public class MetadataClientUtils {

  private static MetadataClientUtils ref;
  private static ScannedClassLoader scl;
  private static final Object lock = new Object();

  static{
    scl = LoginManager.getPrimary().getClassloader();
  }

  public static Iterator<MetadataSPI> getTypesIterator() {
    System.out.println("GOT FROM CLIENT UTILS");
    // search annotations
    // note: for now, this only gets types from the primary server
    // in the future, in a client connected to multiple sessions at the same time,
    // items like the global search will have to make it clear what session/server
    // they are searching
    return scl.getAll(MetadataType.class, MetadataSPI.class);
  }

  private MetadataClientUtils(){
    // singleton
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
