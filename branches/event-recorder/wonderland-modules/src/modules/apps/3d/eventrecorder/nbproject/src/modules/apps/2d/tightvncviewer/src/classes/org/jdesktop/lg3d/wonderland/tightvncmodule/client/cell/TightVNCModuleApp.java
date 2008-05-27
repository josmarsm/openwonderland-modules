/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (canvas) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.lg3d.wonderland.tightvncmodule.client.cell;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import javax.media.j3d.ImageComponent2D;
import org.jdesktop.lg3d.wonderland.appshare.AppGroup;
import org.jdesktop.lg3d.wonderland.appshare.AppWindowGraphics2DApp;
import org.jdesktop.lg3d.wonderland.appshare.SimpleControlArb;
import org.jdesktop.lg3d.wonderland.appshare.DrawingSurface;
import org.jdesktop.lg3d.wonderland.appshare.SimpleDrawingSurface;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DImageCell;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.VncViewerWrapper;
import tightvnc.VncViewer;
import tightvnc.VncCanvas;

/**
 *
 * An VNC application for Wonderland
 *
 * @author nsimpson
 */
public class TightVNCModuleApp extends AppWindowGraphics2DApp implements Runnable {

    private static final Logger logger =
            Logger.getLogger(TightVNCModuleApp.class.getName());
    private static final int DEFAULT_WIDTH = 1024;
    private static final int DEFAULT_HEIGHT = 768;
    private DrawingSurface drawingSurface;
    private ImageComponent2D img2D;
    private boolean readOnly = false;
    // VNC integration
    private String vncServer;
    private int vncPort;
    private String username;
    private String password;
    private Thread vncThread;
    private VncViewer viewer;
    private VncCanvas canvas;
    private Rectangle2D.Double clipRect;
    private ImageComponent2D.Updater updater;

    public TightVNCModuleApp(SharedApp2DImageCell cell) {
        this(cell, 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public TightVNCModuleApp(SharedApp2DImageCell cell, int x, int y, int width, int height) {
        super(new AppGroup(new SimpleControlArb()), true, x, y, width, height, cell);

        clipRect = new Rectangle2D.Double(0, 0, width, height);
        drawingSurface = new SimpleDrawingSurface();
        drawingSurface.setSize(width, height);

        drawingSurface.addSurfaceListener(new DrawingSurface.SurfaceListener() {

            public void redrawSurface() {
                repaint();
            }
        });
        addListeners();
        setShowing(true);
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;

        removeListeners();
        if (!readOnly) {
            addListeners();
        }
    }

    public void addListeners() {
        addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (canvas != null) {
                    canvas.keyPressed(e);
                }
            }

            public void keyReleased(KeyEvent e) {
                if (canvas != null) {
                    canvas.keyReleased(e);
                }
            }

            public void keyTyped(KeyEvent e) {
                if (canvas != null) {
                    canvas.keyTyped(e);
                }
            }
        });

        addMouseMotionListener(new MouseMotionListener() {

            public void mouseDragged(MouseEvent e) {
                if (canvas != null) {
                    canvas.mouseDragged(e);
                }
            }

            public void mouseMoved(MouseEvent e) {
                if (canvas != null) {
                    canvas.mouseMoved(e);
                }
            }
        });

        addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                if (canvas != null) {
                    canvas.mouseClicked(e);
                }
            }

            public void mousePressed(MouseEvent e) {
                if (canvas != null) {
                    canvas.mousePressed(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (canvas != null) {
                    canvas.mouseReleased(e);
                }
            }

            public void mouseEntered(MouseEvent e) {
                if (canvas != null) {
                    canvas.mouseEntered(e);
                }
            }

            public void mouseExited(MouseEvent e) {
                if (canvas != null) {
                    canvas.mouseExited(e);
                }
            }
        });
    }

    public void removeListeners() {
        KeyListener[] keyListeners = getKeyListeners();
        for (Object listener : keyListeners) {
            removeKeyListener((KeyListener) listener);
        }

        MouseListener[] mouseListeners = getMouseListeners();
        for (Object listener : mouseListeners) {
            removeMouseListener((MouseListener) listener);
        }

        MouseMotionListener[] mouseMotionListeners = getMouseMotionListeners();
        for (Object listener : mouseMotionListeners) {
            removeMouseMotionListener((MouseMotionListener) listener);
        }
    }

    public void initializeVNC(String vncServer, int vncPort,
            String username, String password) {
        logger.info("initiating VNC connection to: " + vncServer + ":" + vncPort);
        this.vncServer = vncServer;
        this.vncPort = vncPort;
        this.username = username;
        this.password = password;
        vncThread = new Thread(this);
        vncThread.start();
    }

    public void run() {
        logger.fine("starting VNC viewer thread");
        viewer = new VncViewerWrapper(this);

        viewer.mainArgs = new String[]{
            "HOST", vncServer,
            "PORT", String.valueOf(vncPort),
            "PASSWORD", password
        };
        viewer.inAnApplet = false;
        viewer.inSeparateFrame = true;
        viewer.setEncodings(false);
        viewer.init();
        viewer.start();
    }

    /**
     * Set the size of the application
     * @param width the width of the application
     * @param height the height of the application
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        drawingSurface.setSize(width, height);
    }

    public void scheduleRepaint(long tm, int x, int y, int width, int height) {
        // REMIND: not honoring repaint within time parameter
        repaint(x, y, width, height);
    }

    protected void repaint(int x, int y, int width, int height) {
        if (img2D == null) {
            img2D = getImage();
        }
        if (updater == null) {
            updater = new ImageComponent2D.Updater() {

                public void updateData(ImageComponent2D imageComponent, int x, int y, int width, int height) {
                    clipRect = new Rectangle2D.Double(x, y, width, height);
                    BufferedImage buf = img2D.getImage();
                    paint((Graphics2D) buf.getGraphics());
                }
            };
        }
        img2D.updateData(updater, x, y, width, height);
    }

    /**
     * Paint contents of window
     */
    @Override
    protected void paint(Graphics2D g) {
        if (viewer != null) {
//            System.err.println("clip: " + clipRect.width + "x" + clipRect.height
//                    + " at " + clipRect.x + ", " + clipRect.y);
            g.setClip(clipRect);

            if (canvas == null) {
                canvas = (VncCanvas) viewer.getCanvas();
            }
            if (canvas != null) {
                canvas.paint(g);
            }
        }
    }
}
