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

    private Cell cell;
    private boolean collidable;
    private BasicRenderer renderer;
    private boolean fail = false;
    public String getFunctionName() {
        return "SetCollidable";
    }

    public void setArguments(Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
        cell = (Cell)args[0];
        collidable = ((Boolean)args[1]).booleanValue();

        renderer = (BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
    }

    public String getDescription() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return "Toggle the the collision of a cell.\n" +
                "-- usage: SetCollidable(cell, true_or_false);";
    }

    public String getCategory() {
        return "cell";
    }

    public void run() {
        //throw new UnsupportedOperationException("Not supported yet.");
        renderer.setCollisionEnabled(collidable);
    }
}
