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

package org.jdesktop.wonderland.modules.eventplayer.server;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.messages.MessagePacker.ReceivedMessage;

/**
 * Interface for the event playing manager, responsible (as its name suggests)
 * for replaying changes/events.
 * @author Bernard Horan
 */
public interface EventPlayingManager {

    /**
     * Pause the replaying of changes
     * @param tapeName the name of the recording to pause
     * @param listener the object that should be notified for callbacks
     */
    public void pauseChanges(String tapeName, ChangeReplayingListener listener);

    /**
     * Replay the changes from a named recording
     * @param tapeName the name of the recording
     * @param listener the object that should be notified for callbacks
     */
    public void replayChanges(String tapeName, ChangeReplayingListener listener);

    /**
     * A listener that will be notified of the result of replaying changes
     * from a changes file.  Implementations of ChangeReplayingListener must
     * be either a ManagedObject or Serializable
     */
    public interface ChangeReplayingListener {

        /**
         * All the changes from this recording have been played
         */
        public void allChangesPlayed();

        /**
         * Load a cell
         * @param setup the server state of the cell
         * @param parentID the parent of the cell
         */
        public void loadCell(CellServerState setup, CellID parentID);

        

        /**
         * Replay a message
         * @param message a received message that has been parsed from the recording
         */
        public void playMessage(ReceivedMessage message);

        /**
         * Unload a cell with the id specified
         * @param oldCellID the id of the cell to be unloaded
         */
        public void unloadCell(CellID oldCellID);

    }

    /**
     * Unload a recording. (I.e. remove its cells)
     * @param tapeName the name of the recording to unload
     */
    public void unloadRecording(String tapeName);


}
