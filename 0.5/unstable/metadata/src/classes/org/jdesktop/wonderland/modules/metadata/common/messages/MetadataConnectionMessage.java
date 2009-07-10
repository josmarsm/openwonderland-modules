/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.common.messages;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSearchFilters;

/**
 *
 * @author matt
 */
public class MetadataConnectionMessage extends Message{
  private static Logger logger = Logger.getLogger(MetadataConnectionMessage.class.getName());

  public enum Action { SEARCH }

  private Action action;
  private MetadataSearchFilters filters;
  private CellID cellScope = null;

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

  public Action getAction(){
    return action;
  }

  public MetadataSearchFilters getFilters(){
    return filters;
  }

  public CellID getCellScope() {
    return cellScope;
  }



  @Override
  public String toString(){
    return "MetadataConnMsg with " + filters.filterCount() + " filters";
  }
}
