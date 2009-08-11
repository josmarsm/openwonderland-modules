/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
