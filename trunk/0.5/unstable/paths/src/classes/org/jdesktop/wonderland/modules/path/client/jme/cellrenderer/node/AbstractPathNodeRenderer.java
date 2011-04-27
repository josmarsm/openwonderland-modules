package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
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
public abstract class AbstractPathNodeRenderer implements PathNodeRenderer {

    protected ClientPathNode pathNode;
    protected Node rootNode;
    protected Entity nodeEntity;
    protected PathNodeEventListener listener;

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
        listener = new PathNodeEventListener(pathNode);
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
     * Should be called by the implementing class as soon as an Entity becomes available to set.
     *
     * @param nodeEntity The Entity used to represent the PathNode.
     */
    protected void setEntity(Entity nodeEntity) {
        if (this.nodeEntity != nodeEntity) {
            if (listener != null) {
                if (this.nodeEntity != null) {
                    listener.removeFromEntity(this.nodeEntity);
                }
                if (nodeEntity != null) {
                    listener.addToEntity(nodeEntity);
                }
            }
            this.nodeEntity = nodeEntity;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Entity getEntity() {
        return nodeEntity;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void statusChanged(CellStatus status, boolean increasing) {
       if (nodeEntity != null) {
           if (status == CellStatus.INACTIVE && !increasing && listener != null) {
                listener.removeFromEntity(nodeEntity);
                listener.dispose();
                listener = null;
           }
           else if (status == CellStatus.RENDERING && increasing && listener == null) {
                listener = new PathNodeEventListener(pathNode);
                listener.addToEntity(nodeEntity);
           }
       }
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
        if (listener != null) {
            if (nodeEntity != null) {
                listener.removeFromEntity(nodeEntity);
            }
            listener.dispose();
            listener = null;
        }
        if (nodeEntity != null) {
            while (nodeEntity.numEntities() > 0)
            nodeEntity.removeEntity(nodeEntity.getEntity(0));
            nodeEntity = null;
        }
        pathNode = null;
    }
}
