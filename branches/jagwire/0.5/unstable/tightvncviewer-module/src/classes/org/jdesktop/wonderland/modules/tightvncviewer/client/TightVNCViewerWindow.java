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
package org.jdesktop.wonderland.modules.tightvncviewer.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.hud.HUDMessage;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.tightvncviewer.client.cell.TightVNCViewerCell;
import org.jdesktop.wonderland.modules.tightvncviewer.common.TightVNCViewerConstants;
import org.jdesktop.wonderland.modules.tightvncviewer.common.TightVNCViewerState;

/**
 * A VNC viewer window.
 *
 * @author nsimpson
 */
@ExperimentalAPI
public class TightVNCViewerWindow extends WindowSwing implements TightVNCViewerToolListener {

    /** The logger used by this class. */
    private static final Logger logger = Logger.getLogger(TightVNCViewerWindow.class.getName());
    /** The cell in which this window is displayed. */
    private TightVNCViewerCell cell;
    // shared state
    @UsesCellComponent
    private SharedStateComponent ssc;
    private SharedMapCli statusMap;
    private String server;
    private int port;
    private String username;
    private String password;
    private TightVNCViewerPanel vncViewerPanel;
    private TightVNCViewerToolManager toolManager;
    private TightVNCViewerControlPanel controls;
    private TightVNCViewerWrapper viewer;
    private TightVNCViewerSessionDialog sessionDialog;
    private HUDMessage messageComponent;
    private HUDComponent controlComponent;
    private HUDComponent sessionComponent;
    private boolean synced = true;
    private DisplayMode displayMode;

    /**
     * Create a new instance of a TightVNCViewerWindow.
     *
     * @param cell The cell in which this window is displayed.
     * @param app The app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param topLevel Whether the window is top-level (e.g. is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     */
    public TightVNCViewerWindow(TightVNCViewerCell cell, App2D app, int width, int height,
            boolean topLevel, Vector2f pixelScale)
            throws InstantiationException {
        super(app, width, height, topLevel, pixelScale);
        this.cell = cell;
        setTitle(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/tightvncviewer/client/resources/Bundle").getString("VNC_VIEWER"));

        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    // This must be invoked on the AWT Event Dispatch Thread
                    vncViewerPanel = new TightVNCViewerPanel(TightVNCViewerWindow.this);
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        // Parent to Wonderland main window for proper focus handling
        JmeClientMain.getFrame().getCanvas3DPanel().add(vncViewerPanel);
        setComponent(vncViewerPanel);
        initHUD();
        setDisplayMode(DisplayMode.HUD);
        showControls(false);
    }

    private void initHUD() {
        if (messageComponent == null) {
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
            messageComponent = mainHUD.createMessage("");
            messageComponent.setPreferredLocation(Layout.NORTHEAST);
            messageComponent.setDecoratable(false);
            mainHUD.addComponent(messageComponent);
        }
    }

    /**
     * Show a status message in the HUD and remove it after a timeout
     * @param message the string to display in the message
     * @param timeout the period in milliseconds to display the message for
     */
    public void showHUDMessage(final String message, final int timeout) {

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                messageComponent.setMessage(message);
                messageComponent.setVisible(true);
                if (timeout > 0) {
                    messageComponent.setVisible(false, timeout);
                }
            }
        });
    }

    public void showHUDMessage(String message) {
        showHUDMessage(message, 0);
    }

    public void hideHUDMessage() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                messageComponent.setVisible(false);
            }
        });
    }

    public void setSSC(SharedStateComponent ssc) {
        this.ssc = ssc;
        // load the server player's status map
        statusMap = ssc.get(TightVNCViewerConstants.STATUS_MAP);
        toolManager.setSSC(ssc);
    }

    private void createSessionDialog() {
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        sessionDialog = new TightVNCViewerSessionDialog();
        sessionComponent = mainHUD.createComponent(sessionDialog);
        sessionComponent.setName("Open Connection");
        sessionComponent.setPreferredLocation(Layout.CENTER);
        mainHUD.addComponent(sessionComponent);
        sessionDialog.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                sessionComponent.setVisible(false);
                if (e.getID() == (ActionEvent.ACTION_PERFORMED)) {
                    // open connection
                    server = sessionDialog.getServer();
                    port = sessionDialog.getPort();
                    username = sessionDialog.getUser();
                    password = sessionDialog.getPassword();

                    if (isSynced()) {
                        statusMap.put(TightVNCViewerConstants.VNC_SERVER, SharedString.valueOf(server));
                        statusMap.put(TightVNCViewerConstants.VNC_PORT, SharedInteger.valueOf(port));
                        statusMap.put(TightVNCViewerConstants.VNC_USERNAME, SharedString.valueOf(username));
                        statusMap.put(TightVNCViewerConstants.VNC_PASSWORD, SharedString.valueOf(password));
                        statusMap.put(TightVNCViewerConstants.VNC_VIEWER_STATE, SharedString.valueOf(TightVNCViewerState.CONNECTED.name()));
                    } else {
                        openConnection(server, port, username, password);
                    }
                }
            }
        });
    }

    public void openConnection() {
        if (sessionComponent == null) {
            createSessionDialog();
        }
        sessionComponent.setVisible(true);
    }

    /**
     * Open connection to VNC server
     * @param server the address of the VNC server
     * @param port the port number of the VNC server
     */
    public void openConnection(final String server, final int port,
            final String username, final String password) {
        // terminate any existing VNC session
        //closeConnection();

        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;

        // start a new session
        logger.info("vnc viewer: opening VNC session to: " + server + ":" + port);
        showHUDMessage("Connecting to: " + server, 5000);

        // update dialog
        if (sessionComponent == null) {
            createSessionDialog();
        }

        sessionDialog.setServer(server);
        sessionDialog.setPort(port);
        sessionDialog.setUser(username);
        sessionDialog.setPassword(password);

        // open connection to VNC server
        viewer = new TightVNCViewerWrapper((TightVNCViewerApp) getApp());

        viewer.mainArgs = new String[]{
                    "HOST", server,
                    "PORT", String.valueOf(port),
                    "PASSWORD", password,
                    "Show Offline Desktop", "No",
                    "Encoding", "Tight",
                    "Compression level", "9",
                    "JPEG image quality", "9",
                    "Use CopyRect", "No"
                };
        viewer.inAnApplet = false;
        viewer.inSeparateFrame = false;
        viewer.init();
        viewer.start();
    }

    /**
     * Close connection to VNC server
     */
    public void closeConnection() {
        if (viewer != null) {
            viewer.disconnect();
            viewer.stop();
            viewer = null;
            vncViewerPanel.setCanvas(null);
            vncViewerPanel.repaint();
            showHUDMessage("Connection closed", 3000);
        }
    }

    /**
     * Send Ctrl-Alt-Del to Windows hosts
     */
    public void sendCtrlAltDel() {
        if (viewer != null) {
            viewer.sendCtrlAltDel();
        }
    }

    /**
     * Synchronize with the shared state
     */
    public void sync() {
        sync(true);
    }

    /**
     * Set the sync state
     * @param syncing true if re-syncing, false if unsyncing
     */
    public void sync(boolean syncing) {
        if ((syncing == false) && (synced == true)) {
            synced = false;
            logger.info("unsynced");
        } else if ((syncing == true) && (synced == false)) {
            synced = true;
            logger.info("synced");
        }

        controls.setSynced(syncing);
    }

    /**
     * Gets whether the application is currently synced with the shared
     * state
     * @return true if the application is synced, false otherwise
     */
    public boolean isSynced() {
        return synced;
    }

    /**
     * Toggle the display of the webcam viewer from in-world to on-HUD
     */
    public void toggleHUD() {
    }

    /**
     * Sets the display mode for the control panel to in-world or on-HUD
     * @param mode the control panel display mode
     */
    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    /**
     * Gets the control panel display mode
     * @return the display mode of the control panel: in-world or on HUD
     */
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    /**
     * Shows or hides the HUD controls.
     * The controls are shown in-world or on-HUD depending on the selected
     * DisplayMode.
     * @param visible true to show the controls, hide to hide them
     */
    public void showControls(final boolean visible) {
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

        if (controlComponent == null) {
            // create control panel
            controls = new TightVNCViewerControlPanel(TightVNCViewerWindow.this);

            // add event listeners
            toolManager = new TightVNCViewerToolManager(TightVNCViewerWindow.this);
            controls.addCellMenuListener(toolManager);

            // create HUD control panel
            controlComponent = mainHUD.createComponent(controls, cell);
            controlComponent.setPreferredLocation(Layout.SOUTH);

            // add HUD control panel to HUD
            mainHUD.addComponent(controlComponent);
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                // change visibility of controls
                if (getDisplayMode() == DisplayMode.HUD) {
                    if (controlComponent.isWorldVisible()) {
                        controlComponent.setWorldVisible(false);
                    }

                    controlComponent.setVisible(visible);
                } else {
                    controlComponent.setWorldLocation(new Vector3f(0.0f, -4.2f, 0.1f));
                    if (controlComponent.isVisible()) {
                        controlComponent.setVisible(false);
                    }

                    controlComponent.setWorldVisible(visible); // show world view
                }

                updateControls();
            }
        });
    }

    public boolean showingControls() {
        return ((controlComponent != null) && (controlComponent.isVisible() || controlComponent.isWorldVisible()));
    }

    protected void updateControls() {
        controls.setSynced(isSynced());

        controls.setOnHUD(!toolManager.isOnHUD());
    }

    public Image getImage() {
        return null;
    }

    public void scheduleRepaint(long tm, int x, int y, int width, int height) {
        vncViewerPanel.scheduleRepaint(tm, x, y, width, height);
    }
}
