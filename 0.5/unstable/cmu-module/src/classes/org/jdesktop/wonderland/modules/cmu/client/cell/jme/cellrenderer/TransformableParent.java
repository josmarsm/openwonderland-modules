/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.cmu.client.cell.jme.cellrenderer;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import org.jdesktop.wonderland.modules.cmu.client.cell.TransformationMessage;

/**
 *
 * @author kevin
 */
public class TransformableParent extends Node {

    public synchronized void applyTransformationToChild(TransformationMessage transformation) {
        for (Spatial child : this.getChildren()) {
            if (TransformableParent.class.isAssignableFrom(child.getClass())) {
                ((TransformableParent)child).applyTransformationToChild(transformation);
            }
        }
    }
}
