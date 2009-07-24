/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.cmu.common.cell.player;

import com.jme.image.Texture;
import com.jme.scene.Node;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.jme.util.geom.BufferUtils;
import edu.cmu.cs.dennisc.math.OrthogonalMatrix3x3;
import edu.cmu.cs.dennisc.math.Point3;
import edu.cmu.cs.dennisc.scenegraph.Geometry;
import edu.cmu.cs.dennisc.scenegraph.Vertex;
import edu.cmu.cs.dennisc.texture.TextureCoordinate2f;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.Serializable;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.cmu.client.cell.jme.cellrenderer.VisualNode;

/**
 * Serializable information about a CMU visual; stores geometry and texture
 * information, as well as transformation information.  Associated with a
 * unique node ID.
 * @author kevin
 */
public class VisualMessage implements Serializable {

    private Vector<TriMesh> meshes = new Vector<TriMesh>();
    private final TransformationMessage transformation;
    private int[] texturePixels;
    private int textureWidth,  textureHeight;

    public VisualMessage() {
        transformation = new TransformationMessage();
    }

    public VisualMessage(int nodeID) {
        transformation = new TransformationMessage(nodeID);
    }

    public int getNodeID() {
        return this.transformation.getNodeID();
    }

    public void setNodeID(int nodeID) {
        this.transformation.setNodeID(nodeID);
    }

    public TransformationMessage getTransformation() {
        return this.transformation;
    }

    /**
     * Generate a jME node corresponding to this visual.
     * @return The generated node
     */
    public VisualNode createNode() {
        VisualNode node = new VisualNode(this.getNodeID());

        // Apply geometries.
        for (TriMesh mesh : this.meshes) {
            node.attachChild(mesh);
        }

        // Apply texture.
        applyTextureToNode(node);

        // Apply transformation.
        this.getTransformation().applyToNode(node);

        return node;
    }

    /**
     * Extract the texture image for this message, and wrap it around the given node.
     * @param node The node to receive the texture
     */
    protected void applyTextureToNode(Node node) {
        TextureState ts = null;//(TextureState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.StateType.Texture);
        Texture t = null;
        t = TextureManager.loadTexture(this.getTexture(), Texture.MinificationFilter.Trilinear, Texture.MagnificationFilter.Bilinear, false);
        t.setWrap(Texture.WrapMode.Repeat);
        ts.setTexture(t);
        ts.setEnabled(true);
        node.setRenderState(ts);
    }

    /**
     * Extract the TriMesh for a given CMU geometry, and add it to
     * the collection of meshes.
     * @param g The CMU geometry to parse
     */
    public void addGeometry(Geometry g) {
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

        // Place vertices.
        TriMesh mesh = new TriMesh();

        FloatBuffer fVertexBuf = BufferUtils.createFloatBuffer(fVertices);
        FloatBuffer fTexBuf = BufferUtils.createFloatBuffer(fTexCoords);
        mesh.reconstruct(fVertexBuf, null, null, new TexCoords(fTexBuf, perTexCoord), IntBuffer.wrap(indices));

        // Store mesh.
        this.meshes.add(mesh);
    }

    public void setTexture(Image texture, int width, int height) {
        texturePixels = new int[width * height];
        textureWidth = width;
        textureHeight = height;

        PixelGrabber pg = new PixelGrabber(texture, 0, 0, width, height, texturePixels, 0, width);

        try {
            pg.grabPixels();
        } catch (InterruptedException ex) {
            Logger.getLogger(VisualMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Image getTexture() {
        MemoryImageSource mis = new MemoryImageSource(textureWidth, textureHeight,
                texturePixels, 0, textureWidth);
        Toolkit tk = Toolkit.getDefaultToolkit();
        return tk.createImage(mis);
    }

    public void setScale(float scale) {
        this.transformation.setScale(scale);
    }

    public void setTranslation(Point3 translation) {
        this.transformation.setTranslation(translation);
    }

    public void setRotation(OrthogonalMatrix3x3 rotation) {
        this.transformation.setRotation(rotation);
    }
}