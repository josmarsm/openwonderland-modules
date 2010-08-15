/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.viewcachetest.client;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class MoverCell extends Cell {
    public MoverCell(CellID cellID, CellCache cellCache) {
        super (cellID, cellCache);
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        return new MoverCellRenderer(this);
    }

    class MoverCellRenderer extends BasicRenderer {
        public MoverCellRenderer(Cell cell) {
            super (cell);
        }

        @Override
        protected Node createSceneGraph(Entity entity) {
            Node n = new Node();
            Box b = new Box("Mover", Vector3f.ZERO, 1f, 1f, 1f);
            n.attachChild(b);
            return n;
        }
    }

}
