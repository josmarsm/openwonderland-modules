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

import com.sun.awt.AWTUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author jkaplan
 */
public class SelectorWindow extends JFrame 
        implements MouseListener, MouseMotionListener
{
    private static final Logger LOGGER =
            Logger.getLogger(SelectorWindow.class.getName());
    private static final ResourceBundle BUNDLE =
            ResourceBundle.getBundle("org.jdesktop.wonderland.modules.screenshare.client.resources.Bundle");

    private int startX;
    private int startY;

    private Rectangle resizeBounds;
    private Point movePoint;

    private boolean mouseInWindow;
    private Point mouseLocation;

    public SelectorWindow() {
        super (findTranslucentWindowConfig());

        LOGGER.warning("Created new selector with config " + getGraphicsConfiguration());

        setSize(640, 480);
        setUndecorated(true);
        setAlwaysOnTop(true);
        setContentPane(new SizePanel());

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public boolean isMouseInWindow() {
        return mouseInWindow;
    }

    public Point getMouseLocation() {
        return mouseLocation;
    }

    public Rectangle getRecordBounds() {
        return new Rectangle(getX() + 2, getY() + 12,
                             getWidth() - 4, getHeight() - 14);
    }

    public void mouseClicked(MouseEvent e) {
        // check if the click was on the close X
        if (e.getX() > (getWidth() - 10) && e.getY() < 10) {
            String message = BUNDLE.getString("Confirm_Stop");
            String title = BUNDLE.getString("Confirm_Stop_Title");

            int res = JOptionPane.showConfirmDialog(this, message,
                                                    title, JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                setVisible(false);
                dispose();
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        startX = e.getXOnScreen();
        startY = e.getYOnScreen();

        if (e.getX() > getWidth() - 15 &&
            e.getY() > getHeight() - 15)
        {
            // resize if we are on the drag corner
            resizeBounds = new Rectangle();
            resizeBounds.setLocation(getLocation());
            resizeBounds.setSize(getSize());
        } else if (e.getX() > (getWidth() / 2) - 10 &&
                   e.getX() < (getWidth() / 2) + 10 &&
                   e.getY() < 10)
        {
            // move if we are on the window border
            movePoint = getLocation();
        }
    }

    public void mouseReleased(MouseEvent e) {
        resizeBounds = null;
        movePoint = null;
    }

    public void mouseDragged(MouseEvent e) {
        int dx = e.getXOnScreen() - startX;
        int dy = e.getYOnScreen() - startY;

        if (resizeBounds != null) {
            // reshape
            Rectangle newBounds = new Rectangle(resizeBounds);
            newBounds.setSize(resizeBounds.width + dx, resizeBounds.height + dy);
            setBounds(newBounds);
        } else if (movePoint != null) {
            // move
            setLocation(movePoint.x + dx, movePoint.y + dy);
        }
    }

    public void mouseEntered(MouseEvent e) {
        mouseInWindow = true;
        mouseLocation = e.getPoint();
    }

    public void mouseExited(MouseEvent e) {
        mouseInWindow = false;
    }

    public void mouseMoved(MouseEvent e) {
        mouseLocation = e.getPoint();
    }

    static class SizePanel extends JPanel {
        private static final int WIDTH = 2;

        @Override
        protected void paintComponent(Graphics g) {
          Graphics2D g2d = (Graphics2D) g;
          g2d.setColor(new Color(0, 0, 0, 0));
          g2d.fillRect(0, 0, getWidth(), getHeight());

          // draw the outline
          g2d.setStroke(new BasicStroke(WIDTH));
          g2d.setColor(new Color(255, 0, 0, 255));
          g2d.drawRect(WIDTH / 2, (WIDTH / 2) + 10,
                       getWidth() - WIDTH, getHeight() - (WIDTH + 10));

          // draw the resize corner
          g2d.setStroke(new BasicStroke(WIDTH * 2));
          g2d.setColor(new Color(0, 255, 255, 255));
          g2d.drawLine(getWidth() - WIDTH - 15, getHeight() - WIDTH,
                       getWidth() - WIDTH, getHeight() - WIDTH);
          g2d.drawLine(getWidth() - WIDTH, getHeight() - WIDTH - 15,
                       getWidth() - WIDTH, getHeight() - WIDTH);

          // add the handle
          g2d.setStroke(new BasicStroke(10));
          g2d.drawLine((getWidth() / 2) - 10, 5,
                       (getWidth() / 2) + 10, 5);

          // and the X
          g2d.setStroke(new BasicStroke(WIDTH));
          g2d.drawLine(getWidth() - 9, 1, getWidth() - 1, 9);
          g2d.drawLine(getWidth() - 9, 9, getWidth() - 1, 1);
        }
    }

    // find a translucency capable config
    private static GraphicsConfiguration findTranslucentWindowConfig() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        for (GraphicsConfiguration config : gd.getConfigurations()) {
            if (AWTUtilities.isTranslucencyCapable(config)) {
                LOGGER.warning("Found translucent capable config: " + config);
                return config;
            }
        }

        return null;
    }
}
