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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.ViewManager.ViewManagerListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Client-side plugin for the presentation system. Registers a component on
 * the primary view cell (i.e. avatar) so that it may be moved when the
 * platform moves.
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class PresentationBaseClientPlugin extends BaseClientPlugin
        implements ViewManagerListener {

    // The error logger
    private static final Logger LOGGER =
            Logger.getLogger(PresentationBaseClientPlugin.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ServerSessionManager loginInfo) {
        // Listen for changes in the primary view cell, to add and remove the
        // moving platform component for it.
        ViewManager.getViewManager().addViewManagerListener(this);

        super.initialize(loginInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        // Stop listening for changes in the primary view cell
        ViewManager.getViewManager().removeViewManagerListener(this);

        super.cleanup();
    }

    /**
     * {@inheritDoc}
     */
    public void primaryViewCellChanged(ViewCell oldViewCell, ViewCell newViewCell) {

        // If there is an old view cell, then remove the component from the
        // avatar. If not present, this should fail silently.
        if (oldViewCell != null) {
            oldViewCell.removeComponent(MovingPlatformAvatarComponent.class);
        }

        // If there is a new view cell, then add the component. If already
        // present (none should be), then log an error message and continue
        if (newViewCell != null) {
            try {
                newViewCell.addComponent(new MovingPlatformAvatarComponent(newViewCell));
            } catch (IllegalArgumentException excp) {
                LOGGER.log(Level.WARNING, "Adding a duplicate moving platform" +
                        " avatar component", excp);
            }
        }
    }
}
