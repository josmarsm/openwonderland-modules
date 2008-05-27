/**
 * Project Looking Glass
 *
 * $RCSfile$
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
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.lg3d.wonderland.videomodule.client.cell;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.util.Timer;
import java.util.logging.Logger;
import javax.vecmath.*;
import javax.media.j3d.*;

/**
 *
 * @author nsimpson
 */
public class PanoramaVideo extends Shape3D {

    /** a logger */
    private static final Logger logger =
            Logger.getLogger(PanoramaVideo.class.getName());
    private static final int NUM_VERTS = 4;
    private static final int FORMAT_SIZE = 512;   // the BufferedImage dimensions
    private Texture2D texture;
    private ImageComponent2D ic;
    private BufferedImage im;
    /* the texture uses ImageComponent2D, which uses the BufferedImage frames
    retrieved from the movie */
    private Point3f base;
    private float screenSize;
    private Color frameColor = Color.GREEN;
    private Timer moveTimer = null;
    private Object moveLock = new Object();

    /** Creates a new instance of PanoramaVideo */
    public PanoramaVideo() {

    }

    public PanoramaVideo(Point3f base, float screenSize) {
        this.base = base;
        this.screenSize = screenSize;
    }

    public void setVideo(String videoSource) {
        initComponents();
    }

    private void initComponents() {
        // prepare the ImageComponent2D object
        ic = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA,
                FORMAT_SIZE, FORMAT_SIZE, true, true);
        /* The ImageComponent2D object sets both its "by reference" and 'Y-up"
        arguments to true.
        These flags _should_ reduce the memory needed to store the image, since
        Java 3D will avoid copying it from the application space into graphics
        memory. However, it's also necessary to use specfic formats for
        the ImageComponent2D and BufferedImage objects.
        In a Win32/OpenGL combination, the ImageComponent format should be
        ImageComponent.FORMAT_RGB (as here), and the BufferedImage format
        BufferedImage.TYPE_3BYTE_BGR. This BufferedImage format
        is fixed in JMFSnapper.
        More performance details can be found at
        http://www.j3d.org/tutorials/quick_fix/perf_guide_1_3.html
         */

        // the image will be changing
        ic.setCapability(ImageComponent2D.ALLOW_IMAGE_WRITE);
        ic.setCapabilityIsFrequent(ImageComponent2D.ALLOW_IMAGE_WRITE);
        // this component will be moving
        this.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

        // retrieve the current frame of the movie)
        im = new BufferedImage(FORMAT_SIZE, FORMAT_SIZE, BufferedImage.TYPE_INT_ARGB);
        createAppearance(im);
        createGeometry(base, screenSize);
    }

    private void createAppearance(BufferedImage im) {
        Appearance app = new Appearance();

        // see the texture from both sides
        PolygonAttributes pa = new PolygonAttributes();
        pa.setCullFace(PolygonAttributes.CULL_NONE);
        app.setPolygonAttributes(pa);

        // Create a two dimensional texture with magnification filtering
        texture = new Texture2D(Texture2D.BASE_LEVEL, Texture.RGBA,
                FORMAT_SIZE, FORMAT_SIZE);
        // texture can change
        texture.setCapability(Texture.ALLOW_IMAGE_WRITE);
        texture.setCapabilityIsFrequent(Texture.ALLOW_IMAGE_WRITE);
        // this setting noticably reduces pixilation on the screen
        texture.setMagFilter(Texture2D.BASE_LEVEL_LINEAR);

        // set the texture from the retrieved movie frame
        ic.set(im);
        texture.setImage(0, ic);

        app.setTexture(texture);
        setAppearance(app);
    }

    private Rectangle2D.Float getOptimalVideoSize(float desiredSize) {
        // get the movie image's dimensions
        int imW = 640; //im.getWidth();
        int imH = 480; //im.getHeight();

        // get the quad's default dimensions (equal to the screen size, sz)
        float scrW = desiredSize;
        float scrH = desiredSize;

        /* The aim is to adjust the size of the quad so that it's dimensions
        are proportional to those of the movie image. Also the largest
        dimension (width or height) should be equal to the specified
        screen size (sz).
         */
        if (imW > imH) {
            // image width is biggest dimension
            // so reduce the screen's height
            scrH = (desiredSize / imW) * imH;
        } else {
            // else image height is biggest
            // so reduce the screen's width
            scrW = (desiredSize / imH) * imW;
        }

        return new Rectangle2D.Float(0, 0, scrW, scrH);
    }

    private void createGeometry(Point3f c, float sz) {
        QuadArray plane = new QuadArray(NUM_VERTS,
                GeometryArray.COORDINATES |
                GeometryArray.TEXTURE_COORDINATE_2 |
                GeometryArray.ALLOW_TEXCOORD_WRITE);

        Rectangle2D.Float videoDimensions = getOptimalVideoSize(sz);

        float scrW = (float) videoDimensions.getWidth();
        float scrH = (float) videoDimensions.getHeight();

        /* The screen base is centered at the base point (c), facing
        along the +z axis. */
        Point3f p0 = new Point3f(c.x - scrW / 2, c.y - scrH / 2, c.z);
        Point3f p1 = new Point3f(c.x + scrW / 2, c.y - scrH / 2, c.z);
        Point3f p2 = new Point3f(c.x + scrW / 2, c.y + scrH / 2, c.z);
        Point3f p3 = new Point3f(c.x - scrW / 2, c.y + scrH / 2, c.z);

        logger.fine("p0: " + p0 + ", p1: " + p1 + ", p2: " + p2 + ", p3: " + p3);
        // anti-clockwise from bottom left
        plane.setCoordinate(0, p0);
        plane.setCoordinate(1, p1);
        plane.setCoordinate(2, p2);
        plane.setCoordinate(3, p3);

        setVideoGeometry(plane);
    }

    /**
     * Set the geometry of the panorama video and also update the texture
     * coordinates
     * @param geometry the new geometry of the video
     */
    public void setVideoGeometry(QuadArray geometry) {
        /* The usual ordering for texture coordinates is to start at the bottom
        left of the quad, and specify them anti-clockwise (see above). However,
        since the ImageComponent2D object uses Y-UP, the ordering of the
        texture points must be flipped around the horizontal axis.
         */
        TexCoord2f q = new TexCoord2f();
        q.set(0.0f, 0.0f);
        geometry.setTextureCoordinate(0, 3, q);  // (0,0) tex coord --> top left of quad
        q.set(1.0f, 0.0f);
        geometry.setTextureCoordinate(0, 2, q);  // (1,0) --> top right
        q.set(1.0f, 1.0f);
        geometry.setTextureCoordinate(0, 1, q);  // (1,1) --> bottom right
        q.set(0.0f, 1.0f);
        geometry.setTextureCoordinate(0, 0, q);  // (0,1) --> bottom left

        try {
            setGeometry(geometry);
        } catch (Exception e) {
            logger.warning("failed to set video window geometry: " + e);
        }
    }

    private class Mover extends Thread implements MoveListener {

        private Point3f destination;

        public Mover(Point3f destination) {
            this.destination = destination;
        }

        @Override
        public void run() {
            synchronized (moveLock) {
                try {
                    if (moveTimer != null) {
                        logger.fine("move in progress, waiting for lock");
                        moveLock.wait();
                    }

                } catch (Exception e) {
                    logger.warning("exception waiting for move lock");
                }
            }
            logger.fine("move to: " + destination + " starting");
            moveTimer = new Timer("move timer");
            LinearMover scroller = new LinearMover(getPosition(), moveTimer, this);
            scroller.moveTo(destination);
        }

        /**
         * Move the video to the specific point in 3D space
         * @param c the desired center point of the video
         */
        public void move(Point3f c) {
            logger.finest("moving video to center at: " + c);
            // get current video geometry
            QuadArray videoGeometry = (QuadArray) PanoramaVideo.this.getGeometry();

            Point3f v0 = new Point3f();
            Point3f v2 = new Point3f();
            videoGeometry.getCoordinate(0, v0);     // bottom left
            videoGeometry.getCoordinate(2, v2);     // top right

            float videoW = v2.getX() - v0.getX();   // width
            float videoH = v2.getY() - v0.getY();   // height

            // video is centered at the base point c facing along the +z axis.
            Point3f p0 = new Point3f(c.x - videoW / 2, c.y - videoH / 2, c.z);
            Point3f p1 = new Point3f(c.x + videoW / 2, c.y - videoH / 2, c.z);
            Point3f p2 = new Point3f(c.x + videoW / 2, c.y + videoH / 2, c.z);
            Point3f p3 = new Point3f(c.x - videoW / 2, c.y + videoH / 2, c.z);

            // initialize new geometry
            QuadArray plane = new QuadArray(NUM_VERTS,
                    GeometryArray.COORDINATES |
                    GeometryArray.TEXTURE_COORDINATE_2 |
                    GeometryArray.ALLOW_TEXCOORD_WRITE);

            plane.setCoordinate(0, p0);
            plane.setCoordinate(1, p1);
            plane.setCoordinate(2, p2);
            plane.setCoordinate(3, p3);

            setVideoGeometry(plane);
        }

        public void moveComplete() {
            synchronized (moveLock) {
                logger.fine("move complete, notifying waiting movers");
                moveTimer = null;
                moveLock.notify();
            }
        }
    }

    public void moveTo(Point3f destination) {
        logger.fine("move to: " + destination);
        new Mover(destination).start();
    }

    public Point3f getPosition() {
        Point3f position = new Point3f();
        QuadArray videoGeometry = (QuadArray) this.getGeometry();
        Point3f v0 = new Point3f();
        Point3f v2 = new Point3f();
        videoGeometry.getCoordinate(0, v0);     // bottom left
        videoGeometry.getCoordinate(2, v2);     // top right

        position.setX(v0.getX() + (v2.getX() - v0.getX()) / 2);
        position.setY(v0.getY() + (v2.getY() - v0.getY()) / 2);
        position.setZ(v0.getZ());
        
        logger.fine("current position: " + position);
        return position;
    }

    public void setFrameColor(Color frameColor) {
        this.frameColor = frameColor;
    }

    public void setFrame(BufferedImage frame) {
        if (frame != null) {
            Graphics2D g = im.createGraphics();

            g.drawImage(frame, 0, 0, im.getWidth(), im.getHeight(), 0, 0, frame.getWidth(), frame.getHeight(), null);
            g.setColor(frameColor);
            g.setStroke(new BasicStroke(6));
            g.drawRoundRect(3, 3, im.getWidth() - 6, im.getHeight() - 6, 8, 8);
            ic.set(im);
        }
    }
}
