/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.fairbooth.client.jme.cellrenderer;

import com.jme.bounding.BoundingBox;
import com.jme.math.Quaternion;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.io.*;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.*;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
/*
 * @author Nilang
 */

public class FairBoothCellRenderer extends BasicRenderer {

    private Node node = null;
   
    public FairBoothCellRenderer(Cell cell) {
        super(cell);
    }

    protected Node createSceneGraph(Entity entity) {

        node = new Node();
        node.setModelBound(new BoundingBox());
        node.updateModelBound();
        node.setName("mybooth");
        
        float angles1[]=new float[3];
        angles1[0]=0f;
        angles1[1]=(float)(3.14/2);
        angles1[2]=0f;
        Quaternion rot1=new Quaternion(angles1);
        node.setLocalRotation(rot1);
        
        //attach fairbooth model
        try {
            URL url = AssetUtils.getAssetURL("wla://FairBooth/booth_4-3_colored_v04.DAE"
                    + "/booth_4-3_colored_v04.DAE.gz.dep", cell);
            DeployedModel m = LoaderManager.getLoaderManager().getLoaderFromDeployment(url);
            Spatial mine = m.getModelLoader().loadDeployedModel(m, null);
            mine.setName("FairBooth");
            float angles[]=new float[3];
            angles[0]=(float)(-3.14/2);
            angles[1]=(float)(3.14/2);
            angles[2]=0f;
            node.attachChild(mine);
        } catch (IOException ex) {
            Logger.getLogger(FairBoothCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.setLightingEnabled(true);
        return node;
    }
}
