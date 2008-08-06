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
import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point2f;
import org.jdesktop.lg3d.wonderland.darkstar.client.ExtendedClientChannelListener;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DImageCell;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellStatus;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
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
    private TightVNCModuleCellSetup setup;
    private String myUID = new UID().toString();

    public TightVNCModuleCell(CellID cellID, String channelName, Matrix4d cellOrigin) {
        super(cellID, channelName, cellOrigin);
    }

    @Override
    public void setup(CellSetup setupData) {
        super.setup(setupData);

        setup = (TightVNCModuleCellSetup) setupData;

        ((TightVNCModuleApp) app).setPreferredWidth((int) setup.getPreferredWidth());
        ((TightVNCModuleApp) app).setPreferredHeight((int) setup.getPreferredHeight());
        ((TightVNCModuleApp) app).setDecorated(setup.getDecorated());
        ((TightVNCModuleApp) app).setPixelScale(new Point2f(setup.getPixelScale(), setup.getPixelScale()));
        ((TightVNCModuleApp) app).setReadOnly(setup.getReadOnly());
        ((TightVNCModuleApp) app).setShowing(true);

        // request sync with shared vnc state
        logger.info("vnc cell: requesting initial sync");
        ((TightVNCModuleApp) app).sync(true);
    }

    /**
     * Reconfigure the cell, when the origin, bounds, or other setup information
     * has changed.
     */
    @Override
    public void reconfigure(Matrix4d origin, Bounds bounds, CellSetup setupData) {
        super.reconfigure(origin, bounds, setupData);
        ((TightVNCModuleApp) app).resync();
    }

    public String getUID() {
        return myUID;
    }

    public void setChannel(ClientChannel channel) {
        this.channel = channel;
    }

    protected void handleResponse(TightVNCModuleCellMessage msg) {
        ((TightVNCModuleApp) app).handleResponse(msg);
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
        CellMessage msg = Message.extractMessage(data, CellMessage.class);

        if (msg instanceof TightVNCModuleCellMessage) {
            TightVNCModuleCellMessage vncmsg = Message.extractMessage(data, TightVNCModuleCellMessage.class);
            logger.fine("vnc cell: received message: " + vncmsg);
            handleResponse(vncmsg);
        } else {
            super.receivedMessage(channel, session, data);
        }
    }

    /**
     * Process a channel leave event
     * @param channel the left channel
     */
    public void leftChannel(ClientChannel channel) {
        logger.fine("vnc cell: leftChannel: " + channel);
    }

    @Override
    public synchronized boolean setStatus(CellStatus status) {
        if (status != getStatus()) {
            logger.finest("vnc cell: status changed: " + getStatus() + "-> " + status);
            if (status.equals(CellStatus.BOUNDS)) {
                // could quiesce vnc app here
                logger.fine("vnc cell: received BOUNDS status");
            }
        }

        return super.setStatus(status);
    }
}
