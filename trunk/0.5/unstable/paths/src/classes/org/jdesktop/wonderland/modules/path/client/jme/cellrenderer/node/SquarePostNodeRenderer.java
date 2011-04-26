package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.style.StyleMetaDataAdapter;
import org.jdesktop.wonderland.modules.path.common.style.node.CoreNodeStyleType;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;

/**
 * This kind of of PathNodeRenderer is used to render SquarePosts at PathNodes.
 *
 * @author Carl Jokl
 */
public class SquarePostNodeRenderer extends AbstractPathNodeRenderer implements PathNodeRenderer {

    /**
     * Create a new instance of SquarePostNodeRenderer to render the specified ClientPathNode.
     *
     * @param pathNode The ClientPathNode to be rendered by this PathNodeRenderer.
     */
    public SquarePostNodeRenderer(ClientPathNode pathNode) {
        super(pathNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyleType getRenderedType() {
        return CoreNodeStyleType.SQUARE_POST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node createSceneGraph(Entity entity) {
        rootNode = new Node(entity.getName());
        if (rootNode == null) {
            NodeStyle style = getNodeStyle();
            StyleMetaDataAdapter adapter = new StyleMetaDataAdapter(style);
            float height = adapter.getHeight(1.0f);
            float yOffset = adapter.getYOffset(0.0f);
            float width = adapter.getWidth(0.0623f);
            Vector3f min = new Vector3f(-width, yOffset, -width);
            Vector3f max = new Vector3f(width, height + yOffset, width);
            Box box = new Box(entity.getName(), min, max);
            rootNode.attachChild(box);
        }
        return rootNode;
    }

    
}
