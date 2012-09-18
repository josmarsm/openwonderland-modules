package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.shape.Cylinder;
import com.jme.scene.state.MaterialState;
import com.jme.system.DisplaySystem;
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
        Node poleNode = new Node(entity.getName());
        NodeStyle style = getNodeStyle();
        StyleMetaDataAdapter adapter = new StyleMetaDataAdapter(style);
        float radius1 = adapter.getRadius1(0.0625f, true);
        float radius2 = adapter.getRadius2(0.0625f, true);
        float height = adapter.getHeight(1.0f);
        float yOffset = adapter.getYOffset(0.0f);
        Cylinder cylinder = new Cylinder(entity.getName(), 4, 16, radius1, radius2, height, true, false);
        Vector3f nodePosition = pathNode.getPosition();
        cylinder.setLocalTranslation(nodePosition.x, nodePosition.y + yOffset, nodePosition.z);
        cylinder.lookAt(new Vector3f(nodePosition.x, nodePosition.y + 1.0f, nodePosition.z), new Vector3f(nodePosition.x, nodePosition.y, nodePosition.z -1.0f));
        cylinder.setModelBound(new BoundingBox());
        cylinder.updateModelBound();
        poleNode.attachChild(cylinder);
        initMaterial(poleNode, style);
        return poleNode;
    }

    /**
     * Initialize the material of the edit mode PathNode representation.
     *
     * @param node The node to which to apply the material.
     * @param style The NodeStyle from which to get any material styling hints.
     */
    protected void initMaterial(Node node, NodeStyle style) {
        StyleMetaDataAdapter adapter = new StyleMetaDataAdapter(style);
        Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();
        MaterialState nodeMaterial = renderer.createMaterialState();
        float[] rgbaValues = adapter.getMainColor(0.4f, 0.3f, 0.2f, 1.0f);
        ColorRGBA poleColor = new ColorRGBA(rgbaValues[0], rgbaValues[1], rgbaValues[2], rgbaValues[3]);
        nodeMaterial.setAmbient(poleColor);
        nodeMaterial.setDiffuse(poleColor);
        nodeMaterial.setSpecular(new ColorRGBA(poleColor.r * 0.8f, poleColor.g * 0.8f, poleColor.b * 0.8f, 1.0f));
        nodeMaterial.setShininess(0.8f);
        node.setRenderState(nodeMaterial);
    }

}
