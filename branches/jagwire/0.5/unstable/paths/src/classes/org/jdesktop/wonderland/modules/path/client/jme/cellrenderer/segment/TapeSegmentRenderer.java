package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import com.jme.image.Texture;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.geom.BufferUtils;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.IntBuffer;
import java.util.logging.Level;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;
import org.jdesktop.wonderland.modules.path.common.style.StyleMetaDataAdapter;
import org.jdesktop.wonderland.modules.path.common.style.segment.CoreSegmentStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This PathSegmentRenderer is used to render tape such as hazard tape or queue management or cordon tape.
 *
 * @author Carl Jokl
 */
public class TapeSegmentRenderer extends AbstractPathSegmentRenderer {

    /**
     * Simple factory class used to create instances of a TapeSegmentRenderer.
     */
    public static class TapeSegmentRendererFactory implements PathSegmentRendererFactory {

        /**
         * {@inheritDoc}
         */
        @Override
        public PathSegmentRenderer createRenderer(ClientPathNode startNode) throws IllegalArgumentException {
            return new TapeSegmentRenderer(startNode);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SegmentStyleType getRenderedSegmentStyleType() {
            return CoreSegmentStyleType.TAPE;
        }
        
    }

    /**
     * Create a new instance of TapeSegmentRenderer to render the path segment with the specified attributes.
     *
     * @param startNode The ClientPathNode to which the path segment belongs which is to be rendered.
     * @throws IllegalArgumentException If the specified start ClientPathNode was null.
     */
    public TapeSegmentRenderer(ClientPathNode startNode) {
        super(startNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node createSceneGraph(Entity entity) {
        Node tapeSegmentNode = new Node(entity.getName());
        if (startNode != null && startNode.hasNext()) {
            tapeSegmentNode = new Node("Tape");
            PathStyle pathStyle = startNode.getPath().getPathStyle();
            SegmentStyle style = pathStyle != null ? pathStyle.getSegmentStyle(startNode.getSequenceIndex(), true) : null;
            StyleMetaDataAdapter adapter = new StyleMetaDataAdapter(style);
            float height = adapter.getHeight(0.05f);
            float offset = adapter.getYOffset(1.0f);
            float textureRepeatsPerMeter = adapter.getUTextRepeatsPerM(1.0f);
            Vector3f startPoint = startNode.getPosition();
            Vector3f endPoint = startNode.getNext().getPosition();
            float startX = startPoint.getX();
            float startLY = startPoint.getY() + offset;
            float startUY = startLY + height;
            float startZ = startPoint.getZ();
            float endX = endPoint.getX();
            float endLY = endPoint.getY() + offset;
            float endUY = endLY + height;
            float endZ = endPoint.getZ();
            Vector3f[] vertices = new Vector3f[] {
                new Vector3f(startX, startLY, startZ),
                new Vector3f(startX, startUY, startZ),
                new Vector3f(endX, endLY, endZ),
                new Vector3f(endX, endUY, endZ),
                new Vector3f(endX, endLY, endZ),
                new Vector3f(endX, endUY, endZ),
                new Vector3f(startX, startLY, startZ),
                new Vector3f(startX, startUY, startZ)
            };
            Vector3f upVector = new Vector3f(0.0f, 1.0f, 0.0f);
            Vector3f frontEdge = new Vector3f(startX - endX, startLY - endLY, startZ - endZ);
            Vector3f frontNormal = frontEdge.cross(upVector).normalizeLocal();
            Vector3f backNormal = frontNormal.negate();
            Vector3f[] normals = new Vector3f[] {
                frontNormal, frontNormal, frontNormal, frontNormal,
                backNormal, backNormal, backNormal, backNormal
            };
            float endTexU = frontEdge.length() / textureRepeatsPerMeter;
            Vector2f[] textureVerices = new Vector2f[] {
                new Vector2f(0.0f, 0.0f),
                new Vector2f(0.0f, 1.0f),
                new Vector2f(endTexU, 0.0f),
                new Vector2f(endTexU, 1.0f),
                new Vector2f(0.0f, 0.0f),
                new Vector2f(0.0f, 1.0f),
                new Vector2f(endTexU, 0.0f),
                new Vector2f(endTexU, 1.0f)
            };
            int[] indices = new int[] { 0, 2, 1, 1, 2, 3, 4, 6, 5, 5, 6, 7 };
            TriMesh tapeMesh = new TriMesh(tapeSegmentNode.getName(), BufferUtils.createFloatBuffer(vertices), BufferUtils.createFloatBuffer(normals), null, TexCoords.makeNew(textureVerices), IntBuffer.wrap(indices) );
            tapeSegmentNode.attachChild(tapeMesh);
            initMaterial(tapeSegmentNode, style);
        }
        return tapeSegmentNode;
    }

    /**
     * Initialize the material of the edit mode SegmentNode representation.
     *
     * @param node The node to which to apply the material.
     * @param style The SegmentStyle from which to get any material styling hints.
     */
    protected void initMaterial(Node node, SegmentStyle style) {
        StyleMetaDataAdapter adapter = new StyleMetaDataAdapter(style);
        Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();
        MaterialState segmentMaterial = renderer.createMaterialState();
        segmentMaterial.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);
        float[] rgbaValues = adapter.getMainColor(0.25f, 0.15f, 0.9f, 1.0f);
        ColorRGBA tapeColor = new ColorRGBA(rgbaValues[0], rgbaValues[1], rgbaValues[2], rgbaValues[3]);
        segmentMaterial.setAmbient(tapeColor);
        segmentMaterial.setDiffuse(tapeColor);
        segmentMaterial.setSpecular(new ColorRGBA(tapeColor.r * 0.75f, tapeColor.g * 0.75f, tapeColor.b * 0.75f, 1.0f));
        segmentMaterial.setShininess(0.6f);
        URI textureURI = adapter.getMainTexture(null);
        if (textureURI != null) {
            try {
                Texture texture = TextureManager.loadTexture(textureURI.toURL());
                texture.setWrap(Texture.WrapMode.Repeat);
                TextureState textureState = renderer.createTextureState();
                textureState.setTexture(texture);
                node.setRenderState(textureState);
            }
            catch (MalformedURLException murle) {
                logger.log(Level.WARNING, "Failed to load tape texture from URL!", murle);
            }
        }
        node.setRenderState(segmentMaterial);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyleType getRenderedType() {
        return CoreSegmentStyleType.TAPE;
    }
}
