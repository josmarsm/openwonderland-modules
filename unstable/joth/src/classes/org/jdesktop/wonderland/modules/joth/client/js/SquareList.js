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

/********************************
 * SquareList: A list of squares.
 * @author deronj@dev.java.net
 */

function SquareList () {
    this.ary = [];
}

/**
 * Returns the length of the list
 */
SquareList.prototype.length = function () {
    return this.ary.length;
}

/**
 * Add a square or an array of squares to this list.
 */
SquareList.prototype.add = function (addend) {
    if (addend instanceof Array) {
        // TODO: why doesn't this work?
	//this.ary.concat(addend);
        for (var i=0; i < addend.length; i++) {
            var sq = addend[i];
            this.add(sq);					  
        }					  
    } else {
        this.ary.push(addend);            
    }
}		    

/**
 * Flip the squares in this list in the board to the given color.
 */
SquareList.prototype.flip = function (color) {
    for (var i=0; i<this.ary.length; i++) {
        var sq = this.ary[i];
	sq.placePiece(color);
    }
}

/**
 * Return the string representation of this SquareList.
 */
SquareList.prototype.toString = function () {
    var str = "[";
    for (var i=0; i < this.ary.length; i++) {
        if (i > 0) str += ", ";
	str += this.ary[i];
    }
    str += "]";
    return str;
}
