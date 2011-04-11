package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import com.jme.scene.Node;
import com.jme.scene.shape.Cylinder;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.style.HeightHoldingStyle;
import org.jdesktop.wonderland.modules.path.common.style.HeightOffsetStyle;
import org.jdesktop.wonderland.modules.path.common.style.RadiusHoldingStyle;
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

    @Override
    protected Node createSceneGraph(Entity entity) {
        Node node = new Node(entity.getName());
        NodeStyle style = getNodeStyle();
        float radius = style instanceof RadiusHoldingStyle ? ((RadiusHoldingStyle) style).getRadius() : 0.1f;
        float height = style instanceof HeightHoldingStyle ? ((HeightHoldingStyle) style).getHeight() : 1.0f;
        float heightOffset = style instanceof HeightOffsetStyle ? ((HeightOffsetStyle) style).getHeightOffset() : 0.0f;
        Cylinder cylinder = new Cylinder(entity.getName(), 4, 16, radius, height);
        cylinder.setLocalTranslation(0, heightOffset + (height / 2.0f), 0);
        node.attachChild(cylinder);
        return node;
    }

}
