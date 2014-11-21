/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.appframe.client;

import com.jme.image.Texture;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.sun.awt.AWTUtilities;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.awt.image.VolatileImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.login.LoginManager;
import static org.jdesktop.wonderland.modules.appframe.client.ColorChange.getImageFromArray;
import static org.jdesktop.wonderland.modules.appframe.client.ColorChange.handlepixels;
import static org.jdesktop.wonderland.modules.appframe.client.ColorChange.parentCell;
import static org.jdesktop.wonderland.modules.appframe.client.ColorChange.textureURL;

/**
 *
 * @author jkaplan
 */
public class ColorChange {

    private static final Logger LOGGER =
            Logger.getLogger(AppFrameProperties.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org.jdesktop.wonderland.modules.appframe.client.resources.Bundle");
    public static AppFrame parentCell;
    public static URL textureURL;

    public ColorChange(AppFrame parentCell) {
        this.parentCell = parentCell;
    }

    /**
     * Change the color of the model by replacing one color with another.
     *
     * @param cell the cell to apply the color change to
     * @param fromColor the original color
     * @param toColor the color the replace with
     */
    public void changeColor(final Cell cell,
            final Color fromColor, final Color toColor, boolean flag) {
        // get the renderer for the cell associated with this component
        CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
        // get the RenderComponent that stores the data for this cell and
        // corresponding scene root. This will work for any cell that
        // has a JME renderer
        RenderComponent rc = renderer.getEntity().getComponent(RenderComponent.class);
        final Node sceneRoot = rc.getSceneRoot();
        Spatial inner = sceneRoot.getChild("geom-Frame_inner_01-mat_inner");
        if (flag) {
            applyColorChange(inner, new Color(0, 0, 0), toColor, "inner", flag);
        } else {
            applyColorChange(inner, fromColor, new Color(0, 0, 0), "inner", flag);
        }
        Spatial button1 = sceneRoot.getChild("App Frame Button quad1");
        applyColorChange(button1, fromColor, toColor, "button", flag);
        Spatial button2 = sceneRoot.getChild("App Frame Button quad2");
        applyColorChange(button2, fromColor, toColor, "button", flag);
        Spatial button3 = sceneRoot.getChild("App Frame Button quad3");
        applyColorChange(button3, fromColor, toColor, "button", flag);
        Spatial button4 = sceneRoot.getChild("App Frame Button quad4");
        applyColorChange(button4, fromColor, toColor, "button", flag);

    }

    /**
     * Applies specified color change to a given node.
     *
     * @param node node to apply a change to
     * @param from the color to replace
     * @param to the color to replace it with
     */
    private static void applyColorChange(final Spatial node,
            final Color from, final Color to, final String type, final boolean flag) {
        // get the material state for the current node
        try {
            final MaterialState ms = (MaterialState) node.getRenderState(StateType.Material);
            final TextureState ts = (TextureState) node.getRenderState(StateType.Texture);
            // skip the color change if there is no material or texture state
            if (ms == null && ts == null) {
                return;
            }

            SceneWorker.addWorker(new WorkCommit() {

                public void commit() {
                    // decide whether to change the texture colors or the diffuse
                    // color
                    if (ts != null) {
                        // replace colors in a texture
                        textureURL = getClass().getResource("resources/button_transparent_03.png");
                        ts.setTexture(replaceTextureColors(ts.getTexture(), from, to, type, flag));
                    } else if (ms != null) {
                    }
                    // notify the system that this node has been updated
                    ClientContextJME.getWorldManager().addToUpdateList(node);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
    +     * Replaces colors on texture.
    +     *
    +     * @param tx texture to be processed
    +     * @param from the color to replace
    +     * @param to the color to replace it with
    +     * @return  new texture with replaced colors
    +     */
    private static Texture replaceTextureColors(Texture tx,
            Color from, Color to, String type, boolean flag) {
        try {
            java.awt.Image img = createAwtImage(tx, type);
            BufferedImage dst;
            Texture replaced = null;
            if (type.equals("button")) {
                if (!flag) {
                    replaced = TextureManager.loadTexture(textureURL);
                    replaced.setApply(Texture.ApplyMode.Replace);
                    replaced.setBlendColor(ColorRGBA.white);
                    replaced.setImageLocation(tx.getImageLocation());
                    return replaced;


                } else {

                    GraphicsConfiguration translucencyCapableGC = null;
                    GraphicsEnvironment ge =
                            GraphicsEnvironment.getLocalGraphicsEnvironment();
                    GraphicsDevice[] devices = ge.getScreenDevices();
                    for (int i = 0; i < devices.length && translucencyCapableGC == null; i++) {

                        GraphicsConfiguration[] configs = devices[i].getConfigurations();
                        for (int j = 0; j < configs.length && translucencyCapableGC == null; j++) {
                            if (AWTUtilities.isTranslucencyCapable(configs[j])) {
                                translucencyCapableGC = configs[j];
                            }

                        }
                    }
                    java.awt.Image oldImg = new ImageIcon(img).getImage();
                    int width = (oldImg.getWidth(null) > 0) ? oldImg.getWidth(null) : 1;
                    int height = (oldImg.getHeight(null) > 0) ? oldImg.getHeight(null) : 1;
                    VolatileImage bimage = translucencyCapableGC.createCompatibleVolatileImage(width, height, Transparency.TRANSLUCENT);
                    Graphics2D g = bimage.createGraphics();
                    g.setComposite(AlphaComposite.Src);

                    g.drawImage(oldImg, 0, 0, null);
                    g.dispose();
                    java.awt.Image newImage = handlepixels(bimage, 0, 0, bimage.getWidth(), bimage.getHeight(), from, to);

                    replaced = TextureManager.loadTexture(newImage,
                            Texture.MinificationFilter.BilinearNoMipMaps, Texture.MagnificationFilter.Bilinear, true);
                    replaced.setApply(Texture.ApplyMode.Replace);
                    //  replaced.setBlendColor(ColorRGBA.white);

                    replaced.setImageLocation(tx.getImageLocation());

                }
            } else {
                dst = replaceColors(createBufferedImage(img), from, to);
                replaced = TextureManager.loadTexture(Toolkit.getDefaultToolkit().createImage(dst.getSource()),
                        Texture.MinificationFilter.BilinearNoMipMaps, Texture.MagnificationFilter.Bilinear, true);
                replaced.setImageLocation(tx.getImageLocation());

            }
            return replaced;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
    +     * Creates {@code java.awt.Image} from supplied texture.
    +     *
    +     * @param tx texture
    +     * @return texture image
    +     */
    private static java.awt.Image createAwtImage(Texture tx, String type) {
        try {
            // get the server information
            String server = LoginManager.getPrimary().getServerNameAndPort();

            // load texture as an image
            if (type.equals("inner")) {
                return Toolkit.getDefaultToolkit().createImage(AssetUtils.getAssetURL(tx.getImageLocation(), server));

            } else {
                return Toolkit.getDefaultToolkit().createImage(AssetUtils.getAssetURL("wla://app-frame/button_transparent_03.png", parentCell));
            }
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, "Invalid texture URL: {}", ex);
            return null;
        }
    }

    /**
    +     * Creates buffered image from supplied image.
    +     *
    +     * @param image image to process
    +     * @return  buffered image
    +     */
    public static java.awt.Image handlepixels(VolatileImage bufImg, int x, int y, int w, int h, Color from, Color to) {
        try {
            int[] pixels = new int[w * h * 4];
            PixelGrabber pg = new PixelGrabber(bufImg, x, y, w, h, pixels, 0, w);
            try {
                pg.grabPixels();
            } catch (InterruptedException e) {
                System.err.println("interrupted waiting for pixels!");
                return null;
            }
            if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
                System.err.println("image fetch aborted or errored");
                return null;
            }
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    int val = pixels[j * w + i];
                    if (val == from.getRGB()) {
                        pixels[j * w + i] = to.getRGB();
                    }
//                } else {
//                    pixels[j * w + i] = val;
//                }
                }
            }
            return getImageFromArray(pixels, w, h);
        } catch (Exception ei) {
            ei.printStackTrace();
        }
        return null;
    }

    public static java.awt.Image getImageFromArray(int[] pixels, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }

    private static BufferedImage createBufferedImage(java.awt.Image image) {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            int width, height;

            width = (image.getWidth(null) > 0) ? image.getWidth(null) : 1;
            height = (image.getHeight(null) > 0) ? image.getHeight(null) : 1;
            BufferedImage bimage = gc.createCompatibleImage(width, height);
            Graphics g = bimage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();

            return bimage;
        } catch (Exception ei) {
            ei.printStackTrace();
        }
        return null;
    }

    /**
    +     * Replace colors on buffered image with specified colors.
    +     *
    +     * @param orig original image
    +     * @param colors color replacement map
    +     * @return image with replaced colors
    +     */
    private static BufferedImage replaceColors(BufferedImage orig,
            Color from, Color to) {
        try {
            BufferedImage out;
            out = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(orig.getWidth(), orig.getHeight());
            for (int x = 0; x < orig.getWidth(); x++) {
                for (int y = 0; y < orig.getHeight(); y++) {
                    int val = orig.getRGB(x, y);
                    if (val == from.getRGB()) {
                        out.setRGB(x, y, to.getRGB());

                    } else {
                        out.setRGB(x, y, val);
                    }
                }
            }
            return out;

        } catch (Exception ei) {
            ei.printStackTrace();
        }
        return null;
        //   }
    }
}
