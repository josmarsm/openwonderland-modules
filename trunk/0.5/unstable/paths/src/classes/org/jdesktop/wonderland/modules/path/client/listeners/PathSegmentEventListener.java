package org.jdesktop.wonderland.modules.path.client.listeners;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.modules.path.common.Disposable;
import org.jdesktop.wonderland.modules.path.common.NodePath;

/**
 * This class represents an event listener which listens for events such as mouse clicks on a path segment.
 *
 * @author Carl Jokl
 */
public class PathSegmentEventListener extends EventClassListener implements Disposable {

    private static final Logger logger = Logger.getLogger(PathSegmentEventListener.class.getName());

    private int segmentIndex;
    private NodePath owner;

    public PathSegmentEventListener(NodePath owner, int segmentIndex) {
        this.owner = owner;
        this.segmentIndex = segmentIndex;
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
        logger.warning(String.format("Path segment has received event: %s", event.toString()));
        if (event instanceof MouseButtonEvent3D) {
            logger.warning("Path segment event is confirment to be a Mouse 3D event.");
            MouseButtonEvent3D mouseButtonEvent = (MouseButtonEvent3D) event;
            if (mouseButtonEvent.isClicked() && mouseButtonEvent.getButton() == ButtonId.BUTTON1) {
                javax.swing.JOptionPane.showMessageDialog(null, String.format("Segment Clicked: %d.", segmentIndex));
            }
        }
    }

    @Override
    public void dispose() {
        owner = null;
        segmentIndex = -1;
    }
}
