/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class SetCollidableMethod implements ScriptMethodSPI {

    public String getFunctionName() {
        return "SetCollidable";
    }

    public void setArguments(Object[] args) {
        new SetCollidableInvoker((Cell)args[0],
                                ((Boolean) args[1]).booleanValue()).invoke();
    }

    public String getDescription() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return "Toggle the the collision of a cell.\n"
                + "-- usage: SetCollidable(cell, true_or_false);";
    }

    public String getCategory() {
        return "Cells";
    }

    public void run() {
    }

    private static final class SetCollidableInvoker {

        private Cell cell;
        private boolean collidable;

        public SetCollidableInvoker(Cell cell, boolean collidable) {
            this.cell = cell;
            this.collidable = collidable;
        }

        public void invoke() {
            BasicRenderer renderer = (BasicRenderer) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
            renderer.setCollisionEnabled(collidable);
        }
    }
}
