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

import org.jdesktop.wonderland.client.input.Event;

/*********************************************************
 * UI: Specifies the interface between the Othello game
 * and it's user interface.
 *
 * @author deronj@dev.java.net
 */

public interface UI {

    /**
     * Set the error message to blank.
     */
    public void clearError ();

    /**
     * Display the given message.
     */
    public void error (String message);

    /**
     * Display the current color counts.
     */
    public void displayCounts ();

    /**
     * Update the display of a particular square with its
     * current contents in the board.
     */
    public void updateSquare (Square square);

    /** Display whose turn it is. */
    public void updateTurn ();

    /** The game is over. Notify the user of the result. */
    public void notifyGameOver (String msg);

    /** Given a click event, determine which square it is in. */
    public Square eventToSquare (Event event);

    /** Control the visibility of the UI display. */
    public void setVisible (boolean visible);
}
