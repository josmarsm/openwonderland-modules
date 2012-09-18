package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Line;
import com.jme.scene.Node;
import com.jme.scene.state.MaterialState;
import com.jme.system.DisplaySystem;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.style.segment.CoreSegmentStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This class is used to render a path segment when a path is being edited.
 *
 * @author Carl Jokl
 */
public class EditModePathSegmentRenderer extends AbstractPathSegmentRenderer {

    private static final ColorRGBA CONNECTOR_LINE_COLOR = new ColorRGBA(0.0f, 0.75f, 0.0f, 1.0f);

    /**
     * Simple factory class used to create instances of EditModePathSegmentRenderer.
     */
    public static class EditModePathSegmentRendererFactory implements PathSegmentRendererFactory {

        /**
         * {@inheritDoc}
         */
        @Override
        public PathSegmentRenderer createRenderer(ClientPathNode startNode) throws IllegalArgumentException {
            return new EditModePathSegmentRenderer(startNode);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SegmentStyleType getRenderedSegmentStyleType() {
            return CoreSegmentStyleType.EDIT_MODE;
        }
        
    }

    /**
     * Create a new instance of EditModePathSegmentRenderer to render the path segment with the specified attributes.
     * 
     * @param startNode The ClientPathNode to which the path segment belongs which is to be rendered.
     * @throws IllegalArgumentException If the specified start ClientPathNode was null.
     */
    public EditModePathSegmentRenderer(ClientPathNode startNode) {
        super(startNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node createSceneGraph(Entity entity) {
        Node editSegmentNode = new Node(entity.getName());
        if (startNode != null && startNode.hasNext()) {
            editSegmentNode = new Node("Connector");
            Line connectorLine = new Line(editSegmentNode.getName(),
                                          new Vector3f[] { startNode.getPosition(), startNode.getNext().getPosition() },
                                          null,
                                          new ColorRGBA[] { CONNECTOR_LINE_COLOR, CONNECTOR_LINE_COLOR },
                                          null);
            editSegmentNode.attachChild(connectorLine);
            initMaterial(editSegmentNode);
        }
        return editSegmentNode;
    }

    /**
     * This is a generic renderer used for rendering when editing paths of any
     * style. As such it does not have any style meta-data. This method
     * always returns the EDIT_MODE SegmentStyleType.
     *
     * @return The EDIT_MODE SegmentStyleType.
     */
    @Override
    public SegmentStyleType getRenderedType() {
        return CoreSegmentStyleType.EDIT_MODE;
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
