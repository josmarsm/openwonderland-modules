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

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.>
 */

@Plugin
public class PresentationBaseClientPlugin extends BaseClientPlugin implements ViewManager.ViewManagerListener{

        private static final Logger logger =
            Logger.getLogger(PresentationBaseClientPlugin.class.getName());

        ServerSessionManager session;

        @Override
        public void initialize(ServerSessionManager loginInfo) {
            logger.warning("Initializing ClientPlugin for Presentation-base.");
            session = loginInfo;
            super.initialize(loginInfo);
        }

        @Override
        public void activate() {

            ViewManager.getViewManager().addViewManagerListener(this);
        }

        @Override
        public void deactivate() {
            CellCache cache = ClientContextJME.getCellCache(session.getPrimarySession());
            AvatarCell avatar = (AvatarCell) cache.getViewCell();

            // The javadoc on this method says TEST ME, so I'm not sure
            // this will actually work.
            avatar.removeComponent(MovingPlatformAvatarComponent.class);
            logger.warning("Removing MPAC from local avatar.");
        }

    public void primaryViewCellChanged(ViewCell oldViewCell, ViewCell newViewCell) {

        if(newViewCell!=null) {
            newViewCell.addComponent(new MovingPlatformAvatarComponent(newViewCell));
            logger.warning("Just added a MPAC to the local avatar.");
        }
    }
}
