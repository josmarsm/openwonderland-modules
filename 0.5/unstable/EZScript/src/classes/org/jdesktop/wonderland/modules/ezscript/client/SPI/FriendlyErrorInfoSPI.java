/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.SPI;

import java.util.List;

/**
 *
 * @author JagWire
 */
public interface FriendlyErrorInfoSPI {

    public String getSummary();

    public List<String> getSolutions();
}
