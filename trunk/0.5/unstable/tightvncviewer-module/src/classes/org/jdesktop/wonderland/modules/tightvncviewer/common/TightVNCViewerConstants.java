/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.tightvncviewer.common;

/**
 * Constants relating to the VNC viewer state.
 *
 * @author nsimpson
 */
public class TightVNCViewerConstants {

    // the name of the map where we store status
    public static final String STATUS_MAP = "tightvncviewer-status";
    // the key for the address of the VNC server
    public static final String VNC_SERVER = "server-address";
    // the key for the port number of the VNC server
    public static final String VNC_PORT = "server-port";
    // the key for the username
    public static final String VNC_USERNAME = "server-username";
    // the key for the password
    public static final String VNC_PASSWORD = "server-password";
    // the key for the state of the VNC viewer
    public static final String VNC_VIEWER_STATE = "player-state";
}
