package org.jdesktop.wonderland.modules.webcaster.client.jme.cellrenderer;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellServerState;

@CellFactory
public class WebcasterCellFactory implements CellFactorySPI {

    public String[] getExtensions() {
        return new String[]{};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        WebcasterCellServerState state = new WebcasterCellServerState();
        return (T)state;
    }

    public String getDisplayName() {
        return "Webcaster";
    }

    public Image getPreviewImage() {
        try{
            URL url = AssetUtils.getAssetURL("wla://webcaster/icon.jpg");
            return Toolkit.getDefaultToolkit().createImage(url);
        }catch(MalformedURLException e){
            return null;
        }
    }
}
