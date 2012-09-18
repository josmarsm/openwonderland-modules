package org.jdesktop.wonderland.modules.path.client.listeners;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.Disposable;

/**
 * This class represents an event listener which listens for events such as mouse clicks on a path segment.
 *
 * @author Carl Jokl
 */
public class PathSegmentEventListener extends EventClassListener implements Disposable {

    private static final Logger logger = Logger.getLogger(PathSegmentEventListener.class.getName());

    private ClientPathNode startNode;

    /**
     * Create a new SegmentEventListeners to listen for events on the NodePath segment with the specified
     * start PathNode.
     *
     * @param startNode The start ClientPathNode of the segment for which to listen to events.
     */
    public PathSegmentEventListener(ClientPathNode startNode) {
        this.startNode = startNode;
    }

    /**
     * Get the events to which this PathSegmentEventListener listens.
     *
     * @return An array of classes of Events to which this event listener listens.
     */
    @Override
    public Class[] eventClassesToConsume() {
        return new Class[] { MouseButtonEvent3D.class };
    }

    /**
     * This event method is fired when handling a click event.
     *
     * @param event The event which was fired to which this listener should respond.
     */
    @Override
    public void commitEvent(Event event) {
        
    }

    @Override
    public void dispose() {
        startNode = null;
    }
}
