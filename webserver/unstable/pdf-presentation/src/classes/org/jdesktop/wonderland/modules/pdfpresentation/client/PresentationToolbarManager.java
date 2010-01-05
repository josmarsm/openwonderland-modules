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

package org.jdesktop.wonderland.modules.pdfpresentation.client;

import org.jdesktop.wonderland.modules.pdfpresentation.server.SlidesCell;
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
public class PresentationToolbarManager {

    private static final Logger logger = Logger.getLogger(PresentationToolbarManager.class.getName());

    private static PresentationToolbarManager manager = null;

    public static PresentationToolbarManager getManager() {
        if(manager==null)
            manager = new PresentationToolbarManager();

        return manager;
    }

    private List<JButton> toolbarButtons;
    private JPanel panel;
    private JToolBar toolbar;

    private HUD mainHUD;
    private HUDComponent toolbarHUD;

    private SlidesCell slidesCell;

    private PresentationToolbarManager() {

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

//    public void addPlatform(MovingPlatformCell cell) {
//        logger.warning("Presentation manager is now aware of a new platform!");
//        this.platformCells.add(cell);
//    }
//
//    public void removePlatform(MovingPlatformCell cell) {
//        logger.warning("Presentation mananger is sad to see a cell leave.");
//        this.platformCells.remove(cell);
//    }

}
