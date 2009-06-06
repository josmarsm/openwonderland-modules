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

/**
 * SquareList: A list of squares.
 * @author dj
 */

public class SquareList {

    /** The list of squares. */
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
        list.add(sq);
    }

    /**
     * Append a list of squares to this list.
     */
    public void add (LinkedList<Square> sqs) {
        list.addAll(sqs);
    }

    /**
     * Flip the squares in this list in the board to the given color.
     */
    public void flip (currentColor) {
        for (Square sq : list) {
            sq.placePiece(color);
            sq.updateDisplay();
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
