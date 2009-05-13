/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.sharedstatetest.common.recorder;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author bh37721
 */
@XmlRootElement(name="audiorecorder-cell")
@ServerState
public class AudioRecorderCellServerState extends CellServerState implements Serializable {    

    public AudioRecorderCellServerState() {
    }
    
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.sharedstatetest.server.recorder.AudioRecorderCellMO";
    }
}
