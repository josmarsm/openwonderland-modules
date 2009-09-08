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

package org.jdesktop.wonderland.modules.pdfspreader.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.annotation.ContextMenuFactory;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */

@ContextMenuFactory
public class PDFSpreaderClientPlugin implements ContextMenuFactorySPI {


    private static final Logger logger =
        Logger.getLogger(PDFSpreaderClientPlugin.class.getName());

    private static PDFLayoutHUDPanel layoutPanel = null;
    private static HUDComponent layoutHUD = null;



    private void createHUD(PDFSpreaderCell cell) {
        logger.warning("Creating HUD for cell: " + cell);
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

        layoutPanel = new PDFLayoutHUDPanel(cell);

        layoutHUD = mainHUD.createComponent(layoutPanel);
        layoutHUD.setPreferredLocation(Layout.SOUTH);
        layoutHUD.setName("PDF Layout Attributes");
        layoutHUD.addEventListener(new HUDEventListener() {
            public void HUDObjectChanged(HUDEvent event) {
                if (event.getEventType() == HUDEventType.DISAPPEARED) {
                    // Tell all of the affordances to remove themselves by posting
                    // an event to the input system as such. Also tell the
                    // affordance panel it has closed
                    layoutPanel.closed();
                }
            }
        });

        // add affordances HUD panel to main HUD
        mainHUD.addComponent(layoutHUD);

    }

    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
        if(event.getPrimaryCell() instanceof PDFSpreaderCell) {
            final SimpleContextMenuItem editLayoutItem = new SimpleContextMenuItem("Edit Slide Layout...",(ContextMenuActionListener) new EditLayoutContextListener());
            return new ContextMenuItem[] {editLayoutItem};
        }
        else
            return new ContextMenuItem[] {};
    }


    private class EditLayoutContextListener implements ContextMenuActionListener {

        public void actionPerformed(ContextMenuItemEvent event) {
            // If our panel exists alreay, reshow it. Otherwise,
            // make a new one first.

            // This should always be true, but just making sure.
            assert(event.getCell() instanceof PDFSpreaderCell);

            if(layoutHUD==null) {
                createHUD((PDFSpreaderCell)event.getCell());
            } else {
                // update the cell on the current HUD object.
                layoutPanel.setCell((PDFSpreaderCell) event.getCell());
            }


            layoutHUD.setVisible(true);

            logger.info("PDFSpreaderLayoutHUD now visible.");
        }    
    }
}
