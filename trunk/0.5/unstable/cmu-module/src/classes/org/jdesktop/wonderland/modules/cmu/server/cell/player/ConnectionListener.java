/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.cmu.server.cell.player;

import java.io.OutputStream;

/**
 *
 * @author kevin
 */
public interface ConnectionListener {
    public void connectionAdded(OutputStream connection);
}
