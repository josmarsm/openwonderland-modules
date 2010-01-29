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
package org.jdesktop.wonderland.modules.connectionsample.client;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;

/**
 * Client-side cell for displaying changes made by the custom connection.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ColoredCubeCell extends Cell {
    /**
     * Declare a dependency on the shared state component. This component
     * will be automatically injected.
     */
    @UsesCellComponent
    private SharedStateComponent state;

    /** The cell renderer */
    private ColoredCubeRenderer renderer;

    /**
     * Create a new ColoredCubeCell
     * @param cellID the ID of the cell to create
     * @param cellCache the cell cache to create the cell in
     */
    public ColoredCubeCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * When the cell status becomes BOUNDS, the SharedStateComponent is
     * populated.  Set the shared state of the cell renderer at that point.
     * @param status the status of the cell
     */
    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if (status == CellStatus.RENDERING && increasing) {
            renderer.setSharedState(state);
        }
    }



    /**
     * Create the renderer to actually draw the cell.
     * @param rendererType the type of renderer to use.  Currently only
     * JME is supported.
     * @return the newly created renderer.
     */
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            renderer = new ColoredCubeRenderer(this);
            return renderer;
        }

        return super.createCellRenderer(rendererType);
    }
}
