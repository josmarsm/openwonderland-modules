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
package org.jdesktop.wonderland.modules.xmpp_presence.server.service;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Task;
import java.io.Serializable;

/**
 *
 * @author drew
 */
public class StatusMessageUpdateTask implements Task, Serializable {

    public void run()
        {
            XMPPPresenceManager manager = AppContext.getManager(XMPPPresenceManager.class);
            manager.updatePresence();
        }
}
