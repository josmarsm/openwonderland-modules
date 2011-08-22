/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.ScriptManager;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class RegisterCellMethod implements ScriptMethodSPI {

    private Cell cell = null;
    private String name = "";
    
    public String getFunctionName() {
        return "RegisterCell";
    }

    public void setArguments(Object[] args) {
       cell = (Cell)args[0];
       name = (String)args[1];
    }

    public String getDescription() {
        return "Registers a cell with the EZScript infrastructure.\n"
                + "-- usage: RegisterCell(cell, \"cell-name\");";
    }

    public String getCategory() {
        return "utilities";
    }

    public void run() {
        cell.setName(name);
        ScriptManager.getInstance().addCell(cell);
    }
    
}
