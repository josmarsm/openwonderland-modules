/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.cellperformance.server;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.cellperformance.common.CellPerformanceComponentServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.annotation.NoSnapshot;

/**
 * Server side of cell performance component. This is just a placeholder to
 * ensure the CellPerformanceComponent is available on the client.
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@NoSnapshot
public class CellPerformanceComponentMO extends CellComponentMO {

    private static final Logger logger =
            Logger.getLogger(CellPerformanceComponentMO.class.getName());

    /**
     * Create a CellPerformanceComponentMO for the given cell.
     */
    public CellPerformanceComponentMO(CellMO cell) {
        super(cell);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState serverState) {
        if (serverState == null) {
            serverState = new CellPerformanceComponentServerState();
        }

        return serverState;
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.cellperformance.client.CellPerformanceComponent";
    }
}
