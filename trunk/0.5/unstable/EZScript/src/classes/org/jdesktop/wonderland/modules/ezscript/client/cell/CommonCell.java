

package org.jdesktop.wonderland.modules.ezscript.client.cell;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.ezscript.client.EZScriptComponent;

/**
 * Cell to be created dynamically through a "Create Cell" script.
 *
 * Futher, the cell is meant to be used as a grouping node.
 * 
 * @author JagWire
 */
public class CommonCell extends Cell {

    @UsesCellComponent
    EZScriptComponent scriptComponent;
    
    private CommonCellRenderer renderer = null;
    public CommonCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new CommonCellRenderer(this);
            return this.renderer;
        } else {
            return super.createCellRenderer(rendererType);
        }

    }

    @Override
    public void setClientState(CellClientState configData) {
        super.setClientState(configData);
    }

    

}
