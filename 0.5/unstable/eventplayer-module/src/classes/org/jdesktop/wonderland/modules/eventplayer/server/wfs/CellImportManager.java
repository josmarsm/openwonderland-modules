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

import com.sun.sgs.app.ManagedReference;
import java.util.LinkedList;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.wfs.CellList;
import org.jdesktop.wonderland.modules.eventplayer.server.wfs.RecordingLoaderUtils.CellImportEntry;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.wfs.importer.CellMap;

/**
 * A service for importing cells.  Callers will be notified if the
 * import succeeds or fails.
 *
 * @author jkaplan
 * @author Bernard Horan
 */
public interface CellImportManager {
    /**
     * Create a new recording for writing cells to.  This method will contact
     * the remote web service to create a new recording, and then call the
     * given listener with the result of that call.
     * @param name the name of the recording to create, or null to use the
     * default name
     * @param listener a recording creation listener that will be notified of
     * the result of this call
     */
    public void retrieveCells(String name, CellRetrievalListener listener);

    /**
     * A listener that will be notified of the success or failure of
     * creating a recording.  Implementations of RecordingCreationListener
     * must be either a ManagedObject or Serializable.
     */
    public interface CellRetrievalListener {


        /**
         * Notification that a recording has been created successfully
         * @param children 
         * @param cellID
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
