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

/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.microphone.server.cell;

import com.sun.sgs.app.ManagedReference;
import java.util.Map;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.security.Action;
import org.jdesktop.wonderland.modules.microphone.common.security.ListenAction;
import org.jdesktop.wonderland.modules.microphone.server.cell.MicrophoneComponentMO.Status;
import org.jdesktop.wonderland.server.cell.CellMO;

/**
 * A server cell that provides a microphone proximity listener
 * @author jprovino
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class MicrophoneListenAreaProximityListener
        extends MicrophoneBaseProximityListener
{

    private static final Logger logger =
            Logger.getLogger(MicrophoneListenAreaProximityListener.class.getName());

    public MicrophoneListenAreaProximityListener(CellMO cellMO, String name,
            double speakingVolume, double listenVolume,
            ManagedReference<Map<String, Status>> statusMapRef)
    {
        super (cellMO, name, speakingVolume, listenVolume, statusMapRef);
    }

    @Override
    protected String getAreaType() {
        return "listen";
    }

    @Override
    protected Action getAction() {
        return new ListenAction();
    }

    @Override
    protected Status entered(Status prev) {
        if (prev == null) {
            return Status.LISTENING;
        } else if (prev == Status.SPEAKING) {
            return Status.BOTH;
        } else {
            logger.warning("Enter listen area when already listening");
            return prev;
        }
    }

    @Override
    protected Status exited(Status prev) {
        if (prev == Status.LISTENING) {
            return null;
        } else if (prev == Status.BOTH) {
            return Status.SPEAKING;
        } else {
            logger.warning("Exit listen area when not listening");
            return prev;
        }
    }
}
