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
package org.jdesktop.wonderland.modules.eventplayer.server.wfs;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.eventplayer.server.RecordingRoot;
import org.jdesktop.wonderland.modules.eventplayer.server.wfs.RecordingLoaderUtils.CellImportEntry;
import org.jdesktop.wonderland.server.wfs.importer.CellMap;

/**
 * A service for importing cells.  Callers will be notified if the
 * import succeeds or fails.
 * @author Bernard Horan
 */
public interface CellImportManager {

    /**
     * Load a named recording. This method will contact a remote web service
     * to load the recording, then call the given listener with the result
     * of loading the recording
     * @param name the name of the recording to load
     * @param listener a recording loaded listener that will be notified of the
     * result of this call
     */
    public void loadRecording(String name, RecordingLoadedListener listener);

    /**
     * Retrieve cells from a named recording. This method will contact a remote
     * web service to retrieve the cells, then call the given listener with the
     * result of retrieving the cells.
     * @param name the name of the recording to retrieve
     * @param listener a cell retrieval listener that will be notified of
     * the result of this call
     */
    public void retrieveCells(String name, CellRetrievalListener listener);

    /**
     * A listener that will be notified of the result of loading a recording.
     * Implementations of RecordingLoadedListener
     * must be either a ManagedObject or Serializable.
     */
    public interface RecordingLoadedListener {

        /**
         * Notification that a recording has been loaded
         * @param root the recording root of the recording
         * @param ex if non-null, the loading has failed
         */
        public void recordingLoaded(RecordingRoot root, Exception ex);
        
    }

    /**
     * A listener that will be notified of the success or failure of
     * retrieving cells.  Implementations of CellRetrievalListener
     * must be either a ManagedObject or Serializable.
     */
    public interface CellRetrievalListener {

        /**
         * No more cells to be retrieved
         */
        public void allCellsRetrieved();


        /**
         * Notification that a cells have been retrieved
         * @param cellMOMap a map of cells that have been retrieved
         * @param cellPathMap a map of the paths of the cells that have been retrieved
         */
        public void cellsRetrieved(CellMap<CellImportEntry> cellMOMap, CellMap<CellID> cellPathMap);

        /**
         * Notification that recording creation has failed.
         * @param reason a String describing the reason for failure
         * @param cause an exception that caused the failure.
         */
        public void cellRetrievalFailed(String reason, Throwable cause);
    }    
}
