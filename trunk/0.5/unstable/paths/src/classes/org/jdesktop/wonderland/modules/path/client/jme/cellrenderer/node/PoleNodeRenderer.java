package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import com.jme.scene.Node;
import com.jme.scene.shape.Cylinder;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.style.StyleMetaDataAdapter;
import org.jdesktop.wonderland.modules.path.common.style.node.CoreNodeStyleType;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;

/**
 * This PathNodeRenderer is used to render Pole base PathNodes.
 *
 * @author Carl Jokl
 */
public class PoleNodeRenderer extends AbstractPathNodeRenderer implements PathNodeRenderer {

    /**
     * Simple factory used to create an instance of a PoleNodeRenderer.
     */
    public static class PoleNodeRendererFactory implements PathNodeRendererFactory {

        /**
         * {@inheritDoc}
         */
        @Override
        public PathNodeRenderer createRenderer(ClientPathNode node) throws IllegalArgumentException {
            return new PoleNodeRenderer(node);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NodeStyleType getRenderedNodeStyleType() {
            return CoreNodeStyleType.POLE;
        }
    }

    /**
     * Create a new instance of a PoleNodeRenderer to render the specified ClientPathNode.
     * 
     * @param pathNode The ClientPathNode to be rendered by this PathNodeRenderer.
     */
    public PoleNodeRenderer(ClientPathNode pathNode) {
        super(pathNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyleType getRenderedType() {
        return CoreNodeStyleType.POLE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node createSceneGraph(Entity entity) {
        setEntity(entity);
        if (rootNode == null) {
            rootNode = new Node(entity.getName());
            NodeStyle style = getNodeStyle();
            StyleMetaDataAdapter adapter = new StyleMetaDataAdapter(style);
            float radius1 = adapter.getRadius1(0.0625f, true);
            float radius2 = adapter.getRadius2(0.0625f, true);
            float height = adapter.getHeight(1.0f);
            float yOffset = adapter.getYOffset(0.0f);
            Cylinder cylinder = new Cylinder(entity.getName(), 4, 16, radius1, radius2, height, true, false);
            cylinder.setLocalTranslation(0, yOffset + (height / 2.0f), 0);
            rootNode.attachChild(cylinder);
        }
        return rootNode;
    }

}
