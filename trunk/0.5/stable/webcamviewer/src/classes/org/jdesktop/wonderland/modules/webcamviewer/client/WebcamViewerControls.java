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
package org.jdesktop.wonderland.modules.webcamviewer.client;

import javax.swing.JComponent;
import org.jdesktop.wonderland.modules.webcamviewer.common.WebcamViewerState;

/**
 * Interface for webcam viewer control panels.
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public interface WebcamViewerControls {
    /**
     * Get the component associated with these controls
     * @return the associated component
     */
    public JComponent getComponent();

    /**
     * Add a listener for tool events
     * @param listener a listener to receive tool events
     */
    public void addCellMenuListener(WebcamViewerToolListener listener);

    /**
     * Remove a listener for tool events
     * @param listener the listener to remove
     */
    public void removeCellMenuListener(WebcamViewerToolListener listener);

    /**
     * Update control panel mode to reflect state of player
     * @param state the state of the player
     */
    void setMode(WebcamViewerState state);

    /**
     * Set the state of the on-HUD button
     * @param onHUD true if the control panel is displayed on-HUD, false
     * if in-world
     */
    void setOnHUD(boolean onHUD);

    /**
     * Set the state of the sync button
     * @param synced true if synced, false if not
     */
    void setSynced(boolean synced);

}
