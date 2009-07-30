/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.thoughtbubbles.client.jme.cell;

import com.jme.image.Texture;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.math.FastMath;
import com.jme.math.Vector2f;
import com.jme.renderer.Renderer;
import com.jme.scene.BillboardNode;
import com.jme.scene.Node;
import com.jme.scene.Spatial.LightCombineMode;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.BlendState.TestFunction;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 *
 * @author drew
 */
public class ThoughtBubbleTextNode extends Node {

    private String text;
    private float blurIntensity = 0.1f;
    private int kernelSize = 5;
    private ConvolveOp blur;
    private Color foreground = new Color(1f, 1f, 1f);
    private Color background = new Color(0f, 0f, 0f);
    private float fontResolution = 40f;
    private int shadowOffsetX = 2;
    private int shadowOffsetY = 2;
    private Font font;
    private Font drawFont;
    private float height = 1f;
    private FontRenderContext fontRenderContext = null;
    private Quad quad;

    // In pixels.
    public static final float WRAPPING_WIDTH = 50;

    private static final Logger logger =
        Logger.getLogger(ThoughtBubbleTextNode.class.getName());


    public ThoughtBubbleTextNode(String text) {

        if(text.equals(""))
            this.text = " ";
        else
            this.text = text;
        this.foreground = Color.white;
        this.background = Color.black;
//        this.height = height;
        updateKernel();
        if (font == null) {
            font = Font.decode("Sans PLAIN 40");
        }
        setFont(font);
        attachChild(getBillboard());
    }


    private BillboardNode getBillboard() {
        BillboardNode bb = new BillboardNode("bb");
        bb.attachChild(getQuad());
        return bb;
    }
   private BufferedImage getImage(Vector2f scaleFactors) {

        // calculate the size of the label text rendered with the specified font
        TextLayout layout = new TextLayout(text, font, fontRenderContext);
        Rectangle2D b = layout.getBounds();

        // calculate the width of the label with shadow and blur
        int actualWidth = (int) (b.getWidth() + kernelSize + 1 + shadowOffsetX);

        // calculate the maximum height of the text including the ascents and
        // descents of the characters
        int actualHeight = (int) (layout.getAscent() + layout.getDescent() + kernelSize + 1 + shadowOffsetY);

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
        g2d.setFont(drawFont);

//        // draw debugging text alignment lines
//        g2d.setColor(Color.YELLOW);
//        g2d.drawLine(0, desiredHeight / 2, desiredWidth, desiredHeight / 2);
//        g2d.drawLine(desiredWidth / 2, 0, desiredWidth / 2, desiredHeight);

        // center the text on the label
        int scaledWidth = (int) (actualWidth * scaleFactors.x);
        int textX = desiredWidth / 2 - scaledWidth / 2 + 8;// + kernelSize / 2;
        int textY = desiredHeight / 2 + 8;

//        // draw debugging text left and right bounds lines
//        g2d.setColor(Color.RED);
//        g2d.drawLine(textX, 0, textX, desiredHeight);
//        g2d.drawLine(desiredWidth / 2 + scaledWidth / 2, 0, desiredWidth / 2 + scaledWidth / 2, desiredHeight);

        // draw the shadow of the text
        g2d.setFont(drawFont);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(background);
        g2d.drawRoundRect(textX + shadowOffsetX, 0 + shadowOffsetY, actualWidth, actualHeight, 5, 5);
        g2d.drawString(text, textX + shadowOffsetX, textY + shadowOffsetY);

        // blur the text
        BufferedImage ret = blur.filter(tmp0, null);

        // draw the blurred text over the shadow
        g2d = (Graphics2D) ret.getGraphics();
        g2d.setFont(drawFont);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(foreground);
        g2d.drawRoundRect(textX, 0, actualWidth, actualHeight, 5, 5);
        g2d.drawString(text, textX, textY);


        // draw an outline.

        return ret;
    }

    /**
     * Generate an image of the label
     *
     * Overriding the parent classes implementation to provide
     * for fixed widths and proper text wrapping. 
     *
     * @param scaleFactors is set to the factors needed to adjust texture coords
     * to the next power-of-two-sized resulting image
     */
//    private BufferedImage getImage(Vector2f scaleFactors) {
//
//        // Do the type conversion dance to get an iterator that
//        // the line break system can work with. These objects
//        // bind the character-level styles to the text, so layout
//        // can make the right decisions about how to flow them.
//        AttributedString asText = new AttributedString(text);
//        asText.addAttribute(TextAttribute.FONT, font);
//        AttributedCharacterIterator asItr = asText.getIterator();
//
//        LineBreakMeasurer lines = new LineBreakMeasurer(asItr, fontRenderContext);
//
//        // Loop through the layouts once, to figure out how much space we're going
//        // to need.
//        int totalHeight = 0;
//        int maxWidth = 0;
//        List<TextLayout> layouts = new ArrayList<TextLayout>();
//        while(lines.getPosition() < asItr.getEndIndex()) {
//            TextLayout thisLine = lines.nextLayout(WRAPPING_WIDTH);
//            layouts.add(thisLine);
//
//            Rectangle2D bounds = thisLine.getBounds();
//
//            if(bounds.getWidth() > maxWidth)
//                maxWidth = (int) bounds.getWidth();
//
//            // Copying this from the original size stuff. Having each of these may be overkill for
//            // every line. We'll see.
//            totalHeight += (int)(thisLine.getAscent() + thisLine.getDescent() + kernelSize + 1 + shadowOffsetY);
//        }
//
//        logger.warning("done layout. total lines: " + layouts.size());
//        logger.warning("maxWidth: " + maxWidth + "; totalHeight: " + totalHeight);
//
//        // determine the closest power of two bounding box
//        //
//        // NOTE: we scale the text height to fit the nearest power or two, and
//        // then scale the text width equally to maintain the correct aspect
//        // ratio:
//        int desiredHeight = FastMath.nearestPowerOfTwo(totalHeight);
//        int desiredWidth = FastMath.nearestPowerOfTwo((int) (((float) desiredHeight / (float) totalHeight) * maxWidth));
//
//        // set the scale factors for scaling the text to fit the nearest power
//        // of two bounding box:
//        if (scaleFactors != null) {
//            // scale the text vertically to fit the height
//            scaleFactors.y = (float) desiredHeight / totalHeight;
//            // scale the text an equal amount horizontally to maintain aspect ratio
//            scaleFactors.x = scaleFactors.y;
//        }
//
//
//
//        // create an image to render the text onto
//        BufferedImage img = new BufferedImage(desiredWidth, desiredHeight, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g2d = (Graphics2D) img.getGraphics();
//        g2d.setFont(drawFont);
//
//
//
//
//
//        // now loop through the layouts again, this time rendering each one.
//        // aggregate the initial positions over the whole process.
//        Point pos = new Point(0, 0);
//        for(TextLayout layout : layouts) {
//            img = drawLineOfText(img, layout, pos);
//            pos.y += layout.getAscent() + layout.getDescent() + kernelSize + 1 + shadowOffsetY;
//        }
//
//        this.height = totalHeight;
//        return img;
//    }

    private BufferedImage drawLineOfText(BufferedImage img, TextLayout line, Point pos) {

        
        //        // draw debugging text alignment lines
//        g2d.setColor(Color.YELLOW);
//        g2d.drawLine(0, desiredHeight / 2, desiredWidth, desiredHeight / 2);
//        g2d.drawLine(desiredWidth / 2, 0, desiredWidth / 2, desiredHeight);

        // unpack the argument so it fits with the original code better.
        int textX = pos.x;
        int textY = pos.y;

//        // draw debugging text left and right bounds lines
//        g2d.setColor(Color.RED);
//        g2d.drawLine(textX, 0, textX, desiredHeight);
//        g2d.drawLine(desiredWidth / 2 + scaledWidth / 2, 0, desiredWidth / 2 + scaledWidth / 2, desiredHeight);

        Graphics2D g2d = (Graphics2D) img.getGraphics();

        // draw the shadow of the text
        g2d.setFont(drawFont);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(background);
        line.draw(g2d, textX + shadowOffsetX, textY + shadowOffsetY);

        // blur the text
//        BufferedImage ret = blur.filter(img, null);

        // draw the blurred text over the shadow
        g2d = (Graphics2D) img.getGraphics();
        g2d.setFont(drawFont);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(foreground);
        line.draw(g2d, textX, textY);

        return img;
    }

    private Quad getQuad() {
        Vector2f scales = new Vector2f();
        BufferedImage img = getImage(scales);

        float w = img.getWidth();
        float h = img.getHeight();
        float factor = height / h;

        Quad ret = new Quad("textLabel2d", w * factor, h * factor);
        TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        Texture tex = TextureManager.loadTexture(img, MinificationFilter.BilinearNoMipMaps, MagnificationFilter.Bilinear, true);

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

        ret.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);

        BlendState as = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
        as.setBlendEnabled(true);
        as.setTestEnabled(true);
        as.setTestFunction(TestFunction.GreaterThan);
        as.setEnabled(true);
        ret.setRenderState(as);

        ret.setLightCombineMode(LightCombineMode.Off);
        ret.updateRenderState();
        this.quad = ret;
        return ret;
    }

        public void setFont(Font font) {
        this.font = font;
        BufferedImage tmp0 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) tmp0.getGraphics();
        drawFont = font.deriveFont(fontResolution);

        fontRenderContext = g2d.getFontRenderContext();
    }

    public void setText(String text, Color foreground, Color background) {
        this.text = text;
        this.foreground = foreground;
        this.background = background;
        Node tmpParent = quad.getParent();
        quad.removeFromParent();
        TextureState texState = (TextureState) quad.getRenderState(StateType.Texture);
        Texture tex = texState.getTexture();
        TextureManager.releaseTexture(tex);
        tmpParent.attachChild(getQuad());
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

    public void setFontResolution(float fontResolution) {
        this.fontResolution = fontResolution;
    }

    private void updateKernel() {
        float[] kernel = new float[kernelSize * kernelSize];
        Arrays.fill(kernel, blurIntensity);
        blur = new ConvolveOp(new Kernel(kernelSize, kernelSize, kernel));
    }

}
