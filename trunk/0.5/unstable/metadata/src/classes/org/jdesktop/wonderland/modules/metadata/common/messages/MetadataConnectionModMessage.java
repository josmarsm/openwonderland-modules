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

import java.util.ArrayList;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.metadata.common.MetadataModificationListener;

/**
 * Message for clients to communicate with MetadataService. Used for service to
 * notify client of metadataModficationListeners that have fired
 * @author mabonner
 */
public class MetadataConnectionModMessage extends Message{
  private static Logger logger = Logger.getLogger(MetadataConnectionModMessage.class.getName());

  private ArrayList<MetadataModificationListener> listenerHits;

  public MetadataConnectionModMessage(ArrayList<MetadataModificationListener> l) {
    listenerHits = l;
  }

  public ArrayList<MetadataModificationListener> getListenerHits(){
    return listenerHits;
  }


  @Override
  public String toString(){
    return "MetadataConnModResponseMsg with " + listenerHits.size() + " listener hits";
  }
}
