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
package org.jdesktop.wonderland.modules.colladaavatar.client;

import java.util.HashSet;
import java.util.Set;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.avatarbase.client.registry.AvatarRegistry;
import org.jdesktop.wonderland.modules.avatarbase.client.registry.annotation.AvatarFactory;
import org.jdesktop.wonderland.modules.avatarbase.client.registry.spi.AvatarFactorySPI;

/**
 * Basic avatar factory generates the COLLADA avatar.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@AvatarFactory
public class ColladaAvatarFactory implements AvatarFactorySPI {

    // The set of collada avatars
    private static Set<ColladaAvatar> colladaAvatarSet = null;

    // A hard-coded list of relative URLs of the basic avatar artwork
    private static String AVATARS[][] = {
        {
            "Darth Stewie",
            "darth_stewie.kmz/darth_stewie.kmz.dep"
        }
    };
    
    /**
     * {@inheritDoc}
     */
    public void registerAvatars(ServerSessionManager session) {
        // Create the set of basic avatars from the hard-coded list of URLs
        AvatarRegistry registry = AvatarRegistry.getAvatarRegistry();
        colladaAvatarSet = new HashSet();
        for (int i = 0; i < AVATARS.length; i++) {
            ColladaAvatar avatar = new ColladaAvatar(AVATARS[i][0], AVATARS[i][1]);
            colladaAvatarSet.add(avatar);
            registry.registerAvatar(avatar, false);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unregisterAvatars(ServerSessionManager session) {
        // Look through and unregistry all of the basic avatars
        AvatarRegistry registry = AvatarRegistry.getAvatarRegistry();
        for (ColladaAvatar avatar : colladaAvatarSet) {
            registry.unregisterAvatar(avatar);
        }
        colladaAvatarSet.clear();
        colladaAvatarSet = null;
    }
}
