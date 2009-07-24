/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.cmu.common.cell.player;

import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;
import edu.cmu.cs.dennisc.math.OrthogonalMatrix3x3;
import edu.cmu.cs.dennisc.math.Point3;
import java.io.Serializable;
import org.jdesktop.wonderland.modules.cmu.client.cell.jme.cellrenderer.VisualNode;

/**
 *
 * @author kevin
 */
public class TransformationMessage implements Serializable {

    private int nodeID;
    private float scale = 1;
    private Vector3f translation = null;
    private Matrix3f rotation = null;

    public TransformationMessage() {
        
    }

    public TransformationMessage(int nodeID) {
        this();
        this.setNodeID(nodeID);
    }

    public synchronized void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public synchronized int getNodeID() {
        return this.nodeID;
    }

    public synchronized void setScale(float scale) {
        this.scale = scale;
    }

    public synchronized float getScale() {
        return this.scale;
    }

    public synchronized void setTranslation(Point3 translation) {
        this.translation = new Vector3f(
                (float) translation.x,
                (float) translation.y,
                (float) translation.z);
    }

    public synchronized Vector3f getTranslation() {
        return this.translation;
    }

    public synchronized void setRotation(OrthogonalMatrix3x3 rotation) {
        this.rotation = new Matrix3f(
                (float) rotation.right.x, (float) rotation.up.x, (float) rotation.backward.x,
                (float) rotation.right.y, (float) rotation.up.y, (float) rotation.backward.y,
                (float) rotation.right.z, (float) rotation.up.z, (float) rotation.backward.z);
    }

    public synchronized Matrix3f getRotation() {
        return this.rotation;
    }

    public synchronized void applyToMatchingNode(VisualNode node) {
        if (this.getNodeID() == node.getNodeID()) {
            this.applyToNode(node);
        }
    }
    public synchronized void applyToNode(VisualNode node) {
        for (Spatial mesh : node.getChildren()) {
            this.applyToMesh(mesh);
        }
    }

    public synchronized void applyToMesh(Spatial mesh) {
        mesh.setLocalScale(this.scale);
        if (this.getTranslation() != null) 
            mesh.setLocalTranslation(this.getTranslation());
        if (this.getRotation() != null)
            mesh.setLocalRotation(this.getRotation());
    }
}
