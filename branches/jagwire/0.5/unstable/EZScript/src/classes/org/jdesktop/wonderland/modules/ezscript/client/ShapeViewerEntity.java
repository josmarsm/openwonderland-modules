/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.math.Quaternion;
import com.jme.math.Triangle;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.jdesktop.mtgame.*;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.utils.TextLabel2D;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.ezscript.client.globals.Builder;

/**
 *
 * @author Jagwire
 */
public class ShapeViewerEntity extends Entity {

    protected Node rootNode = null;
    private boolean isVisible = false;
    private String shapeType = "";
    private boolean labeled = false;
    private String label = null;
    private ColorRGBA appearance = null;
    protected boolean blended;
    private boolean ortho = false;
    private JMECollisionComponent cc;
    private String textureURL = null;
    private static final Map<String, Texture> textureCache;

    static {
        textureCache = new HashMap<String, Texture>();
    }
    private static Logger logger = Logger.getLogger(ShapeViewerEntity.class.getName());

    public ShapeViewerEntity() {
        super("Shape Viewer");
    }

    public ShapeViewerEntity(String type) {
        super("Shape Viewer");
        this.shapeType = type;
    }

    public void showShape() {
        if (shapeType == null || shapeType.equals("")) {
            return;
        }

        showShape(shapeType);
    }

    public void showShape(String shapeType, String label) {
        labeled = true;
        this.label = label;
        showShape(shapeType);
    }

    public void setTexture(String url) {
        this.textureURL = url;
    }

    private void generateTexture(Node node) {
//        try {
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        TextureState ts = (TextureState) rm.createRendererState(RenderState.StateType.Texture);

        Texture t = null;


        synchronized (textureCache) {

            if (textureCache.containsKey(textureURL)) {
                logger.warning("GETTING TEXTURE FROM CACHE!");
                t = textureCache.get(textureURL);
                logger.warning("TEXTURE RETRIEVED FROM CACHE!");
            } else {
                logger.warning("GETTING IMAGE FROM CACHE!");



                Image image = Builder.imageCache.get(textureURL);

                if (image == null) {
                    downloadAndCacheImage(textureURL);
                    image = Builder.imageCache.get(textureURL);
                }


                logger.warning("GOT IMAGE FROM CACHE!");

                logger.warning("LOADING TEXTURE!");
                t = TextureManager.loadTexture(image,
                        Texture.MinificationFilter.Trilinear,
                        Texture.MagnificationFilter.Bilinear,
                        1,
                        false);

                logger.warning("CACHING TEXTURE!");
                textureCache.put(textureURL, t);

            }
        }
//
//            URL url = new URL(textureURL);
//            ImageIcon tmp = new ImageIcon(url);
//            
//            Image image = tmp.getImage();


        logger.warning("TEXTURE LOADED!");
        t.setWrap(Texture.WrapMode.MirroredRepeat);
        t.setTranslation(new Vector3f());
        ts.setTexture(t);
        ts.setEnabled(true);

        node.setRenderState(ts);
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(ShapeViewerEntity.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    protected void generateExtendedAppearance(Node node) {
        if (textureURL != null) {
            generateTexture(node);
            if (blended) {
                RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
                BlendState bs = (BlendState) rm.createRendererState(StateType.Blend);
                bs.setEnabled(true);
                bs.setBlendEnabled(true);
                bs.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
                bs.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
                bs.setTestEnabled(true);
                bs.setTestFunction(BlendState.TestFunction.GreaterThan);
                node.setRenderState(bs);
            }

        } else {
            generateAppearance(node);
        }
    }

    protected void generateAppearance(Node node) {
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();

        MaterialState ms = (MaterialState) rm.createRendererState(StateType.Material);

        if (appearance != null) {
            ms.setAmbient(appearance);
            ms.setDiffuse(appearance);
            ms.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);
            ms.setEnabled(true);
        } else {
            ms.setAmbient(new ColorRGBA(0.25f, 0, 0.5f, 0.40f));
            ms.setDiffuse(new ColorRGBA(0.25f, 0, 0.5f, 0.40f));
            ms.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);
            ms.setEnabled(true);
        }

        node.setRenderState(ms);

        if (blended) {
            BlendState bs = (BlendState) rm.createRendererState(StateType.Blend);
            bs.setEnabled(true);
            bs.setBlendEnabled(true);
            bs.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
            bs.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
            bs.setTestEnabled(true);
            bs.setTestFunction(BlendState.TestFunction.GreaterThan);
            node.setRenderState(bs);
        }
    }

    protected void generateZBufferState(Node node) {
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();

        ZBufferState zState = (ZBufferState) rm.createRendererState(StateType.ZBuffer);
        zState.setEnabled(true);
        zState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        node.setRenderState(zState);
    }

    protected void generateCollisionComponent(Node node) {
        ServerSessionManager manager = LoginManager.getPrimary();
        CollisionSystem collisionSystem = ClientContextJME.getCollisionSystem(manager, "Default");

        cc = ((JMECollisionSystem) collisionSystem).createCollisionComponent(node);
        cc.setCollidable(true);
        cc.setPickable(true);
        cc.setEntity(this);
        collisionSystem.addCollisionComponent(cc);
        this.addComponent(CollisionComponent.class, cc);

    }

    protected CollisionSystem defaultCollisionSystem() {
        ServerSessionManager manager = LoginManager.getPrimary();
        return ClientContextJME.getCollisionSystem(manager, "Default");
    }

    public void showShape(String shapeType) {
        if (rootNode != null) {
            dispose();
        }
        generateScenegraph();


        generated();

        setVisible(true);
    }

    public void setOrtho(boolean ortho) {
        this.ortho = ortho;
    }

    public void showShapeWithPosition(Vector3f position, int scale) {
        if (rootNode != null) {
            dispose();
        }

        rootNode = new Node("Shape Viewer Node");
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        RenderComponent rc = rm.createRenderComponent(rootNode);


        if (ortho) {
            rc.setOrtho(true);
//            rc.setLightingEnabled(false);
        }

        this.addComponent(RenderComponent.class, rc);

        //Set the z-buffer state on the root node
        generateZBufferState(rootNode);

        //Set the material state and the blend state
        generateAppearance(rootNode);

        //set collision
        generateCollisionComponent(rootNode);



        if (labeled) {
            TextLabel2D label2D = new TextLabel2D(label,
                    Color.black,
                    Color.white,
                    0.3f,
                    true,
                    Font.getFont("SANS_SERIF"));


            Vector3f labelPosition = new Vector3f(0, 1.5f, 0);
            label2D.setLocalTranslation(labelPosition);
            rootNode.attachChild(label2D);

        }

        generateMesh(rootNode);

        rootNode.setLocalTranslation(position);
        rootNode.setLocalRotation(new Quaternion());
        rootNode.setLocalScale(scale);


        generated();

        setVisible(true);
    }

    public Node getRootNode() {
        return rootNode;
    }

    public synchronized void setVisible(boolean visible) {
        WorldManager manager = ClientContextJME.getWorldManager();

        if (visible == true && isVisible == false) {
            manager.addEntity(this);
            isVisible = true;
            return;
        }

        if (visible == false && isVisible == true) {
            manager.removeEntity(this);
            isVisible = false;
            return;
        }
    }

    public void dispose() {

        defaultCollisionSystem().removeCollisionComponent(cc);

        setVisible(false);

        rootNode = null;
    }

    public synchronized void labelShape(final String label) {
        labeled = true;
        this.label = label;
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                TextLabel2D label2D = new TextLabel2D(label,
                        Color.black,
                        Color.white,
                        0.3f,
                        true,
                        Font.getFont("SANS_SERIF"));
                Vector3f labelPosition = new Vector3f(0, 1.5f, 0);
                label2D.setLocalTranslation(labelPosition);
                rootNode.attachChild(label2D);

                WorldManager.getDefaultWorldManager().addToUpdateList(rootNode);
            }
        });
    }

    public synchronized void updateTransform(final Vector3f position,
            final Quaternion orientation) {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {

                updateTransformInternal(rootNode, position, orientation);
            }
        });
    }

    protected void updateTransformInternal(Node rootNode, Vector3f position, Quaternion orientation) {
        rootNode.setLocalTranslation(position);
        rootNode.setLocalRotation(orientation);

        WorldManager.getDefaultWorldManager().addToUpdateList(rootNode);
    }

    public synchronized void updateShape(String shapeType) {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                rootNode.detachAllChildren();
                Vector3f current = rootNode.getLocalTranslation();
                //TODO: create trimesh

                rootNode.setLocalTranslation(current);
                rootNode.setLocalScale(1);
                WorldManager.getDefaultWorldManager().addToUpdateList(rootNode);

            }
        });
    }

    public void setAppearance(ColorRGBA get) {
        appearance = get;
    }

    public void setBlended(boolean blend) {
        this.blended = blend;
    }

    protected void generateMesh(Node rootNode) {

        TriMesh mesh = ShapeUtils.INSTANCE.buildShape(shapeType);
        Triangle[] tris = new Triangle[mesh.getTriangleCount()];

        mesh.getMeshAsTriangles(tris);
        BoundingBox box = new BoundingBox();
        box.computeFromTris(mesh.getMeshAsTriangles(tris), 0, tris.length);

        mesh.setModelBound(box);
        mesh.updateModelBound();


        rootNode.attachChild(ShapeUtils.INSTANCE.buildShape(shapeType));
        rootNode.setModelBound(box);
        rootNode.updateModelBound();
    }

    protected void generated() {
    }

    public void addEventListener(EventClassListener l) {
        l.addToEntity(this);
        this.addComponent(EventClassListener.class, l);
    }

    private void generateScenegraphWithoutCollision() {
        rootNode = new Node("Shape Viewer Node - No Collision");
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        RenderComponent rc = rm.createRenderComponent(rootNode);

        this.addComponent(RenderComponent.class, rc);

        //Set the z-buffer state on the root node
        generateZBufferState(rootNode);

        //Set the material state and the blend state
        generateExtendedAppearance(rootNode);

        //set collision
//        generateCollisionComponent(rootNode);



        if (labeled) {
            TextLabel2D label2D = new TextLabel2D(label,
                    Color.black,
                    Color.white,
                    0.3f,
                    true,
                    Font.getFont("SANS_SERIF"));


            Vector3f labelPosition = new Vector3f(0, 1.5f, 0);
            label2D.setLocalTranslation(labelPosition);
            rootNode.attachChild(label2D);

        }

        generateMesh(rootNode);

        rootNode.setLocalTranslation(new Vector3f());
        rootNode.setLocalRotation(new Quaternion());
        rootNode.setLocalScale(1);
    }

    private void generateScenegraph() {
        rootNode = new Node("Shape Viewer Node");
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        RenderComponent rc = rm.createRenderComponent(rootNode);

        this.addComponent(RenderComponent.class, rc);

        //Set the z-buffer state on the root node
        generateZBufferState(rootNode);

        //Set the material state and the blend state
        generateAppearance(rootNode);

        //set collision
        generateCollisionComponent(rootNode);



        if (labeled) {
            TextLabel2D label2D = new TextLabel2D(label,
                    Color.black,
                    Color.white,
                    0.3f,
                    true,
                    Font.getFont("SANS_SERIF"));


            Vector3f labelPosition = new Vector3f(0, 1.5f, 0);
            label2D.setLocalTranslation(labelPosition);
            rootNode.attachChild(label2D);

        }

        generateMesh(rootNode);

        rootNode.setLocalTranslation(new Vector3f());
        rootNode.setLocalRotation(new Quaternion());
        rootNode.setLocalScale(1);
    }

    public Node getScenegraph() {
        if (rootNode == null) {
            generateScenegraph();
        }

        return rootNode;
    }

    public Node getScenegraphWithoutCollision() {
        generateScenegraphWithoutCollision();

        return rootNode;
    }

    public void removeCollisionComponent() {
        ServerSessionManager manager = LoginManager.getPrimary();
        CollisionSystem collisionSystem = ClientContextJME.getCollisionSystem(manager, "Default");
        collisionSystem.removeCollisionComponent(cc);
        this.removeComponent(CollisionComponent.class);
    }

    private void downloadAndCacheImage(String url) {
        synchronized (Builder.imageCache) {
            if (!Builder.imageCache.containsKey(url)) {
                try {
                    logger.warning("CACHING IMAGE URL: " + url);
                    URL imageURL = new URL(url);
                    ImageIcon icon = new ImageIcon(imageURL);
                    Builder.imageCache.put(url, icon.getImage());
                    logger.warning(url + " CACHED!");
                } catch (MalformedURLException ex) {
                    Logger.getLogger(Builder.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }
}
