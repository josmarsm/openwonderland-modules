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

package org.jdesktop.wonderland.modules.screenshare.server;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.screenshare.common.ScreenShareServerState;
import org.jdesktop.wonderland.modules.webcamviewer.server.cell.WebcamViewerCellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * CellMO for screen sharing
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class ScreenShareCellMO extends WebcamViewerCellMO {
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID,
                                            ClientCapabilities capabilities)
    {
        return "org.jdesktop.wonderland.modules.screenshare.client.ScreenShareCell";
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new ScreenShareServerState();
        }

        return super.getServerState(state);
    }
}
