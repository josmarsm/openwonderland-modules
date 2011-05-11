package org.jdesktop.wonderland.modules.path.client;

import org.jdesktop.mtgame.EntityComponent;
import org.jdesktop.wonderland.modules.path.common.Disposable;
import org.jdesktop.wonderland.modules.path.common.PathNode;

/**
 * This is an EntityComponent which associates an Entity with a given PathNode.
 *
 * @author Carl Jokl
 */
public class PathNodeComponent extends EntityComponent implements Disposable {

    private PathNode node;

    /**
     * Create a new instance of a PathNodeComponent for the specified PathNode.
     *
     * @param node The PathNode which will be associated with the entity.
     */
    public PathNodeComponent(PathNode node) {
        this.node = node;
    }

    /**
     * Get the PathNode which is associated with the Entity.
     *
     * @return The PathNode associated with the Entity.
     */
    public PathNode getPathNode() {
        return node;
    }

    @Override
    public void dispose() {
        node = null;
    }
}
