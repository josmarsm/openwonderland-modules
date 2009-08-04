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

package org.jdesktop.wonderland.modules.presentationbase.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;

/**
 * Singleton that manages inter-presentation-module communication.
 *
 * For now, it's concerned only with managing tool-bar visibility. 
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class PresentationManager {

    private static final Logger logger = Logger.getLogger(PresentationManager.class.getName());

    private static PresentationManager manager = null;

    public static PresentationManager getManager() {
        if(manager==null)
            manager = new PresentationManager();

        return manager;
    }

    private List<JButton> toolbarButtons;
    private JPanel panel;
    private JToolBar toolbar;

    private HUD mainHUD;
    private HUDComponent toolbarHUD;

    private Set<MovingPlatformCell> platformCells = new HashSet<MovingPlatformCell>();

    private SlidesCell slidesCell;

    private PresentationManager() {

        // Do the startup stuff for a presentation manager.
        // For now, that consists of scanning for buttons to add to the toolbar.
        toolbarButtons = new Vector<JButton>();
        panel = new JPanel();
        toolbar = new JToolBar();
        
        panel.add(toolbar);
    }

    private void setToolbarVisibility(boolean visibility) {

        logger.warning("setting toolbar visibility: " + visibility);
        if(mainHUD==null) {
            logger.warning("Doing initial toolbar visibility setup work.");
            mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

            toolbarHUD = mainHUD.createComponent(panel);
            toolbarHUD.setPreferredLocation(Layout.NORTHWEST);
            mainHUD.addComponent(toolbarHUD);            
        }

        if(visibility) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    toolbarHUD.setName("Presentation Tools");
                    toolbarHUD.setVisible(true);
                }

            });
            logger.warning("Set toolbar visible!");
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    toolbarHUD.setVisible(false);
                }

            });
            logger.warning("hiding toolbarHUD");
        }
    }

    public void addToolbarButton(final JButton b) {
        logger.warning("adding button: " + b);

        if(!toolbarButtons.contains(b)) {
            toolbarButtons.add(b);
         SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    toolbar.add(b);
                }

            });

            if(toolbarButtons.size()==1)
                setToolbarVisibility(true);
        }
    }

    public void removeToolbarButton(final JButton b) {
        logger.warning("removing button: " + b);

        if(toolbarButtons.contains(b)) {
        toolbarButtons.remove(b);

         SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    toolbar.remove(b);
                }

            });

        logger.warning("done removing, about to check and see if we should hide toolbar");

        if(toolbarButtons.size()==0)
            setToolbarVisibility(false);
        }
    }

    public void addPlatform(MovingPlatformCell cell) {
        logger.warning("Presentation manager is now aware of a new platform!");
        this.platformCells.add(cell);
    }

    public void removePlatform(MovingPlatformCell cell) {
        logger.warning("Presentation mananger is sad to see a cell leave.");
        this.platformCells.remove(cell);
    }

    public void createPresentationSpace(SlidesCell slidesCell) {
        if(this.slidesCell==null)
            this.slidesCell = slidesCell;

        // Do a bunch of exciting things now to do this setup, including
        // getting layout information from the slidesCell.

        logger.warning("Setting up a presentation space for slidesCell: " + slidesCell);

        // Overall steps:
        //
        // 1. Put a toolbar up for everyone that gives them next/previous controls.
        //     (eventually this should be just for the username that created
        //      the file, but it's not clear to me how to do that since this
        //      object contains only local state and isn't synced at all.)


        // 2. Create a presentation platform in front of the first slide, sized
        //    so it is as wide as the slide + the inter-slide space.
        //

        // 3. Tell the PDF spreader to grow itself to contain the whole space
        //    of the presentation.

        // 4. Attach a thought bubbles component to the parent cell.

        // 5. Add buttons to the main presentation toolbar for setting camera
        //    positions (back / top)

    }
}
