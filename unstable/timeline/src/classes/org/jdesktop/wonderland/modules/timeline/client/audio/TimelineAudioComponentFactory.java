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
package org.jdesktop.wonderland.modules.timeline.client.audio;

import org.jdesktop.wonderland.client.cell.registry.annotation.CellComponentFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.timeline.common.audio.TimelineAudioComponentServerState;

/**
 * The cell component factory for the sample cell component.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@CellComponentFactory
public class TimelineAudioComponentFactory implements CellComponentFactorySPI {

    public String getDisplayName() {
        return "Timeline Audio Component";
    }

    public <T extends CellComponentServerState> T getDefaultCellComponentServerState() {
        TimelineAudioComponentServerState state = new TimelineAudioComponentServerState();
        return (T)state;
    }

    public String getDescription() {
        return "Timeline Audio Component";
    }

}
