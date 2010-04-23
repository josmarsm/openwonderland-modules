/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.joth.client.uijava;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.awt.event.MouseEvent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.modules.joth.client.utiljava.JothUtilJme;
import org.jdesktop.wonderland.modules.joth.client.gamejava.Board;
import org.jdesktop.wonderland.modules.joth.client.gamejava.Square;

/**********************************************
 * BoardDisplay: Displays the board for the UI.
 * @author deronj@dev.java.net
 */
public class BoardDisplay {

    /** The game board. */
    private Board board;
    /** Whether the scene graph is attached to the cell. */
    private boolean visible;
    /** The square nodes of the board display. */
    private SquareNode[][] squareGraphs;
    /** The event handler. */
    private JothUtilJme.EventClassHandler eventHandler;
    /** The UI which owns this object. */
    private UIWLSimple ui;

    /** Access to simplified the Wonderland API for this cell. */
    private JothUtilJme util;

    /**
     * Create a new instance of BoardDisplay.
     * @param cell The cell in which the board display lives.
     * @param board The game board.
     */
    public BoardDisplay (Cell cell, Board board, UIWLSimple ui) {
         this.board = board;
         this.ui = ui;

         util = new JothUtilJme(cell);

         // Create the nodes of the scene graph
         createSceneGraph();

         // Create event handler (to be attached when visible).
         eventHandler = new MyMouseHandler();
     }

     /**
      * Create the nodes of the scene graph.
      */
     private Node createSceneGraph () {
         Node rootNode = util.getRootNode();
         squareGraphs = new SquareNode[board.getNumRows()][];
         for (int r = 0; r < board.getNumRows(); r++) {
             SquareNode[] row = new SquareNode[board.getNumCols()];
             for (int c = 0; c < board.getNumCols(); c++) {
                 SquareNode sqNode = createSquareGraph(r, c);
                 row[c] = sqNode;
             }
             squareGraphs[r] = row;
         }

         rootNode.setLocalTranslation(new Vector3f(-getWidth()/2f, -getHeight()/2f, 0));
         return rootNode;
     }

    /**
     * Return the total width of the board display.
     */
    public float getWidth() {
        return board.getNumRows() * SquareGeometry.WIDTH;
    }

     /**
     * Return the total height of the board display.
     */
    public float getHeight() {
        return board.getNumCols() * SquareGeometry.HEIGHT;
    }
     /**
      * Create a subgraph for a particular square.
      */
    private SquareNode createSquareGraph (int row, int col) {
         SquareNode sqNode = new SquareNode(util, util.getRootNode(), row, col);
         SquareGeometry sqGeom = new SquareGeometry(util, row, col);
         util.attachChild(sqNode, sqGeom);
         return sqNode;
     }
     
     /**
      * Control the visibility of the board display.
      */
     public void setVisible (boolean visible) {
         if (this.visible == visible) return;
         this.visible = visible;

         // Do this before scene graph becomes visible
         if (visible) {
             util.addEventClassHandler(eventHandler);
         } else {
             util.removeEventHandler(eventHandler);
         }

         util.setVisible(visible);
     }

     /**
      * Update the display of a particular square with its
      * current contents in the board.
      */
     public void updateSquare (Square square) {

         // Remove old piece (if any)
         SquareNode sqNode = squareGraphs[square.getRow()][square.getCol()];
         sqNode.displayColor(Board.Color.EMPTY);

         Board.Color color = board.getContentsOfSquare(square);
         sqNode.displayColor(color);
     }

     /** Given a click event, determine which square it is in. */
     public Square eventToSquare (Event event) {
         MouseEvent3D me3d = (MouseEvent3D) event;
         SquareNode node = (SquareNode) me3d.getNode();
         return new Square(board, node.getRow(), node.getCol());
     }

    /**
     * A mouse event handler. This receives mouse input events from JothUtil.
     * Called on a generic thread.
     */
    private class MyMouseHandler implements JothUtilJme.EventClassHandler {

	/**
	 * This returns the classes of the Wonderland input events we are interested in receiving.
	 */
	public Class[] eventClassesToConsume () {
	    // Only respond to mouse button events
	    return new Class[] { MouseButtonEvent3D.class };
	}

	/**
	 * This will be called when a mouse event occurs over one of the scene graph nodes.
	 */
	public void processEvent (Event event) {

	    // Only respond to mouse left button click events
	    MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D) event;
	    if (buttonEvent.isClicked() && 
		buttonEvent.getButton() == MouseButtonEvent3D.ButtonId.BUTTON1) {
                MouseEvent me = (MouseEvent) buttonEvent.getAwtEvent();
                if (me.getModifiersEx() == 0) {
                    ui.squareClicked(event);
                }
	    }
	}
    }

}
