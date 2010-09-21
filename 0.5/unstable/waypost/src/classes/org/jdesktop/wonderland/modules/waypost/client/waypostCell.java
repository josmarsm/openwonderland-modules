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
package org.jdesktop.wonderland.modules.waypost.client;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.client.jme.cellrenderer.ModelRenderer;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.waypost.common.waypostCellClientState;

public class waypostCell extends Cell
    {
    Spatial recordSpatialOnUV = null;
    Spatial towerRed = null;
    Spatial towerBlue = null;

    private String modelURI = null;

    public waypostCell(CellID cellID, CellCache cellCache)
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
        this.modelURI = ((waypostCellClientState)state).getModelURI();
        }

    @Override
    public void setStatus(CellStatus status, boolean increasing)
        {
        super.setStatus(status, increasing);

        if (status == CellStatus.INACTIVE && increasing == false)
            {
            }
        else if (status == CellStatus.RENDERING && increasing == true)
            {
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
                modr.setCollisionEnabled(false);
                Entity mye = modr.getEntity();
                RenderComponent rc = (RenderComponent)mye.getComponent(RenderComponent.class);
                Node root = rc.getSceneRoot();
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
