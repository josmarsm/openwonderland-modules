/*
 * Sample code form JME wiki, http://jmonkeyengine.com/wiki/doku.php?id=billboard_awt_label
 */
package org.jdesktop.wonderland.modules.cmu.client.jme.cellrenderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import com.jme.image.Texture;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.math.FastMath;
import com.jme.math.Matrix3f;
import com.jme.math.Vector2f;
import com.jme.scene.Node;
import com.jme.scene.Spatial.LightCombineMode;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
//TODO: delete this
public class TextLabelCopy extends Node {

    private String text;
//    private float blurIntensity = 0.1f;
//    private int kernelSize = 5;
//    private ConvolveOp blur;
    private Color foreground = new Color(1f, 1f, 1f);
//    private Color background = new Color(0f, 0f, 0f);
//    private float fontResolution = 20f;
//    private int shadowOffsetX = 2;
//    private int shadowOffsetY = 2;
    private Font font;
//    private Font drawFont;
    private float height = 1f;
    private FontRenderContext fontRenderContext = null;
    private Quad quad;

    public TextLabelCopy(String text) {
//        this(text, new Color(1f, 1f, 1f), new Color(0f, 0f, 0f), 5.3f, false, null);
        this(text, new Color(1, 1, 1), 5.3f, null);
    }

    public TextLabelCopy(String text, Color foreground,// Color background,
            float height, //boolean billboard,
            Font font) {
        super();
        this.text = text;
        this.foreground = foreground;
//        this.background = background;
        this.height = height;
        //updateKernel();
        if (font == null) {
            font = Font.decode("Sans PLAIN 20");
        }
        setFont(font);
        attachChild(getQuad());
    }

    public void setFont(Font font) {
        this.font = font;
        BufferedImage tmp0 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) tmp0.getGraphics();
        //drawFont = font.deriveFont(fontResolution);
//        drawFont = font;

        fontRenderContext = g2d.getFontRenderContext();
        this.height = getActualHeight();
    }

    protected int getActualWidth() {
        // calculate the size of the label text rendered with the specified font
        TextLayout layout = new TextLayout(text, font, fontRenderContext);
        Rectangle2D b = layout.getBounds();

        // calculate the width of the label with shadow and blur
        return (int) (b.getWidth());
    }

    protected int getActualHeight() {
        // calculate the size of the label text rendered with the specified font
        TextLayout layout = new TextLayout(text, font, fontRenderContext);

        // calculate the maximum height of the text including the ascents and
        // descents of the characters
        return (int) (layout.getAscent() + layout.getDescent());
    }

    public void setText(String text, Color foreground) {//, Color background) {
        this.text = text;
        this.foreground = foreground;
//        this.background = background;
        Node tmpParent = quad.getParent();
        quad.removeFromParent();
        TextureState texState = (TextureState) quad.getRenderState(StateType.Texture);
        Texture tex = texState.getTexture();
        TextureManager.releaseTexture(tex);
        tmpParent.attachChild(getQuad());
    }

//    public void setShadowOffsetX(int offsetPixelX) {
//        shadowOffsetX = offsetPixelX;
//    }
//
//    public void setShadowOffsetY(int offsetPixelY) {
//        shadowOffsetY = offsetPixelY;
//    }

//    public void setBlurSize(int kernelSize) {
//        this.kernelSize = kernelSize;
//        updateKernel();
//    }
//
//    public void setBlurStrength(float strength) {
//        this.blurIntensity = strength;
//        updateKernel();
//    }
//    public void setFontResolution(float fontResolution) {
//        this.fontResolution = fontResolution;
//    }

//    private void updateKernel() {
//        float[] kernel = new float[kernelSize * kernelSize];
//        Arrays.fill(kernel, blurIntensity);
//        blur = new ConvolveOp(new Kernel(kernelSize, kernelSize, kernel));
//    }
    /**
     * Generate an image of the label
     *
     * @param scaleFactors is set to the factors needed to adjust texture coords
     * to the next power-of-two-sized resulting image
     */
    private BufferedImage getImage(Vector2f scaleFactors) {

        // calculate the size of the label text rendered with the specified font
        //TextLayout layout = new TextLayout(text, font, fontRenderContext);
        //Rectangle2D b = layout.getBounds();

        // calculate the width of the label with shadow and blur
        int actualWidth = getActualWidth();//(int) (b.getWidth() + kernelSize + 1 + shadowOffsetX);

        // calculate the maximum height of the text including the ascents and
        // descents of the characters
        int actualHeight = getActualHeight();//(int) (layout.getAscent() + layout.getDescent() + kernelSize + 1 + shadowOffsetY);

        // determine the closest power of two bounding box
        //
        // NOTE: we scale the text height to fit the nearest power or two, and
        // then scale the text width equally to maintain the correct aspect
        // ratio:
        int desiredHeight = FastMath.nearestPowerOfTwo(actualHeight);
        int desiredWidth = FastMath.nearestPowerOfTwo((int) (((float) desiredHeight / (float) actualHeight) * actualWidth));

        // set the scale factors for scaling the text to fit the nearest power
        // of two bounding box:
        if (scaleFactors != null) {
            // scale the text vertically to fit the height
            scaleFactors.y = (float) desiredHeight / actualHeight;
            // scale the text an equal amount horizontally to maintain aspect ratio
            scaleFactors.x = scaleFactors.y;
        }

        // create an image to render the text onto
        BufferedImage tmp0 = new BufferedImage(desiredWidth, desiredHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) tmp0.getGraphics();
//      gd2.setFont(drawFont);
        g2d.setFont(font);

        // center the text on the label
        int scaledWidth = (int) (actualWidth * scaleFactors.x);
        int textX = desiredWidth / 2 - scaledWidth / 2;// + kernelSize / 2;
        int textY = desiredHeight / 2;

        // draw the shadow of the text
//        g2d.setFont(drawFont);
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//        g2d.setColor(background);
//        g2d.drawString(text, textX + shadowOffsetX, textY + shadowOffsetY);

        // blur the text
//        BufferedImage tmp0 = blur.filter(tmp0, null);
//        tmp0 = tmp0;

        // draw the blurred text over the shadow
        g2d = (Graphics2D) tmp0.getGraphics();
//      g2d.setFont(drawFont);
        g2d.setFont(font);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(foreground);
        g2d.drawString(text, textX, textY);

        return tmp0;
    }

    private Quad getQuad() {
        Vector2f scales = new Vector2f();
        BufferedImage img = getImage(scales);

        float w = img.getWidth();
        float h = img.getHeight();
        float factor = height / h;

        Quad ret = new Quad("textLabel2d", w * factor, h * factor);
        ret.setLocalRotation(new Matrix3f(-1, 0, 0, 0, -1, 0, 0, 0, 1));
        TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        Texture tex = TextureManager.loadTexture(img, MinificationFilter.BilinearNoMipMaps, MagnificationFilter.Bilinear, false);

//        TexCoords texCo = ret.getTextureCoords(0);
//        texCo.coords = BufferUtils.createFloatBuffer(16);
//        texCo.coords.rewind();
//        for(int i=0; i < texCo.coords.limit(); i+=2){
//            float u = texCo.coords.get();
//            float v = texCo.coords.get();
//            texCo.coords.put(u*scales.x);
//            texCo.coords.put(v*scales.y);
//        }
//        ret.setTextureCoords(texCo);
//        ret.updateGeometricState(0, true);

//        tex.setScale(new Vector3f(scales.x, scales.y, 1));
        ts.setTexture(tex);
        ts.setEnabled(true);
        ret.setRenderState(ts);

//        ret.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
//
//        BlendState as = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
//        as.setBlendEnabled(true);
//        as.setTestEnabled(true);
//        as.setTestFunction(TestFunction.GreaterThan);
//        as.setEnabled(true);
//        ret.setRenderState(as);
//

        // This makes the text show up lighter....
        ret.setLightCombineMode(LightCombineMode.Off);

        ret.updateRenderState();
        this.quad = ret;
        return ret;
    }
}