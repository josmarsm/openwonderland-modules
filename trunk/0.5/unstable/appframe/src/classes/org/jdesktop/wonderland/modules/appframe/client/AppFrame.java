/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.appframe.client;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import java.awt.Color;
import java.awt.Dimension;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellChildrenChangeListener;
import org.jdesktop.wonderland.client.cell.CellEditChannelConnection;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;
import org.jdesktop.wonderland.client.cell.ModelCell;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellCreateMessage;
import org.jdesktop.wonderland.common.cell.messages.CellDeleteMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerStateRequestMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerStateResponseMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerStateFactory;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import static org.jdesktop.wonderland.modules.appframe.client.AppFrame.LOGGER;
import static org.jdesktop.wonderland.modules.appframe.client.AppFrame.getFileExtension;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameApp;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameConstants;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameProp;
import org.jdesktop.wonderland.modules.bestview.client.BestViewUtils;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 * Client side for elevator cell
 */
public class AppFrame extends ModelCell {

    @UsesCellComponent
    public SharedStateComponent sharedState;
    // shared map
    public SharedMapCli historyMap;
    public SharedMapCli propertyMap;
    public static final Logger LOGGER = Logger.getLogger(AppFrame.class.getName());
    public String extension;
    public AppFrameProperties appFrameProperties;
    public JFrame contextMenu = new JFrame();
    public JPanel contextPanel;
    public MouseButtonEvent3D mbe;
    public JMenuItem item[];
    public Color borderColor;
    public float origWidth;
    public float origHeight;
    public float origImageWidth;
    public float origImageHeight;
    public String addDocument;
    public SharedMapCli pinToMenuMap;
    public SharedMapCli dirtyMap;
    public AppFrame.ChildChangeListener lis=null;
    public int flag=0;

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public AppFrame(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        this.addChildrenChangeListener(new AppFrame.ChildChangeListener(this));
        populateExtension();
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        try {
            super.setStatus(status, increasing);
            historyMap = sharedState.get(AppFrameConstants.History_MAP);
            propertyMap = sharedState.get(AppFrameConstants.Prop_MAP);
            pinToMenuMap = sharedState.get(AppFrameConstants.PintoMenu_MAP);
            dirtyMap = sharedState.get(AppFrameConstants.dirtyMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void print(Spatial node, int level) {
        for(int i=0; i<level; i++)
            System.err.print(" ");
        System.err.println(node+"  "+node.getLocalRotation());

        if (node instanceof Node) {
            List<Spatial> children = ((Node)node).getChildren();
            if (children!=null)
                for(Spatial sp : children) {
                    print(sp, level+1);
                }
        }
    }
    
    public void populateExtension() {
        try {
            AppFrameConstants.extension = new ArrayList<String>();
            AppFrameConstants.extension.add("pdf");
            AppFrameConstants.extension.add("svg");
            AppFrameConstants.extension.add("mov");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AppFrameProp InitProps() {
        try {
            propertyMap.addSharedMapListener(new PropertyMapListener(this));
            pinToMenuMap.addSharedMapListener(new PintoMenuMapListener(this));
            propertyMap = sharedState.get(AppFrameConstants.Prop_MAP);
            pinToMenuMap = sharedState.get(AppFrameConstants.PintoMenu_MAP);
            AppFrameProp afp;
            if (propertyMap.size() != 0 ) {
                afp = (AppFrameProp) propertyMap.get("afp");
            } else {
                afp = new AppFrameProp(AppFrameConstants.Orientation,AppFrameConstants.AspectRatio, AppFrameConstants.MaxHistory, AppFrameConstants.BorderColor);
                propertyMap.put("afp", afp);
            }
            Color newColor = AppFrameProperties.parseColorString(afp.getBorderColor());
            MouseClickListener.HIGHLIGHT_COLOR = new ColorRGBA(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), newColor.getAlpha());
            return afp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public class CellListener implements CellStatusChangeListener {

        public CellListener() {
        }
        public AppFrame parentCell;
        public int flag=0;
        
        public CellListener(AppFrame parentCell) {
            this.parentCell = parentCell;
            this.flag = parentCell.flag;
        }
        
        public void cellStatusChanged(Cell cell, CellStatus status) {
            try {   
                if (cell instanceof App2DCell) {
                    final App2DCell child = (App2DCell) cell;
                    switch (status) {
                        case ACTIVE:
                            
                            
                            if (child.getApp().getPrimaryWindow().isDecorated()) {
                                child.getApp().getPrimaryWindow().setDecorated(false);
                            }
                            if (child.getApp().getControlArb() != null) {
                                child.getApp().getControlArb().addListener(new ControlListener(parentCell));
                            }
                            updateSize(child);
                            child.getApp().getPrimaryWindow().addResizeListener(new Window2D.ResizeListener() {

                                public void windowResized(Window2D wd, Dimension dmnsn, Dimension dmnsn1) {
                                    updateSize(child);
                                }
                            });
                            break;
                            
                        case DISK:
                            AppFrameProp afp = (AppFrameProp) propertyMap.get("afp");
                            dirtyMap.putBoolean("dirty", false);
                            break;
                    }
                } else {
                    switch (status) {
                        case ACTIVE:
                            //updateSize(cell);
                            break;
                        case VISIBLE:
                            updateSize(cell);
                            break;
                        case DISK:
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // update the size of a cell once we have the movable component
        private void updateSize(final Cell cell) {
            try {
                
                CellTransform update = cell.getLocalTransform();
                Quaternion rot = new Quaternion();
                update.getRotation(rot);
                float angles[] = new float[3];
                AppFrameProp afp = (AppFrameProp) propertyMap.get("afp");
                final CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);

                if (cell instanceof App2DCell && renderer != null && renderer.getEntity() != null 
                        && renderer.getEntity().getComponent(RenderComponent.class) != null) {

                   App2DCell child = (App2DCell) cell;
                   child.getApp().updateSlaveWindows();
                    rot.toAngles(angles);
                    float w = child.getApp().getPrimaryWindow().getWidth();
                    float h = child.getApp().getPrimaryWindow().getHeight();
                    origWidth = w;
                    origHeight = h;
                    float scale;
                    angles[2] = (float) (0);
                    update.setRotation(new Quaternion(angles));
                    
                    /**
                     * Fit the dropped content into AppFrame but preserve its aspect ration
                     */
                    if (afp.getOrientation().equals("Horizontal")) {
                        if (afp.getAspectRatio().equalsIgnoreCase("3*4")) {
                            if((352-w)<(264-h)) {
                                scale = (Math.abs(352-w))/w;
                                if(w<352) {
                                    update.setScaling(1+scale);
                                } else {
                                    update.setScaling(1-scale);
                                }
                            } else {
                                scale = (Math.abs(264-h))/h;
                                if(h<264) {
                                    update.setScaling(1+scale);
                                } else {
                                    update.setScaling(1-scale);
                                }
                            }
                        } else {
                            if((470-w)<(264-h)) {
                                scale = (Math.abs(470-w))/w;
                                if(w<470) {
                                    update.setScaling(1+scale);
                                } else {
                                    update.setScaling(1-scale);
                                }
                            } else {
                                scale = (Math.abs(264-h))/h;
                                if(h<264) {
                                    update.setScaling(1+scale);
                                } else {
                                    update.setScaling(1-scale);
                                }
                            }
                        }
                    } else {
                        if (afp.getOrientation().equalsIgnoreCase("3*4")) {
                            if((264-w)<(352-h)) {
                                scale = (Math.abs(264-w))/w;
                                if(w<264) {
                                    update.setScaling(1+scale);
                                } else {
                                    update.setScaling(1-scale);
                                }
                            } else {
                                scale = (Math.abs(352-h))/h;
                                if(h<352) {
                                    update.setScaling(1+scale);
                                } else {
                                    update.setScaling(1-scale);
                                }
                            }
                        } else {
                            if((264-w)<(470-h)) {
                                scale = (Math.abs(264-w))/w;
                                if(w<264) {
                                    update.setScaling(1+scale);
                                } else {
                                    update.setScaling(1-scale);
                                }
                            } else {
                                scale = (Math.abs(470-h))/h;
                                if(h<470) {
                                    update.setScaling(1+scale);
                                } else {
                                    update.setScaling(1-scale);
                                }
                            }
                        }
                    }
                    
                    float div = (float)100*child.getApp().getPrimaryWindow().getPixelScale().getX();
                    update.setScaling((float)update.getScaling()/(float)div);
                } else if (renderer != null) {
                    BoundingVolume bv = BestViewUtils.getModelBounds(cell);
                    if (bv instanceof BoundingBox) {
                        BoundingBox bb = (BoundingBox) bv;
                        origImageWidth = bb.xExtent;
                        origImageHeight = bb.yExtent;
                        float scale1, scale2;
                        CellTransform parentTrans = parentCell.getLocalTransform();
                        if (afp.getOrientation().equals("Horizontal")) {
                            angles[2] = (float) (0);
                            update.setRotation(new Quaternion(angles));

                            if (afp.getAspectRatio().equals("3*4")) {
                                scale1 = (float) (((1.75) ) / bb.xExtent);
                                scale2 = (float) (((1.31) ) / bb.yExtent);
                            } else {
                                scale1 = (float) (((2.33) ) / bb.xExtent);
                                scale2 = (float) (((1.31) ) / bb.yExtent);
                            }
                        } else {
                            //   angles[2] = (float) (-3.14 / 2);
                            update.setRotation(new Quaternion(angles));
                            if (afp.getAspectRatio().equals("3*4")) {
                                scale1 = (float) (((1.31) ) / bb.xExtent);
                                scale2 = (float) (((1.75)) / bb.yExtent);
                            } else {
                                scale1 = (float) (((1.31) ) / bb.xExtent);
                                scale2 = (float) (((2.33) ) / bb.yExtent);
                            }
                        }
                        if (scale1 < scale2) {
                            update.setScaling(scale1);
                        } else {
                            update.setScaling(scale2);
                        }
                    }
                }
                final CellTransform ft = update;
                
                SceneWorker.addWorker(new WorkCommit() {

                    public void commit() {
                        final CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);

                        if (renderer != null && renderer.getEntity() != null && renderer.getEntity().getComponent(RenderComponent.class) != null) {
                            RenderComponent rc = renderer.getEntity().getComponent(RenderComponent.class);
                                rc.getSceneRoot().setLocalTranslation(ft.getTranslation(null));
                            rc.getSceneRoot().setLocalRotation(ft.getRotation(null));
                            rc.getSceneRoot().setLocalScale(ft.getScaling(null));
                            ClientContextJME.getWorldManager().addToUpdateList(rc.getSceneRoot());
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void remove() {
        try {
            WonderlandSession session = this.getSession();
            CellEditChannelConnection connection = (CellEditChannelConnection) session.getConnection(CellEditConnectionType.CLIENT_TYPE);
            CellDeleteMessage msg = new CellDeleteMessage(this.getChildren().iterator().next().getCellID());
            connection.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ChildChangeListener implements CellChildrenChangeListener {

        public AppFrame parentCell;

        public ChildChangeListener(AppFrame parentCell) {
            this.parentCell = parentCell;
        }

        public void childAdded(Cell cell, Cell child) {
            try {
                if (cell.getNumChildren() > 1) {
                    remove();
                }
                AppFrame.CellListener listener = new AppFrame.CellListener(parentCell);
                child.addStatusChangeListener(listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void childRemoved(Cell cell, Cell child) {
            try {
                if (child instanceof App2DCell) {
                    App2DCell childCell = (App2DCell) child;
                    if(childCell.getApp()!=null) {
                    if (childCell.getApp().getControlArb().hasControl()) {
                        childCell.getApp().getControlArb().releaseControlAll();
                        new ColorChange(parentCell).changeColor(parentCell, parentCell.borderColor, Color.black, false);
                    }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // overridden to add buttons onto the frame. Note that the position
    // of the buttons is not correct
    @Override
    protected CellRenderer createCellRenderer(Cell.RendererType rendererType) {
        try {
            AppFrameProp afp = InitProps();
            CellRenderer out = super.createCellRenderer(rendererType);
            Entity parent = ((CellRendererJME) out).getEntity();
            RenderComponent rc = parent.getComponent(RenderComponent.class);
            final Node mainRoot = rc.getSceneRoot();
           
            Spatial sp = mainRoot.getChild(0);
            if(sp!=null) {
                mainRoot.detachChild(sp);
            }
            final Node sceneRoot = new Node("FrameNode");
            Quaternion rot = new Quaternion();
            float scale = 0.003f;
            rot.fromAngles(0f, 0f, 0f);
            if (afp.getAspectRatio().equals("3*4")) {

                //sceneRoot.detachChildAt(0);
                DeployedModel m;
                rot.fromAngles(0, 0, 0);
                
                try {
                    URL url = AssetUtils.getAssetURL("wla://app-frame/Frame_4-3_BW_frame_07.DAE"
                            + "/Frame_4-3_BW_frame_07.DAE.gz.dep", this);
                    m = LoaderManager.getLoaderManager().getLoaderFromDeployment(url);
                    Node s = m.getModelLoader().loadDeployedModel(m, null);
                    s.setName("ModelNode");
                    sceneRoot.attachChildAt(s, 0);
                } catch (Exception ex) {
                    Logger.getLogger(AppFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                Entity button1 = createButton(new Vector3f(-1.68f, 1.4f, .08f),
                        rot, (float) (scale * 0.15), sceneRoot, 1);
                Entity button2 = createButton(new Vector3f(1.68f, 1.4f, .08f),
                        rot, (float) (scale * 0.15), sceneRoot, 2);
                Entity button3 = createButton(new Vector3f(-1.68f, -1.4f, .08f), rot, (float) (scale * 0.15), sceneRoot, 3);
                Entity button4 = createButton(new Vector3f(1.68f, -1.4f, .08f), rot, (float) (scale * 0.15), sceneRoot, 4);

                Entity backPanel = createPanel(new Vector3f(0f, 0f, -0.01f), rot, scale, sceneRoot, afp.getAspectRatio());
                parent.addEntity(button1);
                parent.addEntity(button2);
                parent.addEntity(button3);
                parent.addEntity(button4);
                ClickListener cl = new ClickListener(this);
                // LoadListner l1 = new LoadListner(parentCell);
                cl.addToEntity(button1);
                cl.addToEntity(button2);
                cl.addToEntity(button3);
                cl.addToEntity(button4);
                DropTargetListener d1 = new DropTargetListener(this);
                d1.addToEntity(parent);
                parent.addEntity(backPanel);
                
            } else {
                //sceneRoot.detachChildAt(0);
                DeployedModel m;
                rot.fromAngles(0, 0, 0);
                try {
                    URL url = AssetUtils.getAssetURL("wla://app-frame/Frame_16-9_BW_frame_07.DAE/Frame_16-9_BW_frame_07.DAE.gz.dep", this);
                    m = LoaderManager.getLoaderManager().getLoaderFromDeployment(url);
                    Node s = m.getModelLoader().loadDeployedModel(m, null);
                    s.setName("ModelNode");
                    sceneRoot.attachChildAt(s, 0);
                } catch (Exception ex) {
                    Logger.getLogger(AppFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                Entity button1 = createButton(new Vector3f(-2.28f, 1.4f, .08f),
                        rot, (float) (scale * 0.15), sceneRoot, 1);
                Entity button2 = createButton(new Vector3f(2.28f, 1.4f, .08f),
                        rot, (float) (scale * 0.15), sceneRoot, 2);
                Entity button3 = createButton(new Vector3f(-2.28f, -1.4f, .08f), rot, (float) (scale * 0.15), sceneRoot, 3);
                Entity button4 = createButton(new Vector3f(2.28f, -1.4f, .08f), rot, (float) (scale * 0.15), sceneRoot, 4);
                Entity backPanel = createPanel(new Vector3f(0f, 0f, -0.01f), rot, scale, sceneRoot, afp.getAspectRatio());
                parent.addEntity(button1);
                parent.addEntity(button2);
                parent.addEntity(button3);
                parent.addEntity(button4);
                ClickListener cl1 = new ClickListener(this);
                // LoadListner l1 = new LoadListner(parentCell);
                cl1.addToEntity(button1);
                cl1.addToEntity(button2);
                cl1.addToEntity(button3);
                cl1.addToEntity(button4);
                DropTargetListener d1 = new DropTargetListener(this);
                d1.addToEntity(parent);
                parent.addEntity(backPanel);

            }
            if (afp.getOrientation().equalsIgnoreCase("Horizontal")) {
            } else {
                float angles[] = new float[3];
                rot.toAngles(angles);
                angles[2] = (float) (3.14 / 2);

                sceneRoot.setLocalRotation(new Quaternion(angles));
            }
            mainRoot.attachChild(sceneRoot);
            return out;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Entity createPanel(Vector3f location, Quaternion direction, float scale, Node sceneRoot, String aspectRatio) {
        try {
            WorldManager wm = ClientContextJME.getWorldManager();

            // Create a new root node
            Node root = new Node("App Frame BackGround");
            root.setLocalTranslation(location);
            root.setLocalRotation(direction);
            root.setLocalScale(scale);

            // First load the texture to figure out its size
            URL textureURL = getClass().getResource("resources/my.jpeg");

            // Load the texture
            Texture texture = TextureManager.loadTexture(textureURL);
            texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
            texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
            texture.setWrap(Texture.WrapMode.BorderClamp);
            texture.setApply(Texture.ApplyMode.Replace);

            // Figure out what the size of the texture is

            float width;
            float height;
            if (aspectRatio.equals("16*9")) {
                width = 1580f;
                height = 890f;
            } else {
                width = 1180f;
                height = 880f;
            }
            // Create a quad of suitable dimensions
            Quad quad = new Quad("App Frame quad", width, height);

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

            // attach the child

            root.attachChild(quad);
            root.updateModelBound();

            // create an entity
            Entity e = new Entity("App Frame BackGround entity");

            RenderComponent rc = rm.createRenderComponent(root, sceneRoot);
            e.addComponent(RenderComponent.class, rc);

            // make the entity pickable so it responds to mouse clicks
            JMECollisionSystem collisionSystem = (JMECollisionSystem) wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
            CollisionComponent cc = collisionSystem.createCollisionComponent(root);
            cc.setCollidable(false);
            cc.setPickable(true);

            e.addComponent(CollisionComponent.class, cc);

            return e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Entity createButton(Vector3f location,
            Quaternion direction, float scale, Node sceneRoot, int i) {
        try {
            WorldManager wm = ClientContextJME.getWorldManager();

            // Create a new root node
            Node root = new Node("App Frame Button" + i);
            root.setLocalTranslation(location);
            root.setLocalRotation(direction);
            root.setLocalScale(scale);

            // First load the texture to figure out its size
            URL textureURL = getClass().getResource("resources/button_transparent_03.png");

            // Load the texture
            Texture texture = TextureManager.loadTexture(textureURL);
            texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
            texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
            texture.setWrap(Texture.WrapMode.BorderClamp);
            texture.setApply(Texture.ApplyMode.Replace);

            // Figure out what the size of the texture is
            Image image = texture.getImage();
            float width = image.getWidth();
            float height = image.getHeight();

            // Create a quad of suitable dimensions
            Quad quad = new Quad("App Frame Button quad" + i, width, height);

            quad.setModelBound(new BoundingBox(Vector3f.ZERO, width, height, 0f));

            // Set the texture on the node
            RenderManager rm = wm.getRenderManager();
            TextureState ts = (TextureState) rm.createRendererState(StateType.Texture);
            ts.setTexture(texture);
            ts.setEnabled(true);
            quad.setRenderState(ts);
            BlendState bs = (BlendState) rm.createRendererState(StateType.Blend);
            bs.setBlendEnabled(false);
            bs.setReference(0.5f);
            bs.setTestFunction(BlendState.TestFunction.GreaterThan);
            bs.setTestEnabled(true);
            quad.setRenderState(bs);
            // Set z sorting
            ZBufferState zbs = (ZBufferState) rm.createRendererState(StateType.ZBuffer);
            zbs.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
            quad.setRenderState(zbs);

            // attach the child
            root.attachChild(quad);
            root.updateModelBound();
               
            // create an entity
            Entity e = new Entity("App Frame Button entity");

            RenderComponent rc = rm.createRenderComponent(root, sceneRoot);
            e.addComponent(RenderComponent.class, rc);

            // make the entity pickable so it responds to mouse clicks
            JMECollisionSystem collisionSystem = (JMECollisionSystem) wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
            CollisionComponent cc = collisionSystem.createCollisionComponent(root);
            cc.setCollidable(false);
            cc.setPickable(true);
            e.addComponent(CollisionComponent.class, cc);

            return e;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
//this method is use to save the previous document which is displayed in the app frame

    public void savePrevious() {
        try {
            if (dirtyMap.getBoolean("dirty")) {

                Object[] options = {"Save",
                    "Don't Save"};
                int n = JOptionPane.showOptionDialog(null, "Do you want to save before closing?", "Save", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (n == JOptionPane.YES_OPTION) {
                    CellServerState css = getServerState(this.getChildren().iterator().next());
                    String name = getFileExtension(css.getName());
                    if (name == null) {
                        String s = (String) JOptionPane.showInputDialog(null, "Enter File Name:", "File Name", JOptionPane.PLAIN_MESSAGE, null, null, "Enter Name");
                        css.setName(s);
                    }
                    //  SharedStateComponent ssc = this.getComponent(SharedStateComponent.class);
                    // historyMap = ssc.get(AppFrameConstants.History_MAP);
                    try {
                        if (historyMap.get(css.getName()) != null) {
                            AppFrameApp afa = (AppFrameApp) historyMap.get(css.getName());
                            String sss = encodeState(css);
                            afa.setState(sss);
                            afa.setContentURI(getContentURI(sss));
                            afa.setLastUsed(new Date());
                            historyMap.put(css.getName(), afa);
                        } else {
                            if (historyMap.size() >= 20) {
                                dropItem();
                            } else {
                            }
                            String sss = encodeState(css);
                            historyMap.put(css.getName(), new AppFrameApp(sss, new Date(), getSession().getUserID().getUsername()
                                    , new Date(),getContentURI(sss)));
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(DropTargetListener.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else {
                }
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Figure out what the file extension is from the uri,

    public static String getFileExtension(String uri) {
        try {
            // the final '.'.
            int index = uri.lastIndexOf(".");
            if (index == -1) {
                return null;
            }
            return uri.substring(index + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
//this method is used to store the server state of current child added to the app frame

    public void store() {

        try {
            CellServerState currentState;
            if (this.getNumChildren() == 1) {
                String mystate = null;
                currentState = getServerState(this.getChildren().iterator().next());
                String name = getFileExtension(currentState.getName());
               AppFrameProp afp = (AppFrameProp) propertyMap.get("afp");
                if (dirtyMap.getBoolean("dirty")) {
                    if (name == null) {
                        String s = (String) JOptionPane.showInputDialog(null, "Enter File Name:", "File Name", JOptionPane.PLAIN_MESSAGE, null, null, "Enter Name");
                        currentState.setName(s);
                        try {
                            // the parent, so only a small offset in the Z dimension is needed
                            PositionComponentServerState pcss = (PositionComponentServerState) currentState.getComponentServerState(PositionComponentServerState.class);
                            if (pcss == null) {
                                pcss = new PositionComponentServerState();
                                currentState.addComponentServerState(pcss);
                            }
                            pcss.setTranslation(new Vector3f(0f, 0f, 0.02f));
                            WonderlandSession session = this.getSession();
                            CellEditChannelConnection connection = (CellEditChannelConnection) session.getConnection(CellEditConnectionType.CLIENT_TYPE);
                            CellCreateMessage msg = new CellCreateMessage(this.getCellID(), currentState);
                            connection.send(msg);

                        } catch (Exception excp) {
                            LOGGER.log(Level.WARNING, "Unable to create cell for uri ", excp);
                        }
                    } else {
                    }
                }
                mystate = encodeState(currentState);
                //if doc is already there in history then only update date lastused and serverstate of that child
                if (historyMap.get(currentState.getName()) != null) {
                    AppFrameApp afa = (AppFrameApp) historyMap.get(currentState.getName());
                    afa.setState(mystate);
                    afa.setContentURI(getContentURI(mystate));
                    afa.setLastUsed(new Date());
                    historyMap.put(currentState.getName(), afa);
                } //if doc is new then create new entry into sharedmap with  serverstate of that child,name,  creation date,date last used
                else {
                    if (historyMap.size() >= 20) {
                        dropItem();
                    } else {
                    }
                    historyMap.put(currentState.getName(), new AppFrameApp(mystate, new Date(), getSession().getUserID().getUsername()
                            , new Date(),getContentURI(mystate)));
                }
                return;
            } else {
                return;
            }
        } catch (IOException ex) {
            LOGGER.getLogger(AppFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
// this method is used to find shared map

    public void dropItem() {
        try {
            Set<Entry<String, SharedData>> histry = historyMap.entrySet();
            HashMap<Date, String> myMap = new HashMap<Date, String>();
            ArrayList<Date> history = new ArrayList<Date>();
            for (Map.Entry<String, SharedData> list : histry) {
                String name1 = list.getKey();
                AppFrameApp afa1 = (AppFrameApp) list.getValue();
                Date date1 = afa1.getLastUsed();
                history.add(date1);
                myMap.put(date1, name1);
            }
            Date[] dates = new Date[historyMap.size() + 1];
            history.toArray(dates);
            for (int im = 0; dates[im] != null; im++) {
                for (int im2 = im + 1; dates[im2] != null; im2++) {
                    if (dates[im].compareTo(dates[im2]) < 0) {
                        Date temp = dates[im];
                        dates[im] = dates[im2];
                        dates[im2] = temp;
                    }
                }
            }
            int i = dates.length - 1;
          while (historyMap.size() >= 20) {
                String name = myMap.get(dates[i - 1]);
                historyMap.remove(name);
                i--;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SharedMapCli findMap(Cell cell, String mapName) {
        try{SharedStateComponent ssc = cell.getComponent(SharedStateComponent.class);
        return ssc.get(mapName);
          }
          catch(Exception e)
        {e.printStackTrace();
           }
        return null;
    }
//this method is used to fetch the server state of cell

    public CellServerState getServerState(Cell cell) {
        try{ResponseMessage rm = cell.sendCellMessageAndWait(
                new CellServerStateRequestMessage(cell.getCellID()));
        if (rm == null) {
            return null;
        }
        CellServerStateResponseMessage stateMessage =
                (CellServerStateResponseMessage) rm;
        CellServerState state = stateMessage.getCellServerState();
        return state;
        }
      catch(Exception e)
        {e.printStackTrace();
           }
        return null;
    }
//this method is used to encode the serverstate into string

    public String encodeState(CellServerState mystate) throws IOException {
        ScannedClassLoader loader =
                getSession().getSessionManager().getClassloader();


        try {
            StringWriter sw = new StringWriter();
            Marshaller marshaller = CellServerStateFactory.getMarshaller(loader);
           
            mystate.encode(sw, marshaller);
            
            String s ="<![CDATA["+sw.toString()+"]]>";
            //String s =sw.toString();
            sw.close();
            return s;


        } catch (JAXBException je) {
            throw new RuntimeException(je);


        }
    }
    
    public String getContentURI(String s) {
        String contentURI = null;
        String pattern1 = "<image-uri>";
        String pattern2 = "</image-uri>";
        String text = s;
        
        Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
        Matcher m = p.matcher(text);
        if (m.find()) {
            contentURI = m.group(1);
        }
        
        if(contentURI==null) {
            p = Pattern.compile(Pattern.quote("<documentURI>") + "(.*?)" + Pattern.quote("</documentURI>"));
            m = p.matcher(text);
            if (m.find()) {
                contentURI = m.group(1);
            }
        }
        
        return contentURI;
    }
    
//this method is used to decode the serverstate from string to object

    public CellServerState decodeState(String cellstate) {
        ScannedClassLoader loader =
                getSession().getSessionManager().getClassloader();

        
        try {
            if(cellstate.contains("CDATA")) {
                cellstate = cellstate.substring(9, cellstate.length()-3);
            }
            StringReader sr = new StringReader(cellstate);
            
            return CellServerState.decode(sr,
                    CellServerStateFactory.getUnmarshaller(loader));


        } catch (JAXBException je) {
            throw new RuntimeException(je);


        }
    }
    
//this method gives the wonderland session of this appframe object

    protected WonderlandSession getSession() {
        return getCellCache().getSession();

    }
}
