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
package org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteCellServerState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteTypes;

/**
 * The task sticky note factory.
 * @author Ryan (mymegabyte)
 */
@CellFactory
public class TaskStickyNoteCellFactory implements CellFactorySPI {

    public String[] getExtensions() {
        return new String[]{};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        StickyNoteCellServerState state = new StickyNoteCellServerState();
        state.setNoteType(StickyNoteTypes.TASK);
        state.setColor("255:255:255");
        // HACK!
        Map<String, String> metadata = new HashMap();
        metadata.put("sizing-hint", "2.0");
        state.setMetaData(metadata);
        state.setName("StickyNote");

        return (T) state;
    }

    public String getDisplayName() {
        return "Task Sticky Note";
    }

    public Image getPreviewImage() {
        URL url = TaskStickyNoteCellFactory.class.getResource("resources/task_stickynote_preview.png");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
