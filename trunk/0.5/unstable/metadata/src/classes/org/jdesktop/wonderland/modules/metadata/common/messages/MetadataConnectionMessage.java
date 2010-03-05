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
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.metadata.common.MetadataModificationListener;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSearchFilters;

/**
 * Message for clients to communicate with MetadataService. Currently used for
 * searching and adding/removing mod listeners
 * @author mabonner
 */
public class MetadataConnectionMessage extends Message{
  private static Logger logger = Logger.getLogger(MetadataConnectionMessage.class.getName());

  public enum Action { SEARCH, ADD_MOD_LISTENER, REMOVE_MOD_LISTENER }

  private Action action;
  private MetadataSearchFilters filters;
  private CellID cellScope = null;
  private MetadataModificationListener listener = null;

  public MetadataConnectionMessage(MetadataSearchFilters f, Action a){
    filters = f;
    action = a;
    logger.info("[MCM] msg with " + filters.filterCount() + " filters");
  }

  public MetadataConnectionMessage(MetadataSearchFilters f, Action a, CellID cid){
    filters = f;
    action = a;
    cellScope = cid;
  }

  public MetadataConnectionMessage(MetadataModificationListener l, Action a){
    listener = l;
    action = a;
  }

  public Action getAction(){
    return action;
  }

  public MetadataSearchFilters getFilters(){
    return filters;
  }

  public CellID getCellScope() {
    return cellScope;
  }

  public MetadataModificationListener getModListener(){
    return listener;
  }


  @Override
  public String toString(){
    return "MetadataConnMsg with " + filters.filterCount() + " filters";
  }
}
