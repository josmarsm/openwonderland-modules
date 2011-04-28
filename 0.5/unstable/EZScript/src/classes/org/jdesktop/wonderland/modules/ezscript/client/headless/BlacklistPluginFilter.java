/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.headless;

import java.net.URL;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.login.PluginFilter;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.JarURI;

/**
 *
 * @author ryan
 */
class BlacklistPluginFilter implements PluginFilter {

        private final String[] JAR_BLACKLIST = {
            "ant",
            "ant-launcher",
            "artimport-client",
            "audiomanager-client",
            "avatarbase-client",
            "defaultenvironment-client",
            "kmzloader-client",
            "contextmenu"
        };
        private final String[] CLASS_BLACKLIST = {};

        public boolean shouldDownload(ServerSessionManager sessionManager, URL jarURL) {
            String urlPath = jarURL.getPath();
            int idx = urlPath.lastIndexOf("/");
            if (idx != -1) {
                urlPath = urlPath.substring(idx);
            }

            for (String check : JAR_BLACKLIST) {
                if (urlPath.contains(check)) {
                    return false;
                }
            }

            return true;
        }

        public boolean shouldInitialize(ServerSessionManager sessionManager,
                ClientPlugin plugin) {
            for (String check : CLASS_BLACKLIST) {
                if (plugin.getClass().getName().equals(check)) {
                    return false;
                }
            }

            return true;
        }

        public boolean shouldDownload(ServerSessionManager sessionManager, JarURI uri) {
            //throw new UnsupportedOperationException("Not supported yet.");
            return true;
        }
    }
