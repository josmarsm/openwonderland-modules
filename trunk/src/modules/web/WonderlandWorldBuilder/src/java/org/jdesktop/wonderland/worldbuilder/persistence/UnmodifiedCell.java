/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.worldbuilder.persistence;

import org.jdesktop.wonderland.worldbuilder.Cell;

/**
 * When updating a cell tree, any cells that have not been modified
 * are replaced with instances of unmodified cell.  Only the 
 * <code>getCellID()</code> and <code>getParent()</code> methods of an 
 * unmodified cell are valid.  The result of calling other methods are 
 * undefined.
 * @author jkaplan
 */
public class UnmodifiedCell extends Cell {
    public UnmodifiedCell(String cellID) {
        super (cellID);
    }
}
