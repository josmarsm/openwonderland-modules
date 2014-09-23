/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.imageframe.server;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameClientState;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameServerState;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.DependsOnCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * Server side managed object for image frame
 */
@DependsOnCellComponentMO({SharedStateComponentMO.class,MovableComponentMO.class})
public class ImageFrameCellMO extends CellMO {

    private int fit=-1;
    private int aspectRatio=-1;
    private int orientation=-1;
    private int frameWidth=-1;
    private int frameHeight=-1;
    private String imageURL=null;
   
    public ImageFrameCellMO() {
    }
        
    @Override
    public String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
         return "org.jdesktop.wonderland.modules.imageframe.client.ImageFrameCell";
    }

    @Override
    public CellClientState getClientState(CellClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (state == null) {
            state = new ImageFrameClientState();
        }
        ((ImageFrameClientState)state).setFit(fit);
        ((ImageFrameClientState)state).setAspectRatio(aspectRatio);
        ((ImageFrameClientState)state).setOrientation(orientation);
        ((ImageFrameClientState)state).setFrameHeight(frameHeight);
        ((ImageFrameClientState)state).setFrameWidth(frameWidth);
        ((ImageFrameClientState)state).setImageURL(imageURL);
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new ImageFrameServerState();
        }
        ((ImageFrameServerState)state).setFit(fit);
        ((ImageFrameServerState)state).setAspectRatio(aspectRatio);
        ((ImageFrameServerState)state).setOrientation(orientation);
        ((ImageFrameServerState)state).setFrameHeight(frameHeight);
        ((ImageFrameServerState)state).setFrameWidth(frameWidth);
        ((ImageFrameServerState)state).setImageURL(imageURL);
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
        fit = ((ImageFrameServerState)state).getFit();
        aspectRatio = ((ImageFrameServerState)state).getAspectRatio();
        orientation = ((ImageFrameServerState)state).getOrientation();
        frameHeight = ((ImageFrameServerState)state).getFrameHeight();
        frameWidth = ((ImageFrameServerState)state).getFrameWidth();
        imageURL = ((ImageFrameServerState)state).getImageURL();
    }

}
