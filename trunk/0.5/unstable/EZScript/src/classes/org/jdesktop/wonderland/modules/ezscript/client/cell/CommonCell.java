

package org.jdesktop.wonderland.modules.ezscript.client.cell;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.ezscript.client.EZScriptComponent;
import org.jdesktop.wonderland.modules.ezscript.common.AttachModelMessage;

/**
 * Cell to be created dynamically through a "Create Cell" script.
 *
 * Futher, the cell is meant to be used as a grouping node.
 * 
 * @author JagWire
 */
public class CommonCell extends Cell {

    @UsesCellComponent
    EZScriptComponent scriptComponent;

    @UsesCellComponent
    ChannelComponent channelComponent;

    private ModelAttachmentReceiver receiver;

    private CommonCellRenderer renderer = null;
    public CommonCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        switch(status) {
            case ACTIVE:
                if(increasing) {
                    channelComponent.addMessageReceiver(AttachModelMessage.class, new ModelAttachmentReceiver());
                }

                break;
            case INACTIVE:
                if(!increasing) {
                    channelComponent.removeMessageReceiver(AttachModelMessage.class);
                }

                break;
        }
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new CommonCellRenderer(this);
            return this.renderer;
        } else {
            return super.createCellRenderer(rendererType);
        }

    }

    @Override
    public void setClientState(CellClientState configData) {
        super.setClientState(configData);
    }
 
    public void attachModel(String modelURL, String modelID) {
        //send message here, don't do heavy lifting.
        this.sendCellMessage(new AttachModelMessage(getCellID(), modelURL, modelID));
    }

    class ModelAttachmentReceiver implements ComponentMessageReceiver {

        public void messageReceived(CellMessage message) {
           attachModelInternal((AttachModelMessage)message);
        }

        private void attachModelInternal(AttachModelMessage msg) {
            try {
                LoaderManager manager = LoaderManager.getLoaderManager();
                URL url = AssetUtils.getAssetURL(msg.getModelURL());
                DeployedModel model = manager.getLoaderFromDeployment(url);
                logger.warning("Received message to process model: "+model.getModelURL());
                renderer.getModelsMap().put(msg.getModelID(), model);
                renderer.update();
            } catch(MalformedURLException ex) {
                ex.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

    }
}
