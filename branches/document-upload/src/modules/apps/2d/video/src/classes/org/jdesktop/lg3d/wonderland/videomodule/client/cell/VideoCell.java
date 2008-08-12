/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.lg3d.wonderland.videomodule.client.cell;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.SessionId;

import java.rmi.server.UID;

import java.util.logging.Logger;

import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point2f;

import org.jdesktop.lg3d.displayserver.EventProcessor;
import org.jdesktop.lg3d.wg.event.LgEvent;
import org.jdesktop.lg3d.wg.event.LgEventListener;

import org.jdesktop.lg3d.wonderland.darkstar.client.ExtendedClientChannelListener;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DImageCell;

import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellStatus;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;

import org.jdesktop.lg3d.wonderland.scenemanager.events.CellEnterEvent;
import org.jdesktop.lg3d.wonderland.scenemanager.events.CellEnterExitEvent;
import org.jdesktop.lg3d.wonderland.scenemanager.events.CellExitEvent;

import org.jdesktop.lg3d.wonderland.videomodule.common.PTZCamera;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellMessage;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoCellSetup;

/**
 *
 * @author nsimpson
 */
public class VideoCell extends SharedApp2DImageCell
        implements ExtendedClientChannelListener, LgEventListener {

    private static final Logger logger =
            Logger.getLogger(VideoCell.class.getName());
    private VideoCellSetup setup;
    private String myUID = new UID().toString();

    public VideoCell(CellID cellID, String channelName, Matrix4d cellOrigin) {
        super(cellID, channelName, cellOrigin);

        EventProcessor.processor().addListener(this, CellEnterExitEvent.class, this);
    }

    public void setChannel(ClientChannel channel) {
        this.channel = channel;
    }

    @Override
    public void setup(CellSetup setupData) {
        super.setup(setupData);

        setup = (VideoCellSetup) setupData;

        if (app instanceof PTZCamera) {
            PTZCamera ptz = (PTZCamera) setup.getVideoInstance();
            // initialize pan, tilt and zoom state
            logger.fine("initial ptz: " + setup.getPan() + ", " + setup.getTilt() + ", " + setup.getZoom());
            ptz.setPTZPosition(setup.getPan(), setup.getTilt(), setup.getZoom());
            ((VideoApp) app).setVideoInstance(ptz);
        } else {
            // standard video player
            ((VideoApp) app).setVideoInstance(setup.getVideoInstance());
        }

        logger.info("video: " + setup.getSource());
        logger.info("play on load: " + setup.getPlayOnLoad());
        logger.info("sync playback: " + setup.getSynced());

        ((VideoApp) app).setPreferredWidth(setup.getPreferredWidth());
        ((VideoApp) app).setPreferredHeight(setup.getPreferredHeight());
        ((VideoApp) app).setPixelScale(new Point2f(setup.getPixelScale(), setup.getPixelScale()));
        ((VideoApp) app).setFrameRate(setup.getFrameRate());
        ((VideoApp) app).setRequestThrottle(setup.getRequestThrottle());
        ((VideoApp) app).setDecorated(setup.getDecorated());
        ((VideoApp) app).setShowing(true);

        if (setup.getSynced() == true) {
            ((VideoApp) app).sync();
        } else {
            if (setup.getPlayOnLoad() == true) {
                ((VideoApp) app).play(true);
            } else {
                ((VideoApp) app).cue(setup.getPosition(), 0.1);
            }
        }
    }

    /**
     * Reconfigure the cell, when the origin, bounds, or other setup information
     * has changed.
     */
    @Override
    public void reconfigure(Matrix4d origin, Bounds bounds, CellSetup setupData) {
        super.reconfigure(origin, bounds, setupData);
        ((VideoApp) app).resync();
    }

    public String getUID() {
        return myUID;
    }

    protected void handleResponse(VideoCellMessage msg) {
        ((VideoApp) app).handleResponse(msg);
    }

    @Override
    public void receivedMessage(ClientChannel client, SessionId session,
            byte[] data) {
        CellMessage msg = Message.extractMessage(data, CellMessage.class);

        if (msg instanceof VideoCellMessage) {
            VideoCellMessage vcmsg = Message.extractMessage(data, VideoCellMessage.class);
            handleResponse(vcmsg);
        } else {
            super.receivedMessage(channel, session, data);
        }
    }

    public void leftChannel(ClientChannel arg0) {
        // ignore
    }

    @Override
    public synchronized boolean setStatus(CellStatus status) {
        if (status != getStatus()) {
            logger.finest("video cell: status changed: " + getStatus() + "-> " + status);
            if (status.equals(CellStatus.BOUNDS)) {
                ((VideoApp)app).unloadVideo();
            }
        }

        return super.setStatus(status);
    }

    public Class<LgEvent>[] getTargetEventClasses() {
        return new Class[]{VideoCell.class};
    }

    public void processEvent(LgEvent event) {
        String source = event.getSourceClass().getName();

        if (event instanceof CellEnterEvent) {
            enterCell(source);
        } else if (event instanceof CellExitEvent) {
            exitCell(source);
        }
    }

    public void enterCell(String source) {
        logger.fine("video cell: entered from: " + source);
    }

    public void exitCell(String source) {
        logger.fine("video cell: exited from: " + source);
    }
}
