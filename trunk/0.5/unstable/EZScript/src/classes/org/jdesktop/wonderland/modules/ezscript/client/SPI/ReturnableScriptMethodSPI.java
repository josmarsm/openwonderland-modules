/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.SPI;

/**
 *
 * @author JagWire
 */
public interface ReturnableScriptMethodSPI extends Runnable {
    public String getFunctionName();
    public void setArguments(Object[] args);
    public Object returns();
}
