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
package org.jdesktop.wonderland.modules.eventplayer.client.npcplayer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.ComponentLookupClass;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.avatarbase.client.basic.BasicAvatarLoaderFactory;
import org.jdesktop.wonderland.modules.avatarbase.client.cell.AvatarConfigComponent;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;
import org.jdesktop.wonderland.modules.avatarbase.client.loader.spi.AvatarLoaderFactorySPI;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.AvatarConfigComponentClientState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.AvatarConfigInfo;

/**
 * A Cell component that represents the current avatar configured by the system
 * for event player NPCs.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Bernard Horan
 */
@ComponentLookupClass(AvatarConfigComponent.class)
public class NpcPlayerConfigComponent extends AvatarConfigComponent {
    private static final String AVATAR_URL = "default-avatars/maleCartoonAvatar.dae/maleCartoonAvatar.dae.gz.dep";

    private static Logger logger =
            Logger.getLogger(NpcPlayerConfigComponent.class.getName());

    /** Constructor */
    public NpcPlayerConfigComponent(Cell cell) {
        super(cell);
    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
        AvatarConfigComponentClientState acccs = (AvatarConfigComponentClientState) clientState;
        AvatarConfigInfo avatarConfigInfo = acccs.getAvatarConfigInfo();
        
        if (avatarConfigInfo != null) {
            String uri = avatarConfigInfo.getAvatarConfigURL();
            if (uri != null) {
                //logger.info("uri " + uri);
                String clazz = avatarConfigInfo.getLoaderFactoryClassName();
                try {
                    Class avatarLoaderFactoryClass = Class.forName(clazz);
                    AvatarLoaderFactorySPI factory = (AvatarLoaderFactorySPI) avatarLoaderFactoryClass.newInstance();
                    WlAvatarCharacter avatarCharacter = factory.getAvatarLoader().getAvatarCharacter(cell, uri, avatarConfigInfo);
                    //logger.info("avatarCharacter: " + avatarCharacter);
                    if (avatarCharacter == null) {
                        fallback(acccs, avatarConfigInfo);
                    }
                } catch (InstantiationException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    fallback(acccs, avatarConfigInfo);
                } catch (IllegalAccessException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    fallback(acccs, avatarConfigInfo);
                } catch (ClassNotFoundException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    fallback(acccs, avatarConfigInfo);
                }
                
            }
        }
        super.setClientState(clientState);
    }

    private void fallback(AvatarConfigComponentClientState acccs, AvatarConfigInfo avatarConfigInfo) {
        logger.warning("Using fallback avatar: " + AVATAR_URL);
        avatarConfigInfo = new AvatarConfigInfo(AVATAR_URL, BasicAvatarLoaderFactory.class.getName());
        acccs.setAvatarConfigInfo(avatarConfigInfo);
    }
}
