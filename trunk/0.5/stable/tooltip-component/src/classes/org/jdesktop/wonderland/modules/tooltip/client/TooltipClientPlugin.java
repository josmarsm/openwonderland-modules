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
package org.jdesktop.wonderland.modules.tooltip.client;

import java.awt.Canvas;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.scenemanager.SceneManager;
import org.jdesktop.wonderland.client.scenemanager.event.HoverEvent;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * A client-side plugin to listen for hover events from the Scene Manager and
 * display a tooltip in the HUD, if the Tooltip Cell Component is present.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class TooltipClientPlugin extends BaseClientPlugin {

    // The listener for the Scene Manager hover event
    private TooltipHoverListener listener = null;

    // A HUD Component that holds a tooltip label and the JPanel that holds the
    // label.
    private HUDComponent hudComponent = null;
    private TooltipJPanel tooltipPanel = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ServerSessionManager sessionManager) {
        // Create the listener for the Scene Manager hover events; to be added
        // in activate().
        listener = new TooltipHoverListener();

        super.initialize(sessionManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void activate() {

        // Add the tooltip hover listener to the Scene Manager
        SceneManager.getSceneManager().addSceneListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deactivate() {
        
        // Remove the tooltip hover listener from the Scene Manager
        SceneManager.getSceneManager().removeSceneListener(listener);

        // Remove the component from the HUD.
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        mainHUD.removeComponent(hudComponent);
        hudComponent = null;
        tooltipPanel = null;
    }

    /**
     * Listner for the Scene Manager hover event
     */
    private class TooltipHoverListener extends EventClassListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { HoverEvent.class };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void commitEvent(Event event) {
            // Upon a Hover event, see if it is a hover start or stop. If start,
            // then see if the Cell has a Tooltip Cell Component and if so fetch
            // the text, and display the HUD Component in the position of the
            // mouse event
            HoverEvent hoverEvent = (HoverEvent)event;
            Cell cell = hoverEvent.getPrimaryCell();

            // If there is no Cell or if the hover event has ended, then just
            // hide the HUD Component.
            if (cell == null || hoverEvent.isStart() == false) {
                if (hudComponent != null) {
                    hideTooltipHUDComponent();
                }
                return;
            }

            // Fetch the Tooltip Cell Component. If there is none, then hide
            // the component for good measure.
            TooltipCellComponent comp =
                    cell.getComponent(TooltipCellComponent.class);
            if (comp == null) {
                if (hudComponent != null) {
                    hideTooltipHUDComponent();
                }
                return;
            }

            // Otherwise, show the hud at the current mouse position with the
            // given text. We need to adjust for the fact that the position
            // returned in the mouse event has y = 0 at the top, where for the
            // HUD, y = 0 is at the bottom.
            Canvas canvas = JmeClientMain.getFrame().getCanvas();
            MouseEvent mouseEvent = hoverEvent.getMouseEvent();
            Point location = mouseEvent.getPoint();
            location.y = canvas.getHeight() - location.y;
            String text = comp.getText();
            showTooltipHUDComponent(comp, text, location);

        }

        /**
         * Hides the Tooltip HUDComponent. This method performs the action on
         * the AWT Event Thread
         */
        private void hideTooltipHUDComponent() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    HUD hud = HUDManagerFactory.getHUDManager().getHUD("main");
                    hudComponent.setVisible(false);
                    hud.removeComponent(hudComponent);
                    hudComponent = null;
                }
            });
        }

        /**
         * Shows the Tooltip HUDComponent with the given text message at the
         * designated location. This method performs the action on the AWT
         * Event Thread.
         */
        private void showTooltipHUDComponent(
                final TooltipCellComponent component, final String text,
                final Point point) {
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // If there is an existing tooltip hud component, then set
                    // it to invisible, remove it from the hud.
                    HUD hud = HUDManagerFactory.getHUDManager().getHUD("main");
                    if (hudComponent != null) {
                        hudComponent.setVisible(false);
                        hud.removeComponent(hudComponent);
                        hudComponent = null;
                    }

                    // Create the hud component with the panel to display the
                    // text
                    tooltipPanel = new TooltipJPanel();
                    tooltipPanel.setText((text != null) ? text : "");
                    hudComponent = hud.createComponent(tooltipPanel);
                    hudComponent.setName("Tooltip");
                    hudComponent.setDecoratable(false);
                    hudComponent.setVisible(false);
                    hudComponent.setLocation(point);
                    hud.addComponent(hudComponent);

                    // We set it visible. If we wish to add a timeout, then
                    // we immediately set it to invisible after the given
                    // delay.
                    hudComponent.setVisible(true);
                    int timeout = component.getTimeout();
                    if (timeout != -1) {
                        hudComponent.setVisible(false, timeout);
                    }
                }
            });
        }
    }
}
