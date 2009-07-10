/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.server;

import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.CommsManager;

/**
 * Currently, only purpose is to register the MetadataConnection handler
 * @author mabonner
 */
@Plugin
public class MetadataServerPlugin implements ServerPlugin{

  public void initialize() {
    CommsManager cm = WonderlandContext.getCommsManager();

    cm.registerClientHandler(new MetadataConnectionHandler());
  }

}
