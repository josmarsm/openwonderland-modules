package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import com.jme.bounding.BoundingSphere;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.MaterialState;
import com.jme.system.DisplaySystem;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.style.node.CoreNodeStyleType;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;

/**
 * This is an implementation of a PathNodeRenderer used for rendering PathNodes when a PathCell is in edit state.
 *
 * @author Carl Jokl
 */
public class EditModePathNodeRenderer extends AbstractPathNodeRenderer implements PathNodeRenderer {

    /**
     * Class used to create an instance of EditModePathNodeRenderer. It acts somewhat like
     * a delegate.
     */
    public static class EditModePathNodeRendererFactory implements PathNodeRendererFactory {

        /**
         * {@inheritDoc}
         */
        @Override
        public PathNodeRenderer createRenderer(ClientPathNode node) throws IllegalArgumentException {
            return new EditModePathNodeRenderer(node);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NodeStyleType getRenderedNodeStyleType() {
            return CoreNodeStyleType.EDIT_MODE;
        }
    }

    private static final ColorRGBA EDIT_NODE_COLOR = new ColorRGBA(0.5f, 0.0f, 0.6f, 1.0f);

    private static final float RADIUS = 0.0625f;

    /**
     * Create a new instance of EditModePathNodeRenderer to render the specified ClientPathNode.
     *
     * @param pathNode The ClientPathNode to be rendered by this PathNodeRenderer.
     */
    public EditModePathNodeRenderer(ClientPathNode pathNode) {
        super(pathNode);
    }

    /**
     * As this PathNodeRenderer is intended to render PathNodes of any NodeStyleType which are being edited.
     * As such it does not use any style meta-data. This method always returns the EDIT_MODE node style type.
     *
     * @return The EDIT_MODE NodeStyleType.
     */
    @Override
    public NodeStyleType getRenderedType() {
        return CoreNodeStyleType.EDIT_MODE;
    }

    /**
     * Initialize the material of the edit mode PathNode representation.
     *
     * @param node The node to which to apply the material.
     */
    protected void initMaterial(Node node) {
        Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();
        MaterialState nodeMaterial = renderer.createMaterialState();
        nodeMaterial.setAmbient(EDIT_NODE_COLOR);
        nodeMaterial.setDiffuse(EDIT_NODE_COLOR);
        nodeMaterial.setSpecular(new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f));
        nodeMaterial.setShininess(0.4f);
        node.setRenderState(nodeMaterial);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node createSceneGraph(Entity entity) {
        Node editNode = new Node(entity.getName());
        TriMesh nodeSphere = new Sphere(entity.getName(), pathNode.getPosition(), 8, 8, RADIUS);
        editNode.attachChild(nodeSphere);
        editNode.setModelBound(new BoundingSphere(RADIUS, pathNode.getPosition()));
        initMaterial(editNode);
        return editNode;
    }
}
