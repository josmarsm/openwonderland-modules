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
package org.jdesktop.wonderland.modules.marbleous.server.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Utilities for manipulating information about audio files.
 * @author kevin
 */
public final class AudioUtils {

    private final static String PROTOCOL_PREFIX = "wls://";
    private final static Logger logger = Logger.getLogger(AudioUtils.class.getName());
    private final static String serverURL = System.getProperty("wonderland.web.server.url");

    // Should never be instantiated
    private AudioUtils() {
    }

    /**
     * Convert the given treatment URI to a URL.
     * @param treatment The treatment to convert
     * @return The URL pointing to the audio file
     */
    public static String uriToURL(String treatment) {

        if (true) {
            //return "http://kmontag-xps:8080/content-repository/wonderland-content-repository/browse/modules/installed/marbleous/audio/start.au";
        }

        System.out.println("Parsing URI: " + treatment);
        if (treatment.startsWith(PROTOCOL_PREFIX)) {
            // Create a URL from wls://<module>/path
            treatment = treatment.substring(PROTOCOL_PREFIX.length());  // skip past wls://

            int ix = treatment.indexOf("/");
            if (ix < 0) {
                logger.warning("Bad URI");
                return null;
            }

            String moduleName = treatment.substring(0, ix);

            String path = treatment.substring(ix + 1);

            URL url;
            System.out.println("Server URL is : " + serverURL);

            try {
                url = new URL(new URL(serverURL),
                        "webdav/content/modules/installed/" + moduleName + "/audio/" + path);
                treatment = url.toString();
            } catch (MalformedURLException ex) {
                logger.warning("Bad URL");
                return null;
            }
        }
        return treatment;
    }
}
