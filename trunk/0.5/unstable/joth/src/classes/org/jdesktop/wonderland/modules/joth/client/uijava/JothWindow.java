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

import com.jme.math.Vector2f;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import org.jdesktop.wonderland.modules.joth.client.cell.JothCell;

/*********************************************
 * JothWindow: The control window for Othello.
 * @author deronj@dev.java.net
 */

public class JothWindow 
    extends WindowSwing 
    implements JothControlPanel.ControlPanelContainer 
{
    /* The cell. */
    private JothCell cell;

    /** The control panel. */
    private JothControlPanel controlPanel;

    public JothWindow (JothCell cell, App2D app, int width, int height, boolean decorated,
                       Vector2f pixelScale) {
        super(app, Window2D.Type.PRIMARY, width, height, decorated, pixelScale);
        this.cell = cell;

        try {
            SwingUtilities.invokeAndWait(new Runnable () {
                public void run () {
                    controlPanel = new JothControlPanel();
                    controlPanel.setContainer(JothWindow.this);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot create control panel");
        }

	// Parent to Wonderland main window for proper focus handling 
       	JmeClientMain.getFrame().getCanvas3DPanel().add(controlPanel);

    	setComponent(controlPanel);
        setTitle("Othello Controls");
    }

    /**
     * Set the error message to blank.
     */
    public void clearError () {
        SwingUtilities.invokeLater(new Runnable () {
            public void run () {
                controlPanel.clearError();
            }
        });
    }

    /**
     * Display the given message.
     */
    public void error (final String message) {
        SwingUtilities.invokeLater(new Runnable () {
            public void run () {
                controlPanel.error(message);
            }
        });
    }

    /**
     * Display the current color counts.
     */
    public void displayCounts (final int whiteCount, final int blackCount) {
        SwingUtilities.invokeLater(new Runnable () {
            public void run () {
                controlPanel.displayCounts(whiteCount, blackCount);
            }
        });
    }

    /** Display whose turn it is. */
    public void setTurn (final String whoseTurn) {
        SwingUtilities.invokeLater(new Runnable () {
            public void run () {
                controlPanel.setTurn(whoseTurn);
            }
        });
    }

    /** The game is over. Notify the user of the result. */
    public void notifyGameOver (final String msg) {
        SwingUtilities.invokeLater(new Runnable () {
            public void run () {
                controlPanel.notifyGameOver(msg);
            }
        });
    }

    /** Control the visibility of the window. */
    public void setVisible (boolean visible) {
        if (visible) {
            setVisibleApp(true);
            setVisibleUser(cell, true);
        } else {
            setVisibleApp(false);
        }
    }

    /**
     * Called by the user to start a new game.
     */
    public void newGame() {
        // TODO: not yet implemented
    }
}
