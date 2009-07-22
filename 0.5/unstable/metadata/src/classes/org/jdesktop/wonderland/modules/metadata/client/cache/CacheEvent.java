package org.jdesktop.wonderland.modules.metadata.client.cache;

import java.util.EventObject;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSPI;
import org.jdesktop.wonderland.modules.metadata.common.ModifyCacheAction;
import org.jdesktop.wonderland.modules.metadata.common.messages.MetadataModResponseMessage;

/**
 * event fired when a cell component's cache changes
 * includes the action type (from MetadataModResponseMessage)
 * @author mabonner
 */
public class CacheEvent extends EventObject {
  private final MetadataSPI metadata;
  private final ModifyCacheAction action;
  public CacheEvent(Object source, MetadataModResponseMessage msg){
    super(source);
    metadata = msg.metadata;
    action = msg.action;
  }

  public ModifyCacheAction getAction() {
    return action;
  }

  public MetadataSPI getMetadata() {
    return metadata;
  }

}
