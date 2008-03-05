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

import java.util.logging.Logger;

import javax.vecmath.Matrix4d;

import org.jdesktop.lg3d.wonderland.darkstar.client.ExtendedClientChannelListener;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DImageCell;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellStatus;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;

import org.jdesktop.lg3d.wonderland.videomodule.common.VideoModuleCellMessage;
import org.jdesktop.lg3d.wonderland.videomodule.common.VideoModuleCellSetup;

/**
 *
 * @author nsimpson
 */
public class VideoModuleCell extends SharedApp2DImageCell
        implements ExtendedClientChannelListener {
    
    private static final Logger logger =
            Logger.getLogger(VideoModuleCell.class.getName());
    
    private VideoModuleApp app;
    private VideoModuleCellSetup setup;
    
    public VideoModuleCell(CellID cellID, String channelName, Matrix4d cellOrigin) {
        super(cellID, channelName, cellOrigin);
    }
    
    public void setChannel(ClientChannel channel) {
        this.channel = channel;
    }
    
    @Override
    public void setup(CellSetup setupData) {
        app = new VideoModuleApp(this);
        setup = (VideoModuleCellSetup)setupData;
        if (setup != null) {
            logger.fine("loading video: " + setup.getSource());
            logger.fine("play on load: " + setup.getPlayOnLoad());
            logger.fine("sync playback: " + setup.getSyncPlayback());
            
            app.setPreferredWidth(setup.getPreferredWidth());
            app.setPreferredHeight(setup.getPreferredHeight());
            
            app.loadVideo(setup.getSource());
            app.setFrameRate(setup.getFrameRate());
            app.setSynced(setup.getSyncPlayback()); 

            if (setup.getPlayOnLoad() == true) {
                app.play(true);
            } else {
                app.cue(0.5, 0.1);
            }
        }
    }
    
    public void receivedMessage(ClientChannel client, SessionId session,
            byte[] data) {
        VideoModuleCellMessage msg =
                Message.extractMessage(data, VideoModuleCellMessage.class);
        logger.fine("--- cell received message: " + msg);
        
        switch (msg.getAction()) {
            case SET_SOURCE:
                app.loadVideo(msg.getVideoURL());
                break;
            case PLAY:
                if (app.isSynced() == true) {
                    app.setPosition(msg.getPosition());
                    app.play(true);
                }
                break;
            case PAUSE:
                if (app.isSynced() == true) {
                    app.play(false);
                    app.setPosition(msg.getPosition());
                }
                break;
            case STOP:
                if (app.isSynced() == true) {
                    app.play(false);
                    app.setPosition(msg.getPosition());
                }
                break;
            case GET_STATUS:
                app.reportStatus();
                break;
            case STATUS:
                app.handleSync(msg.getVideoURL(), msg.getState(), msg.getPosition());
                break;
        }
    }
    
    public void leftChannel(ClientChannel arg0) {
        // ignore
    }
    
    @Override
    public synchronized boolean setStatus(CellStatus status) {
        if (status != getStatus()) {
            logger.fine("---- cell status changed: " + getStatus() + "-> " + status);
        }
//        if ((getStatus() != CellStatus.VISIBLE) && (status == CellStatus.VISIBLE)) {
//            // video has come into view
//            logger.fine("--- video out of view ---");
//            //app.play(true);
//        } else if ((getStatus() != CellStatus.ACTIVE) && (status == CellStatus.ACTIVE)) {
//            // video is no longer in view
//            logger.fine("--- video in view ---");
//            //app.play(false);
//        }
        
        return super.setStatus(status);
    }
}
