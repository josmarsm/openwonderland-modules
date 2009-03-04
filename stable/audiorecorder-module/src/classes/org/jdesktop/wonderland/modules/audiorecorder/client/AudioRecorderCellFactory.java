/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package org.jdesktop.wonderland.modules.audiorecorder.client;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.audiorecorder.common.AudioRecorderCellServerState;

/**
 * The cell factory for the audio recorder.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@CellFactory
public class AudioRecorderCellFactory implements CellFactorySPI {

    public String[] getExtensions() {
        return new String[] {};
    }

    public <T extends CellServerState> T getDefaultCellServerState() {
        return (T)new AudioRecorderCellServerState();
    }

    

    public String getDisplayName() {
        return "Audio Recorder";
    }

    public Image getPreviewImage() {
        URL url = AudioRecorderCellFactory.class.getResource("resources/audiorecorder_preview.png");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
