
package org.jdesktop.wonderland.modules.ezscript.client.video;

import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.videoplayer.client.VideoPlayerToolManager;
import org.jdesktop.wonderland.modules.videoplayer.client.VideoPlayerWindow;
import org.jdesktop.wonderland.modules.videoplayer.client.cell.VideoPlayerCell;

/**
 *
 * @author Jagwire
 */

@ScriptMethod
public class PlayVideoMethod implements ScriptMethodSPI {

    private VideoPlayerCell videoCell;
    public String getFunctionName() {
        return "PlayVideo";
    }

    public void setArguments(Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
        videoCell = (VideoPlayerCell)args[0];
    }

    public String getDescription() {
        return "Plays a video.\n"
                + "-- usage: PlayVideo(cell);";
    }

    public String getCategory() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "video";
    }

    public void run() {
//        throw new UnsupportedOperationException("Not supported yet.");
        
        //sync'd version
        VideoPlayerWindow window = videoCell.getWindow();
        VideoPlayerToolManager manager = window.getToolManager();
        manager.playAction();
               
        //unsync'd version
        /**
         * VideoPlayerWindow window = videoCell.getWindow();
         * window.play();
         */
    }
    
}
