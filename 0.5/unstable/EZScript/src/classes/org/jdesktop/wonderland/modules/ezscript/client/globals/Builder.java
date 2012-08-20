/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.globals;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.GlobalSPI;
import org.jdesktop.wonderland.modules.ezscript.client.ShapeViewerEntity;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.Global;
import org.jdesktop.wonderland.modules.ezscript.client.globals.builder.brushes.CubeBrush;
import org.jdesktop.wonderland.modules.ezscript.client.globals.builder.brushes.ShapeBrush;

/**
 *
 * @author JagWire
 */
@Global
public class Builder implements GlobalSPI {

    static String[] colors = {"green", "red", "yellow", "blue", "white", "black"};
    public static final Map<String, ColorRGBA> STRING_TO_COLORS;
    public static final Map<String, Image> imageCache;
    private static final Logger logger = Logger.getLogger(Builder.class.getName());
    
    static {
        STRING_TO_COLORS = new HashMap<String, ColorRGBA>();
        imageCache = new HashMap<String, Image>();
        STRING_TO_COLORS.put(colors[0], ColorRGBA.green);
        STRING_TO_COLORS.put(colors[1], ColorRGBA.red);
        STRING_TO_COLORS.put(colors[2], ColorRGBA.yellow);
        STRING_TO_COLORS.put(colors[3], ColorRGBA.blue);
        STRING_TO_COLORS.put(colors[4], ColorRGBA.white);
        STRING_TO_COLORS.put(colors[5], ColorRGBA.black);

    }
    private String shapeType = "BOX";
    private String appearance = "white";
    private boolean followTheMouse = false;
    private String url;

    public String getName() {
        return "Builder";
    }

    public void start() {
    }

    public void setColor(String color) {
        this.appearance = color;

    }

    public void setShapeType(String shapeType) {
        this.shapeType = shapeType;
    }

    public void constructionFollowsTheMouse(boolean followTheMouse) {
        this.followTheMouse = followTheMouse;
    }

    public void setTextureURL(String url) {
        this.url = url;


        synchronized (imageCache) {
            if (!imageCache.containsKey(url)) {
                try {
                    logger.warning("CACHING IMAGE URL: "+url);
                    URL imageURL = new URL(url);
                    ImageIcon icon = new ImageIcon(imageURL);
                    imageCache.put(url, icon.getImage());
                    logger.warning(url+" CACHED!");
                } catch (MalformedURLException ex) {
                    Logger.getLogger(Builder.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }


    }

    public ShapeViewerEntity drawCube(Vector3f position) {

        CubeBrush brush = new CubeBrush(url, appearance, false);
        brush.paintCube(position);
//        ShapeViewerEntity BOX = new ShapeViewerEntity("BOX");
//        BOX.showShape();
//        BOX.updateTransform(position, new Quaternion());
        return brush.getCube();
    }

    public ShapeViewerEntity drawShape(Vector3f position) {
        ShapeBrush brush = new ShapeBrush(shapeType,
                appearance,
                false,
                followTheMouse);

        brush.paintMesh(position);

        return brush.getMesh();
    }

    public void removeShape(Entity e) {
        WorldManager.getDefaultWorldManager().removeEntity(e);
    }

    public void removeCube(Entity e) {
        WorldManager.getDefaultWorldManager().removeEntity(e);
    }
}
