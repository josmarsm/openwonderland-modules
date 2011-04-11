package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.style.HeightHoldingStyle;
import org.jdesktop.wonderland.modules.path.common.style.HeightOffsetStyle;
import org.jdesktop.wonderland.modules.path.common.style.WidthHoldingStyle;
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
    protected Node createSceneGraph(Entity entity) {
        Node node = new Node(entity.getName());
        NodeStyle style = getNodeStyle();
        float height = style instanceof HeightHoldingStyle ? ((HeightHoldingStyle) style).getHeight() : 1.0f;
        float heightOffset = style instanceof HeightOffsetStyle ? ((HeightOffsetStyle) style).getHeightOffset() : 0.0f;
        float width = style instanceof WidthHoldingStyle ? ((WidthHoldingStyle) style).getWidth() : 0.1f;
        Vector3f min = new Vector3f(-width, heightOffset, -width);
        Vector3f max = new Vector3f(width, height + heightOffset, width);
        Box box = new Box(entity.getName(), min, max);
        node.attachChild(box);
        return node;
    }

    
}
