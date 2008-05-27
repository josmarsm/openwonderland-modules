/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.lg3d.wonderland.tightvncmodule.client.cell;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.SessionId;
import java.util.logging.Logger;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point2f;
import org.jdesktop.lg3d.wonderland.darkstar.client.ExtendedClientChannelListener;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DImageCell;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.TightVNCModuleMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.TightVNCModuleCellSetup;

/**
 *
 * @author nsimpson
 */
public class TightVNCModuleCell extends SharedApp2DImageCell
        implements ExtendedClientChannelListener {

    private final Logger logger =
            Logger.getLogger(TightVNCModuleCell.class.getName());
    private TightVNCModuleApp vncApp;
    private TightVNCModuleCellSetup setup;

    public TightVNCModuleCell(CellID cellID, String channelName, Matrix4d cellOrigin) {
        super(cellID, channelName, cellOrigin);
    }

    @Override
    public void setup(CellSetup setupData) {
        setup = (TightVNCModuleCellSetup) setupData;

        if (setup != null) {
            vncApp = new TightVNCModuleApp(this, 0, 0,
                    (int) setup.getPreferredWidth(),
                    (int) setup.getPreferredHeight());
            vncApp.setPixelScale(new Point2f(setup.getPixelScale(), setup.getPixelScale()));
            vncApp.setReadOnly(setup.getReadOnly());
            logger.fine("connecting to VNC server: " + setup.getServer());
            vncApp.initializeVNC(setup.getServer(), setup.getPort(),
                    setup.getUsername(), setup.getPassword());
        }
    }

    public void setChannel(ClientChannel channel) {
        this.channel = channel;
    }

    public void receivedMessage(ClientChannel client, SessionId session,
            byte[] data) {
        TightVNCModuleMessage message =
                Message.extractMessage(data, TightVNCModuleMessage.class);
    }

    public void leftChannel(ClientChannel arg0) {
    // ignore
    }
}
