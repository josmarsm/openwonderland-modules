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
    public String getDescription();
    public String getFunctionName();
    public String getCategory();
    public void setArguments(Object[] args);
    public Object returns();

}
