

package org.jdesktop.wonderland.modules.ezscript.client.cell;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;

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

    private SharedMapCli modelMap = null;
    private ModelAttachmentReceiver receiver;

    private CommonCellRenderer renderer = null;
    public CommonCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        switch(status) {
            case RENDERING:
                /**
                 * This is a bit hacky, but here's the idea. From the cell's
                 * context, we don't know when each component of this cell will
                 * enter active. By the time we are rendering though, we should
                 * already have our scene graph created. So we tell the renderer
                 * let's ask the shared-state component for our list of models.
                 * 
                 * The renderer in turn calls CommonCell.getModels(), which
                 * tries to get the models every half a second until a positive
                 * response is achieved. If anything is to go wrong, it will 
                 * return an empty list.
                 * 
                 * The order of calling is as follows:
                 * renderer.intializeModels() -> CommonCell.getModels().
                 */
                renderer.initializeModels();
                break;
                
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

    /**
     * This could get dicey. Retrieve a shared map from the EZScriptComponent's
     * shared-state component. Specifically, this map is named "models" and is 
     * to be used by CommonCells to store models for it's scene graph.
     * 
     * It's important to note that at the time of calling this function, we don't
     * know whether or not EZScript has already been to ACTIVE state. If it hasn't
     * we will likely get a null sharedStateComponent. We should watch out for 
     * this.
     * 
     * @return the shared map named "models".
     */    
    private SharedMapCli getModelMap() {
        if(scriptComponent == null) {
            return null;
        }
        
        if(scriptComponent.getSharedStateComponent() == null) {
            //recover in some-way. Perhaps we can return an empty?
            return null;
        }
        
        if(modelMap == null) {
            modelMap = scriptComponent.getSharedStateComponent().get("models");
        }
        
        return modelMap;
    }
    
    /**
     * Should exclusively be used by the renderer to retrieve any persisted 
     * models.
     * 
     * @return 
     */
    public Map getModels() {
        Map m = getModelMap();
        if(m == null) {
            try {
                while(m == null) {
                    //if it's not readily available, wait half a second and try
                    //again.
                    Thread.currentThread().wait(500);
                    m = getModelMap();
                }
                return m;
            } catch (InterruptedException ex) {
                Logger.getLogger(CommonCell.class.getName()).log(Level.SEVERE, null, ex);
                return Collections.EMPTY_MAP;
            }
        }
        return m;                
    }
    
    public void clearRendererModels() {
        renderer.clearModels();
    }
    
    public void forceUpdate() {
        renderer.update();
    }
    class ModelAttachmentReceiver implements ComponentMessageReceiver {

        public void messageReceived(CellMessage message) {
           attachModelInternal((AttachModelMessage)message);
        }

        private void attachModelInternal(AttachModelMessage msg) {
            try {
                //populate map so we can persist models
                getModelMap().put(msg.getModelID(), SharedString.valueOf(msg.getModelURL()));
                
                //acquire the LoaderManager to load our model.
                LoaderManager manager = LoaderManager.getLoaderManager();
                
                //Get the URL of the model in webDAV. Typically something like: wla://restaurant-activity/ etc...
                URL url = AssetUtils.getAssetURL(msg.getModelURL());
                
                //Create the deployed model object from our recently acquired URL.
                DeployedModel model = manager.getLoaderFromDeployment(url);
                
                logger.warning("Received message to process model: "+model.getModelURL());
                
                //Send the model to the renderer.
                renderer.getModelsMap().put(msg.getModelID(), model);
                
                //Tell the renderer to re-render.
//                renderer.update();
                renderer.initializeModels();
            } catch(MalformedURLException ex) {
                ex.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

    }
}
