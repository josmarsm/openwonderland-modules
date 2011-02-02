package org.jdesktop.wonderland.modules.ezscript.client.SPI;

/**
 *
 * @author JagWire
 */
public interface ScriptMethodSPI extends Runnable {
    
    public String getFunctionName();
    public void setArguments(Object[] args);
    public String getDescription();
    public String getCategory();
}
