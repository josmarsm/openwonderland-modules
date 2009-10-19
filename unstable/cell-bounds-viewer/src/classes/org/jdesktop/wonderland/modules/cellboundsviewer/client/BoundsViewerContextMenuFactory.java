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
package org.jdesktop.wonderland.modules.cellboundsviewer.client;

import java.util.ResourceBundle;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.annotation.ContextMenuFactory;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.modules.cellboundsviewer.client.cell.BoundsViewerCellComponent;

/**
 * Context menu factory that generates a View/Hide Cell bounds context menu
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ContextMenuFactory
public class BoundsViewerContextMenuFactory implements ContextMenuFactorySPI {

    // The I18N resource bundle
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/cellboundsviewer/client/" +
            "resources/Bundle");

    // The error logger
    private static final Logger LOGGER =
            Logger.getLogger(BoundsViewerContextMenuFactory.class.getName());
    /**
     * {@inheritDoc}
     */
    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
        // Return a new context menu item based upon whether there is a
        // BoundsViewerCellComponent on the Cell. Its presence indicates that
        // the bounds are displayed.
        Cell cell = event.getPrimaryCell();
        if (cell == null) {
            LOGGER.warning("No Cell found for context event, returning.");
            return new ContextMenuItem[] {};
        }

        // Depending upon whether the component is present, use a different
        // name.
        String menuName = null;
        if (cell.getComponent(BoundsViewerCellComponent.class) == null) {
            menuName = BUNDLE.getString("Show_Bounds");
        }
        else {
            menuName = BUNDLE.getString("Hide_Bounds");
        }

        // Create and return a new simple context menu item
        BoundsViewerMenuListener l = new BoundsViewerMenuListener();
        SimpleContextMenuItem item = new SimpleContextMenuItem(menuName, l);
        return new ContextMenuItem[] { item };
    }

    /**
     * Inner class that receives an event when the context menu item has been
     * selected to Show/Hide the Cell bounds
     */
    class BoundsViewerMenuListener implements ContextMenuActionListener {
        /**
         * {@inheritDoc}
         */
        public void actionPerformed(ContextMenuItemEvent event) {
            // Depending upon whether the Cell has a BoundsViewerCellComponent,
            // we either add it ir remove it.
            Cell cell = event.getCell();
            if (cell == null) {
                LOGGER.warning("No Cell found for context event, returning.");
                return;
            }

            if (cell.getComponent(BoundsViewerCellComponent.class) == null) {
                cell.addComponent(new BoundsViewerCellComponent(cell));
            }
            else {
                cell.removeComponent(BoundsViewerCellComponent.class);
            }
        }
    }
}
