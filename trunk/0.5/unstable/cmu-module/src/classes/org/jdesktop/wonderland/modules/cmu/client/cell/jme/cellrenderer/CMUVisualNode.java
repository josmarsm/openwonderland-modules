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

package org.jdesktop.wonderland.modules.cmu.client.cell.jme.cellrenderer;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.math.Matrix3f;
import com.jme.math.Triangle;
import com.jme.scene.Spatial;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.util.TextureManager;
import com.jme.util.geom.BufferUtils;
import edu.cmu.cs.dennisc.math.Matrix3x3;
import edu.cmu.cs.dennisc.math.OrthogonalMatrix3x3;
import edu.cmu.cs.dennisc.math.Point3;
import edu.cmu.cs.dennisc.scenegraph.Appearance;
import edu.cmu.cs.dennisc.scenegraph.Composite;
import edu.cmu.cs.dennisc.scenegraph.Geometry;
import edu.cmu.cs.dennisc.scenegraph.Vertex;
import edu.cmu.cs.dennisc.scenegraph.Visual;
import edu.cmu.cs.dennisc.scenegraph.event.AbsoluteTransformationEvent;
import edu.cmu.cs.dennisc.scenegraph.event.AbsoluteTransformationListener;
import edu.cmu.cs.dennisc.texture.BufferedImageTexture;
import edu.cmu.cs.dennisc.texture.TextureCoordinate2f;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.jme.ClientContextJME;

/**
 * Treats a CMU Visual instance as a jME node, listens for transformation changes.
 * @author kevin
 */
public class CMUVisualNode extends com.jme.scene.Node implements AbsoluteTransformationListener {

    public static final String meshChildName = "trimesh";
    private Visual cmuVisual = null;
    protected Collection<TriMesh> meshes = new ArrayList<TriMesh>();

    // Scale offset factor for MT Game renderer.
    final static private float wonderlandScale = 1f;

    /**
     * Constructor; use the default Node constructor, and attach this node
     * to a CMU Visual.
     * @param v The CMU visual to attach
     */
    public CMUVisualNode(Visual v) {
        super();

        setVisual(v);
    }

    /**
     * Get the associated CMU visual.
     * @return The associated CMU visual
     */
    public Visual getVisual() {
        return cmuVisual;
    }

    /**
     * Get the meshes associated with this node.
     * @return The TriMeshes associated with this node.
     */
    protected Collection<TriMesh> getMeshes() {
        return meshes;
    }

    /**
     * Callback function when the CMU node is updated.
     * @param e {@inheritDoc}
     */
    @Override
    public void absoluteTransformationChanged(AbsoluteTransformationEvent e) {
        updateTransformation();
    }

    /**
     * Get the current CMU node translation, and load it into this node.
     */
    private void updateTransformation() {
        // Get scene graph and mesh.

        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {

            public void update(Object arg0) {
                Composite sg = cmuVisual.getRoot();
                for (Spatial mesh : CMUVisualNode.this.getChildren()) {

                    // Translation, rotation, scaling.
                    Point3 translation = cmuVisual.getTranslation(cmuVisual.getRoot());
                    OrthogonalMatrix3x3 rotation = new OrthogonalMatrix3x3(cmuVisual.getTransformation(sg).orientation);
                    Matrix3x3 scale = cmuVisual.scale.getCopy(cmuVisual);

                    // Apply transformations.
                    mesh.setLocalScale((float) scale.right.x * wonderlandScale);
                    mesh.setLocalTranslation((float) translation.x * wonderlandScale, (float) translation.y * wonderlandScale, (float) translation.z * wonderlandScale);
                    mesh.setLocalRotation(new Matrix3f((float) rotation.right.x, (float) rotation.up.x, (float) rotation.backward.x,
                            (float) rotation.right.y, (float) rotation.up.y, (float) rotation.backward.y,
                            (float) rotation.right.z, (float) rotation.up.z, (float) rotation.backward.z));
                }

            }
        }, null);
        ClientContextJME.getWorldManager().addToUpdateList(this);
    }

    /**
     * Set the CMU visual to mirror, and load its visual properties.
     * @param v The CMU visual to mirror
     */
    public void setVisual(Visual v) {
        assert v != null;
        this.setName(v.getName());
        cmuVisual = v;
        for (Geometry g : v.geometries.getValue()) {
            addGeometry(g);
        }

        Appearance app = v.frontFacingAppearance.getValue();
        //for (Property p : app.getProperties()) {
        //    System.out.println("APPEARANCE PROPERTY: " + p);
        //    System.out.println(p.getValue(app));
        //}

        // Set texture properties.
        edu.cmu.cs.dennisc.texture.Texture cmuText = (edu.cmu.cs.dennisc.texture.Texture) (app.getPropertyNamed("bumpTexture").getValue(app));
        if (cmuText == null) {
            cmuText = (edu.cmu.cs.dennisc.texture.Texture) (app.getPropertyNamed("diffuseColorTexture").getValue(app));
        }
        if (cmuText != null && BufferedImageTexture.class.isAssignableFrom(cmuText.getClass())) {
            BufferedImage image = ((BufferedImageTexture) cmuText).getBufferedImage();

            TextureState ts = (TextureState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.StateType.Texture);
            Texture t = null;
            t = TextureManager.loadTexture(image, Texture.MinificationFilter.Trilinear, Texture.MagnificationFilter.Bilinear, false);
            t.setWrap(Texture.WrapMode.Repeat);
            ts.setTexture(t);
            ts.setEnabled(true);
            this.setRenderState(ts);
        }

        updateTransformation();
        cmuVisual.addAbsoluteTransformationListener(this);
    }

    /**
     * Extract the TriMesh for a given CMU geometry, and add it to this node.
     * @param g The CMU geometry to parse
     */
    private void addGeometry(Geometry g) {
        // Get vertex data.
        Vertex[] vertices = (Vertex[]) g.getPropertyNamed("vertices").getValue(g);
        int[] indices = (int[]) g.getPropertyNamed("polygonData").getValue(g);

        final int perVertex = 3;
        final int perTexCoord = 2;

        float[] fVertices = new float[vertices.length * perVertex];
        float[] fTexCoords = new float[vertices.length * perTexCoord];
        for (int i = 0; i < vertices.length; i++) {
            Point3 p = vertices[i].position;
            fVertices[i * perVertex + 0] = (float) p.x;
            fVertices[i * perVertex + 1] = (float) p.y;
            fVertices[i * perVertex + 2] = (float) p.z;

            TextureCoordinate2f t = vertices[i].textureCoordinate0;
            fTexCoords[i * perTexCoord + 0] = t.u;
            fTexCoords[i * perTexCoord + 1] = t.v;
        }

        TriMesh mesh = new TriMesh(meshChildName + this.getMeshes().size());
        this.meshes.add(mesh);
        this.attachChild(mesh);

        // Place vertices.
        FloatBuffer fVertexBuf = BufferUtils.createFloatBuffer(fVertices);
        FloatBuffer fTexBuf = BufferUtils.createFloatBuffer(fTexCoords);
        mesh.reconstruct(fVertexBuf, null, null, new TexCoords(fTexBuf, perTexCoord), IntBuffer.wrap(indices));

        // Bounding box and translation.
        Triangle[] tris = new Triangle[mesh.getTriangleCount()];

        BoundingBox bbox = new BoundingBox();
        bbox.computeFromTris(mesh.getMeshAsTriangles(tris), 0, tris.length);

        this.setLocalTranslation(0, 0, 0);
        this.setModelBound(bbox);
    }
}
