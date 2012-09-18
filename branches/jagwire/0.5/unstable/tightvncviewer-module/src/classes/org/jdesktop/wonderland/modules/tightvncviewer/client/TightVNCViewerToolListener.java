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
package org.jdesktop.wonderland.modules.tightvncviewer.client;

/**
 * Listener methods for VNC Viewer's control panel buttons.
 *
 * @author nsimpson
 */
public interface TightVNCViewerToolListener {

    /**
     * Toggle the display of the VNC viewer from in-world to on-HUD
     */
    public void toggleHUD();

    /**
     * Open a connection to a VNC server
     */
    public void openConnection();

    /**
     * Close a connection to a VNC server
     */
    public void closeConnection();

    /**
     * Send Ctrl-Alt-Del to Windows hosts
     */
    public void sendCtrlAltDel();
    
    /**
     * Toggle synchronized state on/off
     */
    public void sync();
}
