/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.globals;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.GlobalSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.Global;
import org.jdesktop.wonderland.modules.ezscript.client.globals.gui.UIViewEntity;

/**
 *
 * @author Ryan
 */
@Global
public class GuiBuilder implements GlobalSPI {

    private boolean useURL;
    private URL url;
    private boolean useImage;
    private Image image;
    private int size;
    private static final Logger logger = Logger.getLogger(GuiBuilder.class.getName());

    public String getName() {
        return "GUI";
    }

    public void start() {
    }

    public void small() {
        size = 32;
    }

    public void medium() {
        size = 48;
    }

    public void large() {
        size = 64;
    }

    public void setTexture(String url) {
        try {
            URL _url = new URL(url);
            useURL = true;
            this.url = _url;

            synchronized (Builder.imageCache) {
                if (Builder.imageCache.containsKey(url)) {
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


        } catch (MalformedURLException ex) {
            Logger.getLogger(GuiBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setTexture(Image image) {
        useImage = true;
        this.image = image;
    }

    public UIViewEntity drawQuad(float positionx, float positiony) {
        Vector3f position = new Vector3f(positionx, positiony, 0.5f);

        UIViewBrush brush = null;
        if (useURL) {
            brush = new UIViewBrush(url);
            brush.blend(true);
        } else if (useImage) {
            brush = new UIViewBrush(image);
        } else {
            brush = new UIViewBrush();
        }

        brush.paintMesh(position, size);

        return brush.getMesh();


    }

    public void removeQuad(final UIViewEntity entity) {
        logger.warning("REMOVING QUAD ON THREAD: " + Thread.currentThread().getName());


        entity.removeMouseListener();
        entity.setVisible(false);


        //        WorldManager.getDefaultWorldManager().removeEntity(entity);
    }

    private static class UIViewBrush {

        private final UIViewEntity entity;
        private URL url = null;
        private Image textureImage = null;

        public UIViewBrush() {
            entity = new UIViewEntity();
        }

        public UIViewBrush(URL url) {
            this.url = url;

            entity = new UIViewEntity(url);
        }

        public UIViewBrush(Image texture) {
            this.textureImage = texture;

            entity = new UIViewEntity(texture);
        }

        public void paintMesh(Vector3f position, int size) {
            entity.setOrtho(true);
            entity.showShapeWithPosition(position, size);
//            entity.updateTransform(position, new Quaternion());
        }

        public UIViewEntity getMesh() {
            return entity;
        }

        private void blend(boolean b) {
            entity.setBlended(b);
        }
    }
}
