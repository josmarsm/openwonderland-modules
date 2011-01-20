/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.SPI;

/**
 *
 * @author JagWire
 */
public interface FarCellEventSPI extends Runnable {
    public String getCellClassName();
    public String getEventName();
    public void setArguments(Object[] arguments);

}
