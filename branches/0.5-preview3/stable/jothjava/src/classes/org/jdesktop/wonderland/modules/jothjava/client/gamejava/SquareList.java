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

package org.jdesktop.wonderland.modules.jothjava.client.gamejava;

import java.util.LinkedList;

/********************************
 * SquareList: A list of squares.
 * @author deronj@dev.java.net
 */

public class SquareList {

    private LinkedList<Square> list = new LinkedList<Square>();

    /**
     * Returns the length of the list
     */
    public int length () {
        return list.size();
    }

    /**
     * Add a square to this list.
     */
    public void add (Square sq) {
        list.addLast(sq);
    }

    /**
     * Add a list of squares to this list.
     */
    public void add (LinkedList<Square> addendList) {
        for (Square sq : addendList) {
            list.add(sq);            
        }					  
    }

    /**
     * Flip the squares in this list in the board to the given color.
     */
    public void flip (Board.Color color) {
        for (Square sq : list) {
            sq.placePiece(color);
        }
    }

    /**
     * Return the string representation of this SquareList.
     */
    public String toString () {
        String str = "[";
        int i = 0;
        for (Square sq : list) {
            if (i > 0) str += ", ";
            str += sq;
            i++;
        }
        str += "]";
        return str;
    }
}
