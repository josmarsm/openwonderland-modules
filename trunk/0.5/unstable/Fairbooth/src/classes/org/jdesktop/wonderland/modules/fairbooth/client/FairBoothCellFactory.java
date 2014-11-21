/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.fairbooth.client;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.BoundingVolumeHint;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.fairbooth.common.FairBoothServerState;

/**
 *
 * @author Nilang
 */
@CellFactory
public class FairBoothCellFactory implements CellFactorySPI {

    public String[] getExtensions() {
        return new String[] {};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {

        //Show Initial Configuration Dialog
        FairBoothPropertyPanel panel = new FairBoothPropertyPanel();
        Object[] options = {"Create","Cancel"};
        int ans=JOptionPane.showOptionDialog(null,panel,"FairBooth configuration", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,null,options,options[0]);
        if(ans==0) { 
            panel.setValues();
            FairBoothServerState state = new FairBoothServerState();

            //set booth properties to server state
            state.setBoothName(panel.boothName);
            state.setColorTheme(panel.colorTheme);
            state.setInfoText(panel.infoText);
            state.setLeftPanelFrames(panel.leftPanelFrames);
            state.setRightPanelFrames(panel.rightPanelFrames);
            
            BoundingVolume bv = new BoundingBox(new Vector3f(0f, 0f, 0f),
                            3f,
                            2f,
                            -3f);
            Quaternion rot = new Quaternion(new float[]{0f,(float)(-3.14/2),0f});
            BoundingVolumeHint hint = new BoundingVolumeHint(true, bv);
            hint.getBoundsHint().transform(rot, Vector3f.ZERO, new Vector3f(3f, 2f, -3f));
            state.setBoundingVolumeHint(hint);
            state.setName("FairBooth");
            return (T)state;
        }
        else {
            return null;
        }
    }
    
    public String getDisplayName() {
        return "Fair Booth";
    }
    
    public Image getPreviewImage() {
        URL url = FairBoothCellFactory.class.getResource("resources/FairBooth.png");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
