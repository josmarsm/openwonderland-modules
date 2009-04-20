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

package org.jdesktop.wonderland.modules.eventplayer.web;

import org.jdesktop.wonderland.web.wfs.*;
import org.jdesktop.wonderland.modules.eventplayer.server.ChangesFile;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bh37721
 */
public class ChangesManager {
    /* The logger for the changes manager */
    private static final Logger logger = Logger.getLogger(ChangesManager.class.getName());
    private static final Map<String, ChangesFile> changesFiles = new HashMap<String, ChangesFile>();

    private final static String FILENAME = "changes.xml";

    /**
     * Singleton to hold instance of ChangesManager. This holder class is loaded
     * on the first execution of ChangesManager.getChangesManager().
     */
    private static class ChangesManagerHolder {
        private final static ChangesManager changesManager = new ChangesManager();
    }

    /**
     * Returns a single instance of this class
     * <p>
     * @return Single instance of this class.
     */
    public static final ChangesManager getChangesManager() {
        return ChangesManagerHolder.changesManager;
    }

    public void removeChangesFile(String tapeName) {
        changesFiles.remove(tapeName);
    }

    /**
     * Create a new changes file given its tapeName.
     * @param recording
     * @param timestamp 
     * @return
     */
    public ChangesFile createChangesFile(WFSRecording recording, long timestamp) {
        String tapeName = recording.getName();
        ChangesFile cFile = changesFiles.get(tapeName);
        if (cFile != null) {
            logger.warning("changes file for " + tapeName + " already exists, deleting");
            cFile.delete();
            changesFiles.remove(tapeName);
        }
        File file = new File(recording.getDirectory(), FILENAME);
        logger.info("Created file: " + file);

        try {
            cFile = new ChangesFile(file, timestamp);
            changesFiles.put(tapeName, cFile);
            System.out.println("Added changes file: " + changesFiles);
            return cFile;
        } catch (java.lang.Exception excp) {
            logger.log(Level.WARNING, "[WFS] Unable to create changes file", excp);
            return null;
        }
    }

    public ChangesFile getChangesFile(String tapeName) {
        System.out.println("Getting changes file from: " + changesFiles);
        return changesFiles.get(tapeName);
    }

}
