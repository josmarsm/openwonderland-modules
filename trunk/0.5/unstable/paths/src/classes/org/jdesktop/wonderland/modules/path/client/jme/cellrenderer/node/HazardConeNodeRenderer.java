package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Cylinder;
import com.jme.scene.state.MaterialState;
import com.jme.system.DisplaySystem;
import java.awt.Color;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.style.ColoredStyle;
import org.jdesktop.wonderland.modules.path.common.style.HeightHoldingStyle;
import org.jdesktop.wonderland.modules.path.common.style.RadiusHoldingStyle;
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
    protected Node createSceneGraph(Entity entity) {
        Node hazardConeNode = new Node(entity.getName());
        NodeStyle style = getNodeStyle();
        float radius = (style instanceof RadiusHoldingStyle) ? ((RadiusHoldingStyle) style).getRadius() : 0.1f;
        float height = (style instanceof HeightHoldingStyle) ? ((HeightHoldingStyle) style).getHeight() : 0.5f;
        TriMesh coneMesh = new Cylinder(hazardConeNode.getName(), 8, 16, 0.0f, radius, height, true, false);
        hazardConeNode.attachChild(coneMesh);
        Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();
        MaterialState rulerMaterial = renderer.createMaterialState();
        if (style instanceof ColoredStyle) {
            ColoredStyle coloredStyle = (ColoredStyle) style;
            if (coloredStyle.getColorCount() > 0) {
                Color bodyColor = coloredStyle.getColor(0);
                rulerMaterial.setAmbient(new ColorRGBA(bodyColor.getRed() / 255.0f, bodyColor.getGreen() / 255.0f, bodyColor.getBlue() / 255.0f, 1.0f));
                rulerMaterial.setDiffuse(new ColorRGBA(bodyColor.getRed() / 255.0f, bodyColor.getGreen() / 255.0f, bodyColor.getBlue() / 255.0f, 1.0f));
                rulerMaterial.setSpecular(new ColorRGBA((bodyColor.getRed() * 0.25f) / 255.0f, (bodyColor.getGreen() * 0.25f) / 255.0f, (bodyColor.getBlue() * 0.25f) / 255.0f, 1.0f));
                rulerMaterial.setShininess(0.4f);
            }
        }
        coneMesh.setRenderState(rulerMaterial);
        return hazardConeNode;
    }
}
