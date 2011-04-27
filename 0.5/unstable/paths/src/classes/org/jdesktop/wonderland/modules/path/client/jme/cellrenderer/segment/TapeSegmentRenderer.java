package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.util.geom.BufferUtils;
import java.nio.IntBuffer;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
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
        public PathSegmentRenderer createRenderer(ClientNodePath path, int segmentIndex, int startNodeIndex, int endNodeIndex) throws IllegalArgumentException {
            return new TapeSegmentRenderer(path, segmentIndex, startNodeIndex, endNodeIndex);
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
     * @param segmentNodePath The NodePath to which the path segment to be rendered belongs.
     * @param segmentIndex The index of the path segment to be rendered.
     * @param startNodeIndex The index of the PathNode at which the path segment to be rendered begins.
     * @param endNodeIndex The index of the PathNode at which the path segment ends.
     */
    public TapeSegmentRenderer(ClientNodePath segmentNodePath, int segmentIndex, int startNodeIndex, int endNodeIndex) {
        super(segmentNodePath, segmentIndex, startNodeIndex, endNodeIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node createSceneGraph(Entity entity) {
        setEntity(entity);
        if (rootNode == null && segmentNodePath != null && startNodeIndex >= 0 && endNodeIndex >= 0) {
            final int noOfNodes = segmentNodePath.noOfNodes();
            if (startNodeIndex < noOfNodes && endNodeIndex < noOfNodes) {
                ClientPathNode startNode = segmentNodePath.getPathNode(startNodeIndex);
                ClientPathNode endNode = segmentNodePath.getPathNode(endNodeIndex);
                rootNode = new Node("Tape");
                PathStyle pathStyle = segmentNodePath.getPathStyle();
                StyleMetaDataAdapter adapter = new StyleMetaDataAdapter(pathStyle != null && segmentIndex >= 0 ? pathStyle.getSegmentStyle(segmentIndex, true) : null);
                float height = adapter.getHeight(0.05f);
                float offset = adapter.getYOffset(1.0f);
                float textureRepeatsPerMeter = adapter.getUTextRepeatsPerM(1.0f);
                Vector3f startPoint = startNode.getPosition();
                Vector3f endPoint = endNode.getPosition();
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
                TriMesh tapeMesh = new TriMesh(rootNode.getName(), BufferUtils.createFloatBuffer(vertices), BufferUtils.createFloatBuffer(normals), null, TexCoords.makeNew(textureVerices), IntBuffer.wrap(indices) );
                rootNode.attachChild(tapeMesh);
            }
        }
        return rootNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyleType getRenderedType() {
        return CoreSegmentStyleType.TAPE;
    }
}
