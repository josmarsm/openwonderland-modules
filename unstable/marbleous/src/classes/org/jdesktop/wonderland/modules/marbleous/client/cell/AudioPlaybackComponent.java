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
package org.jdesktop.wonderland.modules.marbleous.client.cell;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SoundPlaybackMessage;

/**
 * Component which interacts with the server to play back sounds.
 * @author kevin
 */
public class AudioPlaybackComponent extends CellComponent {

    @UsesCellComponent
    private ChannelComponent channel;

    /**
     * 
     * @param cell
     */
    public AudioPlaybackComponent(Cell cell) {
        super(cell);
    }

    public void sendPlayMessage(String uri) {
        // Play a sound
        String callID = SoftphoneControlImpl.getInstance().getCallID();
        channel.send(new SoundPlaybackMessage(callID, uri, true));
    }
}
