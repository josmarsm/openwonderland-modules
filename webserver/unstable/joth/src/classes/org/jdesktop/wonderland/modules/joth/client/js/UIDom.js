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

/******************************************************
 * UIDom: The HTML-specific UI for the game of Othello.
 * @author deronj@dev.java.net
 */

/**
 * Constructor for the UI which uses the DOM to display game results.
 * @param game The game which is being displayed by this UI.
 */
function UIDom (game) {
    this.game = game;
    this.errorMessageElement = document.getElementById("errorMsg");
    this.whiteCountElement = document.getElementById("whiteCount")
    this.blackCountElement = document.getElementById("blackCount")
}

/**
 * Set the error message to blank.
 */
UIDom.prototype.clearError = function () {
    this.errorMessageElement.innerHTML = "";
}

/**
 * Display the given message.
 */
UIDom.prototype.error = function (message) {
    this.errorMessageElement.innerHTML = "Error: " + message;
}

/**
 * Display the current color counts.
 */
UIDom.prototype.displayCounts = function () {
    this.whiteCountElement.innerHTML = this.game.getBoard().getWhiteCount();
    this.blackCountElement.innerHTML = this.game.getBoard().getBlackCount();
}

// The images which display the colored pieces
UIDom.WHITE_COLOR_IMAGE = "images/white.gif";
UIDom.BLACK_COLOR_IMAGE = "images/black.gif";

/**
 * Update the display of a particular square with its
 * current contents in the board.
 */
UIDom.prototype.updateSquare = function (square) {
    var sqContents = square.contents(square);
    var imageName;
    if (sqContents == Board.Color.WHITE) {
        imageName = UIDom.WHITE_COLOR_IMAGE;
    } else if (sqContents == Board.Color.BLACK) {
        imageName = UIDom.BLACK_COLOR_IMAGE;
    } else {
        return;
    }

    var imageElement = UIDom.getImageElementOfSquare(square);
    imageElement.src = imageName;
}

/** Display whose turn it is. */
UIDom.prototype.updateTurn = function () {
    document.getElementById("whoseTurn").innerHTML = this.game.getCurrentTurn();
}

/** The game is over. Notify the user of the result. */
UIDom.prototype.notifyGameOver = function (msg) {
    alert(msg);
}

/** Given a click event, determine which square it is in. */
UIDom.prototype.eventToSquare = function (event) {
    var targ = UIDom.whichElement(event);
    return UIDom.whichSquare(targ.id);
}

// Private class method: Returns the document image element of the given square. */
UIDom.getImageElementOfSquare = function (sq) {
    var boardElement = document.getElementById("board");
    var row = boardElement.rows[sq.getRow()];
    var cell = row.cells[sq.getCol()];
    return cell.firstChild;
}

// Private class method: Returns the target of a document element.
// (From Flanagan's Rhino book).
UIDom.whichElement = function (event) {
    var targ;
    if (!event) {
        var event=window.event;
    }
    if (event.target) {
        targ=event.target;
    } else if (event.srcElement) {
        targ=event.srcElement;
    }
    if (targ.nodeType==3) { // defeat Safari bug
        targ = targ.parentNode;
    }
    return targ;
}

// Private class method: Returns a new Square with the given id.
UIDom.whichSquare = function (id) {
    var coords = id.split("_");
    return new Square(game.getBoard(), Number(coords[0]), Number(coords[1]));    
}

// Private class variable. The debug console window.
UIDom._console = null;

// Private class method: Print a message to the debug console.
// (Derived from Flanagan's Rhino book).
UIDom.debug = function (msg) {
    // Open a new window the first time, or after an existing console window has been closed
    if ((UIDom._console == null) || (UIDom._console.closed)) {
	UIDom._console = window.open("", "console", "width=600,height=300,resizable");
	UIDom._console.document.open("text/plain");
    }
    UIDom._console.document.writeln(msg);
}
