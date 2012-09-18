package org.jdesktop.wonderland.modules.path.client.listeners;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.common.Disposable;

/**
 * This class represents an event listener which listens for events such as mouse clicks on a NodePath.
 * This is a top level event listener for the NodePath.
 *
 * @author Carl Jokl
 */
public class PathEventListener extends EventClassListener implements Disposable {

    private static final Logger logger = Logger.getLogger(PathEventListener.class.getName());

    private ClientNodePath owner;

    /**
     * Create a new PathEventListener to listen for events such as mouse events on the specified
     * ClientNodePath.
     *
     * @param owner The ClientNodePath on which this PathEventListener will listen for events.
     */
    public PathEventListener(ClientNodePath owner) {
        this.owner = owner;
    }

    /**
     * Get the events to which this PathEventListener listens.
     *
     * @return An array of classes of Events to which this mouse listener listens.
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

    /**
     * Method to dispose of this listener to allow for proper garbage collection.
     */
    @Override
    public void dispose() {
        owner = null;
    }
}
