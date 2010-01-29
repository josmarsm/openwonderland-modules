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
package org.jdesktop.wonderland.modules.cmu.common.messages.serverclient;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * Message to notify the server/clients that a scene's title has changed.
 * @author kevin
 */
public class SceneTitleChangeMessage extends CellMessage {

    private String sceneTitle;

    /**
     * Standard constructor.
     * @param sceneTitle The new title of the scene
     */
    public SceneTitleChangeMessage(String sceneTitle) {
        super();
        setSceneTitle(sceneTitle);
    }

    /**
     * Get the new title of the scene.
     * @return Title of the scene
     */
    public String getSceneTitle() {
        return sceneTitle;
    }

    /**
     * Set the new title of the scene.
     * @param sceneTitle Title of the scene
     */
    public void setSceneTitle(String sceneTitle) {
        this.sceneTitle = sceneTitle;
    }
}
