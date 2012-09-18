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
package org.jdesktop.wonderland.modules.cmu.client.events.cmu;

import org.jdesktop.wonderland.modules.cmu.client.CMUCell;

/**
 * Event representing a change in a scene's title.
 * @author kevin
 */
public class SceneTitleChangeEvent extends CMUChangeEvent {

    private String sceneTitle;

    /**
     * Standard constructor.
     * @param cell The cell whose title has changed
     * @param sceneTitle The new title for the scene
     */
    public SceneTitleChangeEvent(CMUCell cell, String sceneTitle) {
        super(cell);
        setSceneTitle(sceneTitle);
    }
    
    /**
     * Get the new title for the scene.
     * @return The new title for the scene
     */
    public String getSceneTitle() {
        return sceneTitle;
    }

    /**
     * Set the new title for the scene.
     * @param sceneTitle The new title for the scene
     */
    public void setSceneTitle(String sceneTitle) {
        this.sceneTitle = sceneTitle;
    }
}
