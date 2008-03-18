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

import javax.vecmath.Matrix4d;

import org.jdesktop.lg3d.displayserver.EventProcessor;
import org.jdesktop.lg3d.wg.event.LgEvent;
import org.jdesktop.lg3d.wg.event.LgEventListener;
import org.jdesktop.lg3d.wonderland.darkstar.client.ExtendedClientChannelListener;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DImageCell;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellStatus;
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
    private VideoApp app;
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
        setup = (VideoCellSetup) setupData;

        if (setup != null) {
            if (setup.getVideoInstance() instanceof PTZCamera) {
                PTZCamera ptz = (PTZCamera) setup.getVideoInstance();
                // initialize pan, tilt and zoom state
                logger.fine("initial ptz: " + setup.getPan() + ", " + setup.getTilt() + ", " + setup.getZoom());
                ptz.setPTZPosition(setup.getPan(), setup.getTilt(), setup.getZoom());

                if (setup.getPanoramic() == true) {
                    // create a panorama viewer
                    app = new PTZPanoramaApp(this);
                } else {
                    // create a simple PTZ viewer
                    app = new PTZCameraApp(this);
                }
                app.setVideoInstance(ptz);
            } else {
                // standard video player
                app = new VideoApp(this);
                app.setVideoInstance(setup.getVideoInstance());
            }

            logger.fine("loading video: " + setup.getSource());
            logger.fine("play on load: " + setup.getPlayOnLoad());
            logger.fine("sync playback: " + setup.getSynced());

            app.setPreferredWidth(setup.getPreferredWidth());
            app.setPreferredHeight(setup.getPreferredHeight());
            app.setFrameRate(setup.getFrameRate());
            app.setSynced(setup.getSynced());

            if (setup.getSynced() == true) {
                app.getSynced();
            } else {
                if (setup.getPlayOnLoad() == true) {
                    logger.fine("--- play on load is true, playing");
                    app.play(true);
                } else {
                    logger.fine("--- play on load is false, cueing");
                    app.cue(0.5, 0.1);
                }
            }
        }
    }

    public String getUID() {
        return myUID;
    }

    protected void handleResponse(VideoCellMessage msg) {
        app.handleResponse(msg);
    }

    public void receivedMessage(ClientChannel client, SessionId session,
            byte[] data) {
        VideoCellMessage msg =
                Message.extractMessage(data, VideoCellMessage.class);
        logger.fine("cell received message: " + msg);
        handleResponse(msg);
    }

    public void leftChannel(ClientChannel arg0) {
    // ignore
    }

    @Override
    public synchronized boolean setStatus(CellStatus status) {
        if (status != getStatus()) {
            logger.fine("cell status changed: " + getStatus() + "-> " + status);
        }
//        if ((getStatus() != CellStatus.VISIBLE) && (status == CellStatus.VISIBLE)) {
//            // video has come into view
//            logger.fine("video out of view");
//            //app.play(true);
//        } else if ((getStatus() != CellStatus.ACTIVE) && (status == CellStatus.ACTIVE)) {
//            // video is no longer in view
//            logger.fine("video in view");
//            //app.play(false);
//        }
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
        logger.fine("--- enter cell from: " + source);
    }

    public void exitCell(String source) {
        logger.fine("--- exit cell from: " + source);
    }
}
