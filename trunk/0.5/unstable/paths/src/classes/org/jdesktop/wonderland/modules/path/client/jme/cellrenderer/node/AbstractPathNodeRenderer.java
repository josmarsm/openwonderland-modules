package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import com.jme.scene.Node;
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
public abstract class AbstractPathNodeRenderer implements PathNodeRenderer {

    protected ClientPathNode pathNode;
    protected Node rootNode;

    /**
     * Initialize this AbstractPathNodeRenderer to render the specified ClientPathNode.
     *
     * @param pathNode The ClientPathNode to be rendered.
     */
    protected AbstractPathNodeRenderer(ClientPathNode pathNode) {
        if (pathNode == null) {
            throw new IllegalArgumentException("The client path node for a path node renderer cannot be null!");
        }
        this.pathNode = pathNode;
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
        if (rootNode != null) {
            rootNode.detachAllChildren();
            rootNode.removeFromParent();
            rootNode = null;
        }
        pathNode = null;
    }

}
