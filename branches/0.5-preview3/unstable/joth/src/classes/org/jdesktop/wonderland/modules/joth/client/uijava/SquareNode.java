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
import org.jdesktop.wonderland.modules.joth.client.utiljava.JothUtilJme;
import org.jdesktop.wonderland.modules.joth.client.gamejava.Board;

/***************************************************************************
 * SquareNode: A JME node for a square which contains additional information 
 * about the squares location in the board.
 * @author deronj@dev.java.net
 */
public class SquareNode extends Node {

    private static final float Z_OFFSET = 0.05f;

    private JothUtilJme util;
    private int row;
    private int col;
    private Piece displayedPiece;

    public SquareNode (JothUtilJme util, Node parentNode, int row, int col) {
        super("SquareNode for " + row + ", " + col);
        this.util = util;
        this.row = row;
        this.col = col;
        
        // Init transform
        float x = SquareGeometry.WIDTH * ((float)row + 0.5f);
        float y = SquareGeometry.HEIGHT * ((float)col + 0.5f);
        util.setLocalTranslation(this, new Vector3f(x, y, Z_OFFSET));

        util.setPickable(this, true);
        util.attachChild(parentNode, this);
    }

    public int getRow () {
        return row;
    }

    public int getCol () {
        return col;
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

        // Take down previous piece (if necessary)
        if (displayedPiece != null && displayedPiece.getColor() != Board.Color.EMPTY) {
            util.detachChild(this, displayedPiece);
            displayedPiece = null;
        }

        // Put up the new piece (if necessary)
        if (color != Board.Color.EMPTY) {
            displayedPiece = new Piece(color, util);
            util.attachChild(this, displayedPiece);
        }
    }
}
