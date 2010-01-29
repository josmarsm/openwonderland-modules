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
package org.jdesktop.wonderland.modules.timeline.client.provider;

import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Client plugin used by the timeline provider component
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@Plugin
public class TimelineProviderClientPlugin extends BaseClientPlugin {
    /** the session for this plugin */
    private static ServerSessionManager sessionManager;

    @Override
    public void initialize(ServerSessionManager sessionManager) {
        super.initialize(sessionManager);

        TimelineProviderClientPlugin.sessionManager = sessionManager;
    }

    /**
     * Static method to get the session manager.  This works because
     * each server connection is loaded in a separate classloader, so the
     * static method will return the correct session manager for this
     * particular server connection.
     * @return the session manager.
     */
    public static ServerSessionManager getServerSessionManager() {
        return sessionManager;
    }
}
