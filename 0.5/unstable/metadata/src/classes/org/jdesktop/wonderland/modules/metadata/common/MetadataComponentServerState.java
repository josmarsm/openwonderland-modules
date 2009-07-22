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

package org.jdesktop.wonderland.modules.metadata.common;

import java.util.ArrayList;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * Server state for metadata cell component
 *
 * @author mabonner
 */
@XmlRootElement(name="metadata-cell-component")
@ServerState
public class MetadataComponentServerState extends CellComponentServerState {
    private static Logger logger = Logger.getLogger(MetadataComponentServerState.class.getName());

    private ArrayList<MetadataSPI> metadata;

    /** Default constructor */
    public MetadataComponentServerState() {
        metadata = new ArrayList<MetadataSPI>();
    }

    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.metadata.server.MetadataComponentMO";
    }

    public void addMetadata(MetadataSPI meta){
      logger.info("Added metadata to server state, mid:" + meta.getID() );
      metadata.add(meta);
    }

    public void removeMetadata(MetadataSPI meta){
      metadata.remove(meta);
    }

    public void removeMetadata(int idx){
      int count = 0;
      for(MetadataSPI m : metadata){
        if(m.getID() == idx){
          metadata.remove(count);
        }
        count += 1;
      }
    }

    public void removeAllMetadata(){
      logger.info("Removed all metadata from server state");
      metadata.clear();
    }

//    public Enumeration getAllMetadata(){
//        return metadata.elements();
//    }

    public ArrayList<MetadataSPI> getMetadata(){
      return metadata;
    }

    public int metaCount(){
      return metadata.size();
    }

    public boolean contains(MetadataSPI m){
      return metadata.contains(m);
    }

    public boolean contains(int mid){
      for(MetadataSPI m:metadata){
        if(m.getID() == mid){
          return true;
        }
      }
      return false;
    }
    // public String getInfo() {
    //     return info;
    // }
    // 
    // public void setInfo(String info) {
    //     this.info = info;
    // }
}
