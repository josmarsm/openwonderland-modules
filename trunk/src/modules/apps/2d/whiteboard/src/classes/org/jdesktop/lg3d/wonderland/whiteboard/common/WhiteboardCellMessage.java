/**
 * Project Looking Glass
 *
 * $RCSfile: WhiteboardCellMessage.java,v $
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
 * $Revision: 1.1 $
 * $Date: 2007/09/21 16:54:20 $
 * $State: Exp $
 */
package org.jdesktop.lg3d.wonderland.whiteboard.common;

import java.awt.Color;
import java.awt.Point;
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import javax.vecmath.Color3f;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataColor3f;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataDouble;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataInt;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardAction.Action;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardActionType.ActionType;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardCommand.Command;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardTool.Tool;

/**
 * A Cell Message that carries whiteboard actions
 *
 * @author nsimpson
 */
public class WhiteboardCellMessage extends CellMessage {
    
    private static final Logger logger =
            Logger.getLogger(WhiteboardCellMessage.class.getName());
    
    protected ActionType actionType;
    protected Action action = WhiteboardAction.NO_ACTION;
    protected Point position;
    protected Tool tool;
    protected Command command;
    protected Color color;
    
    public WhiteboardCellMessage() {
        super();
    }
    
    public WhiteboardCellMessage(Action action) {
        this(action, null);
    }
    
    public WhiteboardCellMessage(Action action, Point position) {
        super();
        this.action = action;
        this.position = position;
    }
    
    public WhiteboardCellMessage(CellID cellID, Action action) {
        super(cellID);
        this.action = action;
    }
    
    public WhiteboardCellMessage(CellID cellID, Action action, Point position) {
        super(cellID);
        this.action = action;
        this.position = position;
    }
    
    public WhiteboardCellMessage(CellID cellID, Action action, Tool tool) {
        super(cellID);
        this.action = action;
        this.tool = tool;
        actionType = WhiteboardActionType.TOOL;
    }
    
    public WhiteboardCellMessage(CellID cellID, Action action, Command command) {
        super(cellID);
        this.action = action;
        this.command = command;
        actionType = WhiteboardActionType.COMMAND;
    }
    
    public WhiteboardCellMessage(CellID cellID, Action action, Color color) {
        super(cellID);
        this.action = action;
        this.color = color;
        actionType = WhiteboardActionType.COLOR;
    }
    
    public void mySetCellID(CellID cellID) {
        setCellID(cellID);
    }
    
    /**
     * Set the action
     * @param action the action
     */
    public void setAction(Action action) {
        this.action = action;
    }
    
    /**
     * Get the action
     * @return the action
     */
    public Action getAction() {
        return action;
    }
    
    /**
     * Get the type of the action
     * @return the action type
     */
    public ActionType getActionType() {
        return actionType;
    }
    
    /**
     * Set the (x, y) position of the page
     * @param position the (x, y) position of the page
     */
    public void setPosition(Point position) {
        this.position = position;
    }
    
    /**
     * Get the (x, y) position of the page
     * @return the (x, y) position
     */
    public Point getPosition() {
        return position;
    }
    
    /**
     * Set the active tool
     * @param tool the new tool
     */
    public void setTool(Tool tool) {
        this.tool = tool;
    }
    
    /**
     * Get the active tool
     * @return the active tool
     */
    public Tool getTool() {
        return tool;
    }
    
    /**
     * Set the active command
     * @param command the new command
     */
    public void setCommand(Command command) {
        this.command = command;
    }
    
    /**
     * Get the active command
     * @return the active command
     */
    public Command getCommand() {
        return command;
    }
    
    /**
     * Set the color
     * @param color the new color
     */
    public void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * Get the color
     * @return the current color
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * Get a string representation of the whiteboard cell message
     * @return a the cell message as as String
     */
    public String toString() {
        return getCellID() + ", " + getActionType() + ", " + getAction() 
        + ", " + getCommand() + ", " + getColor() + ", " + getPosition();
    }
    
    protected void addPositionData() {
        dataElements.add(new DataInt(1));
        dataElements.add(new DataDouble((position == null) ? 0 : position.getX()));
        dataElements.add(new DataDouble((position == null) ? 0 : position.getY()));
    }
    
    protected void extractPositionData(ByteBuffer data) {
        int pos = DataInt.value(data);
        position = new Point((int)DataDouble.value(data), (int)DataDouble.value(data));
    }
    
    /**
     * Extract the message from binary data
     */
    @Override
    protected void extractMessageImpl(ByteBuffer data) {
        super.extractMessageImpl(data);
        
        action = Action.values()[DataInt.value(data)];

        switch (action) {
            case SET_TOOL:
                // tool
                tool = Tool.values()[DataInt.value(data)];
                break;
            case SET_COLOR:
                // color
                color = DataColor3f.value(data).get();
                break;
            case MOVE_TO:
            case DRAG_TO:
                // position
                extractPositionData(data);
                break;
            case REQUEST_SYNC:
                break;
            case EXECUTE_COMMAND:
                // command
                command = Command.values()[DataInt.value(data)];
        }
    }
    
    /**
     * Create a binary version of the message
     */
    @Override
    protected void populateDataElements() {
        super.populateDataElements();

        // action
        dataElements.add(new DataInt(action.ordinal()));
        switch (action) {
            case SET_TOOL:
                // tool
                dataElements.add(new DataInt(tool.ordinal()));
                break;
            case SET_COLOR:
                // color
                Color3f c3 = new Color3f();
                c3.set(color);
                dataElements.add(new DataColor3f(c3));
                break;
            case MOVE_TO:
            case DRAG_TO:
                // position
                addPositionData();
                break;
            case REQUEST_SYNC:
                break;
            case EXECUTE_COMMAND:
                // command
                dataElements.add(new DataInt(command.ordinal()));
                break;
        }
    }
}
