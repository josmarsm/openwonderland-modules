/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *
 *  Copyright (c) 2010-12, University of Essex, UK, 2010-12, All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package uk.ac.essex.wonderland.modules.twitter.client;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.renderer.Renderer;
import com.jme.scene.BillboardNode;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.BlendState.TestFunction;

import com.jme.system.DisplaySystem;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.util.logging.Level;

import java.util.logging.Logger;
import com.jme.util.TextureManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import twitter4j.Tweet;


/**
 * A billboarding node that attaches graphics representing tweet.
 *
 * Heavily modified from SampleDisplayNode
 * @author Bernard Horan
 */
public class TweetNode extends BillboardNode {

    private static final Logger logger = Logger.getLogger(TweetNode.class.getName());

    // Default Colors
    private final static Color DEFAULT_FONT_COLOR = Color.BLACK;
    private final static Color BORDER_COLOR = new Color(0, 0.75f, 0.75f);

    /** base fonts */
    private final static Font TEXT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private final static Font AUTHOR_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 12);

    /** controls size of fonts */
    private float fontSizeModifier;

    // padding between text and edges
    private final static int PADDING_LEFT = 20;
    private final static int PADDING_RIGHT = 20;
    private final static int PADDING_TOP = /*5*/ 20;
    private final static int PADDING_BOTTOM = /*5*/20;
    /** padding between Author and Text */
    private final static int PADDING_LINE = 5;
    /** padding between icon and text */
    private final static int PADDING_ICON = 8;

    /** width of border */
    private final static int BORDER_WIDTH = 6;

    private final static int TEXT_WIDTH = 200;
    private final static int ICON_WIDTH = 48;


    // strings pulled from tweet this node represents
    private String author;
    private String text;
    private Icon profileImageIcon;

    private Quad quad;

    private float height3D;

    public TweetNode(Tweet tweet, float fontMod){
        super("Tweet node for: " + tweet);
        fontSizeModifier = fontMod;

        author = tweet.getFromUser();
        text = tweet.getText();
        profileImageIcon = getProfileIcon(tweet);
        
        update();
    }
  
    private void update () {
        // build child
        if (quad != null) {
            detachChild(quad);
        }
        quad = getQuad();
        attachChild(quad);

        // set bounds to make pickable
        setModelBound(new BoundingBox());
        updateModelBound();
    }


    /**
     * Generate an image of the label
     */
    private BufferedImage getImage() {
        // calculate the size of the label text rendered with the specified TEXT_FONT
        FontRenderContext frc = getFontRenderContext();
    
        //Author
        TextLayout authorLayout = new TextLayout(author, AUTHOR_FONT, frc);

        // prepare and split up text
        // split into lines
        String [] lines = text.split("\n");
        // make each line fit into TEXT_WIDTH
        ArrayList<TextLayout> chunks = new ArrayList<TextLayout>();
        for(String s:lines){
            splitText(chunks, s);
        }

        // now we can do the heights
        // calculate the maximum height of the text including the ascents and
        // descents of the characters, both lines, padding between lines
        int textHeight = getImageHeight(authorLayout, chunks);

        
        int authorHeight= (int)(authorLayout.getAscent() + authorLayout.getDescent());
        TextLayout aLine = chunks.get(0);
        int actualTextLineHeight = (int)(aLine.getAscent() + aLine.getDescent());
        
        // create an image to render the text onto
        int totalWidth = PADDING_LEFT + ICON_WIDTH + PADDING_ICON + TEXT_WIDTH + PADDING_RIGHT;
        BufferedImage tweetImage = new BufferedImage(totalWidth+ BORDER_WIDTH*2, textHeight+BORDER_WIDTH*2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) tweetImage.getGraphics();

        // draw background
        int x = 0 + BORDER_WIDTH;
        int y = 0 + BORDER_WIDTH;

        int h = textHeight;
        int w = totalWidth;

        int arc = 60;

        // draw background rectangle
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(x, y, w, h, arc, arc);

        // draw border
        g2d.setStroke(new BasicStroke(BORDER_WIDTH));
        g2d.setColor(BORDER_COLOR);

        g2d.setPaintMode();
        g2d.drawRoundRect(x, y, w, h, arc, arc);

        // used to draw
        int textX = 0;
        int textY = 0;

        //draw the profile image icon
        profileImageIcon.paintIcon(null, g2d, PADDING_LEFT, PADDING_TOP);

        // draw author text
        textX = PADDING_LEFT + ICON_WIDTH + PADDING_ICON;
        textY = authorHeight + PADDING_TOP;
        g2d.setFont(AUTHOR_FONT);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(DEFAULT_FONT_COLOR);
        g2d.drawString(author, textX, textY);

        // draw the tweet text
        textY += authorHeight + PADDING_LINE;
        g2d.setFont(TEXT_FONT);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(DEFAULT_FONT_COLOR);
        for(TextLayout t:chunks){
            t.draw(g2d, textX, textY);
            textY += actualTextLineHeight + PADDING_LINE;
        }
        
        return tweetImage;
    }







    // -------------------------------------------
    // helper functions for getImage
    // -------------------------------------------

    /**
     * Calculate the appropriate height of the image
     *
     * Used by getImage.
     * @return the actual height the image should have
     * @param authorLayout TextLayout of createdAt
     * @param chunks contains annotation's text, broken up into lines
     */
    private int getImageHeight(TextLayout authorLayout, ArrayList<TextLayout> chunks) {
        int ret = PADDING_BOTTOM + PADDING_TOP;
        ret += (int) (authorLayout.getAscent() + authorLayout.getDescent() +
                       + PADDING_LINE);

        // also add lines of text from chunks to height
        for (TextLayout t : chunks) {
            ret += (int) (t.getAscent() + t.getDescent());
            ret += PADDING_LINE;
        }

        return ret;
    }

    /**
     * Calculates width of text, splits onto multiple lines of maximum length
     * lineWidth if necessary. Stores line(s) in the chunks ArrayList.
     * @param chunks ArrayList to store line(s) in
     * @param lineWidth maximum length of each line
     * @param str string to split
     */
    private void splitText(ArrayList<TextLayout> chunks, String str){
        if(str.length() == 0){
            str = " ";
        }
        FontRenderContext frc = getFontRenderContext();
        TextLayout textLayout = new TextLayout(str,
                                               TEXT_FONT, frc);
        Rectangle2D textRect = textLayout.getBounds();
        // does text need to be split?
        if(textRect.getWidth() > TEXT_WIDTH){

            AttributedString asText = new AttributedString(str);
            asText.addAttribute(TextAttribute.FONT, TEXT_FONT);
            AttributedCharacterIterator asItr = asText.getIterator();

            int start = asItr.getBeginIndex();
            int end = asItr.getEndIndex();

            LineBreakMeasurer line = new LineBreakMeasurer(asItr, frc);
            line.setPosition(start);
            // Get lines from lineMeasurer until the entire
            // paragraph has been displayed.
            while (line.getPosition() < end) {

                // Retrieve next layout.
                // width = maximum line width
                TextLayout layout = line.nextLayout(TEXT_WIDTH);
                chunks.add(layout);
            }
        }
        else{
            chunks.add(textLayout);
        }
    }

    /**
     * A quad to display the image created in getImage
     * @return
     */
    private Quad getQuad() {
        final BufferedImage img = getImage();
        if(img == null){
            logger.severe("[tweet node] image is null!!!");
        }

        float w = img.getWidth();
        float h = img.getHeight();
    
        height3D = h * fontSizeModifier;
        final Quad ret = new Quad("tweet node", w * fontSizeModifier, height3D);

        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
                Texture tex = TextureManager.loadTexture(img, MinificationFilter.BilinearNoMipMaps, MagnificationFilter.Bilinear, true);
                
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

                ClientContextJME.getWorldManager().addToUpdateList(TweetNode.this);
            }
        }, null);

        return ret;
    }

    private FontRenderContext getFontRenderContext(){
        BufferedImage tmp0 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) tmp0.getGraphics();
        return g2d.getFontRenderContext();
    }

    private Icon getProfileIcon(Tweet aTweet) {
        try {
            URL profileImageURL = new URL(aTweet.getProfileImageUrl());
            //Create an image icon from the URL
            ImageIcon icon = new ImageIcon(profileImageURL, aTweet.getFromUser());
            int iconHeight = icon.getIconHeight();
            //If the icon is wrong size
            if (iconHeight != ICON_WIDTH) {
                Image img = icon.getImage();
                Image scaledImage = img.getScaledInstance(ICON_WIDTH, ICON_WIDTH, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaledImage);
            }
            return icon;
        } catch (MalformedURLException ex) {
            Logger.getLogger(TweetNode.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Workaround for animation framework.
     * Superclass has two setLocalScale methods with different signatures,
     * but the trident animation framework gets confused.
     * @param scale the sclae of the node
     */
    public void setScale(float scale) {
        setLocalScale(scale);
    }
 
}
