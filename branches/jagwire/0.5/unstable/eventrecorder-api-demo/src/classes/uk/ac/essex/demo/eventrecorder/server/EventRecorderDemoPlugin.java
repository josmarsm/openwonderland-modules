/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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
package uk.ac.essex.demo.eventrecorder.server;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.eventrecorder.RecorderManager;

/**
 * Only purpose is to create and register demo implementation of an EventRecorder
 * @author Bernard Horan
 */
@Plugin
public class EventRecorderDemoPlugin implements ServerPlugin {

    private static final Logger logger = Logger.getLogger(EventRecorderDemoPlugin.class.getName());

    public void initialize() {
        logger.warning("Initialising EventDemoRecorderPlugin");
        EventRecorderImpl ev = new EventRecorderImpl();
        RecorderManager.getDefaultManager().register(ev);
        ev.startRecording();
    }
}
