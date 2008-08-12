/**
 * Project Wonderland
 *
 * $URL$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Rev$
 * $Date$
 * $Author$
 */
package com.sun.labs.miw.client.cell;

import java.util.HashMap;
import java.util.Map;

public class UI {
    static void init() {
        playlist = new Playlist();
        albums = new HashMap<String, Album>();
        albumViewer = new AlbumViewer();
        console = new Console();
        playlistViewer = new PlaylistViewer();
    }
    static Console console;
    static AlbumCloudCell albumCloudCell;
    static AlbumCloud albumCloud;
    static AlbumViewer albumViewer;
    static PlaylistViewer playlistViewer;
    static Map<String,Album> albums;
    static Playlist playlist;

    static boolean showWindows = false;
}
