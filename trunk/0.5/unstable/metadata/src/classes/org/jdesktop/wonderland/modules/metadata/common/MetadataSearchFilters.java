/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.common;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A simple container for search filters.
 * @author mabonner
 */
public class MetadataSearchFilters implements Serializable{
  private ArrayList<Metadata> filters = new ArrayList<Metadata>();
  public void addFilter(Metadata m) {
    filters.add(m);
  }

  public int filterCount(){
    return filters.size();
  }

  public ArrayList<Metadata> getFilters(){
    return filters;
  }
}
