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

/*****************************************************
 * The game of Othello displayed in HTML in a browser.
 * @author deronj@dev.java.net
 */

// Create the game. 
var game = new Game();

// Display the game in a browser using HTML. 
game.setUI(new UIDom(game));

function onclickHandler (event) {
    var square = game.getUI().eventToSquare(event);
    game.performTurn(square);
}

