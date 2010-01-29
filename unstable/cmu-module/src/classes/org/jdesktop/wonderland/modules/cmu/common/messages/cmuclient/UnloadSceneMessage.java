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
package org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient;

import org.jdesktop.wonderland.modules.cmu.common.UnloadSceneReason;

/**
 * Message to notify clients that a scene is being unloaded.
 * @author kevin
 */
public class UnloadSceneMessage extends CMUClientMessage {

    private UnloadSceneReason reason = null;

    /**
     * Standard constructor.
     * @param reason The reason for which this scene is being unloaded
     */
    public UnloadSceneMessage(UnloadSceneReason reason) {
        setReason(reason);
    }

    /**
     * Get the reason for which the scene is being unloaded.
     * @return Reason for scene unload
     */
    public UnloadSceneReason getReason() {
        return reason;
    }

    /**
     * Set the reason for which the scene is being unloaded.
     * @param reason Reason for scene unload
     */
    public void setReason(UnloadSceneReason reason) {
        this.reason = reason;
    }
}
