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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
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
import org.jdesktop.wonderland.modules.satellite.common.SatelliteCellClientState;

public class SatelliteCell extends Cell {

    private String modelURI = null;

    public SatelliteCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        this.modelURI = ((SatelliteCellClientState)state).getModelURI();
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
                return new ModelRenderer(this, dm);
            } catch (MalformedURLException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (IOException e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
        return super.createCellRenderer(rendererType);
    }
}
