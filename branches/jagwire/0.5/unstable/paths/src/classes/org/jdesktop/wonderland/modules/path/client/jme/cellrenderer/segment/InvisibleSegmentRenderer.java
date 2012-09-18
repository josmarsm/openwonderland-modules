package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
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
        public PathSegmentRenderer createRenderer(ClientPathNode startNode) throws IllegalArgumentException {
            return new InvisibleSegmentRenderer(startNode);
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
     * @param startNode The ClientPathNode to which the path segment belongs which is to be rendered.
     * @throws IllegalArgumentException If the specified start ClientPathNode was null.
     */
    public InvisibleSegmentRenderer(ClientPathNode startNode) {
        super(startNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node createSceneGraph(Entity entity) {
        return new Node(entity.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyleType getRenderedType() {
        return CoreSegmentStyleType.INVISIBLE;
    }
}
