/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.cmu.client.jme.cellrenderer;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.math.Triangle;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import java.awt.Image;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.TransformationMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualMessage;

/**
 *
 * @author kevin
 */
public class VisualNode extends TransformableParent {

    protected final int nodeID;

    public VisualNode(int nodeID) {
        super();
        this.nodeID = nodeID;
    }

    public VisualNode(VisualMessage message) {
        this(message.getNodeID());

        // Apply geometries, compute bounding box
        BoundingBox bound = null;
        for (TriMesh mesh : message.getMeshes()) {
            this.attachChild(mesh);

            // Merge this bounding box with the cumulative bound
            Triangle[] tris = new Triangle[mesh.getTriangleCount()];

            BoundingBox currentBound = new BoundingBox();
            currentBound.computeFromTris(mesh.getMeshAsTriangles(tris), 0, tris.length);

            if (bound == null) {
                bound = currentBound;
            } else {
                bound.mergeLocal(currentBound);
            }
        }
        if (bound != null) {
            this.setModelBound(bound);
        }

        // Apply texture
        this.applyTexture(message.getTexture());

        // Apply transformation
        this.applyTransformation(message.getTransformation());
    }

    /**
     * Get the unique node ID for this visual.
     * @return This visual's node ID
     */
    public int getNodeID() {
        return this.nodeID;
    }

    @Override
    public synchronized void applyTransformationToChild(TransformationMessage transformation) {
        if (transformation.getNodeID() == this.getNodeID()) {
            this.applyTransformation(transformation);
        }
        super.applyTransformationToChild(transformation);
    }

    protected void applyTransformation(final TransformationMessage transformation) {

        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {

            public void update(Object arg0) {
                for (Spatial mesh : VisualNode.this.getChildren()) {
                    mesh.setLocalScale(transformation.getScale());
                    mesh.setLocalTranslation(transformation.getTranslation());
                    mesh.setLocalRotation(transformation.getRotation());
                }
            }
        }, null);
        ClientContextJME.getWorldManager().addToUpdateList(this);
    }

    protected void applyTexture(Image texture) {
        TextureState ts = (TextureState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.StateType.Texture);
        Texture t = null;
        t = TextureManager.loadTexture(texture, Texture.MinificationFilter.Trilinear, Texture.MagnificationFilter.Bilinear, false);
        t.setWrap(Texture.WrapMode.Repeat);
        ts.setTexture(t);
        ts.setEnabled(true);
        this.setRenderState(ts);

    }
}
