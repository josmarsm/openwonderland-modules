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

package org.jdesktop.wonderland.modules.metadata.server.service;

import java.util.HashMap;
import java.util.Set;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;
import org.jdesktop.wonderland.modules.metadata.common.MetadataID;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSearchFilters;

/**
 *
 * Backends to the metadata service (DB's to support searching) must implement
 * this interface.
 *
 * @author mabonner
 */
public interface MetadataBackendInterface {
  /**
   * adds a new cell to the top level (e.g., has no parent besides the world)
   * @param cid id of cell to create
   */
  void addCell(CellID cid);

  /**
   * adds a new cell beneath the passed in cell
   * @param cid id of cell to create
   * @param parent id of the parent cell to create under
   */
  void addCell(CellID cid, CellID parent);


  /**
   * adds the passed metadata object to the cell with cellID cid.
   * logs errors if the cell does not exist or the
   * metadata type has not been registered.
   * @param cid id of cell to add metadata to
   * @param metadata metadata object to add
   */
  void addMetadata(CellID cid, Metadata metadata);


  /**
   * Remove cell and all metadata. This should be called when a cell is deleted.
   * 
   * @param cid cellID of the cell to delete
   */
  public void eraseCell(CellID cid);

  /**
   * Delete the specified metadata object
   * @param mid MetadataID designating the metadata to remove
   */
  public void removeMetadata(MetadataID mid);

  /**
   * Remove all metadata from a cell
   *
   * @param cid id of cell to remove metadata from
   */
  public void clearCellMetadata(CellID cid);

  /**
   * Unregister all metadata types.
   *
   * Perhaps unnecessary? Just re-build db when the primary server changes instead
   *
   * TODO will scan class loader take care of duplication checking anyway?
   * @param m example of the type to register
   */
  // public void unregisterAllMetadataTypes();

  /**
   * Take any action necessary to register this metadatatype as an option.
   * Name collision on class name or attribute name is up to the implementation.
   *
   * The default implementation uses the full package name to describe a Metadata
   * obj and its attributes, avoiding collisions.
   *
   * TODO will scan class loader take care of duplication checking anyway?
   * @param m example of the type to register
   */
  public void registerMetadataType(Metadata m) throws Exception;


  /**
   * Search all cells in the world, finding cells with metadata satisfying the
   * passed in MetadataSearchFilters
   *
   * @param filters search criteria
   * @param cid id of parent cell to scope the search
   * @return map, mapping CellID's whose metadata that matched the
   * search, to a set of MetadataID's that matched the search for that cell.
   */
  public HashMap<CellID, Set<MetadataID> > searchMetadata(MetadataSearchFilters filters);

  /**
   * Search all cells beneath cid, finding cells with metadata satisfying the
   * passed in MetadataSearchFilters
   *
   * @param filters search criteria
   * @param cid id of parent cell to scope the search
   * @return map, mapping CellID's whose metadata that matched the
   * search, to a set of MetadataID's that matched the search for that cell.
   */
  public HashMap<CellID, Set<MetadataID> > searchMetadata(MetadataSearchFilters filters, CellID cid);
}
