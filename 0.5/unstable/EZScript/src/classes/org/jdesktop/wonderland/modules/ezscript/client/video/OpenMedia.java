
package org.jdesktop.wonderland.modules.ezscript.client.video;

import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.videoplayer.client.cell.VideoPlayerCell;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class OpenMedia implements ScriptMethodSPI {

    private VideoPlayerCell videoCell = null;
    public String getFunctionName() {
        return "OpenMedia";
    }

    public void setArguments(Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
        videoCell = (VideoPlayerCell)args[0];
    }

    public String getDescription() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Opens the \"open media\" window for a given video object.\n"
                + "-- usage: OpenMedia(cell);";
    }

    public String getCategory() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "video";
    }

    public void run() {
//        throw new UnsupportedOperationException("Not supported yet.");
        videoCell.getWindow().getToolManager().openMediaAction();
    }
    
}
