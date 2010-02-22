/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.satellite.client;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.client.jme.cellrenderer.ModelRenderer;
import org.jdesktop.wonderland.client.jme.utils.ScenegraphUtils;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.satellite.common.SatelliteCellClientState;
import org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingActionClass;
import org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingComponent;
import org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingRunnable;

public class SatelliteCell extends Cell 
    {
    @UsesCellComponent
    private ScriptingComponent scriptingComponent;
    Spatial recordSpatialOnUV = null;
    Spatial towerRed = null;
    Spatial towerBlue = null;

    private String modelURI = null;

    public SatelliteCell(CellID cellID, CellCache cellCache)
        {
        super(cellID, cellCache);
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClientState(CellClientState state)
        {
        super.setClientState(state);
        this.modelURI = ((SatelliteCellClientState)state).getModelURI();
        }

    public void satelliteRecordDown()
        {
        SceneWorker.addWorker(new WorkCommit()
            {
            public void commit()
                {
                recordSpatialOnUV.setLocalTranslation(0.0f, 0.2f, 0.0f);
                ClientContextJME.getWorldManager().addToUpdateList(recordSpatialOnUV);
                }
            });
        }

    ScriptingRunnable satelliteRecordDownRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            satelliteRecordDown();
            System.out.println("ScriptingActionClass - enter satelliteRecordDown");
            }
        };


    public void towerShowRed()
        {
        SceneWorker.addWorker(new WorkCommit()
            {
            public void commit()
                {
                towerRed.setVisible(true);
                towerBlue.setVisible(false);
                ClientContextJME.getWorldManager().addToUpdateList(towerRed);
                ClientContextJME.getWorldManager().addToUpdateList(towerBlue);
                }
            });
        }

    ScriptingRunnable towerShowRedRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            towerShowRed();
            System.out.println("ScriptingActionClass - enter towerShowRed");
            }
        };

    public void towerShowBlue()
        {
        SceneWorker.addWorker(new WorkCommit()
            {
            public void commit()
                {
                towerBlue.setVisible(true);
                towerRed.setVisible(false);
                ClientContextJME.getWorldManager().addToUpdateList(towerRed);
                ClientContextJME.getWorldManager().addToUpdateList(towerBlue);
                }
            });
        }

    ScriptingRunnable towerShowBlueRun = new ScriptingRunnable()
        {
        @Override
        public void run()
            {
            towerShowBlue();
            System.out.println("ScriptingActionClass - enter towerShowBlue");
            }
        };


    @Override
    public void setStatus(CellStatus status, boolean increasing)
        {
        super.setStatus(status, increasing);

        if (status == CellStatus.INACTIVE && increasing == false)
            {
            }
        else if (status == CellStatus.RENDERING && increasing == true)
            {
            ScriptingActionClass sac = new ScriptingActionClass();
            sac.setName("satellite");
            sac.insertCmdMap("towerShowRed", towerShowRedRun);
            sac.insertCmdMap("towerShowBlue", towerShowBlueRun);
            scriptingComponent.putActionObject(sac);
            }
        }

    private void printTree(Node root, int indent)
        {
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < indent; i++)
            {
            buffer.append('\t');
            }
        System.out.println(buffer.toString() + "node: " + root);
        List<Spatial> children = root.getChildren();
        if(children == null)
            {
            return;
            }
        for(Spatial spatial : children)
            {
            if(spatial instanceof Node)
                {
                printTree((Node)spatial, indent + 1);

                }
            else
                {
                System.out.println("spatial: " + spatial.getName());
//                System.out.println("spatial color = " + spatial.getGlowColor());
//                System.out.println("spatial rotation = " + spatial.getLocalRotation());
//                System.out.println("spatial translation = " + spatial.getLocalTranslation());
//                System.out.println("spatial scale = " + spatial.getLocalScale());
               }
            }
        }
    /**
     * {@inheritDoc}
     */
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            try {
                LoaderManager manager = LoaderManager.getLoaderManager();
                URL url = AssetUtils.getAssetURL(modelURI, this);
                DeployedModel dm = manager.getLoaderFromDeployment(url);
                ModelRenderer modr = new ModelRenderer(this, dm);
                Entity mye = modr.getEntity();
                RenderComponent rc = (RenderComponent)mye.getComponent(RenderComponent.class);
                Node root = rc.getSceneRoot();
//                recordSpatialOnUV = ScenegraphUtils.findNamedNode(root, "btnRecord_001-arUV-symbol");
                towerRed = ScenegraphUtils.findNamedNode(root, "ID14-Material2");
                towerBlue = ScenegraphUtils.findNamedNode(root, "ID4-Material2");
                System.out.println("towerRed = " + towerRed.getName());
                printTree(root, 2);
                return modr;
            } catch (MalformedURLException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (IOException e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
        return super.createCellRenderer(rendererType);
    }
}
