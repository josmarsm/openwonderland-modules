/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.poster.client;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;

/**
 * Extension to JEditorPane to resolve images using the Wonderland asset
 * maanger.
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class PosterEditorPane extends JEditorPane {
    private static final Logger LOGGER =
            Logger.getLogger(PosterEditorPane.class.getName());
    
    private final Cell cell;
    
    public PosterEditorPane(Cell cell) {
        this.cell = cell;
        
        setEditorKitForContentType("text/html", new MyHTMLEditorKit());        
        addHyperlinkListener(createHyperlinkListener());
        setEditable(false);
        setFocusable(false);
    }

    @Override
    public void setText(final String t) {
        // make sure we are on the event dispatch thread
        if (SwingUtilities.isEventDispatchThread()) {
            doSetText(t);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    doSetText(t);
                }
            });   
        }
    }
    
    private void doSetText(final String text) {
        setContentType("text/html");
        super.setText(text);
    }
    
    protected HyperlinkListener createHyperlinkListener() {
        return new PosterHyperlinkListener(this, cell);
    }
    
    class MyHTMLEditorKit extends HTMLEditorKit {
        @Override
        public ViewFactory getViewFactory() {
            return new MyHTMLFactory();
        }
    }

    class MyHTMLFactory extends HTMLEditorKit.HTMLFactory {
        @Override
        public View create(Element elem) {
            if (elem.getName().equalsIgnoreCase(HTML.Tag.IMG.toString())) {
                return new MyImageView(elem);
            }

            return super.create(elem);
        }
    }

    class MyImageView extends ImageView {

        private final Image image;
        private final URL imageURL;

        public MyImageView(Element elem) {
            super(elem);

            String src = null;
            for (Enumeration e = elem.getAttributes().getAttributeNames();
                    e.hasMoreElements();) {
                Object key = e.nextElement();
                Object value = elem.getAttributes().getAttribute(key);

                if (key.toString().equalsIgnoreCase("src")) {
                    src = value.toString();
                    break;
                }
            }

            LOGGER.fine("SRC = " + src);
            imageURL = initializeImageURL(src);
            image = initializeImage(imageURL);
        }

        @Override
        public Image getImage() {
            return image;
        }

        @Override
        public URL getImageURL() {
            return imageURL;
        }

        @Override
        public float getMinimumSpan(int axis) {
            if (image == null) {
                return super.getMinimumSpan(axis);
            }

            if (axis == View.X_AXIS) {
                return image.getWidth(null);
            } else {
                return image.getHeight(null);
            }
        }

        private URL initializeImageURL(String uri) {
            try {
                return AssetUtils.getAssetURL(uri, cell);
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "Error initializing image URL", ioe);
                return null;
            }
        }

        private Image initializeImage(URL url) {
            if (url == null) {
                return null;
            }

            try {
                return ImageIO.read(url);
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "Error initializing image", ioe);
                return null;
            }
        }
    }
}
