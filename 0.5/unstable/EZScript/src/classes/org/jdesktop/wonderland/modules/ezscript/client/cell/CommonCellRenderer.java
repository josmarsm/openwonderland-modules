
package org.jdesktop.wonderland.modules.ezscript.client.cell;

import com.jme.scene.Node;
import java.util.HashMap;
import java.util.Map;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;

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

        if(models.isEmpty())
            return modelsRoot;

        for(DeployedModel model: models.values()) {
            modelsRoot.attachChild(model.getModelLoader().loadDeployedModel(model, entity));
        }
        
        return modelsRoot;

        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map getModelsMap() {
        return models;
    }

    public void update() {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                modelsRoot.detachAllChildren();
                //modelsRoot = new Node();

                if(models.isEmpty()) {
                    //finish up here
                    modelsRoot.updateModelBound();
                    modelsRoot.updateGeometricState(0, true);
                }

                for(DeployedModel model: models.values()) {
                    logger.warning("Processing model: "+model.getModelURL());
                    Node modelNode = model.getModelLoader().loadDeployedModel(model, entity);
                    modelNode.updateModelBound();
                    modelNode.updateGeometricState(0, true);
                    modelNode.setVisible(true);
                    modelNode.setIsCollidable(true);
                    
                    modelsRoot.attachChild(modelNode);
                }

                modelsRoot.updateModelBound();
                modelsRoot.updateGeometricState(0, true);
                ClientContextJME.getWorldManager().addToUpdateList(modelsRoot);
                //finish up here too
            }
        });
    }
}
