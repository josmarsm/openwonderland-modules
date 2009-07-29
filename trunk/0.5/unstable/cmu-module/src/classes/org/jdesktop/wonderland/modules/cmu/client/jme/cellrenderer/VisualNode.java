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

import com.jme.image.Texture;
import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import java.awt.Image;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.cmu.common.TransformationMessage;
import org.jdesktop.wonderland.modules.cmu.common.VisualMessage;

/**
 *
 * @author kevin
 */
public class VisualNode extends TransformableParent {

    protected final int nodeID;

    public VisualNode(int nodeID) {
        super();
        this.nodeID = nodeID;
        System.out.println("Created visual node with id: " + nodeID);
    }

    public VisualNode(VisualMessage message) {
        this(message.getNodeID());

        // Apply geometries.
        for (TriMesh mesh : message.getMeshes()) {
            this.attachChild(mesh);
        }

        // Apply texture.
        this.applyTexture(message.getTexture());

        // Apply transformation.
        this.applyTransformation(message.getTransformation());
    }

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

    protected void applyTransformation(TransformationMessage transformation) {
        final float scale = transformation.getScale();
        final Vector3f translation = transformation.getTranslation();
        final Matrix3f rotation = transformation.getRotation();

        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {

            public void update(Object arg0) {
                for (Spatial mesh : VisualNode.this.getChildren()) {
                    // Apply transformations.
                    mesh.setLocalScale(scale);
                    mesh.setLocalTranslation(translation);
                    mesh.setLocalRotation(rotation);
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
