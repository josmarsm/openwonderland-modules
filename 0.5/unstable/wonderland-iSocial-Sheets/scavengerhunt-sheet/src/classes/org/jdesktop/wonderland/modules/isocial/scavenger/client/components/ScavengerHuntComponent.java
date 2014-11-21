/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.client.components;


import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.wonderbuilders.modules.capabilitybridge.client.CapabilityBridge;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.TextureRenderBuffer;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.bestview.client.BestViewUtils;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.scavenger.client.AnswerPanel;
import org.jdesktop.wonderland.modules.isocial.scavenger.client.ScavengerHuntUtils;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.FindMethod;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntComponentClientState;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntConstants;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntItem;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntResult;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.SharedDataItem;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.SharedFindMethod;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 * Cell component for Scavenger Hunt.
 *
 * @author Vladimir Djurovic
 * @author Abhishek Upadhyay
 */
public class ScavengerHuntComponent extends CellComponent implements 
        ContextMenuActionListener, ProximityListener, RenderUpdater
        , SharedMapListenerCli, CapabilityBridge {
    
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(ScavengerHuntConstants.BUNDLE_PATH);
    private static final Logger LOGGER = Logger.getLogger(ScavengerHuntComponent.class.getName());
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    
    private static final int THUMBNAIL_WIDTH = 400;
    private static final int THUMBNAIL_HEIGHT = 300;
    
    /** Reference to shared state component holding items. */
    private static SharedStateComponent sharedState;
    private String sheetId;
    private ScavengerHuntItem item;
    private FindMethod findMethod;
    private SharedMapCli globalSharedMap;
    
    @UsesCellComponent
    private ContextMenuComponent ctxMenu;
    
    @UsesCellComponent
    private ProximityComponent proximity;
    
    /** Context menu i which appears when right-click find method is selected. */
    private ContextMenuItem rightClickMenuItem;
    
    /** Factory for producing context menu items. */
    private ContextMenuFactorySPI ctxMenuFactory;
    private MouseEventListener mouseEventListener;
    
    /** Pixels for found item image */
    private int[] pixels;
    private TextureRenderBuffer textureBuffer;
     private Node cameraNode = null;
     private BufferedImage bimg;
     
//     private Semaphore semaphore;
     /** True if thumbnail renderer is being updated, false otherwise. */
     private boolean doingRenderUpdate = false;
     
      /** Indicates whether answer dialog is currently opened (true) or closed (false) */
     private boolean answerDlgOpened = false;
    
    /**
     * Returns shared map instance, if exists. This method will return "per-user" 
     * shared map which reflects scavenger hunt results. If there's no existing map for user,
     * general sheet map will be copied and returned.
     * 
     * @param id sheet ID
     * @param username user who is requesting the map
     * @return  shared map instance
     */
    public static SharedMapCli getSharedMap(String id, String username) {
        SharedMapCli map = null;
        if (sharedState == null) {
            CellClientSession session = (CellClientSession) LoginManager.getPrimary().getPrimarySession();
            CellCache cache = session.getCellCache();
            if (cache.getEnvironmentCell() != null) {
                sharedState = cache.getEnvironmentCell().getComponent(SharedStateComponent.class);
            }
        }
        
        if (sharedState != null) {
            if (username != null) {
                map = sharedState.get(username);
                // copy all items from sheet map, since existing items may be updated
                SharedMapCli sheetMap = sharedState.get(id);
                for (String key : sheetMap.keySet()) {
                    if (sheetMap.get(key) instanceof SharedDataItem) {
                        ScavengerHuntItem globalItem = ((SharedDataItem) sheetMap.get(key)).getItem();
                        ScavengerHuntItem localItem = null;
                        boolean doUpdate = false;
                        if (map.containsKey(key)) {
                            localItem = ((SharedDataItem) map.get(key)).getItem();
                            doUpdate = localItem.update(globalItem);
                        } else {
                            localItem = globalItem;
                            doUpdate = true;
                        }
                        if(doUpdate){
                            map.put(key, new SharedDataItem(localItem));
                        }
                        
                    } else {
                        map.put(key, sheetMap.get(key));
                    }
                }
            } else {
                // if no username is specified, return global (sheet) map
                map = sharedState.get(id);
            }

        }
        return map;
    }

    public ScavengerHuntComponent(Cell cell){
        super(cell);
    }

    public SharedMapCli getGlobalSharedMap() {
        return globalSharedMap;
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        if (status == CellStatus.RENDERING) {
            if (mouseEventListener == null) {
                mouseEventListener = new MouseEventListener();
                mouseEventListener.addToEntity(((BasicRenderer) cell.getCellRenderer(Cell.RendererType.RENDERER_JME)).getEntity());
            }
           
        } else if(status == CellStatus.ACTIVE && increasing){
            // need to initialize shared map in separate thread to avoid deadlock
            // map hangs waiting for initialization
            Thread th = new Thread(new Runnable() {

                public void run() {
                    globalSharedMap = ScavengerHuntComponent.getSharedMap(sheetId, null);
                    globalSharedMap.addSharedMapListener(ScavengerHuntComponent.this);
                    
                    SharedFindMethod sfm = (SharedFindMethod)globalSharedMap.get(ScavengerHuntConstants.FIND_METHOD_KEY_NAME);
                    FindMethod fm = null;
                    if(sfm != null){
                        fm = sfm.getMethod();
                    }
                    if(fm == null){
                        fm = new FindMethod();
                        globalSharedMap.put(ScavengerHuntConstants.FIND_METHOD_KEY_NAME, new SharedFindMethod(fm));
                    }
                    setFindMethod(fm);
                    
                }
            });
            th.start();
        } else if(status == CellStatus.DISK && !increasing){
            // remove proximity listener
            proximity.removeProximityListener(this);
        }
    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
        if (clientState != null) {
            sheetId = ((ScavengerHuntComponentClientState) clientState).getSheetId();
            item = ((ScavengerHuntComponentClientState) clientState).getItem();
        }
    }
    
    /**
     * EVent listener for context menu i. This is used only if RIGHT_CLICK is set as find method 
     * for the component
     * @param event 
     */
    public void actionPerformed(ContextMenuItemEvent event) {
        onItemFound();
    }

    /**
     * Handles proximity events, ie. when avatar is within proximity range of the component. Only used if Find method
     * for scavenger hunt item is set to Proximity.
     * 
     * @param entered whether avatar entered or exited range
     * @param cell item cell
     * @param viewCellID cell ID
     * @param proximityVolume range volume
     * @param proximityIndex 
     */
    public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {
        if(entered){
            // only do this when entering bounds
            onItemFound();
        }
    }

    public void propertyChanged(SharedMapEventCli smec) {
        SharedData newData = smec.getNewValue();
        if(newData instanceof SharedFindMethod){
            FindMethod newfm = ((SharedFindMethod)newData).getMethod();
            if(findMethod == null){
                setFindMethod(newfm);
            } else if(findMethod.getFindType() != newfm.getFindType() || (findMethod.getParam() != null && !findMethod.getParam().equals(newfm.getParam()))){
                setFindMethod(newfm);
            }
        }
    }
    
    /**
     * Set current global find method
     * 
     * @param fm new find method
     */
    private void setFindMethod(FindMethod fm){
        findMethod = fm;
        switch(fm.getFindType()){
            case FindMethod.LEFT_CLICK:
                // remove proximity listener and context menu item
                if(proximity != null){
                    proximity.removeProximityListener(this);
                }
                if (rightClickMenuItem != null) {
                    ctxMenu.removeContextMenuFactory(ctxMenuFactory);
                }
                break;
            case FindMethod.PROXIMITY:
                // remove right click menu item
                if (rightClickMenuItem != null) {
                    ctxMenu.removeContextMenuFactory(ctxMenuFactory);
                }
                // add proximity listener
                if (proximity != null) {
                    BoundingVolume bv = new BoundingSphere(Float.parseFloat(findMethod.getParam()), new Vector3f(0, 0, 0));
                    proximity.addProximityListener(this, new BoundingVolume[]{bv});
                }
                break;
            case FindMethod.RIGHT_CLICK:
                // remove proximity listener
                 if(proximity != null){
                    proximity.removeProximityListener(this);
                }
                 // set context menu
                  if (ctxMenu != null) {
                    rightClickMenuItem = new SimpleContextMenuItem(findMethod.getParam(), this);
                    ctxMenuFactory = new ContextMenuFactorySPI() {

                        public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                            return new ContextMenuItem[]{rightClickMenuItem};
                        }
                    };
                     ctxMenu.addContextMenuFactory(ctxMenuFactory);
                }
                break;
        }
    }
    
    private class MouseEventListener extends EventClassListener{

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseButtonEvent3D.class};
        }

        @Override
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
            if(mbe.isClicked() && mbe.getButton() == ButtonId.BUTTON1){
                if(findMethod != null && findMethod.getFindType() == FindMethod.LEFT_CLICK){
                    onItemFound();
                }
            }
        }
    }
    
    /**
     * Called when i is found to perform correct action
     */
    private void onItemFound() {
         // if answer dialog is already opened, return immediately
         if(answerDlgOpened){
             return;
         }
        // if sheet ID is not set, show warning and exit
        if(sheetId == null){
             JOptionPane.showMessageDialog(JmeClientMain.getFrame().getFrame(), "No sheet associated with this item", "Warning", JOptionPane.WARNING_MESSAGE);
             return;
        }
        // check if user has started the hunt
         boolean found = false;
        final String username = ISocialManager.INSTANCE.getUsername();
        try{
            Collection<Result> results = ISocialManager.INSTANCE.getResults(sheetId);
           
            for(Result r : results){
                if(r.getCreator().equals(username) && r.getDetails() instanceof ScavengerHuntResult){
                    found = true;
                    break;
                }
            }
            
        } catch(IOException ex){
            LOGGER.log(Level.SEVERE, "Could not find result: {0}", ex.getMessage());
            JOptionPane.showConfirmDialog(JmeClientMain.getFrame().getFrame(), "Unable to fetch results: {0}", 
                    "Error", JOptionPane.OK_OPTION);
        }
        
        // need to run in separate thread to avoid blocking EDT
        final boolean isFound = found; // copy to final variable for usage in inner class
        SwingUtilities.invokeLater(new Runnable() {
            
            public void run() {
                SharedDataItem sdi = (SharedDataItem) getSharedMap(sheetId, username).get(cell.getCellID().toString());
                final ScavengerHuntItem i = sdi.getItem();
                final AnswerPanel<ScavengerHuntItem> panel = new AnswerPanel<ScavengerHuntItem>(i, cell);
                // display answer dialog
                Object[] options = (i.getAnswer() == null) ? new Object[]{"Submit"} : new Object[]{"Update", "Cancel"};
                final JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_OPTION, null, options, null);
                JDialog dlg = pane.createDialog(JmeClientMain.getFrame().getFrame(), i.getName());
                 // position dialog to upper-left corner
                Rectangle bounds = JmeClientMain.getFrame().getFrame().getBounds();
                dlg.setLocation(bounds.x + ScavengerHuntConstants.DLG_OFFSET_X, bounds.y + ScavengerHuntConstants.DLG_OFFSET_Y);
                dlg.setModal(false);
                dlg.addComponentListener(new ComponentAdapter() {

                    @Override
                    public void componentHidden(ComponentEvent e) {
                        answerDlgOpened = false;
                        // if no result exists, do not submit anything
                        if(!isFound){
                            return;
                        }
                        Object selValue = pane.getValue();
                        if (selValue != null && (selValue.equals("Submit") || selValue.equals("Update"))) {
                            i.setFound(true);
                            i.setTimestamp(new Date());
                            i.setAnswer(panel.getAnswer());
                            // set snapshot image URL
                            i.setSnapshotImageUrl(ScavengerHuntUtils.getSnapshotHttpUrl(item.getCellId(), sheetId));
                            //take snapshot only the first time
                            if(selValue.equals("Submit")){
                                Thread th = new Thread(new Runnable() {

                                    public void run() {
                                        setUpCamera();
                                    }
                                });
                                th.start();
                            }
                   
                            getSharedMap(sheetId, username).put(cell.getCellID().toString(), new SharedDataItem(i));
                        }
                        
                    }
                });
                 answerDlgOpened = true;
                dlg.setVisible(true);
                
            }
        });

    }
    
    // Camera related stuff
    
    private void setUpCamera(){
        Entity cameraEntity = new Entity("ScavengerHuntCamera");
        
         // Fetch the world and render managers for use
        final WorldManager wm = ClientContextJME.getWorldManager();
        final RenderManager rm = wm.getRenderManager();
        
        bimg = new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = new int[THUMBNAIL_WIDTH * THUMBNAIL_HEIGHT];
        textureBuffer = (TextureRenderBuffer) rm.createRenderBuffer(RenderBuffer.Target.TEXTURE_2D, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
        textureBuffer.setIncludeOrtho(false);
          
        // use best-view module to figure out camera position
        CellTransform bestView = BestViewUtils.getBestView(cell);
          Vector3f trans = bestView.getTranslation(null);
          Quaternion rot = bestView.getRotation(null);
          
          // back up 5 meters
         Vector3f backup = rot.mult(new Vector3f(0f, 0f, 50f));
        bestView.setTranslation(trans.add(backup));
        
         // Create the new camera and a node to hold it. We attach this to the
        // Entity
        cameraNode = new Node();
        cameraNode.setLocalTranslation(trans);
        cameraNode.setLocalRotation(rot);
        CameraNode cn = new CameraNode("Top Camera", null);
        cameraNode.attachChild(cn);

        // Create a camera component and associated with the texture buffer we
        // have created.
        CameraComponent cc = rm.createCameraComponent(
                cameraNode,      // The Node of the camera scene graph
                cn,              // The Camera
                THUMBNAIL_WIDTH,           // Viewport width
                THUMBNAIL_HEIGHT,          // Viewport height
                90.0f,           // Field of view
                1.0f,            // Aspect ratio
                1.0f,            // Front clip
                30000.0f,         // Rear clip
                false            // Primary?
                );
        cameraEntity.addComponent(CameraComponent.class, cc);
        wm.addEntity(cameraEntity);
         textureBuffer.setEnable(true);
        textureBuffer.setCameraComponent(cc);
        rm.addRenderBuffer(textureBuffer);
        textureBuffer.setRenderUpdater(this);
    }
    
    /**
     * Draws camera image to buffer.
     * @param o 
     */
    @Override
     public void update(Object o) {
        if(doingRenderUpdate){
            return;
        }
        doingRenderUpdate = true;
        ByteBuffer bb = textureBuffer.getTextureData();
        fill(bimg, bb, bimg.getWidth(), bimg.getHeight());
        // do this in separate thread to avoid GUI lock
        Thread th = new Thread(new Runnable() {
            public void run() {
                try {
                    WonderlandSession session = cell.getCellCache().getSession();
                    ContentRepositoryRegistry repoReg = ContentRepositoryRegistry.getInstance();
                    ContentRepository repo = repoReg.getRepository(session.getSessionManager());

                    ContentCollection root = repo.getUserRoot();
                    ContentResource node = (ContentResource) root.getChild(ScavengerHuntUtils.createImageContentName(item.getCellId(), sheetId));
                    if (node == null) {
                        node = (ContentResource) root.createChild(ScavengerHuntUtils.createImageContentName(item.getCellId(), sheetId), Type.RESOURCE);
                    }
                    File tmp = File.createTempFile("sc_hunt_" + item.getCellId() + "_", ".png");
                    tmp.deleteOnExit();
                    ImageIO.write(bimg, "PNG", tmp);
                    node.put(tmp);
                    LOGGER.log(Level.INFO, "Created snapshot image: {0}", node.getPath());

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                } finally {
                    doingRenderUpdate = false;
                }

                // Remove the render buffer to clean it up
                if (textureBuffer != null) {
                    WorldManager wm = ClientContextJME.getWorldManager();
                    RenderManager rm = wm.getRenderManager();
                    rm.removeRenderBuffer(textureBuffer);
                }
            }
        });
        th.start();
        
    }
    
    /**
     * Takes a BufferedImage and a ByteBuffer and fills the values found in the
     * byte buffer into the buffered image, assuming consecutive RGB values.
     */
    private void fill(BufferedImage bi, ByteBuffer bb, int width, int height) {
        bb.rewind();
        int pi = 0;
        for (int y = (height - 1); y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int index = (y * width + x) * 3;
                int b = bb.get(index);
                int g = bb.get(index + 1);
                int r = bb.get(index + 2);

                pixels[pi++] = ((r & 255) << 16) | ((g & 255) << 8) |
                        ((b & 255)) | 0xff000000;
                //bi.setRGB(x, (height - y) - 1, pixel);
            }
        }
        bi.setRGB(0, 0, width, height, pixels, 0, width);
    }
    public EventClassListener getMouseEventListener() {
             return mouseEventListener;
    }
}
