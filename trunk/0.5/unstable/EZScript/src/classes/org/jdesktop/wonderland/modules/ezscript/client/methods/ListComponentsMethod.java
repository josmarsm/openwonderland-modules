/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import java.util.ArrayList;
import java.util.List;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class ListComponentsMethod implements ReturnableScriptMethodSPI {

    private Cell cell;
    private List<String> components = new ArrayList<String>();
    public String getDescription() {
        return "Return a list<String> of all the components current attached to the cell.\n"
                + "-- usage: ListComponents(cell)\n"
                + "-- great candidate to pass as an argument to the show() command";
    }

    public String getFunctionName() {
//        throw new UnsupportedOperationException("Not supported yet.")
        return "ListComponents";
    }

    public String getCategory() {
//        throw new UnsupportedOpe/rationException("Not supported yet.");
        return "utilities";
    }

    public void setArguments(Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
        components = new ArrayList<String>();
        cell = (Cell)args[0];
    }

    public Object returns() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return components;
    }

    public void run() {
        //throw new UnsupportedOperationException("Not supported yet.");
        for(CellComponent c: cell.getComponents()) {
            String name = c.getClass().getName();
            name = name.replaceFirst("org.jdesktop.wonderland.", "");
            components.add(name);
        }
        //components.addAll(cell.getComponents());
    }
    
}
