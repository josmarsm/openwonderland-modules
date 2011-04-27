package org.jdesktop.wonderland.modules.path.client.listeners;

import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.Disposable;

/**
 * This class represents an event listener which listens for events such as mouse clicks on a PathNode.
 *
 * @author Carl Jokl
 */
public class PathNodeEventListener extends EventClassListener implements Disposable {

    private ClientPathNode owner;

    /**
     * Create a new PathNodeEventListener to listen for mouse events on the specified
     * ClientPathNode.
     *
     * @param owner The ClientPathNode on which this PathNodeEventListener will listen for events.
     */
    public PathNodeEventListener(ClientPathNode owner) {
        this.owner = owner;
    }

    /**
     * Get the events to which this PathNodeMouseEventListener listens.
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
        if (event instanceof MouseButtonEvent3D) {
            MouseButtonEvent3D mouseButtonEvent = (MouseButtonEvent3D) event;
            if (mouseButtonEvent.isClicked() && mouseButtonEvent.getButton() == ButtonId.BUTTON1) {
                javax.swing.JOptionPane.showMessageDialog(null, String.format("Node Clicked (%d): %s.", owner.getSequenceIndex(), owner.getName()));
            }
        }
    }

    /**
     * Method to dispose of this listener to allow for proper garbage collection.
     */
    public void dispose() {
        owner = null;
    }
}
