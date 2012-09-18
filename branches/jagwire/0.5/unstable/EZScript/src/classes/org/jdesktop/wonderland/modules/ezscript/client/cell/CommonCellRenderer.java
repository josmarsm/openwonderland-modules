package org.jdesktop.wonderland.modules.ezscript.client.cell;

import com.jme.scene.Node;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;

/**
 *
 * @author JagWire
 */
public class CommonCellRenderer extends BasicRenderer {

    Map<String, DeployedModel> models = new HashMap<String, DeployedModel>();
    private Node modelsRoot = null;

    public CommonCellRenderer(Cell cell) {
        super(cell);
    }

    @Override
    protected Node createSceneGraph(Entity entity) {
        modelsRoot = new Node();

        if (models.isEmpty()) {
            return modelsRoot;
        }

        for (DeployedModel model : models.values()) {
            modelsRoot.attachChild(model.getModelLoader().loadDeployedModel(model, entity));
        }

        return modelsRoot;

        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map getModelsMap() {
        return models;
    }

    public void clearModels() {
        models.clear();
    }

    public void update() {
        SceneWorker.addWorker(new WorkCommit() {

            public void commit() {
                modelsRoot.detachAllChildren();
                //modelsRoot = new Node();

                if (models.isEmpty()) {
                    //finish up here
                    modelsRoot.updateModelBound();
                    modelsRoot.updateGeometricState(0, true);
                    ClientContextJME.getWorldManager().addToUpdateList(modelsRoot);
                    return;
                }

                for (DeployedModel model : models.values()) {
                    logger.warning("Processing model: " + model.getModelURL());
                    Node modelNode = model.getModelLoader().loadDeployedModel(model, entity);
//                    modelNode.updateModelBound();
//                    modelNode.updateGeometricState(0, true);
//                    modelNode.setVisible(true);
//                    modelNode.setIsCollidable(true);

                    modelsRoot.attachChild(modelNode);
                }

//                modelsRoot.updateModelBound();
//                modelsRoot.updateGeometricState(0, true);
                ClientContextJME.getWorldManager().addToUpdateList(modelsRoot);
                //finish up here too
            }
        });
    }

    public void initializeModels() {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());



        //grab the persisted map of models from our cell
        Map<String, SharedData> modelsMap = ((CommonCell) cell).getModels();

        //acquire the LoaderManager to load our model.        
        final LoaderManager manager = LoaderManager.getLoaderManager();

        //for every entry in the cell's shared map
        for (final Map.Entry<String, SharedData> e : modelsMap.entrySet()) {
            executor.submit(new Runnable() {

                public void run() {
                    try {

                        //acquire the url in string form 
                        String urlString = ((SharedString) e.getValue()).getValue();

                        //Get the URL of the model in webDAV. Typically something like: wla://restaurant-activity/ etc...
                        URL url = AssetUtils.getAssetURL(urlString);

                        //Create the deployed model object from our recently acquired URL.
                        DeployedModel model = manager.getLoaderFromDeployment(url);

                        
                        //put in our local map
                        models.put(e.getKey(), model);

                    } catch (IOException ex) {
                        Logger.getLogger(CommonCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });


        }
        try {
            
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
           
        } catch (InterruptedException ex) {
            Logger.getLogger(CommonCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }

        //finally, re-render the common cell with the appropriate models.
        update();
    }
}
