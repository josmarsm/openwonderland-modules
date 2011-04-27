package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.common.style.segment.CoreSegmentStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This class represents a SegmentRenderer to be used to render path segments which have invisible style.
 *
 * @author Carl Jokl
 */
public class InvisibleSegmentRenderer extends AbstractPathSegmentRenderer {

    /**
     * Simple factory class used to create instances of an InvisibleSegmentRenderer.
     */
    public static class InvisibleSegmentRendererFactory implements PathSegmentRendererFactory {

        /**
         * {@inheritDoc}
         */
        @Override
        public PathSegmentRenderer createRenderer(ClientNodePath path, int segmentIndex, int startNodeIndex, int endNodeIndex) throws IllegalArgumentException {
            return new InvisibleSegmentRenderer(path, segmentIndex, startNodeIndex, endNodeIndex);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SegmentStyleType getRenderedSegmentStyleType() {
            return CoreSegmentStyleType.INVISIBLE;
        }

    }

    /**
     * Create a new instance of InvisibleSegmentRenderer to render the path segment with the specified attributes.
     *
     * @param segmentNodePath The NodePath to which the path segment to be rendered belongs.
     * @param segmentIndex The index of the path segment to be rendered.
     * @param startNodeIndex The index of the PathNode at which the path segment to be rendered begins.
     * @param endNodeIndex The index of the PathNode at which the path segment ends.
     */
    public InvisibleSegmentRenderer(ClientNodePath segmentNodePath, int segmentIndex, int startNodeIndex, int endNodeIndex) {
        super(segmentNodePath, segmentIndex, startNodeIndex, endNodeIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node createSceneGraph(Entity entity) {
        segmentEntity = entity;
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyleType getRenderedType() {
        return CoreSegmentStyleType.INVISIBLE;
    }

    @Override
    public void statusChanged(CellStatus status, boolean increasing) {
        //Bypass event listener logic.
    }
}
