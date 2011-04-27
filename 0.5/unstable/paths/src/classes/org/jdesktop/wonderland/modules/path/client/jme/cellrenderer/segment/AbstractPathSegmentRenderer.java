package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.client.listeners.PathSegmentEventListener;

/**
 * This is a convenience base class to use when creating PathSegmentRenderers and contains
 * standard common functionality.
 *
 * @author Carl Jokl
 */
public abstract class AbstractPathSegmentRenderer implements PathSegmentRenderer {

    protected ClientNodePath segmentNodePath;
    protected int segmentIndex;
    protected int startNodeIndex;
    protected int endNodeIndex;
    protected PathSegmentEventListener listener;
    protected Entity segmentEntity;
    protected Node rootNode;

    /**
     * Initialize this AbstractPathSegmentRenderer with the specified attributes of the path segment to be rendered.
     *
     * @param segmentNodePath The ClientNodePath to which the path segment belongs which is to be rendered.
     * @param segmentIndex The index of the segment within the node path to be rendered.
     * @param startNodeIndex The index of the start PathNode at which the segment begins.
     * @param endNodeIndex The index of the end PathNode at which the segment ends.
     * @throws IllegalArgumentException
     */
    protected AbstractPathSegmentRenderer(ClientNodePath segmentNodePath, int segmentIndex, int startNodeIndex, int endNodeIndex) throws IllegalArgumentException {
        if (segmentNodePath == null) {
            throw new IllegalArgumentException("The path segment's node path cannot be null!");
        }
        this.segmentNodePath = segmentNodePath;
        this.segmentIndex = segmentIndex;
        this.startNodeIndex = startNodeIndex;
        this.endNodeIndex = endNodeIndex;
        listener = new PathSegmentEventListener(segmentNodePath, segmentIndex);
    }

    /**
     * Should be called by the implementing class as soon as an Entity becomes available to set.
     *
     * @param segmentEntity The Entity used to represent the path segment.
     */
    protected void setEntity(Entity segmentEntity) {
        if (this.segmentEntity != segmentEntity) {
            if (listener != null) {
                if (this.segmentEntity != null) {
                    listener.removeFromEntity(this.segmentEntity);
                }
                if (segmentEntity != null) {
                    listener.addToEntity(segmentEntity);
                }
            }
            this.segmentEntity = segmentEntity;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Entity getEntity() {
       return segmentEntity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void statusChanged(CellStatus status, boolean increasing) {
       if (segmentEntity != null) {
           if (status == CellStatus.INACTIVE && !increasing && listener != null) {
                listener.removeFromEntity(segmentEntity);
                listener.dispose();
                listener = null;
           }
           else if (status == CellStatus.RENDERING && increasing && listener == null) {
                listener = new PathSegmentEventListener(segmentNodePath, segmentIndex);
                listener.addToEntity(segmentEntity);
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
            if (segmentEntity != null) {
                listener.removeFromEntity(segmentEntity);
            }
            listener.dispose();
            listener = null;
        }
        if (segmentEntity != null) {
            while (segmentEntity.numEntities() > 0)
            segmentEntity.removeEntity(segmentEntity.getEntity(0));
            segmentEntity = null;
        }
        segmentNodePath = null;
    }
}
