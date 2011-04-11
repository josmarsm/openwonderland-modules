package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.NodePath;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;

/**
 * This is the abstract base class for PathNodeRenderers which contains common functionality which
 * can be used by all implementations.
 *
 * @author Carl Jokl
 */
public abstract class AbstractPathNodeRenderer extends BasicRenderer implements PathNodeRenderer {

    protected ClientPathNode pathNode;

    /**
     * Initialize this AbstractPathNodeRenderer to render the specified ClientPathNode.
     *
     * @param pathNode The ClientPathNode to be rendered.
     */
    protected AbstractPathNodeRenderer(ClientPathNode pathNode) {
        super(pathNode instanceof Cell ? (Cell) pathNode : null);
        if (pathNode == null) {
            throw new IllegalArgumentException("The client path node for a path node renderer cannot be null!");
        }
        if (pathNode instanceof Cell) {
            this.pathNode = pathNode;
        }
        else {
            throw new IllegalArgumentException("The client path node for a path node must be an instance of a cell!");
        }
    }

    /**
     * Get the NodeStyle for this node.
     *
     * @return The NodeStyle for this node if it was able to be retrieved.
     */
    protected NodeStyle getNodeStyle() {
        NodePath path = pathNode.getPath();
        if (path != null) {
            PathStyle pathStyle = path.getPathStyle();
            if (pathStyle != null) {
                return pathStyle.getNodeStyle(pathNode.getSequenceIndex(), true);
            }
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ClientPathNode getPathNode() {
        return pathNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        pathNode = null;
        super.cleanupSceneGraph(entity);
    }

}
