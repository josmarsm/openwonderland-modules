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

package org.jdesktop.wonderland.modules.joth.client;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.jme.ClientContextJME;

/***************************************************************************
 * SquareNode: A JME node for a square which contains additional information 
 * about the squares location in the board.
 * @author deronj@dev.java.net
 */
public class SquareNode extends Node {

    private static final float Z_OFFSET = 0.05f;

    private int row;
    private int col;
    private Piece displayedPiece;

    public SquareNode (final Node parentNode, JMECollisionSystem colSys, CollisionComponent parentCC,
                       int row, int col) {
        super("SquareNode for " + row + ", " + col);
        this.row = row;
        this.col = col;
        
        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                initTransform();
                parentNode.attachChild(SquareNode.this);
                ClientContextJME.getWorldManager().addToUpdateList(SquareNode.this);
            }
        } , null);

        colSys.addReportingNode(this, parentCC);
    }

    public int getRow () {
        return row;
    }

    public int getCol () {
        return col;
    }

    private void initTransform() {
        float x = SquareGeometry.WIDTH * ((float)row + 0.5f);
        float y = SquareGeometry.HEIGHT * ((float)col + 0.5f);
        setLocalTranslation(new Vector3f(x, y, Z_OFFSET));
    }

    /**
     * If color is EMPTY, removes any piece displayed in the square.
     * If color is not empty, displays a piece of the given color
     * in the square (that is, slightly above the board centered in 
     * that square.
     */
    public void displayColor (final Board.Color color) {

        // See whether any change is necessary
        if (displayedPiece == null) {
            if (color == Board.Color.EMPTY) {
                return;
            }
        } else {
            if (displayedPiece.getColor() == color) {
                return;
            }
        }
        
        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {

                // Take down previous piece (if necessary)
                if (displayedPiece != null && displayedPiece.getColor() != Board.Color.EMPTY) {
                    detachChild(displayedPiece);
                    displayedPiece = null;
                }

                if (color != Board.Color.EMPTY) {
                    displayedPiece = new Piece(color);
                    attachChild(displayedPiece);
                } 
                ClientContextJME.getWorldManager().addToUpdateList(SquareNode.this);
            }
        } , null);
    }
}
