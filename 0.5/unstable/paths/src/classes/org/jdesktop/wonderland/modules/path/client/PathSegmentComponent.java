package org.jdesktop.wonderland.modules.path.client;

import org.jdesktop.mtgame.EntityComponent;
import org.jdesktop.wonderland.modules.path.common.Disposable;
import org.jdesktop.wonderland.modules.path.common.PathNode;

/**
 * This is an EntityComponent which associates an Entity with a given NodePath
 * segment starting at a specified StartNode.
 *
 * @author Carl Jokl
 */
public class PathSegmentComponent extends EntityComponent implements Disposable {

    private PathNode startNode;

    /**
     * Create a new instance of a PathSegmentComponent for the specified PathNode representing
     * the start PathNode at which the associated segment begins.
     *
     * @param startNode The PathNode at which the NodePath segment starts which
     *                  is to be associated with this Entity.
     */
    public PathSegmentComponent(PathNode startNode) {
        this.startNode = startNode;
    }

    /**
     * Get the PathNode at which the NodePath segment associated with the Entity begins.
     *
     * @return The PathNode at which the NodePath segment associated with the Entity begins.
     */
    public PathNode getStartNode() {
        return startNode;
    }

    /**
     * Dispose of the internal state of the PathSegmentComponent to aid garbage collection.
     */
    @Override
    public void dispose() {
        startNode = null;
    }
}
