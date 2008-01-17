/**
 * Project Looking Glass
 *
 * $RCSfile: WhiteboardCell.java,v $
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
 * $Revision: 1.3 $
 * $Date: 2007/11/30 22:53:59 $
 * $State: Exp $
 */
package org.jdesktop.lg3d.wonderland.whiteboard.client.cell;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.SessionId;
import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;
import javax.vecmath.Matrix4d;
import org.jdesktop.lg3d.wonderland.darkstar.client.ExtendedClientChannelListener;
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DImageCell;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardCellSetup;
import org.jdesktop.lg3d.wonderland.whiteboard.common.CompoundWhiteboardCellMessage;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardAction;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardAction.Action;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardCommand.Command;

/**
 * Client Cell for a whiteboard shared application.
 *
 * @author nsimpson
 */
public class WhiteboardCell extends SharedApp2DImageCell
        implements ExtendedClientChannelListener {
    
    private static final Logger logger =
            Logger.getLogger(WhiteboardCell.class.getName());
    
    private WhiteboardApp whiteboard;
    private WhiteboardCellSetup setup;
    
    public WhiteboardCell(final CellID cellID, String channelName, Matrix4d origin) {
        super(cellID, channelName, origin);
    }
    
    /**
     * Initialize the whiteboard
     * @param setupData the setup data to initialize the cell with
     */
    public void setup(CellSetup setupData) {
        setup = (WhiteboardCellSetup)setupData;
        whiteboard = new WhiteboardApp(this, 0, 0,
                (int)setup.getPreferredWidth(),
                (int)setup.getPreferredHeight());
        
//      logger.info("******* " + setup.getPixelScaleX());
//	logger.info("******* " + setup.getPixelScaleY());
//	logger.info("******* " + setup.getViewRectMat());
        
        // request sync with shared whiteboard state
        logger.info("whiteboard requesting initial sync");
        CompoundWhiteboardCellMessage cmsg = new CompoundWhiteboardCellMessage(this.getCellID(),
                WhiteboardAction.REQUEST_SYNC);
        ChannelController.getController().sendMessage(cmsg);
    }
    
    /**
     * Set the channel associated with this cell
     * @param channel the channel to associate with this cell
     */
    public void setChannel(ClientChannel channel) {
        this.channel = channel;
    }
    
    /**
     * Process the actions in a compound message
     * @param msg a compound message
     */
    private void processMessage(CompoundWhiteboardCellMessage msg) {
        switch (msg.getAction()) {
            case SET_TOOL:
                whiteboard.setTool(msg.getTool());
                break;
            case SET_COLOR:
                whiteboard.setPenColor(msg.getColor());
                break;
            case MOVE_TO:
            case DRAG_TO:
                LinkedList<Point> positions = msg.getPositions();
                Iterator<Point> iter = positions.iterator();
                
                while (iter.hasNext()) {
                    Point position = iter.next();
                    if (msg.getAction() == Action.MOVE_TO) {
                        whiteboard.moveTo(position);
                    } else if (msg.getAction() == Action.DRAG_TO) {
                        whiteboard.dragTo(position);
                    }
                }
                break;
            case EXECUTE_COMMAND:
                if (msg.getCommand() == Command.ERASE) {
                    whiteboard.erase();
                }
        }
    }
    
    /**
     * Handles an incoming cell message
     * @param channel the channel
     * @param session the session id
     * @param data the message data
     */
    public void receivedMessage(ClientChannel channel, SessionId session,
            byte[] data) {
        CompoundWhiteboardCellMessage msg = Message.extractMessage(data, CompoundWhiteboardCellMessage.class);
        logger.finest("receivedMessage: " + msg);
        
        processMessage(msg);
    }
    
    /**
     * Process a channel leave event
     * @param channel the left channel
     */
    public void leftChannel(ClientChannel channel) {
        logger.finest("leftChannel: " + channel);
    }
}
