package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.client.PathSegmentComponent;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.AbstractChildComponentRenderer;
import org.jdesktop.wonderland.modules.path.client.listeners.PathSegmentEventListener;

/**
 * This is a convenience base class to use when creating PathSegmentRenderers and contains
 * standard common functionality.
 *
 * @author Carl Jokl
 */
public abstract class AbstractPathSegmentRenderer extends AbstractChildComponentRenderer implements PathSegmentRenderer {

    protected ClientPathNode startNode;

    /**
     * Initialize this AbstractPathSegmentRenderer with the specified attributes of the path segment to be rendered.
     *
     * @param startNode The ClientPathNode to which the path segment belongs which is to be rendered.
     * @throws IllegalArgumentException If the specified start ClientPathNode was null.
     */
    protected AbstractPathSegmentRenderer(ClientPathNode startNode) throws IllegalArgumentException {
        if (startNode == null) {
            throw new IllegalArgumentException("The start ClientPathNode of the segment cannot be null!");
        }
        this.startNode = startNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientPathNode getStartNode() {
        return startNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EventClassListener createEventListener() {
        return new PathSegmentEventListener(startNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getOwnerName() {
        return String.format("Segment %d", startNode.getSequenceIndex());
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
        return startNode != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addComponents(Entity entity, Node childTopNode) {
        super.addComponents(entity, childTopNode);
        entity.addComponent(PathSegmentComponent.class, new PathSegmentComponent(startNode));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        super.dispose();
        startNode = null;
    }
}
