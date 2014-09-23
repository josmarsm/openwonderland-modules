/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.imageframe.client;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import java.net.URL;
import java.util.logging.Logger;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameConstants;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameProperties;

/**
 * cell renderer for image frame
 */
public class ImageFrameCellRenderer extends BasicRenderer {

    private static final Logger LOGGER
            = Logger.getLogger(ImageFrameCellRenderer.class.getName());
    private Node node = null;
    private ImageFrameCell parentCell;

    public ImageFrameCellRenderer(Cell cell) {
        super(cell);
        parentCell = (ImageFrameCell) cell;
    }

    protected Node createSceneGraph(Entity entity) {

        node = new Node();
        node.setModelBound(new BoundingBox());
        node.updateModelBound();
        node.setName("ImageFrame");

        Quaternion rot = new Quaternion();
        float scale = 0.003f;
        rot.fromAngles(0f, 0f, 0f);
        Entity backPanel = createPanel(new Vector3f(0f, 0f, -0.01f), rot, scale, node);
        DropTargetListener d1 = new DropTargetListener(parentCell);
        d1.addToEntity(entity);
        entity.addEntity(backPanel);
        return node;
    }

    protected Entity createPanel(Vector3f location, Quaternion direction, float scale, Node sceneRoot) {
        try {
            WorldManager wm = ClientContextJME.getWorldManager();

            // Create a new root node
            Node root = new Node("Image Frame BackGround");
            root.setLocalTranslation(location);
            root.setLocalRotation(direction);
            root.setLocalScale(scale);

            // First load the texture to figure out its size
            URL textureURL = getClass().getResource("resources/FrameDropTarget.jpg");
            if (parentCell.imageURL != null) {
                textureURL = AssetUtils.getAssetURL(parentCell.imageURL);
            }

            // Load the texture
            Texture texture = TextureManager.loadTexture(textureURL);
            texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
            texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
            texture.setWrap(Texture.WrapMode.BorderClamp);
            texture.setApply(Texture.ApplyMode.Replace);

            // Figure out what the size of the texture is
            float width = 880f;
            float height = 880f;
            ImageFrameProperties ifp = (ImageFrameProperties) parentCell.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
            //Set AspectRation
            if (ifp.getAspectRatio() == 0) {
                width = 880f;
                height = 880f;
            } else if (ifp.getAspectRatio() == 1) {
                double ar = (double) 5 / (double) 4;
                width = 880f;
                height = ((int) ((double) width / (double) ar));
            } else if (ifp.getAspectRatio() == 2) {
                double ar = (double) 4 / (double) 3;
                width = 880f;
                height = ((int) ((double) width / (double) ar));
            } else if (ifp.getAspectRatio() == 3) {
                double ar = (double) 16 / (double) 9;
                width = 880f;
                height = ((int) ((double) width / (double) ar));
            } else {
                double ar = (double) 2 / (double) 3;
                width = 880f;
                height = ((int) ((double) width / (double) ar));
            }

            if (ifp.getOrientation() == 1) {
                float k = height;
                height = width;
                width = k;
            }

            if (parentCell.frameWidth != -1) {
                width = parentCell.frameWidth;
                height = parentCell.frameHeight;
            }

            // Create a quad of suitable dimensions
            Quad quad = new Quad("Image Frame quad", width, height);

            quad.setModelBound(new BoundingBox(Vector3f.ZERO, width, height, 0f));

            // Set the texture on the node
            RenderManager rm = wm.getRenderManager();
            TextureState ts = (TextureState) rm.createRendererState(StateType.Texture);
            ts.setTexture(texture);
            ts.setEnabled(true);
            quad.setRenderState(ts);

            // Set z sorting
            ZBufferState zbs = (ZBufferState) rm.createRendererState(StateType.ZBuffer);
            zbs.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
            quad.setRenderState(zbs);

            BlendState bs = (BlendState) rm.createRendererState(StateType.Blend);
            bs.setBlendEnabled(false);
            bs.setReference(0.5f);
            bs.setTestFunction(BlendState.TestFunction.GreaterThan);
            bs.setTestEnabled(true);
            quad.setRenderState(bs);

            // attach the child
            root.attachChild(quad);
            root.updateModelBound();

            // create an entity
            Entity e = new Entity("Image Frame BackGround entity");

            RenderComponent rc = rm.createRenderComponent(root, sceneRoot);
            e.addComponent(RenderComponent.class, rc);

            // make the entity pickable so it responds to mouse clicks
            JMECollisionSystem collisionSystem = (JMECollisionSystem) wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
            CollisionComponent cc = collisionSystem.createCollisionComponent(root);
            cc.setCollidable(true);
            cc.setPickable(true);

            e.addComponent(CollisionComponent.class, cc);

            return e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
