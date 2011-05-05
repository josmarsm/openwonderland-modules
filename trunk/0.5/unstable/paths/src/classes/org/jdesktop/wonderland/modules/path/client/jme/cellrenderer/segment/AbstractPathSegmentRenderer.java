package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.AbstractChildComponentRenderer;
import org.jdesktop.wonderland.modules.path.client.listeners.PathSegmentEventListener;

/**
 * This is a convenience base class to use when creating PathSegmentRenderers and contains
 * standard common functionality.
 *
 * @author Carl Jokl
 */
public abstract class AbstractPathSegmentRenderer extends AbstractChildComponentRenderer implements PathSegmentRenderer {

    protected ClientNodePath segmentNodePath;
    protected int segmentIndex;
    protected int startNodeIndex;
    protected int endNodeIndex;

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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EventClassListener createEventListener() {
        return new PathSegmentEventListener(segmentNodePath, segmentIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getOwnerName() {
        return String.format("Segment %d", segmentIndex);
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
        return segmentNodePath != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        super.dispose();
        segmentNodePath = null;
    }
}
