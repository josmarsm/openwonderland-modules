/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.metadata.server.service;

import java.util.HashMap;
import java.util.Set;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.metadata.common.MetadataSPI;
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
  void addMetadata(CellID cid, MetadataSPI metadata);


  /**
   * Remove cell and all metadata. This should be called when a cell is deleted.
   * 
   * @param cid cellID of the cell to delete
   */
  public void eraseCell(CellID cid);

  /**
   * Delete the specified metadata object
   * @param mid metadata id designating the metadata to remove
   */
  public void removeMetadata(int mid);

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
  public void registerMetadataType(MetadataSPI m) throws Exception;


  /**
   * Search all cells in the world, finding cells with metadata satisfying the
   * passed in MetadataSearchFilters
   *
   * @param filters search criteria
   * @param cid id of parent cell to scope the search
   * @return map, mapping cell id's (as Integers) whose metadata that matched the
   * search, to a set of metadata id's that matched the search for that cell.
   */
  public HashMap<CellID, Set<Integer> > searchMetadata(MetadataSearchFilters filters);

  /**
   * Search all cells beneath cid, finding cells with metadata satisfying the
   * passed in MetadataSearchFilters
   *
   * @param filters search criteria
   * @param cid id of parent cell to scope the search
   * @return map, mapping cell id's (as Integers) whose metadata that matched the
   * search, to a set of metadata id's that matched the search for that cell.
   */
  public HashMap<CellID, Set<Integer> > searchMetadata(MetadataSearchFilters filters, CellID cid);
}
