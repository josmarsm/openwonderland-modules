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
import java.util.LinkedHashMap;
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

    private LinkedHashMap<MetadataID, Metadata> metadata;

    /** Default constructor */
    public MetadataComponentServerState() {
        metadata = new LinkedHashMap<MetadataID, Metadata>();
    }

    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.metadata.server.MetadataComponentMO";
    }

    /**
     * Add a piece of metadata to the server state
     * @param meta
     */
    public void addMetadata(Metadata meta){
      logger.info("Adding metadata to server state:\n" + meta);
      metadata.put(meta.getID(), meta);
    }

    /**
     * Remove a piece of metadata from the server state
     * @param id MetadataID of piece to remove
     */
    public void removeMetadata(MetadataID id){
      logger.info("removing metadata with id " + id + " from server state");
      metadata.remove(id);
    }

    /**
     * Convenience overload
     * @param meta
     */
    public void removeMetadata(Metadata meta){
      metadata.remove(meta.getID());
    }

    /**
     * Remove every piece of metadata from the server state
     */
    public void removeAllMetadata(){
      logger.info("Removed all metadata from server state");
      metadata.clear();
    }

//    public Enumeration getAllMetadata(){
//        return metadata.elements();
//    }

    /**
     * Get LinkedHashMap backing metadata - useful if client will
     * eventually be looking up metadata by ID.
     * @return
     */
    public LinkedHashMap<MetadataID, Metadata> getAllMetadataMap(){
      return metadata;
    }

    /**
     * Get all metadata in the server state. Will be ordered by addition. If
     * client will eventually be looking up metadata by ID, use getAllMetadataMap
     * instead
     * @return
     */
    public ArrayList<Metadata> getAllMetadata(){
      ArrayList<Metadata> l = new ArrayList<Metadata>(metadata.values());
      return l;
    }

    public int metaCount(){
      return metadata.size();
    }

    /**
     * Returns true if the a piece of metadata with an id equal to mid is
     * present in this server state
     * @param mid
     * @return
     */
    public boolean contains(MetadataID mid){
      return metadata.containsKey(mid);
    }

    /**
     * Returns true if the a piece of metadata with an id equal to mid is
     * present in this server state. Convenience overload.
     * @param m
     * @return
     */
    public boolean contains(Metadata m){
      return metadata.containsKey(m.getID());
    }

    
    // public String getInfo() {
    //     return info;
    // }
    // 
    // public void setInfo(String info) {
    //     this.info = info;
    // }
}
