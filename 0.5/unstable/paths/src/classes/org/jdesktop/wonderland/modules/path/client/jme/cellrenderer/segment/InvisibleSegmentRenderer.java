package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import com.jme.scene.Node;
import org.jdesktop.wonderland.modules.path.common.PathNode;
import org.jdesktop.wonderland.modules.path.common.style.segment.CoreSegmentStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This class represents a SegmentRenderer to be used to render path segments which have invisible style.
 *
 * @author Carl Jokl
 */
public class InvisibleSegmentRenderer implements PathSegmentRenderer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Node render(SegmentStyle style, PathNode startNode, PathNode endNode) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyleType getRenderedType() {
        return CoreSegmentStyleType.INVISIBLE;
    }

}
