package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.style.node.CoreNodeStyleType;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;

/**
 * This class is used to render invisible nodes.
 * As nothing is displayed for invisible nodes
 * rendering does not perform any operation.
 *
 * @author Carl Jokl
 */
public class InvisibleNodeRenderer extends AbstractPathNodeRenderer implements PathNodeRenderer {

    /**
     * Create a new instance of the InvisibleNodeRenderer to render the specified ClientPathNode.
     * 
     * @param pathNode The ClientPathNode to be rendered by this InvisibleNodeRenderer.
     */
    public InvisibleNodeRenderer(ClientPathNode pathNode) {
        super(pathNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyleType getRenderedType() {
        return CoreNodeStyleType.INVISIBLE;
    }

    @Override
    protected Node createSceneGraph(Entity entity) {
        return new Node(entity.getName());
    }

}
