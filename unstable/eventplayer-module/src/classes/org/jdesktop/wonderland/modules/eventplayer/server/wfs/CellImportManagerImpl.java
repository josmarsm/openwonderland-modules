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

/**
 * Implementation of CellImportManager.  This just forwards everything to
 * the service.
 * @author bernard horan
 */
public class CellImportManagerImpl implements CellImportManager {
    private CellImportService service;

    public CellImportManagerImpl(CellImportService service) {
        this.service = service;
    }

    public void retrieveCells(String tapeName, CellRetrievalListener listener) {
        service.retrieveCells(tapeName, listener);
    }

    public void loadRecording(String name, RecordingLoadedListener listener) {
        service.loadRecording(name, listener);
    }
}
