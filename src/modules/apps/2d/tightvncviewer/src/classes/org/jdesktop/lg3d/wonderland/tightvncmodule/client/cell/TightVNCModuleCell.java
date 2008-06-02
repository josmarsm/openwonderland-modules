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
import java.rmi.server.UID;
import java.util.logging.Logger;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point2f;
import org.jdesktop.lg3d.wonderland.darkstar.client.ExtendedClientChannelListener;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DImageCell;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.TightVNCModuleCellMessage;
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
    private String myUID = new UID().toString();

    public TightVNCModuleCell(CellID cellID, String channelName, Matrix4d cellOrigin) {
        super(cellID, channelName, cellOrigin);
    }

    @Override
    public void setup(CellSetup setupData) {
        setup = (TightVNCModuleCellSetup) setupData;

        if (setup != null) {
            vncApp = new TightVNCModuleApp(this, 0, 0,
                    (int) setup.getPreferredWidth(),
                    (int) setup.getPreferredHeight(),
                    setup.getDecorated());
            vncApp.setPixelScale(new Point2f(setup.getPixelScale(), setup.getPixelScale()));
            vncApp.setReadOnly(setup.getReadOnly());
            vncApp.sync(true);
        }
    }

    public String getUID() {
        return myUID;
    }

    public void setChannel(ClientChannel channel) {
        this.channel = channel;
    }

    protected void handleResponse(TightVNCModuleCellMessage msg) {
        vncApp.handleResponse(msg);
    }

    /**
     * Process a cell message
     * @param channel the channel
     * @param session the session id
     * @param data the message data
     */
    @Override
    public void receivedMessage(ClientChannel channel, SessionId session,
            byte[] data) {
        TightVNCModuleCellMessage msg = Message.extractMessage(data, TightVNCModuleCellMessage.class);

        logger.fine("cell received message: " + msg);
        handleResponse(msg);
    }

    /**
     * Process a channel leave event
     * @param channel the left channel
     */
    public void leftChannel(ClientChannel channel) {
        logger.fine("leftChannel: " + channel);
    }
}
