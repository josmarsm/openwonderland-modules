/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.colortheme.client;

import com.jme.image.Texture;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Geometry;
import com.jme.scene.Spatial;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import static com.wonderbuilders.modules.colortheme.client.ColorThemeComponent.scaleSubsamplingMaintainAspectRatio;
import static com.wonderbuilders.modules.colortheme.client.ColorThemeComponent.subsampleImage;
import com.wonderbuilders.modules.colortheme.common.ColorTheme;
import com.wonderbuilders.modules.colortheme.common.ColorThemeComponentClientState;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderComponent.AttachPointNode;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 * Color theme component
 */
public class ColorThemeComponent extends CellComponent implements ContextMenuActionListener, SharedMapListenerCli {
    
    /** Logger for messages. */
    private static final Logger LOGGER = Logger.getLogger(ColorThemeComponent.class.getName());
    
    /** Resource bundle for UI strings. */
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com.wonderbuilders.modules.colortheme.client.resources.Bundle");
    
    /** Red color information for color swapping. */
    private static final short[] RED = new short[256];
    
    /** Green color information for color swapping. */
    private static final short[] GREEN = new short[256];
    
    /** Blue color information for color swapping. */
    private static final short[] BLUE = new short[256];
    
    /** Context menu component. Add context menu item here. */
    @UsesCellComponent
    private ContextMenuComponent ctxMenu;
    /** Shared state component for color themes. */
    @UsesCellComponent
    private SharedStateComponent ssc;

    /** Factory for producing menu items. */
    private final ContextMenuFactorySPI ctxMenuFactory;
    
    /** Name of color theme currently in use. */
    private String currentColorTheme;
    
    /** Name of previously used color theme. */
    private String previousTheme;
    
    /** Texture mappings map. */
    private Map<String, String> textureMapping;
    
    // variable used for preview
    private String previewTheme;
    
    /** Static initializer. */
    static {
        for(short i = 0;i < RED.length;i ++){
            RED[i] = i;
            GREEN[i] = i;
            BLUE[i] = i;
        }
    }
    Cell parentCell;
    /**
     * Constructor, takes the Cell associated with the Cell Component.
     * @param cell The Cell associated with this component
     */
    public ColorThemeComponent(Cell cell) {
        super(cell);
        this.parentCell = cell;
        // create context menu item and register ir
        final ContextMenuItem item = new SimpleContextMenuItem(BUNDLE.getString(ColorThemeComponentConstants.KEY_COMPONENT_LABEL),this);
        ctxMenuFactory = new ContextMenuFactorySPI() {

            public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                return new ContextMenuItem[]{item};
            }
        };
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
        currentColorTheme = ((ColorThemeComponentClientState)clientState).getCurrentColorTheme();
        previewTheme = currentColorTheme;
        previousTheme = ((ColorThemeComponentClientState)clientState).getPreviousTheme();
        textureMapping = ((ColorThemeComponentClientState)clientState).getTextureMapping();
        if(this.status == CellStatus.VISIBLE){
            //changeColor(false, null, null);
        }
        
    }
    
    /**
     * Displays preview of color theme without actually applying it to model.
     * 
     * @param themeName name of previewed theme
     * @param themes existing color themes
     * @param previewTextureMap texture map for preview
     */
    public void previewColor(String themeName, Map<String, ColorTheme> themes
            , Map<String, String> previewTextureMap){
        previousTheme = previewTheme;
        previewTheme = themeName;
        changeColor(true,themes,previewTextureMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        // do color change only when cell is in Rendering state
        if(status == CellStatus.RENDERING){
            
            // status change may happen on the Darkstar receiver thread. Change
            // the color in a different thread to avoid deadlock
            Thread t = new Thread(new Runnable() {
                public void run() {
                    //changeColor(false,null, null);
                }
            });
            t.start();
        } else if(status == CellStatus.ACTIVE && increasing){
            // add shared map listener in another thread
            Thread t = new Thread(new Runnable() {

                public void run() {
                    SharedStateComponent ssc = (SharedStateComponent)cell.getCellCache().getEnvironmentCell().getComponent(SharedStateComponent.class);
                    ssc.get(ColorThemeComponentConstants.COLOR_THEME_SHARED_MAP).addSharedMapListener(ColorThemeComponent.this);
                }
            });
            t.start();
        } else if(status == CellStatus.INACTIVE && !increasing){
            // remove shared map listener
            Thread t = new Thread(new Runnable() {

                public void run() {
                    SharedStateComponent ssc = (SharedStateComponent)cell.getCellCache().getEnvironmentCell().getComponent(SharedStateComponent.class);
                    ssc.get(ColorThemeComponentConstants.COLOR_THEME_SHARED_MAP).removeSharedMapListener(ColorThemeComponent.this);
                }
            });
            t.start();
        }
    }
    
    /**
     * Change the color of the model. This can be either a preview or a global change. This is 
     * controlled by <code>preview</code> parameter.
     * 
     * @param preview <code>true</code> if this is only a preview, <code>false</code> otherwise
     */
    private void changeColor(boolean preview, Map<String, ColorTheme> themes, Map<String, String> previewTextureMap) {
        
        if(preview){
            doChangeColor(previousTheme, previewTheme, previewTextureMap,themes);
        } else {
            doChangeColor(previousTheme, currentColorTheme, textureMapping,null);
        }
              

    }
    
    /**
     * Update JME nodes to change the color and textures.
     * 
     * @param prevTheme  name of previously applied color theme
     * @param curTheme  name of current color theme (to be applied)
     * @param txMap  textures mapping
     * @param currentThemes all currently existing themes, including the ones being edited and not submitted to shared state
     */
    private void doChangeColor(final String prevTheme, final String curTheme, final Map<String, String> txMap, Map<String, ColorTheme> currentThemes) {     
        // lazily find the shared map that holds color themes to avoid deadlocks
        
        Cell envCell = cell.getCellCache().getEnvironmentCell();
        SharedStateComponent envssc = envCell.getComponent(SharedStateComponent.class);
        final SharedMapCli sharedMap = envssc.get(ColorThemeComponentConstants.COLOR_THEME_SHARED_MAP);
        
        final Map<String, ColorTheme> paramThemes = new HashMap<String, ColorTheme>();
        for(Entry<String, SharedData> e : sharedMap.entrySet()){
            paramThemes.put(e.getKey(), (ColorTheme)e.getValue());
        }
        if(currentThemes != null){
            paramThemes.putAll(currentThemes);
        }
        
        
        // get the renderer for the cell associated with this component
        CellRendererJME renderer = (CellRendererJME)cell.getCellRenderer(RendererType.RENDERER_JME);
        
        // get the RenderComponent that stores the data for this cell and
        // corresponding scene root. This will work for any cell that
        // has a JME renderer
        RenderComponent rc = renderer.getEntity().getComponent(RenderComponent.class);
        Spatial sceneRoot = rc.getSceneRoot();
        
        // attach points are special types that mark where a parent cell is
        // joined to a client cell. If the root is an attach point, start with
        // the first child.
        if (sceneRoot instanceof AttachPointNode) {
            sceneRoot = ((AttachPointNode) sceneRoot).getChild(0);
        }
        
        // TreeScan is a utility that scans a JME hierarchy and visits each
        // node in the hierarchy. Use this to update the actual properties
        // of each node.
        TreeScan.findNode(sceneRoot, new ProcessNodeInterface() {
            public boolean processNode(final Spatial node) {
                // if we reach an attach point, stop processing any further to
                // avoid changing colors in child cells.
                if (node instanceof AttachPointNode) {
                    return false;
                }
                
                // render color theme change
                applyColorTheme(paramThemes, node, prevTheme, curTheme);
                
                // replace textures, if required
                applyTextureChange(node, txMap, paramThemes, prevTheme,curTheme);
                
                return true;
            }
        });        
    }

    public void actionPerformed(ContextMenuItemEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Applies specified color theme to a given node. This  method will take into account any color theme 
     * previously applied and render colors correctly.
     * 
     * @param themeMap a map containing color themes
     * @param node node to apply a theme to
     * @param previous previous color theme
     * @param current  current color theme
     */
    private void applyColorTheme(Map<String, ColorTheme> themeMap, final Spatial node, String previous, String current) {
        // get the material state for the current node
        final MaterialState ms = (MaterialState) node.getRenderState(StateType.Material);
        // do not apply color theme if texture is present
        final TextureState ts = (TextureState)node.getRenderState(StateType.Texture);
        
        if (ms != null && ts == null) {
            // make changes
            // ...
            // get current node color
            final ColorRGBA oldDifuse = ms.getDiffuse();
            Color col = new Color(oldDifuse.r, oldDifuse.g, oldDifuse.b, oldDifuse.a);
            // find base color for this color theme
            String baseColor = ColorThemeUtils.convertColorToString(col);
            // if another color theme is previously applied, use it as a base color
            if (previous != null) {
                baseColor = themeMap.get(previous).getBaseColor(baseColor);
            }
            if (themeMap != null && current != null && themeMap.containsKey(current)) {
                String colorString = themeMap.get(current).getColorMap().get(baseColor);
                if (colorString != null) {
                    Color c = ColorThemeUtils.convertStringToColor(colorString);
                    float[] comps = new float[3];
                    c.getColorComponents(comps);
                    oldDifuse.set(comps[0], comps[1], comps[2], 0f);

                    // do on MT-Game render thread
                    SceneWorker.addWorker(new WorkCommit() {

                        public void commit() {
                            ms.setDiffuse(oldDifuse);
                            if (node instanceof Geometry) {
                                ((Geometry) node).setDefaultColor(new ColorRGBA(oldDifuse));

                            }

                            // notify the system that this node has been updated
                            ClientContextJME.getWorldManager().addToUpdateList(node);
                        }
                    });
                }

            }
        }
    }
    
    /**
     * Applies texture replacement to specified node.
     * 
     * @param node node to apply texture change to
     * @param mapping  texture mapping
     * @param  themeMap color theme map
     * @param prev previously used theme
     * @param current currently used theme
     */
    private void applyTextureChange(final Spatial node, Map<String, String> mapping, Map<String, ColorTheme> themeMap, String prev, String current) {
         // used for texture finding
        final String serverNameAndPort = cell.getCellCache().getSession().getSessionManager().getServerNameAndPort();
        final TextureState ts = (TextureState) node.getRenderState(StateType.Texture);
        if(ts == null){
            return;
        }
           
         Texture newTx = null;
             
        if (mapping != null && !mapping.isEmpty()) {
            Texture tx = ts.getTexture();
            // replace the texture, if there is a mapping
            if (mapping.containsKey(tx.getImageLocation())) {
                try {
                    URL url = AssetUtils.getAssetURL(mapping.get(tx.getImageLocation()), serverNameAndPort);
                    Texture tex = TextureManager.loadTexture(url, true);
                    // apply theme map to a given texture
                    newTx = replaceTextureColors(tex, themeMap, prev, current);

                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "An error occurred whiel appying texture: {}", ex);
                }

            } else {
                // apply color theme to original texture
                newTx = replaceTextureColors(tx, themeMap, prev, current);
            }
        } else if (current != null) {
            newTx = replaceTextureColors(ts.getTexture(), themeMap, prev, current);
        }
         // do on MT-Game render thread
        if (newTx != null) {
            final Texture finalTex = newTx;
            finalTex.setWrap(Texture.WrapMode.Repeat);
            finalTex.setMinificationFilter(Texture.MinificationFilter.Trilinear);
            finalTex.setApply(Texture.ApplyMode.Replace);

            SceneWorker.addWorker(new WorkCommit() {

                public void commit() {

                    ts.removeTexture(0);
                    ts.setTexture(finalTex);
                    ts.setEnabled(true);

                    node.setRenderState(ts);

                    // notify the system that this node has been updated
                    ClientContextJME.getWorldManager().addToUpdateList(node);
                }
            });
        }
       
    }
    
    /**
     * Replaces colors on texture with corresponding  colors from color theme.
     * 
     * @param tx texture to be processed
     * @param themeMap color theme map
     * @param previous previously used theme
     * @param current currently used theme
     * @return  new texture with replaced colors
     */
    private Texture replaceTextureColors(Texture tx, Map<String, ColorTheme> themeMap, String previous, String current){
        if(current == null || current.equals(ColorTheme.NONE_THEME_NAME)){
            return tx;
        }
        Image img = createAwtImage(tx);
        // lookup table
        short[] r = new short[256];
        short[] g = new short[256];
        short[] b = new short[256];
        
        System.arraycopy(RED, 0, r, 0, RED.length);
        System.arraycopy(GREEN, 0, g, 0, GREEN.length);
        System.arraycopy(BLUE, 0, b, 0, BLUE.length);
        Map<Integer,Integer> rgbs = new HashMap<Integer, Integer>();
        Map<String, String> colMap = themeMap.get(current).getColorMap();
        // set up values for lookup table
        for(Entry<String,String> e : colMap.entrySet()){
//            String colorString = (previous == null) ?  e.getKey() : themeMap.get(previous).getColorMap().get(e.getKey());
            String colorString = e.getKey();
            Color base = ColorThemeUtils.convertStringToColor(colorString);
            Color actual = ColorThemeUtils.convertStringToColor(e.getValue());
            rgbs.put(base.getRGB(), actual.getRGB());
            // set replacement values
            r[(short)base.getRed()] = (short)actual.getRed();
            g[(short)base.getGreen()] = (short)actual.getGreen();
            b[(short)base.getBlue()] = (short)actual.getBlue();
        }
       
        
        //BufferedImage dst = replaceColors(createBufferedImage(img), rgbs);
        /**
         * Create buffered image using subsampling.
         * It will consume less memory than creating buffered image by drawing graphics.
         */
        BufferedImage dst = null;
        try {
            
            InputStream is = AssetUtils.getAssetURL(tx.getImageLocation()).openStream();
            dst = replaceColors(subsampleImage(ImageIO.createImageInputStream(is), 400, 400), rgbs);
        } catch (IOException ex) {
            Logger.getLogger(ColorThemeComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
         Texture replaced =  TextureManager.loadTexture(Toolkit.getDefaultToolkit().createImage(dst.getSource()), 
                                       Texture.MinificationFilter.BilinearNoMipMaps, Texture.MagnificationFilter.Bilinear, true);
        replaced.setImageLocation(tx.getImageLocation());
        return replaced;

    }
    
    /**
     * Creates buffered image from supplied image.
     * 
     * @param image image to process
     * @return  buffered image
     */
    private BufferedImage createBufferedImage(Image image){
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        int widht = (image.getWidth(null) > 0) ?  image.getWidth(null) : 1;
        int height = (image.getHeight(null) > 0) ?  image.getHeight(null) : 1;
        BufferedImage bimage = gc.createCompatibleImage(widht, height);
        Graphics g = bimage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        return bimage;
    }
    
    /**
     * Replace colors on buffered image with specified colors.
     * 
     * @param orig original image
     * @param colors color replacement map
     * @return image with replaced colors
     */
    private BufferedImage replaceColors(BufferedImage orig,Map<Integer,Integer> colors){
        BufferedImage out = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(orig.getWidth(), orig.getHeight());
        Set<Integer> cl = new HashSet<Integer>();
        for(int x = 0;x < orig.getWidth(); x++){
            for(int y = 0;y < orig.getHeight(); y++){
                int val = orig.getRGB(x, y);
                cl.add(val);
                if(colors.containsKey(val)){
                    out.setRGB(x, y, colors.get(val));
                } else {
                    out.setRGB(x, y, val);
                }
            }
        }
        return out;
    }
    
    /**
     * Creates {@code java.awt.Image} from supplied texture.
     * 
     * @param tx texture
     * @return  texture image
     */
    private Image createAwtImage(Texture tx){
        Image img = null;
        try{
            ImageIcon icon = new ImageIcon(AssetUtils.getAssetURL(tx.getImageLocation(), cell.getCellCache().getSession().getSessionManager().getServerNameAndPort()));
            img =  icon.getImage();
        } catch(MalformedURLException ex){
            LOGGER.log(Level.SEVERE, "Invalid texture URL: {}", ex);
        }
        return img;
    }

    /**
     * Listen to shared map change events. This method should force rendering cell if current color 
     * theme is changed.
     * 
     * @param smec shared map event
     */
    public void propertyChanged(SharedMapEventCli smec) {
        ColorTheme old = (ColorTheme)smec.getOldValue();
        ColorTheme newVal = (ColorTheme)smec.getNewValue();
        if((old == null && newVal != null) || (old != null && old.getThemeName().equals(currentColorTheme) && !newVal.equals(old))){
            doChangeColor(currentColorTheme, currentColorTheme, null,null);
        }
    }

    /**
     * Subsampling method to reduce memory consumption
     * 
     * @param inputStream
     * @param x
     * @param y
     * @return
     * @throws IOException 
     */
    public static BufferedImage subsampleImage(
        ImageInputStream inputStream,
        int x,
        int y) throws IOException {
        BufferedImage resampledImage = null;

        Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);

        if(!readers.hasNext()) {
        throw new IOException("No reader available for supplied image stream.");
        }

        ImageReader reader = readers.next();

        ImageReadParam imageReaderParams = reader.getDefaultReadParam();
        reader.setInput(inputStream);

        Dimension d1 = new Dimension(reader.getWidth(0), reader.getHeight(0));
        Dimension d2 = new Dimension(x, y);
        int subsampling = (int)scaleSubsamplingMaintainAspectRatio(d1, d2);
        imageReaderParams.setSourceSubsampling(subsampling, subsampling, 0, 0);

        
        resampledImage = reader.read(0, imageReaderParams);
        reader.removeAllIIOReadProgressListeners();

        return resampledImage;
    }

    public static long scaleSubsamplingMaintainAspectRatio(Dimension d1, Dimension d2) {
        long subsampling = 1;

        if(d1.getWidth() > d2.getWidth()) {
        subsampling = Math.round(d1.getWidth() / d2.getWidth());
        } else if(d1.getHeight() > d2.getHeight()) {
        subsampling = Math.round(d1.getHeight() / d2.getHeight());
        }

        return subsampling;
    }
    
}
