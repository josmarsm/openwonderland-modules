/*
 * Sample code form JME wiki, http://jmonkeyengine.com/wiki/doku.php?id=billboard_awt_label
 */
package org.jdesktop.wonderland.modules.poster.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;

import com.jme.image.Texture;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.math.FastMath;
import com.jme.math.Vector2f;
import com.jme.scene.BillboardNode;
import com.jme.scene.Node;
import com.jme.scene.Spatial.LightCombineMode;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import java.awt.Image;
import java.util.logging.Logger;

public class PosterNode extends Node {


    private static final Logger posterNodeLogger = Logger.getLogger(PosterNode.class.getName());

    private Image image;
    private float blurIntensity = 0.5f;
    private int kernelSize = 5;
    private ConvolveOp blur;
    private Color foreground = new Color(1f, 1f, 1f);
    private Color background = new Color(0f, 0f, 0f);
    private float shadowOffsetX = 1.5f;
    private float shadowOffsetY = 1.5f;
    private Quad quad;
    private int height;
    private float imgWidth = 0f;
    private float imgHeight = 0f;
    private float imgFactor = 0f;

    public PosterNode(Image image) {
        this(image, new Color(1f, 1f, 1f), new Color(0f, 0f, 0f), false, null);
    }

    public PosterNode(Image image, Color foreground, Color background, boolean billboard, Font font) {
        super();
        this.image = image;
        this.foreground = foreground;
        this.background = background;
        height = image.getHeight(null);
        posterNodeLogger.severe("image height: " + height);
        updateKernel();
        //attachChild(getBillboard());
        attachChild(getQuad());
    }

    public void setImage(Image image, Color foreground, Color background) {
        this.image = image;
        this.foreground = foreground;
        this.background = background;

        Quad oldQuad = quad;
        Quad updatedQuad = getQuad();

        if (updatedQuad!=oldQuad) {
            Node tmpParent = oldQuad.getParent();
            oldQuad.removeFromParent();
            TextureState texState = (TextureState) oldQuad.getRenderState(StateType.Texture);
            Texture tex = texState.getTexture();
            TextureManager.releaseTexture(tex);
            tmpParent.attachChild(updatedQuad);
        }
    }

    public void setShadowOffsetX(int offsetPixelX) {
        shadowOffsetX = offsetPixelX;
    }

    public void setShadowOffsetY(int offsetPixelY) {
        shadowOffsetY = offsetPixelY;
    }

    public void setBlurSize(int kernelSize) {
        this.kernelSize = kernelSize;
        updateKernel();
    }

    public void setBlurStrength(float strength) {
        this.blurIntensity = strength;
        updateKernel();
    }

    private void updateKernel() {
        float[] kernel = new float[kernelSize * kernelSize];
        Arrays.fill(kernel, blurIntensity);
        blur = new ConvolveOp(new Kernel(kernelSize, kernelSize, kernel));
    }

    /**
     * Generate an image of the label
     *
     * @param scaleFactors is set to the factors needed to adjust texture coords
     * to the next power-of-two-sized resulting image
     */
    private BufferedImage getImage(Vector2f scaleFactors) {

        // calculate the size of the label image rendered with the specified font
        int width = image.getWidth(null);
        posterNodeLogger.severe("image width: " + width);
        int height = image.getHeight(null);
        posterNodeLogger.severe("image height: " + width);

        // calculate the width of the label with shadow and blur
        int actualWidth = (int) (width + kernelSize + 1 + shadowOffsetX);
        posterNodeLogger.severe("actual width: " + actualWidth);

        // calculate the maximum height of the image including the ascents and
        // descents of the characters
        int actualHeight = (int) (height + kernelSize + 1 + shadowOffsetY);
        posterNodeLogger.severe("actual height: " + actualHeight);


        // determine the closest power of two bounding box
        //
        // NOTE: we scale the image height to fit the nearest power or two, and
        // then scale the image width equally to maintain the correct aspect
        // ratio:
        int desiredHeight = FastMath.nearestPowerOfTwo(actualHeight);
        posterNodeLogger.severe("desired height: " + desiredHeight);

        int desiredWidth = FastMath.nearestPowerOfTwo((int) (((float) desiredHeight / (float) actualHeight) * actualWidth));
        posterNodeLogger.severe("desired width: " + desiredWidth);

        // set the scale factors for scaling the image to fit the nearest power
        // of two bounding box:
        if (scaleFactors != null) {
            // scale the image vertically to fit the height
            scaleFactors.y = (float) desiredHeight / actualHeight;
            // scale the image an equal amount horizontally to maintain aspect ratio
            scaleFactors.x = scaleFactors.y;
        }

        // create an image to render the image onto
        BufferedImage tmp0 = new BufferedImage(desiredWidth, desiredHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) tmp0.getGraphics();

        // center the image on the label
        int scaledWidth = (int) (actualWidth * scaleFactors.x);
        int textX = desiredWidth / 2 - scaledWidth / 2;// + kernelSize / 2;
        int textY = desiredHeight / 2;

        //g2d.setColor(background);
        //g2d.drawImage(image, (int) (textX + shadowOffsetX), (int) (textY + shadowOffsetY), null);

        // blur the image
        //BufferedImage ret = blur.filter(tmp0, null);

        // draw the blurred image over the shadow
        //g2d = (Graphics2D) ret.getGraphics();
        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(foreground);
        g2d.drawImage(image, textX, textY, null);

        //return ret;
        return tmp0;
    }

    private Quad getQuad() {
        Vector2f scales = new Vector2f();
        Image img = getImage(scales);
        img = image;
        float w = img.getWidth(null);
        float h = img.getHeight(null);
        float factor = height / h;
        factor = 0.0078125f; // 1/128
        factor = factor * 2;
        Quad ret;

        if (imgWidth==w && imgHeight==h && imgFactor==factor) {
            // Reuse quad and texture
            ret = quad;
            TextureState texState = (TextureState) quad.getRenderState(StateType.Texture);
            Texture oldtex = texState.getTexture();
            // Not sure why this does not work, instead release the current texture and create a new one.
//            oldtex.setImage(TextureManager.loadImage(img, true));
//            texState.setTexture(oldtex);
            TextureManager.releaseTexture(oldtex);

            Texture tex = TextureManager.loadTexture(img, MinificationFilter.BilinearNoMipMaps, MagnificationFilter.Bilinear, true);

            texState.setTexture(tex);
            //end workaround
        } else {
            ret = new Quad("textLabel2d", w * factor, h * factor);
            TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
            Texture tex = TextureManager.loadTexture(img, MinificationFilter.BilinearNoMipMaps, MagnificationFilter.Bilinear, true);

            ts.setTexture(tex);
            ts.setEnabled(true);
            ret.setRenderState(ts);

            BlendState as = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
            as.setBlendEnabled(false);
            as.setReference(0.5f);
            as.setTestFunction(BlendState.TestFunction.GreaterThan);
            as.setTestEnabled(true);
            ret.setRenderState(as);

            ret.setLightCombineMode(LightCombineMode.Off);
            ret.updateRenderState();
            this.quad = ret;
            imgWidth = w;
            imgHeight = h;
            imgFactor = factor;
        }

        return ret;
    }

    private BillboardNode getBillboard() {
        BillboardNode bb = new BillboardNode("bb");
        bb.attachChild(getQuad());
        return bb;
    }
}