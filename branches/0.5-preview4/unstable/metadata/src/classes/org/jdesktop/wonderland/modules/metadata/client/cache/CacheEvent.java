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

import java.util.EventObject;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.ModifyCacheAction;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataModResponseMessage;

/**
 * event fired when a cell component's cache changes
 * includes the action type (from MetadataModResponseMessage)
 * @author mabonner
 */
public class CacheEvent extends EventObject {
  private final Metadata metadata;
  private final ModifyCacheAction action;
  public CacheEvent(Object source, MetadataModResponseMessage msg){
    super(source);
    metadata = msg.metadata;
    action = msg.action;
  }

  public ModifyCacheAction getAction() {
    return action;
  }

  public Metadata getMetadata() {
    return metadata;
  }

}
