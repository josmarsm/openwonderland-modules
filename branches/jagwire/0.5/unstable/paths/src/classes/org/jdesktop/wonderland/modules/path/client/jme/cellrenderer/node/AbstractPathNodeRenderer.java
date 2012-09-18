package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.client.PathNodeComponent;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.AbstractChildComponentRenderer;
import org.jdesktop.wonderland.modules.path.client.listeners.PathNodeEventListener;
import org.jdesktop.wonderland.modules.path.common.NodePath;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;

/**
 * This is the abstract base class for PathNodeRenderers which contains common functionality which
 * can be used by all implementations.
 *
 * @author Carl Jokl
 */
public abstract class AbstractPathNodeRenderer extends AbstractChildComponentRenderer implements PathNodeRenderer {

    protected ClientPathNode pathNode;

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
    protected EventClassListener createEventListener() {
        return new PathNodeEventListener(pathNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isListeningChild() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isOwnerSet() {
        return pathNode != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getOwnerName() {
        return pathNode != null ? (pathNode.isNamed() ? pathNode.getName() : String.format("Path Node %d", pathNode.getSequenceIndex())) : "Null Node";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addComponents(Entity entity, Node childTopNode) {
        super.addComponents(entity, childTopNode);
        entity.addComponent(PathNodeComponent.class, new PathNodeComponent(pathNode));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        pathNode = null;
        super.dispose();
    }
}
