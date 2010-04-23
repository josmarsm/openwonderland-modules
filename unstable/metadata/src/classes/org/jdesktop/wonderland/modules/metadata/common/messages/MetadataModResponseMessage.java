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

package org.jdesktop.wonderland.modules.metadata.common.messages;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
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
    public Metadata metadata;

    /**
     * Constructor
     * @param act type of action that changed cache
     * @param meta relevant metadata, in its latest version
     */
    public MetadataModResponseMessage(ModifyCacheAction act, Metadata meta){
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