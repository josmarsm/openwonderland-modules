/**
 * Project Looking Glass
 *
 * $RCSfile: WhiteboardCellGLO.java,v $
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
 * $Revision: 1.7 $
 * $Date: 2007/11/30 22:53:59 $
 * $State: Exp $
 */
package org.jdesktop.lg3d.wonderland.whiteboard.server.cell;

import com.sun.sgs.app.ClientSession;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.AxisAngle4d;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.server.CellMessageListener;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.SharedApp2DImageCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BeanSetupGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.CellGLOSetup;
import org.jdesktop.lg3d.wonderland.whiteboard.common.CompoundWhiteboardCellMessage;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardAction.Action;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardCellSetup;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardCommand.Command;

/**
 * A server cell associated with a whiteboard
 * @author nsimpson
 */
public class WhiteboardCellGLO extends SharedApp2DImageCellGLO
        implements BeanSetupGLO, CellMessageListener {
    
    private static final Logger logger =
            Logger.getLogger(WhiteboardCellGLO.class.getName());
    
    // The messages list contains the current state of the whiteboard.
    // It's updated every time a client makes a change to the whiteboard
    // so that when new clients join, they receive the current state
    private static LinkedList<CompoundWhiteboardCellMessage> messages;
    private static CompoundWhiteboardCellMessage lastMessage;
    
    private BasicCellGLOSetup<WhiteboardCellSetup> setup;
    
    public WhiteboardCellGLO() {
        this(null, null, null, null);
    }
    
    public WhiteboardCellGLO(Bounds bounds, String appName, Matrix4d cellOrigin,
            Matrix4f viewRectMat) {
        super(bounds, appName, cellOrigin, viewRectMat, WhiteboardCellGLO.class.getName());
        messages = new LinkedList<CompoundWhiteboardCellMessage>();
    }
    
    /**
     * Returns the fully qualified name of the class that represents
     * this cell on the client
     * @return the class name of the corresponding client cell
     */
    public String getClientCellClassName() {
        return "org.jdesktop.lg3d.wonderland.whiteboard.client.cell.WhiteboardCell";
    }
    
    /**
     * Get the setup data for this cell
     * @return the cell setup data
     */
    public WhiteboardCellSetup getSetupData() {
        return setup.getCellSetup();
    }
    
    /**
     * Set up the properties of this cell GLO from a JavaBean.  After calling
     * this method, the state of the cell GLO should contain all the information
     * represented in the given cell properties file.
     *
     * @param setup the Java bean to read setup information from
     */
    public void setupCell(CellGLOSetup setupData) {
        setup = (BasicCellGLOSetup<WhiteboardCellSetup>) setupData;
        
        AxisAngle4d aa = new AxisAngle4d(setup.getRotation());
        Matrix3d rot = new Matrix3d();
        rot.set(aa);
        Vector3d origin = new Vector3d(setup.getOrigin());
        
        Matrix4d o = new Matrix4d(rot, origin, setup.getScale() );
        setOrigin(o);
        
        if (setup.getBoundsType().equals("SPHERE")) {
            setBounds(createBoundingSphere(origin, (float)setup.getBoundsRadius()));
        } else {
            throw new RuntimeException("Unimplemented bounds type");
        }
            
    }
    
    /**
     * Called when the properties of a cell have changed.
     *
     * @param setup a Java bean with updated properties
     */
    public void reconfigureCell(CellGLOSetup setupData) {
        setupCell(setupData);
    }
    
    /**
     * Write the cell's current state to a JavaBean.
     * @return a JavaBean representing the current state
     */
    public CellGLOSetup getCellGLOSetup() {
        return new BasicCellGLOSetup<WhiteboardCellSetup>(getBounds(),
                getOrigin(), getClass().getName(),
                getSetupData());
    }
    
    /**
     * Open the cell channel
     */
    @Override
    public void openChannel() {
        this.openDefaultChannel();
    }
    
    /*
     * Handle message
     * @param client the client that sent the message
     * @param message the message
     */
    public void receivedMessage(ClientSession client, CellMessage message) {
        CompoundWhiteboardCellMessage cmsg = (CompoundWhiteboardCellMessage)message;
        logger.fine("received whiteboard message: " + cmsg);
        
        if (cmsg.getAction() == Action.REQUEST_SYNC) {
            logger.fine("sending " + messages.size() + " whiteboard sync messages");
            Iterator<CompoundWhiteboardCellMessage> iter = messages.iterator();
            
            while (iter.hasNext()) {
                CompoundWhiteboardCellMessage msg = iter.next();
                getCellChannel().send(client, msg.getBytes());
            }
        } else {
            // record the message in setup data (move events are not recorded)
            if (cmsg.getAction() == Action.EXECUTE_COMMAND) {
                if (cmsg.getCommand() == Command.ERASE) {
                    // clear the action history
                    logger.fine("clearing message history");
                    messages.clear();
                }
            } else {
                if (cmsg.getAction() != Action.MOVE_TO) {
                    if ((lastMessage != null) &&
                            lastMessage.getAction() == Action.MOVE_TO) {
                        messages.add(lastMessage);
                    }
                    messages.add(cmsg);
                }
            }
            lastMessage = cmsg;
            // notify all clients except the client that sent the message
            CompoundWhiteboardCellMessage msg = new CompoundWhiteboardCellMessage(cmsg.getAction());
            switch (cmsg.getAction()) {
                case SET_TOOL:
                    // tool
                    msg.setTool(cmsg.getTool());
                    break;
                case SET_COLOR:
                    // color
                    msg.setColor(cmsg.getColor());
                    break;
                case MOVE_TO:
                case DRAG_TO:
                    // position
                    msg.setPositions(cmsg.getPositions());
                    break;
                case REQUEST_SYNC:
                    break;
                case EXECUTE_COMMAND:
                    // command
                    msg.setCommand(cmsg.getCommand());
                    break;
            }
            
            Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());
            sessions.remove(client);
            logger.fine("distributing whiteboard message: " + msg);
            getCellChannel().send(sessions, msg.getBytes());
        }
    }
}
