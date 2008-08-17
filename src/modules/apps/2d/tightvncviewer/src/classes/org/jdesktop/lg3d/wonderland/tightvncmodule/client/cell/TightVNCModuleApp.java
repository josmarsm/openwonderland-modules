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
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jdesktop.lg3d.wonderland.appshare.AppGroup;
import org.jdesktop.lg3d.wonderland.appshare.AppWindowGraphics2DApp;
import org.jdesktop.lg3d.wonderland.appshare.SimpleControlArb;
import org.jdesktop.lg3d.wonderland.appshare.DrawingSurface;
import org.jdesktop.lg3d.wonderland.appshare.SimpleDrawingSurface;
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.SharedApp2DImageCell;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.WonderDACDialog;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD.HUDButton;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUDFactory;
import org.jdesktop.lg3d.wonderland.tightvncmodule.client.cell.TightVNCCellMenu.Button;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.TightVNCModuleCellMessage;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.TightVNCModuleCellMessage.Action;
import org.jdesktop.lg3d.wonderland.tightvncmodule.common.TightVNCModuleCellMessage.RequestStatus;
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
    private final float PIX_PER_VRU = 256; // TW    
    private int preferredWidth = DEFAULT_WIDTH;
    private int preferredHeight = DEFAULT_HEIGHT;
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
    private SharedApp2DImageCell vncCell;  // TW
    private boolean listenersOn = true;  // TW
    protected Object actionLock = new Object();

    public TightVNCModuleApp(SharedApp2DImageCell cell) {
        this(cell, 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, true);
    }

    public TightVNCModuleApp(SharedApp2DImageCell cell, int x, int y, int width, int height,
            boolean decorated) {
        super(new AppGroup(new SimpleControlArb()), true, x, y, width, height, cell);

        vncCell = cell;  // TW
        
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
                // Invoke the WonderDAC dialog box if there was a right
                // mouse button click.  If we're in a VNC sessions,
                // however, let the click go through!
                // TW
                if ((e.getButton() == MouseEvent.BUTTON3) && (canvas == null)) {  // TW
                   
                   JFrame f = WonderDACDialog.getWonderDACDialog(); // TW
                      
                   WonderDACDialog.getWonderDACDialog().setObjectID(vncCell.getCellName(), vncCell.getCellID().toString());  // TW
                   WonderDACDialog.getWonderDACDialog().setDefaultListName(vncCell.getCellAccessOwner()); // TW
                   WonderDACDialog.getWonderDACDialog().setDefaultGroupName(vncCell.getCellAccessGroup());  // TW
                   WonderDACDialog.getWonderDACDialog().setGrpIntCheckBox(vncCell.getCellAccessGroupPermissions());  // TW
                   WonderDACDialog.getWonderDACDialog().setGrpAltCheckBox(vncCell.getCellAccessGroupPermissions());  // TW
                   WonderDACDialog.getWonderDACDialog().setOthIntCheckBox(vncCell.getCellAccessOtherPermissions());  // TW
                   WonderDACDialog.getWonderDACDialog().setOthAltCheckBox(vncCell.getCellAccessOtherPermissions());  // TW
                   
                   f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // TW
                   f.setVisible(true);  // TW
                }
                else if (canvas != null) {                    
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
                        sendRequest(TightVNCModuleCellMessage.Action.OPEN_SESSION,
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
        logger.info("vnc viewer: opening VNC session to: " + vncServer + ":" + vncPort);
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
            sendRequest(TightVNCModuleCellMessage.Action.CLOSE_SESSION,
                    vncDialog.getServer(),
                    vncDialog.getPort(),
                    vncDialog.getUser(),
                    vncDialog.getPassword());
        } else {
            closeVNCSession();
        }
    }

    public void run() {
        logger.fine("vnc viewer: starting VNC viewer thread");
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

    /** 
     * Resynchronize the state of the cell.
     * 
     * A resync is necessary when the cell transitions from INACTIVE to 
     * ACTIVE cell state, where the cell may have missed state synchronization 
     * messages while in the INACTIVE state.
     * 
     * Resynchronization is only performed if the cell is currently synced.
     * To sync an unsynced cell, call sync(true) instead.
     */
    public void resync() {
        if (isSynced()) {
            synced = false;
            sync(true);
        }
    }

    /** 
     * Sets the sync state of the player
     * @param syncing true to sync, false to unsync
     */
    public void sync(boolean syncing) {
        if ((syncing == false) && (synced == true)) {
            synced = false;
            logger.info("vnc viewer: unsynced");
            showHUDMessage("unsynced", 3000);
            updateMenu();
        } else if ((syncing == true) && (synced == false)) {
            synced = true;
            logger.info("vnc viewer: requesting sync with shared state");
            showHUDMessage("syncing...", 5000);
            sendRequest(Action.GET_STATE, null, vncPort, null, null);
        }
    }

    /**
     * Toggle the sync state of the player
     */
    public void sync() {
        sync(!isSynced());
    }

    /**
     * Toggle the sync state of the player
     */
    public void unsync() {
        sync(!isSynced());
    }

    /**
     * A simple method to send a ping message to the
     * server.  Particularly useful when we first
     * initialize this cell and need to find out what
     * sort of access we have.
     * 
     * @author twright
     */
    public void pingServer () {
        TightVNCModuleCellMessage vmcm = new TightVNCModuleCellMessage(this.getCell().getCellID(), Action.PING);
        ChannelController.getController().sendMessage(vmcm);        
    }
    
    /**
     * ActionScheduler manages the retrying of application requests which
     * were previously denied due to request contention in the server GLO.
     */
    protected class ActionScheduler extends Thread {

        private Action action;
        private String vncServer;
        private int vncPort;
        private String vncUsername;
        private String vncPassword;

        public ActionScheduler(Action action, String vncServer, int vncPort, String vncUsername, String vncPassword) {
            this.action = action;
            this.vncServer = vncServer;
            this.vncPort = vncPort;
            this.vncUsername = vncUsername;
            this.vncPassword = vncPassword;
        }

        @Override
        public void run() {
            // wait for a retry window
            synchronized (actionLock) {
                try {
                    logger.fine("vnc viewer: waiting for retry window");
                    actionLock.wait();
                } catch (Exception e) {
                    logger.fine("vnc viewer: exception waiting for retry: " + e);
                }
            }
            // retry this request
            logger.fine("vnc viewer: retrying: " + action + ", " + vncServer + ", " + vncPort);
            sendRequest(action, vncServer, vncPort, vncUsername, vncPassword);
        }
    }

    protected void sendRequest(Action action, String vncServer, int vncPort, String vncUsername, String vncPassword) {
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
            logger.fine("vnc viewer: sending request: " + msg);
            ChannelController.getController().sendMessage(msg);
        }
    }

    /**
     * Retries a denied request
     * @param action the action to retry
     * @param position the "position" of the video - this is not used by the
     * VideoApp base class
     */
    protected void retryRequest(Action action, String vncServer, int vncPort, String vncUsername, String vncPassword) {
        new ActionScheduler(action, vncServer, vncPort, vncUsername, vncPassword).start();
    }

    /**
     * Handle a request from the server GLO
     * @param msg the request
     */
    public void handleResponse(TightVNCModuleCellMessage msg) {
        String controlling = msg.getUID();
        String myUID = ((TightVNCModuleCell) cell).getUID();
        boolean forMe = (myUID.equals(controlling));
        TightVNCModuleCellMessage vnccn = null;

        if (isSynced()) {
            logger.fine("vnc viewer: " + myUID + " received message: " + msg);
            if (msg.getRequestStatus() == RequestStatus.REQUEST_DENIED) {
                // this request was denied, create a retry thread
                try {
                    logger.info("vnc viewer: scheduling retry of request: " + msg);
                    retryRequest(msg.getAction(), msg.getServer(), msg.getPort(), msg.getUsername(), msg.getPassword());
                } catch (Exception e) {
                    logger.warning("vnc viewer: failed to create retry request for: " + msg);
                }
            } else {
                switch (msg.getAction()) {
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
                                if (cellMenu.isActive()){
                                    cellMenu.disableButton(Button.UNSYNC);
                                    cellMenu.enableButton(Button.SYNC);
                                }
                                logger.info("vnc viewer: synced");
                                showHUDMessage("synced", 3000);
                            }
                        }
                        break;
                    case REQUEST_COMPLETE:
                        synchronized (actionLock) {
                            try {
                                logger.fine("vnc viewer: waking retry threads");
                                actionLock.notify();
                            } catch (Exception e) {
                                logger.warning("vnc viewer: exception notifying retry threads: " + e);
                            }
                        }
                        break;
                    case NO_ALTER_PERM:  // TW -- If we receive this message (in
                                         // response to a message) it means we
                                         // do not have permission to alter the
                                         // TightVNC cell.
                
                        listenersOn = false;  // TW
                        setInControl(false);  // TW
                        setReadOnly (true);  // TW                     
                        break;  // TW
                    case ALTER_PERM:
                        listenersOn = true; // TW
                        setReadOnly (false);  // TW
                        break;  // TW
                    case PING:  // Respond to a ping from the server.
                        pingServer();
                        break;
                    default:
                        logger.warning("vnc viewer: unhandled message type: " + msg.getAction());
                        break;
                }
                if ((forMe == true) && (msg.getAction() != Action.REQUEST_COMPLETE)) {
                    // notify everyone that the request has completed
                    vnccn = new TightVNCModuleCellMessage(msg);
                    vnccn.setAction(Action.REQUEST_COMPLETE);
                }
            }
        }
        if (vnccn != null) {
            logger.fine("vnc viewer: sending message: " + vnccn);
            ChannelController.getController().sendMessage(vnccn);
        }
    }

    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    /**
     * Set the size of the application
     * @param width the width of the application
     * @param height the height of the application
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // If the user resizes the application window,
        // we need to update the window's size in
        // VR units (where one 2-D unit is a square of
        // 256 pixels on each side).  This will enable
        // our impromptu "button" (that allows the
        // user to right-click for the WonderDAC dialog)
        // to relocate itself to the new upper-left
        // corner of the application window.
        // TW
        vncCell.setCellWidth((float)width/PIX_PER_VRU);  // TW
        vncCell.setCellHeight((float)height/PIX_PER_VRU);  // TW
        
        drawingSurface.setSize(width, height);
    }

    /**
     * Updates the button state of the HUD control panel to match the
     * current state of the vnc viewer
     */
    protected void updateMenu() {
        if (((TightVNCCellMenu) cellMenu).isActive()) {
            if (isSynced()) {
                ((TightVNCCellMenu) cellMenu).enableButton(Button.SYNC);
                ((TightVNCCellMenu) cellMenu).disableButton(Button.UNSYNC);
            } else {
                ((TightVNCCellMenu) cellMenu).enableButton(Button.UNSYNC);
                ((TightVNCCellMenu) cellMenu).disableButton(Button.SYNC);
            }
        }
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

        if ((inControl == true) && listenersOn) {  // TW
            CellMenuManager.getInstance().showMenu(this.getCell(), cellMenu, null);
        } else {
            CellMenuManager.getInstance().hideMenu();
        }
    }

    @Override
    public void takeControl(MouseEvent me) {
        logger.info("vnc viewer: has control");
        super.takeControl(me);
        setInControl(true);
        }

    @Override
    public void releaseControl(MouseEvent me) {
        logger.info("vnc viewer: lost control");
        super.releaseControl(me);
        setInControl(false);
    }
}
