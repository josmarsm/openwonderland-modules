/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.screenshare.client;

import java.awt.AWTException;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author jkaplan
 */
public class ImageReader {
    private static final Logger LOGGER =
            Logger.getLogger(ImageReader.class.getName());

    private final SelectorWindow window;
    private final Image cursor;

    private Robot robot;
    private Rectangle deviceBounds;

    public ImageReader(SelectorWindow window) {
        this.window = window;

        window.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                updateRobot();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                updateRobot();
            }
        });

        URL imageURL = getClass().getResource("resources/cursor.png");
        cursor = Toolkit.getDefaultToolkit().createImage(imageURL);

        updateRobot();
    }

    public byte[] getImage() {
        if (robot == null) {
            return null;
        }

        // create a rectangle in device relative coordinates
        Rectangle bounds = window.getRecordBounds();
        bounds.setLocation(bounds.x - deviceBounds.x,
                           bounds.y - deviceBounds.y);

        LOGGER.fine("Capturing " + bounds);

        BufferedImage bi = robot.createScreenCapture(bounds);

        // draw the mouse if it is in frame
        if (window.isMouseInWindow()) {
            Point mouseLocation = window.getMouseLocation();
            mouseLocation = new Point(mouseLocation.x - (cursor.getWidth(null) / 2),
                                      mouseLocation.y - (cursor.getWidth(null) / 2));
            Graphics g = bi.getGraphics();
            g.drawImage(cursor, mouseLocation.x, mouseLocation.y, window);
        }
        
        // create a JPEG image
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error reading image", ex);
            return null;
        }
    }

    /**
     * Called when the component has moved, and may therefore be on a
     * different graphics device
     */
    private void updateRobot() {
        LOGGER.fine("Update robot: " + window.getGraphicsConfiguration().getDevice() +
                    ", bounds: " + window.getGraphicsConfiguration().getBounds());

        try {
            robot = new Robot(window.getGraphicsConfiguration().getDevice());
            deviceBounds = window.getGraphicsConfiguration().getBounds();

        } catch (AWTException ae) {
            LOGGER.log(Level.WARNING, "Error creating robot for device " +
                       window.getGraphicsConfiguration().getDevice(), ae);

            // try creating a generic robot
            try {
                robot = new Robot();

                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                deviceBounds = ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
            } catch (AWTException ae2) {
                LOGGER.log(Level.WARNING, "Error creating robot", ae2);
            }
        }
    }
}
