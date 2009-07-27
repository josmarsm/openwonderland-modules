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

import java.util.LinkedHashMap;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.MetadataID;

/**
 * Used to return all of the metadata in a cell for the component to cache.
 * TODO: in the future, this may return only pieces of metadata, rather than all.
 * @author mabonner
 */
public class MetadataCacheResponseMessage extends ResponseMessage {
  private LinkedHashMap<MetadataID, Metadata> metadata = null;
  public MetadataCacheResponseMessage(MessageID id, LinkedHashMap<MetadataID, Metadata> m){
    super(id);
    metadata = m;
  }

  /**
   * Get the metadata this message is wrapping
   * @return
   */
  public LinkedHashMap<MetadataID, Metadata> getMetadata(){
    return metadata;
  }
}