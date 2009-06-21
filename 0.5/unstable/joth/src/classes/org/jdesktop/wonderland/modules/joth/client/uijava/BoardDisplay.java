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
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.EventListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.modules.joth.client.gamejava.Board;
import org.jdesktop.wonderland.modules.joth.client.gamejava.Square;

/**********************************************
 * BoardDisplay: Displays the board for the UI.
 * @author deronj@dev.java.net
 */
public class BoardDisplay {

    /** The cell. */
    private Cell cell;
    /** The game board. */
    private Board board;
    /** The entity of the cell. */
    private Entity cellEntity;
    /** Whether the scene graph is attached to the cell. */
    private boolean visible;
    /** The root entity of the board display. */
    private Entity rootEntity;
    /** The root node of the board display. */
    private Node rootNode;
    /** The square nodes of the board display. */
    private SquareNode[][] squareGraphs;
    /** The event listener. */
    private EventListener eventListener;
    /** The UI which owns this object. */
    private UIWLSimple ui;

    /**
     * Create a new instance of BoardDisplay.
     * @param cell The cell in which the board display lives.
     * @param board The game board.
     */
    public BoardDisplay (Cell cell, Board board, UIWLSimple ui) {
         this.cell = cell;
         this.board = board;
         this.ui = ui;

         cellEntity = 
             ((BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME)).getEntity();

         // Create the entity and nodes of the scene graph
         createSceneGraph();

         // Create event listener (to be attached when visible).
         eventListener = new MyMouseListener();
     }

     /**
      * Create the entity and nodes of the scene graph.
      */
     private void createSceneGraph () {
         rootEntity = new Entity("BoardDisplay Entity");
         rootNode = createSceneGraphNodes();
         RenderComponent rc =
             ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(rootNode);
         rootEntity.addComponent(RenderComponent.class, rc);

         // Center the board display in the cell
         rootNode.setLocalTranslation(new Vector3f(-getWidth()/2f, -getHeight()/2f, 0f));
     }

     /**
      * Create the nodes of the scene graph.
      */
     private Node createSceneGraphNodes () {
         rootNode = new Node("BoardDisplay Root Node");

         JMECollisionSystem colSys = 
             (JMECollisionSystem) ClientContextJME.getWorldManager().getCollisionManager().
             loadCollisionSystem(JMECollisionSystem.class);
         CollisionComponent cc = colSys.createCollisionComponent(rootNode);
         rootEntity.addComponent(CollisionComponent.class, cc);

         squareGraphs = new SquareNode[board.getNumRows()][];
         for (int r = 0; r < board.getNumRows(); r++) {
             SquareNode[] row = new SquareNode[board.getNumCols()];
             for (int c = 0; c < board.getNumCols(); c++) {
                 SquareNode sqNode = createSquareGraph(rootNode, colSys, cc, r, c);
                 row[c] = sqNode;
             }
             squareGraphs[r] = row;
         }

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
     private SquareNode createSquareGraph (Node parentNode, JMECollisionSystem colSys,
                                           CollisionComponent parentCC, int row, int col) {
         SquareNode sqNode = new SquareNode(parentNode, colSys, parentCC, row, col);
         SquareGeometry sqGeom = new SquareGeometry(sqNode, row, col);
         sqNode.attachChild(sqGeom);
         return sqNode;
     }
     
     /**
      * Control the visibility of the board display.
      */
     public void setVisible (boolean visible) {
         if (this.visible == visible) return;
         this.visible = visible;

         if (visible) {
             // Arrange for board display scene graph to be attached to cell.
             cellEntity.addEntity(rootEntity);
             RenderComponent rcCellEntity = (RenderComponent) cellEntity.getComponent(RenderComponent.class);
             RenderComponent rcRootEntity = (RenderComponent) rootEntity.getComponent(RenderComponent.class);
             if (rcCellEntity != null && rcCellEntity.getSceneRoot() != null && rcRootEntity != null) {
                 rcRootEntity.setAttachPoint(rcCellEntity.getSceneRoot());
             }
             eventListener.addToEntity(rootEntity);
         } else {
             // Arrange for board display scene graph to be detached from cell.
             eventListener.removeFromEntity(rootEntity);
             if (cellEntity != null) {
                 cellEntity.removeEntity(rootEntity);
                 RenderComponent rcRootEntity = (RenderComponent) rootEntity.getComponent(RenderComponent.class);
                 if (rcRootEntity != null) {
                     rcRootEntity.setAttachPoint(null);
                 }
             }
         }
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
     * A mouse event listener. This receives mouse input events from the button box.
     */
    private class MyMouseListener extends EventClassListener {

	/**
	 * This returns the classes of the Wonderland input events we are interested in receiving.
	 */
	public Class[] eventClassesToConsume () {
	    // Only respond to mouse button events
	    return new Class[] { MouseButtonEvent3D.class };
	}

	/**
	 * This will be called when a mouse event occurs over one of the components of the button box.
	 */
	public void commitEvent (final Event event) {

	    // Only respond to mouse button click events
	    MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D) event;
	    if (buttonEvent.isClicked() && 
		buttonEvent.getButton() == MouseButtonEvent3D.ButtonId.BUTTON1) {
                MouseEvent me = (MouseEvent) buttonEvent.getAwtEvent();
                if (me.getModifiersEx() == 0) {
                    SwingUtilities.invokeLater(new Runnable () {
                        public void run () {
                            ui.squareClicked(event);
                        }
                    });
                }
	    }
	}
    }

}
