/**
 * Project Looking Glass
 *
 * $RCSfile: WhiteboardApp.java,v $
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
 * $Date: 2007/11/29 22:46:49 $
 * $State: Exp $
 */
package org.jdesktop.lg3d.wonderland.whiteboard.client.cell;

import java.awt.Color;
import java.awt.event.MouseEvent;
import org.jdesktop.lg3d.wonderland.appshare.AppWindowGraphics2DApp;
import org.jdesktop.lg3d.wonderland.appshare.SimpleControlArb;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.logging.Logger;
import org.jdesktop.lg3d.wonderland.appshare.AppGroup;
import org.jdesktop.lg3d.wonderland.appshare.DrawingSurface;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DImageCell;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardCellMessage;
import org.jdesktop.lg3d.wonderland.whiteboard.common.BufferedCompoundMessageSender;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardAction;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardDrawingSurface;
import org.jdesktop.lg3d.wonderland.whiteboard.common.WhiteboardTool.Tool;

/**
 *
 * A 2D whiteboard application
 *
 * @author paulby
 */
public class WhiteboardApp extends AppWindowGraphics2DApp  {
    
    private static final Logger logger =
            Logger.getLogger(WhiteboardApp.class.getName());
    
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;
    
    private WhiteboardDrawingSurface drawingSurface;
    private BufferedCompoundMessageSender sender = new BufferedCompoundMessageSender();
    private int preferredWidth = DEFAULT_WIDTH;
    private int preferredHeight = DEFAULT_HEIGHT;
    
    public WhiteboardApp(SharedApp2DImageCell cell) {
        this(cell, 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    public WhiteboardApp(SharedApp2DImageCell cell, int x, int y, int width, int height) {
        super(new AppGroup(new SimpleControlArb()), true, x, y, width, height, cell);
        drawingSurface = new WhiteboardDrawingSurface();
        drawingSurface.setSize(width, height);
        
        drawingSurface.addSurfaceListener(new DrawingSurface.SurfaceListener() {
            public void redrawSurface() {
                repaint();
            }
        });
        
        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                dragTo(e.getPoint());
                
                // notify other clients
                WhiteboardCellMessage msg = new WhiteboardCellMessage(WhiteboardApp.this.getCell().getCellID(),
                        WhiteboardAction.DRAG_TO,
                        e.getPoint());
                sender.enqueue(msg);
            }
            
            public void mouseMoved(MouseEvent e) {
                moveTo(e.getPoint());
                
                // notify other clients
                WhiteboardCellMessage msg = new WhiteboardCellMessage(WhiteboardApp.this.getCell().getCellID(),
                        WhiteboardAction.MOVE_TO,
                        e.getPoint());
                sender.enqueue(msg);
            }
            
        });
        
        addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                selectPen(e.getPoint());
                
                // notify other clients
                WhiteboardCellMessage msg = null;
                
                switch (drawingSurface.getActionType()) {
                    case COLOR:
                        logger.info("select color: " + drawingSurface.getPenColor());
                        msg = new WhiteboardCellMessage(WhiteboardApp.this.getCell().getCellID(),
                                WhiteboardAction.SET_COLOR,
                                drawingSurface.getPenColor());
                        break;
                    case TOOL:
                        logger.info("select tool: " + drawingSurface.getTool());
                        msg = new WhiteboardCellMessage(WhiteboardApp.this.getCell().getCellID(),
                                WhiteboardAction.SET_TOOL, 
                                drawingSurface.getTool());
                        break;
                    case COMMAND:
                        logger.info("execute command: " + drawingSurface.getCommand());
                        msg = new WhiteboardCellMessage(WhiteboardApp.this.getCell().getCellID(),
                                WhiteboardAction.EXECUTE_COMMAND, 
                                drawingSurface.getCommand());
                        break;
                }
                if (msg != null) {
                    sender.enqueue(msg);
                }
            }
            
            public void mousePressed(MouseEvent e) {
            }
            
            public void mouseReleased(MouseEvent e) {
            }
            
            public void mouseEntered(MouseEvent e) {
            }
            
            public void mouseExited(MouseEvent e) {
            }
            
        });
        
        setShowing(true);
    }
    
    /**
     * Move the cursor to the specified position
     * @param position the coordinate to move to
     */
    public void moveTo(Point position) {
        if (drawingSurface!=null) {
            logger.finest("moveTo: " + position);
            drawingSurface.penMove(position);
        }
    }
    
    /**
     * Drag the mouse to the specified position
     * @param position the coordinate to drag to
     */
    public void dragTo(Point position) {
        if (drawingSurface!=null) {
            logger.finest("dragTo: " + position);
            drawingSurface.penDrag(position);
        }
    }
    
    /**
     * Select the pen at the specified position
     * @param position the coordinate of the pen
     */
    public void selectPen(Point position) {
        if (drawingSurface!=null) {
            logger.fine("selectPen: " + position);
            drawingSurface.penSelect(position);
        }
    }
    
    /**
     * Set the pen color
     * @param color the pen color
     */
    public void setPenColor(Color color) {
         if (drawingSurface!=null) {
            logger.info("selectColor: " + color);
            drawingSurface.setPenColor(color);
        }       
    }

    /**
     * Erase the whiteboard
     */
    public void erase() {
        if (drawingSurface!=null) {
            logger.info("erasing whiteboard");
            drawingSurface.erase();
            repaint();
        }
    }
    
    /**
     * Set the current tool
     * @param tool the tool
     */
    public void setTool(Tool tool) {
        if (drawingSurface!=null) {
            logger.info("selecting tool: " + tool);
            drawingSurface.setTool(tool);
        }
    }
    
    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }
    
    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }
    
    /**
     * Set the size of the application
     * @param width the width of the application
     * @param height the height of the application
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        drawingSurface.setSize(width, height);
    }
    
    /**
     * Paint contents of window
     */
    @Override
    protected void paint(Graphics2D g) {
        logger.finest("whiteboard paint");
        if (drawingSurface!=null)
            drawingSurface.paint(g);
    }
}
