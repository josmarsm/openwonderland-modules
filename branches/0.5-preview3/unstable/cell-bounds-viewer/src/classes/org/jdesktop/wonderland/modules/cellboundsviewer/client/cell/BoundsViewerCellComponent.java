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
package org.jdesktop.wonderland.modules.cellboundsviewer.client.cell;

import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.cellboundsviewer.client.cell.jme.BoundsViewerEntity;

/**
 * A Cell Component that draws a wireframe object that shows the Cell bounds.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class BoundsViewerCellComponent extends CellComponent {

    // The bounds viewer entity
    private BoundsViewerEntity boundsViewerEntity = null;

    public BoundsViewerCellComponent(Cell cell) {
        super(cell);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        // If we are making the Cell Component active we want to add the
        // Entity to the scene graph.
        if (status == CellStatus.ACTIVE && increasing == true) {
            boundsViewerEntity = new BoundsViewerEntity(cell);
            boundsViewerEntity.setVisible(true);
            return;
        }

        // Otherwise if we are making the Cell Component inactive, we want
        // to remove the Entity from the scene graph.
        if (status == CellStatus.INACTIVE && increasing == false) {
            if (boundsViewerEntity != null) {
                boundsViewerEntity.dispose();
                boundsViewerEntity = null;
            }
            return;
        }
    }

    /**
     * Returns the scene root for the Cell's scene graph
     */
    protected Node getSceneGraphRoot() {
        CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
        Entity entity = renderer.getEntity();
        RenderComponent cellRC = (RenderComponent) entity.getComponent(RenderComponent.class);
        return cellRC.getSceneRoot();
    }
}
