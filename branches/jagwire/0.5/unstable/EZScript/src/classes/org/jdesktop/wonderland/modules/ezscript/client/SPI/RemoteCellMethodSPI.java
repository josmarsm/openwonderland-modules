/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.SPI;

/**
 * Classes annotated with @RemoteCellMethod should implement this class.
 * This interface represents cell code that can be executed from another cell.
 * 
 * @author JagWire
 */
public interface RemoteCellMethodSPI extends Runnable {
    public Class[] getCellClassesToConsume();
    public String getTriggerName();
    public void setArguments(Object[] args);
}
