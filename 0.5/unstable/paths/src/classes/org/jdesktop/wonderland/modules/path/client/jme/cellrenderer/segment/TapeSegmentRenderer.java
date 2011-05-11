package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.util.geom.BufferUtils;
import java.nio.IntBuffer;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;
import org.jdesktop.wonderland.modules.path.common.style.StyleMetaDataAdapter;
import org.jdesktop.wonderland.modules.path.common.style.segment.CoreSegmentStyleType;
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
            StyleMetaDataAdapter adapter = new StyleMetaDataAdapter(pathStyle != null ? pathStyle.getSegmentStyle(startNode.getSequenceIndex(), true) : null);
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
        }
        return tapeSegmentNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyleType getRenderedType() {
        return CoreSegmentStyleType.TAPE;
    }
}
