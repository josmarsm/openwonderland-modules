
package org.jdesktop.wonderland.modules.ezscript.client.video;

import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.videoplayer.client.VideoPlayerWindow;
import org.jdesktop.wonderland.modules.videoplayer.client.cell.VideoPlayerCell;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class FastForwardVideoMethod implements ScriptMethodSPI {

    private VideoPlayerCell videoCell = null;
    public String getFunctionName() {
        return "FastForwardVideo";
    }

    public void setArguments(Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
        videoCell = (VideoPlayerCell)args[0];
//        amount = ((Double)args[1]).doubleValue();
    }

    public String getDescription() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Fast-Forwards a video by 10 frames.\n"
                + "-- usage: FastForward(cell);";
    }

    public String getCategory() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "video";
    }

    public void run() {
//        throw new UnsupportedOperationException("Not supported yet.");
        //unsync'd version
        //VideoPlayerWindow window = videoCell.getWindow();
        //window.forward(10d);
        
        //sync'd version
        videoCell.getWindow().getToolManager().forwardAction();
    }
    
}
