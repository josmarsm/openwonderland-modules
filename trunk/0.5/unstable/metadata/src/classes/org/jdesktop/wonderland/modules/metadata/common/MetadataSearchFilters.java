/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.common;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author mabonner
 */
public class MetadataSearchFilters implements Serializable{
  ArrayList<MetadataSPI> filters = new ArrayList<MetadataSPI>();
  public void addFilter(MetadataSPI m) {
    filters.add(m);
  }

  public int filterCount(){
    return filters.size();
  }
}
