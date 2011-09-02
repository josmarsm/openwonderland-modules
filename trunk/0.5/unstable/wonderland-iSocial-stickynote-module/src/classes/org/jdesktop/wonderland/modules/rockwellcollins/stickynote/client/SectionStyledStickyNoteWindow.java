
/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import com.jme.math.Vector2f;
import java.util.ResourceBundle;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client.cell.SectionStyledStickyNoteCell;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.SectionStyledStickyNoteCellClientState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.SectionStyledStickyNoteCellClientState;

/**
 *
 * The window for the sticky note.
 *
 * @author Ryan (mymegabyte)
 */
@ExperimentalAPI
public class SectionStyledStickyNoteWindow
        extends WindowSwing
        implements SectionStyledStickyNoteParentPanel.Container {

    /** The logger used by this class. */
    private static final Logger LOGGER = Logger.getLogger(StickyNoteWindow.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/rockwellcollins/stickynote/client/resources/Bundle");

    /** The cell in which this window is displayed. */
    private SectionStyledStickyNoteCell cell;
    /** Whether the window is currently displayed "on the glass". */
    private boolean ortho;
    private SectionStyledStickyNoteParentPanel stickynotePanel = null;

    /**
     * Create a new instance of StickyNoteWindow.
     *
     * @param cell The cell in which this window is displayed.
     * @param app The app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param topLevel Whether the window is top-level (e.g. is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     */
    public SectionStyledStickyNoteWindow(SectionStyledStickyNoteCell cell, App2D app, int width, int height, boolean topLevel,
            Vector2f pixelScale, SectionStyledStickyNoteCellClientState state)
            throws InstantiationException {
        super(app, width, height, topLevel, pixelScale);
        this.cell = cell;
        setDecorated(false);

        stickynotePanel = new SectionStyledStickyNoteParentPanel(cell, state);


        // Parent to Wonderland main window for proper focus handling
        JmeClientMain.getFrame().getCanvas3DPanel().add(stickynotePanel);

        stickynotePanel.setContainer(this);

        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        setComponent(stickynotePanel);
                    }
                });
        setTitle(BUNDLE.getString("Post-it_Note"));
        setUserResizable(false);


        /* Test Force a the preferred size
        System.err.println("test panel size = " + width + ", " + height);
        setSize(width, height);
         */
    }

    public void setOrtho(boolean ortho) {
        if (this.ortho == ortho) {
            return;
        }

        View2DEntity view = (View2DEntity) getView(cell);

        if (ortho) {

            // In this test, the view in the ortho plane is at a fixed location.
            view.setLocationOrtho(new Vector2f(300f, 300f), false);

            // Move the window view into the ortho plane
            view.setOrtho(true, false);

        } else {

            // Move the window view into the cell
            view.setOrtho(false, false);
        }

        // Now make it all happen
        view.update();

        this.ortho = ortho;
    }

    public SectionStyledStickyNoteParentPanel getStickynoteParentPanel() {
        return stickynotePanel;
    }
}
