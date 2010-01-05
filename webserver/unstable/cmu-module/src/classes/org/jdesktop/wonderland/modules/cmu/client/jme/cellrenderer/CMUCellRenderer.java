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

package org.jdesktop.wonderland.modules.cmu.client.jme.cellrenderer;

import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.cmu.client.CMUCell;

/**
 * Very simple renderer; actual rendering updates happen from within
 * the nodes themselves, so this just fetches the scene graph root
 * from the cell and lets updates happen on their own.
 * @author kevin
 */
public class CMUCellRenderer extends BasicRenderer {

    /**
     * Standard constructor.
     * @param cell The associated CMU cell.
     */
    public CMUCellRenderer(Cell cell) {
        super(cell);
    }

    /**
     * Get the collection of JME nodes from the associated CMU cell, and
     * use it to populate the scene graph.
     * @param entity {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected Node createSceneGraph(Entity entity) {
        assert this.getCell().getSceneRoot() != null;
        return this.getCell().getSceneRoot();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CMUCell getCell() {
        return (CMUCell)(super.getCell());
    }
}
