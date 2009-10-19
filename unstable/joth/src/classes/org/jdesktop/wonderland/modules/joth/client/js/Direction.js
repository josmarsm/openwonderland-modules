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

/***************************************************************
 * Direction: Defines a direction for stepping within the board.
 * @author deronj@dev.java.net
 */

/** 
 * Construct a new instance of a Direction.
 * @param deltaRow The movement along the row (-1 is to the left, 0 is no movement, +1 is to the right).
 * @param deltaCol The movement along the column (-1 is above, 0 is no movement, +1 is below).
 */
function Direction (deltaRow, deltaCol) {
    this.deltaRow = deltaRow;
    this.deltaCol = deltaCol;
}

/**
 * Returns the deltaRow of the direction.
 */
Direction.prototype.getDeltaRow = function () {
    return this.deltaRow;
}

/**
 * Returns the deltaCol of the direction.
 */
Direction.prototype.getDeltaCol = function () {
    return this.deltaCol;
}

Direction.prototype.toString = function () {
    return "[" + this.deltaRow + "," + this.deltaCol + "]";
}

// Orthogonal and diagonal directions
Direction.NORTH = new Direction(-1, 0);
Direction.NORTHEAST = new Direction(-1, 1);
Direction.EAST = new Direction(0, 1);
Direction.SOUTHEAST = new Direction(1, 1);
Direction.SOUTH = new Direction(1, 0);
Direction.SOUTHWEST = new Direction(1, -1);
Direction.WEST = new Direction(0, -1);
Direction.NORTHWEST = new Direction(-1, -1);

// All possible directions
Direction.ALL_DIRECTIONS = [ Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, 
                             Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST ];
