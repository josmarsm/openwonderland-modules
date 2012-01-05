
package org.jdesktop.wonderland.modules.ezscript.client.video;

import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.videoplayer.client.cell.VideoPlayerCell;

/**
 *
 * @author JagWire
 */

@ScriptMethod
public class PauseVideoMethod implements ScriptMethodSPI {

    private VideoPlayerCell videoCell = null;
    public String getFunctionName() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "PauseVideo";
        
    }

    public void setArguments(Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
        videoCell = (VideoPlayerCell)videoCell;
    }

    public String getDescription() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Pauses a video.\n"
                + "-- usage: PauseVideo(cell);";
    }

    public String getCategory() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "video";
    }

    public void run() {
//        throw new UnsupportedOperationException("Not supported yet.");
        videoCell.getWindow().getToolManager().pauseAction();
    }
    
}
