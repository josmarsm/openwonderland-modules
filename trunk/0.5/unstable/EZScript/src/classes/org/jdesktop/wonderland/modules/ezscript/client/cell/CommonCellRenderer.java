
package org.jdesktop.wonderland.modules.ezscript.client.cell;

import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;

/**
 *
 * @author JagWire
 */
public class CommonCellRenderer extends BasicRenderer {

    public CommonCellRenderer(Cell cell) {
        super(cell);
    }

    @Override
    protected Node createSceneGraph(Entity entity) {
        return new Node();
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
