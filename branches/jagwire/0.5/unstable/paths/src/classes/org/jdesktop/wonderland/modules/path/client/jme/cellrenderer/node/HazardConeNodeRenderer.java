package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
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
 * This PathNode renderer is used to render PathNodes as HazardCones.
 *
 * @author Carl Jokl
 */
public class HazardConeNodeRenderer extends AbstractPathNodeRenderer implements PathNodeRenderer {

    /**
     * Simple factory used to create an instance of a HazardConeNodeRenderer.
     */
    public static class HazardConeNodeRendererFactory implements PathNodeRendererFactory {

        /**
         * {@inheritDoc}
         */
        @Override
        public PathNodeRenderer createRenderer(ClientPathNode node) throws IllegalArgumentException {
            return new HazardConeNodeRenderer(node);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NodeStyleType getRenderedNodeStyleType() {
            return CoreNodeStyleType.HAZARD_CONE;
        }
    }

    /**
     * Create a new instance of a HazardConeNodeRenderer to render the specified node.
     *
     * @param pathNode The ClientPathNode to be rendered by this PathNodeRenderer.
     */
    public HazardConeNodeRenderer(ClientPathNode pathNode) {
        super(pathNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyleType getRenderedType() {
        return CoreNodeStyleType.HAZARD_CONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node createSceneGraph(Entity entity) {
        Node hazardConeNode = new Node(entity.getName());
        NodeStyle style = getNodeStyle();
        StyleMetaDataAdapter adapter = new StyleMetaDataAdapter(style);
        float radius1 = adapter.getRadius1(0.1f, true);
        float radius2 = adapter.getRadius2(0.125f, true);
        float height = adapter.getHeight(0.5f);
        TriMesh coneMesh = new Cylinder(entity.getName(), 8, 16, radius1, radius2, height, true, false);
        hazardConeNode.attachChild(coneMesh);
        Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();
        MaterialState rulerMaterial = renderer.createMaterialState();
        float[] backgroundColorComponents = adapter.getBackgroundColor(1.0f, 0.31f, 0.0f, 1.0f, true);
        float[] foregroundColorComponents = adapter.getForegroundColor(1.0f, 1.0f, 1.0f, 1.0f, true);
        if (backgroundColorComponents != null && backgroundColorComponents.length >= 4) {
            ColorRGBA backgroundColor = new ColorRGBA(backgroundColorComponents[0], backgroundColorComponents[1], backgroundColorComponents[2], backgroundColorComponents[3]);
            rulerMaterial.setAmbient(backgroundColor);
            rulerMaterial.setDiffuse(backgroundColor);
            rulerMaterial.setSpecular(new ColorRGBA(backgroundColor.r * 0.25f, backgroundColor.g * 0.25f, backgroundColor.b, 1.0f));
            rulerMaterial.setShininess(0.4f);
        }
        coneMesh.setRenderState(rulerMaterial);
        return hazardConeNode;
    }
}
