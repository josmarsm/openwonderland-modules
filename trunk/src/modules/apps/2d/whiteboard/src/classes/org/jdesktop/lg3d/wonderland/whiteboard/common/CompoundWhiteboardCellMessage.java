/**
 * Project Looking Glass
 *
 * $RCSfile: CompoundWhiteboardCellMessage.java,v $
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
 * $Date: 2007/09/21 16:54:19 $
 * $State: Exp $
 */
package org.jdesktop.lg3d.wonderland.whiteboard.common;

import java.awt.Color;
import java.awt.Point;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataDouble;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataInt;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardAction.Action;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardCommand.Command;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardTool.Tool;

/**
 * A compunding Cell Message that coalesces messages of the same type
 * (in this case with the same action) but differ only in other properties
 * (position of the actions)
 *
 * @author nsimpson
 */
public class CompoundWhiteboardCellMessage extends WhiteboardCellMessage {
    
    private static final Logger logger =
            Logger.getLogger(CompoundWhiteboardCellMessage.class.getName());
    
    private LinkedList<Point> positions = new LinkedList<Point>();
    
    
    public CompoundWhiteboardCellMessage() {
        super();
    }
    
    public CompoundWhiteboardCellMessage(WhiteboardCellMessage src) {
        this.mySetCellID(src.getCellID());
        this.actionType = src.actionType;        
        this.action = src.action;
        this.color = src.color;
        this.command = src.command;
        this.tool = src.tool;
        addPosition(src.getPosition());
    }
    
    public CompoundWhiteboardCellMessage(Action action) {
        super(action);
    }
    
    public CompoundWhiteboardCellMessage(Action action, Point position) {
        super(action, position);
    }
    
    public CompoundWhiteboardCellMessage(CellID cellID, Action action) {
        super(cellID, action);
    }
    
    public CompoundWhiteboardCellMessage(CellID cellID, Action action, Point position) {
        super(cellID, action);
        setPosition(position);
    }
    
    public CompoundWhiteboardCellMessage(CellID cellID, Action action, Tool tool) {
        super(cellID, action, tool);
    }
    
    public CompoundWhiteboardCellMessage(CellID cellID, Action action, Command command) {
        super(cellID, action, command);
    }
    
    public CompoundWhiteboardCellMessage(CellID cellID, Action action, Color color) {
        super(cellID, action, color);
    }
    
    /**
     * Add an (x, y) position of an action
     * @param position the (x, y) position of the action
     */
    @Override
    public void setPosition(Point position) {
        positions.add(position);
    }
    
    /**
     * Get the last (x, y) position of the last action
     * @return the (x, y) position
     */
    @Override
    public Point getPosition() {
        return positions.getLast();
    }
    
    /**
     * Add a new position
     * @param position the (x, y) position of the action
     */
    public void addPosition(Point position) {
        positions.add(position);
    }
    
    /**
     * Set the (x, y) positions of all of the actions
     * @param a list of (x, y) positions
     */
    public void setPositions(LinkedList<Point> positions) {
        this.positions = positions;
    }
    
    /**
     * Get the (x, y) positions of all of the actions
     * @return a list of (x, y) positions
     */
    public LinkedList<Point> getPositions() {
        return positions;
    }
    
    /**
     * Get a string representation of the whiteboard cell message
     * @return a the cell message as as String
     */
    @Override
    public String toString() {
        return getCellID() + ", " + getActionType() + ", " + getAction() 
        + ", " + getCommand() + ", " + getColor() + ", " + getPositions();
    }
    
    @Override
    protected void addPositionData() {
        dataElements.add(new DataInt(positions.size()));    // number of positions
        
        Iterator<Point> iter = positions.iterator();
        while (iter.hasNext()) {
            Point position = (Point)iter.next();
            dataElements.add(new DataDouble(position.getX()));
            dataElements.add(new DataDouble(position.getY()));
        }
    }
    
    @Override
    protected void extractPositionData(ByteBuffer data) {
        int points = DataInt.value(data);
        logger.finest("message has " + points + " points");
        
        for (int i = 0;i < points;i++) {
            position = new Point((int)DataDouble.value(data), (int)DataDouble.value(data));
            logger.finest("adding position: "+ position);
            addPosition(position);
        }
    }
}
