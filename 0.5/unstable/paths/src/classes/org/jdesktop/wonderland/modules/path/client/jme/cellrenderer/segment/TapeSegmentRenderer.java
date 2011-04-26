package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.util.geom.BufferUtils;
import java.nio.IntBuffer;
import org.jdesktop.wonderland.modules.path.common.PathNode;
import org.jdesktop.wonderland.modules.path.common.style.StyleMetaDataAdapter;
import org.jdesktop.wonderland.modules.path.common.style.segment.CoreSegmentStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This PathSegmentRenderer is used to render tape such as hazard tape or queue management or cordon tape.
 *
 * @author Carl Jokl
 */
public class TapeSegmentRenderer implements PathSegmentRenderer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Node render(SegmentStyle style, PathNode startNode, PathNode endNode) {
        Node tapeNode = new Node("Tape");
        StyleMetaDataAdapter adapter = new StyleMetaDataAdapter(style);
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
        TriMesh tapeMesh = new TriMesh(tapeNode.getName(), BufferUtils.createFloatBuffer(vertices), BufferUtils.createFloatBuffer(normals), null, TexCoords.makeNew(textureVerices), IntBuffer.wrap(indices) );
        tapeNode.attachChild(tapeMesh);
        return tapeNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyleType getRenderedType() {
        return CoreSegmentStyleType.TAPE;
    }

}
