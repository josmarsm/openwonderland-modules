/**
 * Open Wonderland
 *
 * Copyright (c) 2011-12, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.webcaster.client;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.client.utils.AudioResource;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.webcaster.client.utils.RTMPOut;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellChangeMessage;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellClientState;
import org.jdesktop.wonderland.video.client.VideoLibraryLoader;

/**
 * @author Christian O'Connell
 * @author Bernard Horan
 */
public class WebcasterCell extends Cell {

    private static final boolean VIDEO_AVAILABLE = VideoLibraryLoader.loadVideoLibraries();
    private static final String SERVER_URL;

    static {
        //This seems to be the wrong property, why isn't it the web server?
        //Because the web server url doesn't seem to be available from the client
        String sgs_server = System.getProperty("sgs.server");
        //logger.warning("sgs.server: " + sgs_server);
        URL sgs_serverURL = null;
        String host = "127.0.0.1";
        try {
            sgs_serverURL = new URL(sgs_server);
            //logger.warning("sgs_serverURL: " + sgs_serverURL);
        } catch (MalformedURLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        if (sgs_serverURL != null) {
            host = sgs_serverURL.getHost();
        }
        //logger.warning("host: " + host);
        SERVER_URL = host;
        //logger.warning("SERVER_URL: " + SERVER_URL);
    }
    private static final ResourceBundle bundle = ResourceBundle.getBundle("org/jdesktop/wonderland/modules/webcaster/client/resources/Bundle");

    private WebcasterCellRenderer renderer = null;
    @UsesCellComponent
    private ContextMenuComponent contextComp = null;
    private ContextMenuFactorySPI menuFactory = null;
    private HUD mainHUD;
    private HUDComponent hudComponent;
    private WebcasterControlPanel controlPanel;

    private boolean localWebcasting;
    private boolean remoteWebcasting;
    private RTMPOut streamOutput;
    private AudioResource startSound = null;
    /** the message handler, or null if no message handler is registered */
    private WebcasterCellMessageReceiver receiver = null;
    private int streamID;

    public WebcasterCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        localWebcasting = false;
        remoteWebcasting = false;
        URL url = WebcasterCell.class.getResource("resources/startsound.au");
        startSound = new AudioResource(url);
    }

    public void showControlPanel() {
        try {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    hudComponent.setVisible(true);
                }
            });
        } catch (Exception x) {
            throw new RuntimeException("Cannot add hud component to main hud", x);
        }
    }

    public JComponent getCaptureComponent() {
        return renderer.getCaptureComponent();
    }

    public void setWebcasting(boolean isWebcasting) {
        logger.warning("webcasting: " + isWebcasting);
        //TODO need to check if remote webcasting
        renderer.setButtonWebcastingState(isWebcasting);
        if (!isWebcasting & localWebcasting) {
            try {
                streamOutput.close();
                streamOutput = null;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "problem closing stream", e);
            }
        }

        startSound.play();
        localWebcasting = isWebcasting;
        WebcasterCellChangeMessage msg = new WebcasterCellChangeMessage(localWebcasting);
        sendCellMessage(msg);
    }

    public boolean getWebcasting(){
        return localWebcasting;
    }

    public void write(BufferedImage frame) {
        if (streamOutput == null) {
            streamOutput = new RTMPOut("rtmp://" + SERVER_URL + ":1935/live/" + streamID);
        }
        streamOutput.write(frame);
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        switch (status) {
            case RENDERING:
                if (increasing) {
                    if (mainHUD == null) {
                        mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                        try {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    controlPanel = new WebcasterControlPanel(WebcasterCell.this, streamID);
                                    hudComponent = mainHUD.createComponent(controlPanel);
                                    hudComponent.setPreferredLocation(Layout.NORTHWEST);
                                    hudComponent.setName(bundle.getString("WEBCASTER CONTROL PANEL"));
                                    mainHUD.addComponent(hudComponent);
                                }
                            });
                        } catch (Exception x) {
                            throw new RuntimeException("Cannot create control panel", x);
                        }
                    }

                    if (menuFactory == null) {
                        menuFactory = new ContextMenuFactorySPI() {
                            public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                                return new ContextMenuItem[]{
                                    new SimpleContextMenuItem(bundle.getString("OPEN CONTROL PANEL"), new ContextMenuActionListener() {

                                        public void actionPerformed(ContextMenuItemEvent event) {
                                            try {
                                                SwingUtilities.invokeLater(new Runnable() {

                                                    public void run() {
                                                        hudComponent.setVisible(true);
                                                    }
                                                });
                                            } catch (Exception x) {
                                                throw new RuntimeException("Cannot add hud component to main hud", x);
                                            }
                                        }
                                    }),
                                       new SimpleContextMenuItem(bundle.getString("OPEN BROWSER VIEWER"), new ContextMenuActionListener() {

                                        public void actionPerformed(ContextMenuItemEvent event) {
                                            try {
                                                SwingUtilities.invokeLater(new Runnable() {

                                                    public void run() {

                                                        try {
                                                            java.awt.Desktop.getDesktop().browse(URI.create("http://" + SERVER_URL + ":8080/webcaster/webcaster/index.html?server=" + SERVER_URL + "&stream=" + streamID));
                                                        } catch (IOException e) {
                                                            throw new RuntimeException("Error opening browser", e);
                                                        }
                                                    }
                                                });
                                            } catch (Exception x) {
                                                throw new RuntimeException("Cannot find browser", x);
                                            }
                                        }
                                    }),
                                            new SimpleContextMenuItem(bundle.getString("COPY WEB URL"), new ContextMenuActionListener() {

                                        public void actionPerformed(ContextMenuItemEvent event) {
                                            try {
                                                SwingUtilities.invokeLater(new Runnable() {

                                                    public void run() {

                                                        String selection = "http://" + SERVER_URL + ":8080/webcaster/webcaster/index.html?server=" + SERVER_URL + "&stream=" + streamID;
                                                        StringSelection data = new StringSelection(selection);
                                                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                                        clipboard.setContents(data, data);
                                                    }
                                                });
                                            } catch (Exception x) {
                                                throw new RuntimeException("Cannot find browser", x);
                                            }
                                        }
                                    })};
                            }
                        };
                        contextComp.addContextMenuFactory(menuFactory);
                    }
                }

                break;
            case ACTIVE: {
                if (increasing) {
                    //About to become visible, so add the message receiver
                    if (receiver == null) {
                        receiver = new WebcasterCellMessageReceiver();
                        getChannel().addMessageReceiver(WebcasterCellChangeMessage.class, receiver);
                    }
                }
                break;
            }
            case DISK:
                if (!increasing) {
                    setWebcasting(false);
                    if (getChannel() != null) {
                        getChannel().removeMessageReceiver(WebcasterCellChangeMessage.class);
                    }
                    receiver = null;
                    if (menuFactory != null) {
                        contextComp.removeContextMenuFactory(menuFactory);
                        menuFactory = null;
                    }
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            hudComponent.setVisible(false);
                        }
                    });

                }
                break;
        }
    }

    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        remoteWebcasting = ((WebcasterCellClientState) state).isWebcasting();
        streamID = ((WebcasterCellClientState) state).getStreamID();
    }

    private ChannelComponent getChannel() {
        return getComponent(ChannelComponent.class);
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new WebcasterCellRenderer(this);
            return this.renderer;
        } else {
            return super.createCellRenderer(rendererType);
        }
    }

    private void setRemoteWebcasting(boolean b) {
        logger.warning("setRemoteWebcasting: " + b);
        controlPanel.setRemoteWebcasting(b);
        remoteWebcasting = b;
    }

    public boolean isRemoteWebcasting() {
        return remoteWebcasting;
    }

    public void updateControlPanel() {
        controlPanel.updateWebcasting();
    }

    class WebcasterCellMessageReceiver implements ComponentMessageReceiver {

        public void messageReceived(CellMessage message) {
            WebcasterCellChangeMessage wccm = (WebcasterCellChangeMessage) message;
            BigInteger senderID = wccm.getSenderID();
            if (senderID == null) {
                //Broadcast from server
                senderID = BigInteger.ZERO;
            }
            if (!senderID.equals(getCellCache().getSession().getID())) {
                setRemoteWebcasting(wccm.isWebcasting());

            } else {
                //logger.warning("it's from me to me!");
            }
        }
    }
}
