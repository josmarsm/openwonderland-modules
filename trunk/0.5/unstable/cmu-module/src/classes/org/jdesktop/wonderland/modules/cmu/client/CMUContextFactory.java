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
package org.jdesktop.wonderland.modules.cmu.client;

import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.modules.cmu.client.ui.events.EventEditor;

/**
 * Factory for a CMU cell's context menu.
 * @author kevin
 */
public class CMUContextFactory implements ContextMenuFactorySPI {

    private final CMUCell parent;
    private EventEditor eventEditor = null;

    public CMUContextFactory(CMUCell parent) {
        this.parent = parent;
    }

    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
        return new ContextMenuItem[]{
                    new HUDToggleMenuItem(),
                    new EditEventsMenuItem()};
    }

    private class HUDToggleMenuItem extends SimpleContextMenuItem {

        public HUDToggleMenuItem() {
            super("Show controls", new HUDActionListener());
            ((HUDActionListener) this.getActionListener()).setParent(this);
        }

        public String getAppropriateLabel() {
            if (parent.getHudControl().isHUDShowing()) {
                return "Show controls";
            } else {
                return "Hide controls";
            }
        }
    }

    private class HUDActionListener implements ContextMenuActionListener {

        private HUDToggleMenuItem parentItem = null;

        public void setParent(HUDToggleMenuItem parentItem) {
            this.parentItem = parentItem;
        }

        @Override
        public void actionPerformed(ContextMenuItemEvent event) {
            boolean desiredShowingState = !(parent.getHudControl().isHUDShowing());
            parent.getHudControl().setHUDShowing(desiredShowingState);
            if (this.parentItem != null) {
                parentItem.setLabel(parentItem.getAppropriateLabel());
            }
        }
    }

    private class EditEventsMenuItem extends SimpleContextMenuItem {

        public EditEventsMenuItem() {
            super("Edit events", new EditEventsActionListener());
        }
    }

    private class EditEventsActionListener implements ContextMenuActionListener {

        @Override
        public void actionPerformed(ContextMenuItemEvent event) {
            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    //TODO: make a listener interface for changes to available responses, so we don't have to defer creation like this
                    if (eventEditor == null) {
                        eventEditor = new EventEditor(parent);
                    }
                    eventEditor.setVisible(true);
                }
            });
        }
    }
}
