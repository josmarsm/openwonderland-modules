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

import com.jme.math.Vector2f;
import com.sun.awt.AWTUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.screenshare.common.ScreenShareConstants;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.webcamviewer.client.WebcamViewerApp;
import org.jdesktop.wonderland.modules.webcamviewer.client.WebcamViewerWindow;
import org.jdesktop.wonderland.modules.webcamviewer.client.cell.WebcamViewerCell;

/**
 * Screen sharing cell
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class ScreenShareCell extends WebcamViewerCell
    implements SharedMapListenerCli, ContextMenuActionListener
{
    private static final Logger LOGGER =
            Logger.getLogger(ScreenShareCell.class.getName());
    private static final ResourceBundle BUNDLE =
            ResourceBundle.getBundle("org.jdesktop.wonderland.modules.screenshare.client.resources.Bundle");

    private static final String SCREENSHARE_URL =
            "screenshare/screenshare/ScreenShareAtmosphereHandler";

    @UsesCellComponent
    private SharedStateComponent ssc;

    @UsesCellComponent
    private ContextMenuComponent menu;

    private final ContextMenuFactory menuFactory;
    private final SimpleContextMenuItem startItem;
    private final SimpleContextMenuItem stopItem;

    private ScreenShareWindow window;
    private SelectorWindow selector;
    private Uploader server;

    /**
     * Create an instance of ScreenShareCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public ScreenShareCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);

        menuFactory = new ContextMenuFactory();
        startItem = new SimpleContextMenuItem(BUNDLE.getString("Start_Sharing"), this);
        stopItem = new SimpleContextMenuItem(BUNDLE.getString("Stop_Sharing"), this);
    }

    public boolean isSharing() {
        SharedMapCli statusMap = ssc.get(ScreenShareConstants.STATUS_MAP);
        SharedString session = statusMap.get(ScreenShareConstants.CONTROLLER,
                                             SharedString.class);
        if (session == null) {
            return false;
        }

        return getSessionID().equals(session.getValue());
    }

    public String getCurrentController() {
        SharedMapCli statusMap = ssc.get(ScreenShareConstants.STATUS_MAP);
        SharedString name = statusMap.get(ScreenShareConstants.CONTROLLER_NAME,
                                          SharedString.class);

        if (name == null) {
            return null;
        }

        return name.getValue();
    }

    public void startSharing() {
        // if someone else is in control, double check that we want to steal
        // control
        String currentController = getCurrentController();
        if (currentController != null) {
            String title = BUNDLE.getString("Take_Control_Title");
            String message = MessageFormat.format(BUNDLE.getString("Take_Control"),
                                                  currentController);

            int res = JOptionPane.showConfirmDialog(window.getComponent(),
                    message, title, JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (res == JOptionPane.NO_OPTION) {
                return;
            }
        }

        String username = getCellCache().getSession().getUserID().getUsername();

        SharedMapCli statusMap = ssc.get(ScreenShareConstants.STATUS_MAP);
        statusMap.put(ScreenShareConstants.CONTROLLER,
                      SharedString.valueOf(getSessionID()));
        statusMap.put(ScreenShareConstants.CONTROLLER_NAME,
                      SharedString.valueOf(username));

        window.setSharing(true);
    }

    public void stopSharing() {
        // be sure to stop uploading no matter what
        if (server != null) {
            server.stop();
        }

        if (isSharing()) {
            SharedMapCli statusMap = ssc.get(ScreenShareConstants.STATUS_MAP);
            statusMap.put(ScreenShareConstants.CONTROLLER, null);
            statusMap.put(ScreenShareConstants.CONTROLLER_NAME, null);
        }

        window.setSharing(false);
    }

    public void toggleSharing() {
        if (isSharing()) {
            stopSharing();
        } else {
            startSharing();
        }
    }

    public void actionPerformed(ContextMenuItemEvent event) {
        if (event.getContextMenuItem() == startItem) {
            startSharing();
        } else if (event.getContextMenuItem() == stopItem) {
            stopSharing();
        }
    }

    @Override
    public void propertyChanged(SharedMapEventCli event) {
        if (event.getMap().getName().equals(ScreenShareConstants.STATUS_MAP)) {
            if (event.getPropertyName().equals(ScreenShareConstants.CONTROLLER)) {

                // update on the AWT event thread
                SharedString val = ((SharedString) event.getNewValue());
                final String session = (val==null)?null:val.getValue();

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setControllingSession(session);
                    }
                });
            }
        } else {
            super.propertyChanged(event);
        }
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if (status == CellStatus.ACTIVE && increasing) {
            // add a shared map listener
            ssc.get(ScreenShareConstants.STATUS_MAP).addSharedMapListener(this);

            // add menu
            menu.addContextMenuFactory(menuFactory);

            // connect to sharing if active
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    SharedMapCli statusMap = ssc.get(ScreenShareConstants.STATUS_MAP);
                    SharedString session = statusMap.get(ScreenShareConstants.CONTROLLER,
                                                         SharedString.class);
                    setControllingSession(session==null?null:session.getValue());
                }
            });
        } else if (status == CellStatus.INACTIVE && !increasing) {
            // remove shared map listener
            ssc.get(ScreenShareConstants.STATUS_MAP).removeSharedMapListener(this);

            // remove menu
            menu.removeContextMenuFactory(menuFactory);

            if (isSharing()) {
                stopSharing();
            }

            // make sure we are no longer capturing the screen
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    stopScreenCapture();
                }
            });
        }
    }

    @Override
    protected WebcamViewerWindow createWindow(WebcamViewerCell cell, App2D app,
            int width, int height, boolean topLevel, Vector2f pixelScale)
        throws InstantiationException
    {
        window = new ScreenShareWindow((ScreenShareCell) cell, app, width,
                                       height, topLevel, pixelScale);
        return window;
    }

    @Override
    protected WebcamViewerApp createApp(String name, Vector2f pixelScale) {
        name = BUNDLE.getString("Screen_Share");
        return super.createApp(name, pixelScale);
    }

    private void setControllingSession(String session) {
        boolean capture = (selector != null);
        boolean us = getSessionID().equals(session);

        if (us && !capture) {
            // we should be sharing, and are not sharing, so start our sharing
            startScreenCapture();
        } else if (!us && capture) {
            // we were sharing, and shouldn't be, so stop our sharing
            stopScreenCapture();
        }

        
        if (session == null) {
            // if no one is sharing any more, clear the screen and stop the
            // camera
            window.stop();
            window.clear();
        } else if (!window.isPlaying()) {
            // if someone is sharing, make sure the camera is connected
            window.connectCamera(getURL(), null, null);
        }
    }

    private void startScreenCapture() {
        selector = new SelectorWindow();
        AWTUtilities.setWindowOpaque(selector, false);
        selector.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopSharing();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                stopSharing();
            }
        });
        selector.setVisible(true);

        ImageReader reader = new ImageReader(selector);
        server = new Uploader(reader, getURL(), getCellID().toString());
        server.start();
    }

    private void stopScreenCapture() {
        if (server != null) {
            server.stop();
            server = null;
        }

        if (selector != null) {
            selector.setVisible(false);
            selector = null;
        }
    }

    private String getSessionID() {
        return getCellCache().getSession().getID().toString();
    }

    private String getURL() {
        String serverURL =
                getCellCache().getSession().getSessionManager().getServerURL();
        return serverURL + SCREENSHARE_URL + "?cellID=" + getCellID();
    }

    private class ContextMenuFactory implements ContextMenuFactorySPI {
        public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
            if (isSharing()) {
                return new ContextMenuItem[] { stopItem };
            } else {
                return new ContextMenuItem[] { startItem };
            }
        }
    }
}
