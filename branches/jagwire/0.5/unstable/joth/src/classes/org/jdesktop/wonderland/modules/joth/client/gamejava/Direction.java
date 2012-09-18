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

package org.jdesktop.wonderland.modules.joth.client.gamejava;

/***************************************************************
 * Direction: Defines a direction for stepping within the board.
 * @author deronj@dev.java.net
 */
public class Direction {

    // Orthogonal and diagonal directions
    public static final Direction NORTH = new Direction(-1, 0);
    public static final Direction NORTHEAST = new Direction(-1, 1);
    public static final Direction EAST = new Direction(0, 1);
    public static final Direction SOUTHEAST = new Direction(1, 1);
    public static final Direction SOUTH = new Direction(1, 0);
    public static final Direction SOUTHWEST = new Direction(1, -1);
    public static final Direction WEST = new Direction(0, -1);
    public static final Direction NORTHWEST = new Direction(-1, -1);

    // All possible directions
    public static final Direction[] ALL_DIRECTIONS = new Direction[] {
        NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST };

    /** The movement along the row (-1 is to the left, 0 is no movement, +1 is to the right). */
    private int deltaRow;

    /** The movement along the column (-1 is above, 0 is no movement, +1 is below). */
    private int deltaCol;

    /** 
     * Construct a new instance of a Direction.
     * @param deltaRow The movement along the row (-1 is to the left, 0 is no movement, +1 is to the right).
     * @param deltaCol The movement along the column (-1 is above, 0 is no movement, +1 is below).
     */
    public Direction (int deltaRow, int deltaCol) {
        this.deltaRow = deltaRow;
        this.deltaCol = deltaCol;
    }

    /**
     * Returns the deltaRow of the direction.
     */
    public int getDeltaRow () {
        return deltaRow;
    }

    /**
     * Returns the deltaCol of the direction.
     */
    public int getDeltaCol () {
        return this.deltaCol;
    }

    @Override
    public String toString () {
        return "[" + deltaRow + "," + deltaCol + "]";
    }
}