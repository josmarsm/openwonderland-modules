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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.Logger;
import javax.media.j3d.ImageComponent2D;
import javax.swing.SwingUtilities;
import org.jdesktop.lg3d.wonderland.appshare.AppGroup;
import org.jdesktop.lg3d.wonderland.appshare.AppWindowGraphics2DApp;
import org.jdesktop.lg3d.wonderland.appshare.SimpleControlArb;
import org.jdesktop.lg3d.wonderland.appshare.DrawingSurface;
import org.jdesktop.lg3d.wonderland.appshare.SimpleDrawingSurface;
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DImageCell;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD.HUDButton;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUDFactory;
import org.jdesktop.lg3d.wonderland.tightvncmodule.client.cell.TightVNCCellMenu.Button;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.TightVNCModuleCellMessage;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.TightVNCModuleCellMessage.Action;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.VncViewerWrapper;
import tightvnc.VncViewer;
import tightvnc.VncCanvas;

/**
 *
 * An VNC application for Wonderland
 *
 * @author nsimpson
 */
public class TightVNCModuleApp extends AppWindowGraphics2DApp implements Runnable,
        TightVNCCellMenuListener {

    private static final Logger logger =
            Logger.getLogger(TightVNCModuleApp.class.getName());
    private static final int DEFAULT_WIDTH = 1024;
    private static final int DEFAULT_HEIGHT = 768;
    private VNCSessionDialog vncDialog;
    private HUDButton msgButton;
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
    private TightVNCCellMenu cellMenu;
    private boolean synced = false;
    private boolean inControl = false;

    public TightVNCModuleApp(SharedApp2DImageCell cell) {
        this(cell, 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, true);
    }

    public TightVNCModuleApp(SharedApp2DImageCell cell, int x, int y, int width, int height,
            boolean decorated) {
        super(new AppGroup(new SimpleControlArb()), true, x, y, width, height, cell);

        clipRect = new Rectangle2D.Double(0, 0, width, height);
        drawingSurface = new SimpleDrawingSurface();
        drawingSurface.setSize(width, height);
        drawingSurface.addSurfaceListener(new DrawingSurface.SurfaceListener() {

            public void redrawSurface() {
                repaint();
            }
        });

        initVNCDialog();
        initHUDMenu();
        addListeners();
        setDecorated(decorated);
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

        cellMenu.addCellMenuListener(this);
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
        cellMenu.removeCellMenuListener(this);
    }

    private void initHUDMenu() {
        cellMenu = new TightVNCCellMenu();
    }

    protected void sendDocumentRequest(Action action, String vncServer, int vncPort, String vncUsername, String vncPassword) {
        TightVNCModuleCellMessage msg = null;

        msg = new TightVNCModuleCellMessage(this.getCell().getCellID(),
                ((TightVNCModuleCell) cell).getUID(),
                action,
                vncServer,
                vncPort,
                vncUsername,
                vncPassword);

        if (msg != null) {
            // send request to server
            logger.fine("VNC app sending document request: " + msg);
            ChannelController.getController().sendMessage(msg);
        }
    }

    /**
     * Initialize the dialog for opening VNC sessions
     */
    private void initVNCDialog() {
        vncDialog = new VNCSessionDialog(null, false);
        vncDialog.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideVNCDialog();
                if (evt.getActionCommand().equals("OK")) {
                    if (isSynced()) {
                        sendDocumentRequest(TightVNCModuleCellMessage.Action.OPEN_SESSION,
                                vncDialog.getServer(),
                                vncDialog.getPort(),
                                vncDialog.getUser(),
                                vncDialog.getPassword());
                    } else {
                        openVNCSession(vncDialog.getServer(),
                                vncDialog.getPort(),
                                vncDialog.getUser(),
                                vncDialog.getPassword());
                    }
                }
            }
        });
    }

    /**
     * Display the VNC session dialog
     */
    private void showVNCDialog() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                vncDialog.setVisible(true);
            }
        });
    }

    /**
     * Hide the VNC sessiob dialog
     */
    public void hideVNCDialog() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (vncDialog != null) {
                    vncDialog.setVisible(false);
                }
            }
        });
    }

    /**
     * Show a status message in the HUD
     * @param message the string to display in the message
     */
    private void showHUDMessage(String message) {
        showHUDMessage(message, HUD.NO_TIMEOUT);
    }

    /**
     * Show a status message in the HUD and remove it after a timeout
     * @param message the string to display in the message
     * @param timeout the period in milliseconds to display the message for
     */
    private void showHUDMessage(String message, int timeout) {
        URL[] imgURLs = {HUD.SIMPLE_BOX_IMAGE_URL,
            TightVNCModuleApp.class.getResource("resources/vnc.png")
        };

        Point[] imagePoints = {new Point(), new Point(10, 10)};

        // dismiss currently active HUD message
        if ((msgButton != null) && msgButton.isActive()) {
            hideHUDMessage(true);
        }

        // display a new HUD message
        msgButton = HUDFactory.getHUD().addHUDMultiImageButton(imgURLs,
                imagePoints, message, new Point(50, 25),
                Font.decode("dialog" + "-BOLD-14"),
                -300, 50, 300, 50,
                timeout, true);
    }

    /**
     * Hide the HUD message
     * @param immediately if true, remove the message now, otherwise slide it
     * off the screen first
     */
    private void hideHUDMessage(boolean immediately) {
        if (msgButton != null) {
            if (!immediately) {
                msgButton.changeLocation(new Point(-45, 50));
            }
            msgButton.setActive(false);
        }
    }

    public void openVNCSession(String vncServer, int vncPort, String username, String password) {
        // terminate any existing VNC session
        closeVNCSession();

        // start a new session
        logger.info("opening VNC session to: " + vncServer + ":" + vncPort);
        showHUDMessage("Connecting to: " + vncServer, 5000);
        this.vncServer = vncServer;
        this.vncPort = vncPort;
        this.username = username;
        this.password = password;

        // update dialog
        vncDialog.setServer(vncServer);
        vncDialog.setPort(vncPort);
        vncDialog.setUser(username);
        vncDialog.setPassword(password);

        // open connection to VNC server
        vncThread = new Thread(this);
        vncThread.start();
    }

    public void closeVNCSession() {
        if (vncThread != null) {
            if (viewer != null) {
                viewer.disconnect();
                viewer.stop();
                viewer = null;
                canvas = null;
                img2D = null;
            }
            vncThread = null;
            showHUDMessage("Connection closed", 3000);
        }
    }

    public void connect() {
        showVNCDialog();
    }

    public void disconnect() {
        if (isSynced()) {
            sendDocumentRequest(TightVNCModuleCellMessage.Action.CLOSE_SESSION,
                    vncDialog.getServer(),
                    vncDialog.getPort(),
                    vncDialog.getUser(),
                    vncDialog.getPassword());
        } else {
            closeVNCSession();
        }
    }

    public void run() {
        logger.fine("starting VNC viewer thread");
        viewer = new VncViewerWrapper(this);

        viewer.mainArgs = new String[]{
                    "HOST", vncServer,
                    "PORT", String.valueOf(vncPort),
                    "PASSWORD", password,
                    "Show Offline Desktop", "No",
                    "Encoding", "Tight",
                    "Compression level", "9",
                    "JPEG image quality", "5",
                    "Use CopyRect", "Yes"
                };
        viewer.inAnApplet = false;
        viewer.inSeparateFrame = true;
        viewer.init();
        viewer.start();
    }

    public boolean isSynced() {
        return synced;
    }

    public void sync(boolean syncing) {
        if ((syncing == false) && (synced == true)) {
            synced = false;
            logger.info("VNC app unsynced");
            showHUDMessage("unsynced", 3000);
            cellMenu.enableButton(Button.UNSYNC);
            cellMenu.disableButton(Button.SYNC);
        } else if ((syncing == true) && (synced == false)) {
            synced = true;
            logger.info("VNC app requesting sync with shared state");
            showHUDMessage("syncing...", 3000);
            sendDocumentRequest(Action.GET_STATE,
                    null,
                    0,
                    null,
                    null);
        }
    }

    public void sync() {
        sync(!isSynced());
    }

    public void unsync() {
        sync(!isSynced());
    }

    public void handleResponse(TightVNCModuleCellMessage msg) {
        String controlling = msg.getUID();
        String myUID = ((TightVNCModuleCell) cell).getUID();
        boolean forMe = (myUID.equals(controlling));
        TightVNCModuleCellMessage vnccm = null;

        if (isSynced()) {
            switch (msg.getAction()) {
                case REQUEST_DENIED:
                    // could queue denied request here
                    break;
                case OPEN_SESSION:
                    openVNCSession(msg.getServer(), msg.getPort(), msg.getUsername(), msg.getPassword());
                    break;
                case CLOSE_SESSION:
                    closeVNCSession();
                    break;
                case SET_STATE:
                    if (forMe == true) {
                        if (isSynced()) {
                            openVNCSession(msg.getServer(), msg.getPort(), msg.getUsername(), msg.getPassword());
                            cellMenu.disableButton(Button.UNSYNC);
                            cellMenu.enableButton(Button.SYNC);
                            logger.info("synced");
                            showHUDMessage("synced", 3000);
                        }
                    }
                    break;
                case REQUEST_COMPLETE:
                    // could retry queued requests here
                    break;
            }
            if ((forMe == true) &&
                    (msg.getAction() != Action.REQUEST_COMPLETE) &&
                    (msg.getAction() != Action.REQUEST_DENIED)) {
                // notify everyone that the request has completed
                vnccm = new TightVNCModuleCellMessage(msg);
                vnccm.setAction(Action.REQUEST_COMPLETE);
            }
        }
        if (vnccm != null) {
            logger.fine("sending message: " + vnccm);
            ChannelController.getController().sendMessage(vnccm);
        }
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

    public void setInControl(boolean inControl) {
        this.inControl = inControl;

        if (inControl == true) {
            CellMenuManager.getInstance().showMenu(this.getCell(), cellMenu, null);
        } else {
            CellMenuManager.getInstance().hideMenu();
        }
    }

    @Override
    public void takeControl(MouseEvent me) {
        logger.info("vnc application has control");
        super.takeControl(me);
        setInControl(true);
    }

    @Override
    public void releaseControl(MouseEvent me) {
        logger.info("vnc application lost control");
        super.releaseControl(me);
        setInControl(false);
    }
}
