/**
 * Project Wonderland
 *
 * $URL$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Rev$
 * $Date$
 * $Author$
 */
package com.sun.labs.miw.client.cell;

import com.sun.j3d.utils.image.TextureLoader;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.Transform3D;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;
import org.jdesktop.j3d.util.TextureUtil;
import org.jdesktop.j3d.utils.scenegraph.ImageComponent2DURL;
import org.jdesktop.lg3d.wonderland.scenemanager.AssetManager;

public class Util {
     static Appearance TextureApp(URL file) {
        Appearance app = new Appearance();
       if (!FileExists(file)) {
            System.out.println("ERROR: file "+file+" doesn't exist");
       }
        app.setTexture(new TextureLoader(file,new Container()).getTexture());
        return app;
    }
     static boolean FileExists(URL file) {
         return true;
     }
     static URL URL(String url) {
         URL u = null;
         try {
             u = new URL(url);
         } catch (MalformedURLException e) {
             System.err.println("ERROR: url "+url + " doesn't exist");
         }
         return u;
     }
     static Appearance TextureApp(URL base, String file, int width, int height) {
        //System.out.println("Load texture: " + base.toExternalForm() + "/" + file);
              
        // Use the asset manager to load the texture image.  It is 
        // critical for performance that the size of the image get scaled
        // to a power of two.
        AssetManager am =  AssetManager.getAssetManager();
        ImageComponent2DURL coverImage = new ImageComponent2DURL(
                    ImageComponent2D.FORMAT_RGB, width, height, true, true,
                    base, file,true);
        am.loadImage(coverImage);
        
        // setup the texture
        Texture texture = new Texture2D(javax.media.j3d.Texture.BASE_LEVEL, 
                                        Texture2D.RGB, width, height);
        texture.setImage(0, coverImage);
        
        // map the texture to an appearance
        Appearance app = new Appearance();  
        app.setTexture(texture);
        return app;
    }
    static Appearance LineApp() {
            Appearance app = new Appearance();
            PolygonAttributes polyAttr = new PolygonAttributes(
                    PolygonAttributes.POLYGON_LINE,
                    PolygonAttributes.CULL_NONE,0);
            polyAttr.setBackFaceNormalFlip(true);
            app.setPolygonAttributes(polyAttr);
            return app;
    }
    static Appearance colorApp(float r, float g, float b) {
        Appearance app = new Appearance();
        ColoringAttributes colorAttr = new ColoringAttributes(new Color3f(r,g,b),1);
        colorAttr.setCapability(colorAttr.ALLOW_COLOR_WRITE);
        app.setColoringAttributes(colorAttr);
        return app;
    }
    static Transform3D makeTransform(double x, double y, double z, double scale) {
        Transform3D tr = new Transform3D();
        tr.setTranslation(new Vector3d(x,y,z));
        tr.setScale(scale);
        return tr;
    }
     static Transform3D makeTransform(double x, double y, double z, double rx, double ry, double rz, double scale) {
        Matrix m = new Matrix();
        m.rotate(1,0,0,rx);
        m.rotate(0,1,0,ry);
        m.rotate(0,0,1,rz);
        m.translate(x,y,z);
        m.scale(scale);
       return m.getTransform();
    }
    //<editor-fold defaultstate="collapsed" desc="Math">
    static Vector3d add(Vector3d v1, Vector3d v2) {
        return new Vector3d(v1.x + v2.x,v1.y + v2.y,v1.z + v2.z);
    }
    static Vector3d sub(Vector3d v1, Vector3d v2) {
        return new Vector3d(v1.x - v2.x,v1.y-v2.y,v1.z-v2.z);
    }
    static Vector3d mul(Vector3d v1, Vector3d v2) {
        return new Vector3d(v1.x * v2.x,v1.y * v2.y,v1.z *v2.z);
    }
    static Vector3d scale(double f, Vector3d v1) {
        return new Vector3d(v1.x*f,v1.y*f,v1.z*f);
    }
    //</editor-fold>
    // rescale an image
    static BufferedImage scaleImage(Image img, int w, int h)
    {
        int imageType = BufferedImage.TYPE_INT_RGB;
        BufferedImage img2 = new BufferedImage(w, h, imageType);
        Graphics2D g = img2.createGraphics();
        g.drawImage(img, 0, 0, w, h, null); 
        g.dispose();
        return img2;
    }
}
