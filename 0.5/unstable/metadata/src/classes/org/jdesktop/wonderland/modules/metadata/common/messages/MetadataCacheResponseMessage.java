/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.common.messages;

import java.util.ArrayList;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSPI;

/**
 * Used to return all of the metadata in a cell for the component to cache.
 * TODO: in the future, this may return only pieces of metadata, rather than all.
 * @author mabonner
 */
public class MetadataCacheResponseMessage extends ResponseMessage {
  private ArrayList<MetadataSPI> metadata = null;
  public MetadataCacheResponseMessage(MessageID id, ArrayList<MetadataSPI> m){
    super(id);
    metadata = m;
  }

  public ArrayList<MetadataSPI> getMetadata(){
    return metadata;
  }
}