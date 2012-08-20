/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.cell;

import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.ezscript.client.cell.SmallCell;

/**
 *
 * @author Ryan
 */
public class ScriptedCell extends SmallCell {

    
    public ScriptedCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    @Override
    protected void rendering(boolean increasing) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void active(boolean increasing) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void inactive(boolean increasing) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void disk(boolean increasing) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
