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
package org.jdesktop.wonderland.modules.cmu.client;

import java.util.ArrayList;
import java.util.List;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.modules.cmu.client.events.wonderland.CMUContextListener;
import org.jdesktop.wonderland.modules.cmu.client.ui.events.EventEditor;
import org.jdesktop.wonderland.modules.cmu.common.events.ContextMenuEvent;
import org.jdesktop.wonderland.modules.cmu.common.events.EventResponsePair;
import org.jdesktop.wonderland.modules.cmu.common.events.responses.CMUResponseFunction;

/**
 * Factory for a CMU cell's context menu.
 * @author kevin
 */
public class CMUContextFactory implements ContextMenuFactorySPI {

    private final CMUCell parent;

    /**
     * Standard constructor; needs parent for e.g. HUD manager access.
     * @param parent The cell to which this context factory belongs
     */
    public CMUContextFactory(CMUCell parent) {
        this.parent = parent;
    }

    public CMUCell getParent() {
        return parent;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
        List<ContextMenuItem> items = new ArrayList<ContextMenuItem>();
        items.add(new HUDToggleMenuItem());
        items.add(new EditEventsMenuItem());

        // Add context events from parent cell
        for (EventResponsePair pair : getParent().getEventList()) {
            if (pair.getEvent() instanceof ContextMenuEvent) {
                items.add(new ContextEventMenuItem((ContextMenuEvent) pair.getEvent(),
                        pair.getResponse()));
            }
        }

        return items.toArray(new ContextMenuItem[] { });
    }

    protected class ContextEventMenuItem extends SimpleContextMenuItem {

        public ContextEventMenuItem(ContextMenuEvent event, CMUResponseFunction response) {
            super(event.getMenuText(), new CMUContextListener(getParent(), response));
        }
    }

    /**
     * Menu item which can toggle the visibility of the HUD for this cell.
     */
    protected class HUDToggleMenuItem extends SimpleContextMenuItem {

        public HUDToggleMenuItem() {
            super("", new HUDActionListener());
            this.setLabel(getAppropriateLabel());
        }

        public String getAppropriateLabel() {
            if (getParent().getHudControl().isHUDShowing()) {
                return "Hide controls";
            } else {
                return "Show controls";
            }
        }
    }

    protected class HUDActionListener implements ContextMenuActionListener {

        @Override
        public void actionPerformed(ContextMenuItemEvent event) {
            boolean desiredShowingState = !(getParent().getHudControl().isHUDShowing());
            getParent().getHudControl().setHUDShowing(desiredShowingState);
        }
    }

    protected class EditEventsMenuItem extends SimpleContextMenuItem {

        public EditEventsMenuItem() {
            super("Edit events", new EditEventsActionListener());
        }
    }

    protected class EditEventsActionListener implements ContextMenuActionListener {

        @Override
        public void actionPerformed(ContextMenuItemEvent event) {
            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    new EventEditor(parent).setVisible(true);
                }
            });
        }
    }
}
