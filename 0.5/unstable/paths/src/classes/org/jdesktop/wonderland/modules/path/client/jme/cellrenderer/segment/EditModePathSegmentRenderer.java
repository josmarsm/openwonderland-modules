package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Line;
import com.jme.scene.Node;
import com.jme.scene.state.MaterialState;
import com.jme.system.DisplaySystem;
import org.jdesktop.wonderland.modules.path.common.PathNode;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This class is used to render a path segment when a path is being edited.
 *
 * @author Carl Jokl
 */
public class EditModePathSegmentRenderer implements PathSegmentRenderer {

    private static final ColorRGBA CONNECTOR_LINE_COLOR = new ColorRGBA(0.0f, 0.75f, 0.0f, 1.0f);

    /**
     * {@inheritDoc}
     */
    @Override
    public Node render(SegmentStyle style, PathNode startNode, PathNode endNode) {
        Node connectorNode = new Node("Connector");
        Line connectorLine = new Line(connectorNode.getName(),
                                      new Vector3f[] { startNode.getLocalPosition(), endNode.getLocalPosition() },
                                      null,
                                      new ColorRGBA[] { CONNECTOR_LINE_COLOR, CONNECTOR_LINE_COLOR },
                                      null);
        connectorNode.attachChild(connectorLine);
        initMaterial(connectorNode);
        return connectorNode;
    }

    /**
     * This is a generic renderer used for rendering when editing paths of any
     * style. As such there is no specific SegmentStyleType and so this method
     * returns null.
     *
     * @return Null due to this implementation not being bound to any specific kind
     *         of SegmentStyleType.
     */
    @Override
    public SegmentStyleType getRenderedType() {
        return null;
    }

    /**
     * Initialize the material of the edit mode PathSegment representation.
     *
     * @param node The node to which to apply the material.
     */
    protected void initMaterial(Node node) {
        Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();
        MaterialState nodeMaterial = renderer.createMaterialState();
        nodeMaterial.setAmbient(CONNECTOR_LINE_COLOR);
        nodeMaterial.setDiffuse(CONNECTOR_LINE_COLOR);
        nodeMaterial.setSpecular(new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f));
        nodeMaterial.setShininess(0.4f);
        node.setRenderState(nodeMaterial);
    }
}
