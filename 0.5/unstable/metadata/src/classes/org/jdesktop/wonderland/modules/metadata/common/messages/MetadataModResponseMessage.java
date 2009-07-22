/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.common.messages;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSPI;
import org.jdesktop.wonderland.modules.metadata.common.ModifyCacheAction;

/**
 * The server MetadataComponentMO uses this message type to alert client compo's
 * that it has been updated. Caching compos should update their cache using the
 * included metadata.
 *
 * This is currently a copy of MetadataModMessage. Present in case the server's
 * responses ever need to change, and for clarity.
 * @author mabonner
 */
public class MetadataModResponseMessage extends CellMessage{
  private static Logger logger = Logger.getLogger(MetadataModResponseMessage.class.getName());
    public ModifyCacheAction action;
    public MetadataSPI metadata;

    public MetadataModResponseMessage(ModifyCacheAction act, MetadataSPI meta){
      logger.info("[MOD RESPONSE] creating message!!!");
        action = act;
        metadata = meta;
    }

    /**
     * build an appropriate (identical) response message based on a mod message
     * @param msg
     */
    public MetadataModResponseMessage(MetadataModMessage msg) {
      logger.info("[MOD RESPONSE] creating message!!!");
      switch (msg.action){
        case ADD:
            action = ModifyCacheAction.ADD;
            break;
        case REMOVE:
            action = ModifyCacheAction.REMOVE;
            break;
        case MODIFY:
            action = ModifyCacheAction.MODIFY;
            break;
      }
      metadata = msg.metadata;
    }

}