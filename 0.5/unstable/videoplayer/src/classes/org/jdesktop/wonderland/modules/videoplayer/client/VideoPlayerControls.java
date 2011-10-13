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
package org.jdesktop.wonderland.modules.videoplayer.client;

import javax.swing.JComponent;
import org.jdesktop.wonderland.modules.videoplayer.common.VideoPlayerActions;

/**
 * Interface for video player control panels.
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public interface VideoPlayerControls {
    /**
     * Get the component associated with these controls
     * @return the associated component
     */
    JComponent getComponent();
    
    /**
     * Add a listener for tool events
     * @param listener a listener to receive tool events
     */
    void addCellMenuListener(VideoPlayerToolActionListener listener);

    /**
     * Remove a listener for tool events
     * @param listener the listener to remove
     */
    void removeCellMenuListener(VideoPlayerToolActionListener listener);

    /**
     * Update control panel mode to reflect state of player
     * @param state the state of the player
     */
    void setMode(VideoPlayerActions mode);

    /**
     * Update control panel to reflect seek capabilities of the video
     * @param seekEnabled true if the video supports seeking, or false if not
     */
    void setSeekEnabled(boolean seekEnabled);
    
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
