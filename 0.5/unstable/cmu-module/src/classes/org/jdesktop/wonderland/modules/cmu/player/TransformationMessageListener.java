/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.cmu.player;

import org.jdesktop.wonderland.modules.cmu.client.cell.TransformationMessage;

/**
 *
 * @author kevin
 */
public interface TransformationMessageListener {
    public void transformationMessageChanged(TransformationMessage message);
}
