/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.webcaster.client.jme.cellrenderer.WebcasterCellRenderer;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
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
import org.jdesktop.wonderland.client.utils.VideoLibraryLoader;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.webcaster.client.utils.RTMPOut;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellChangeMessage;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellClientState;

/**
 * @author Christian O'Connell
 * @author Bernard Horan
 */
public class WebcasterCell extends Cell
{
    private static final boolean VIDEO_AVAILABLE = VideoLibraryLoader.loadVideoLibraries();

    private static final String SERVER_URL;

    static
    {
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
            //logger.log(Level.SEVERE, null, ex);
        }
        if (sgs_serverURL != null) {
            host = sgs_serverURL.getHost();
        }
        //logger.warning("host: " + host);
        SERVER_URL = host;
        //logger.warning("SERVER_URL: " + SERVER_URL);
    }

    private WebcasterCellRenderer renderer = null;

    @UsesCellComponent private ContextMenuComponent contextComp = null;
    private ContextMenuFactorySPI menuFactory = null;

    private HUD mainHUD;
    private HUDComponent hudComponent;
    private WebcasterControlPanel controlPanel;

    private boolean localRecording = false;
    private boolean remoteWebcasting = false;
    private RTMPOut streamOutput;

    private AudioResource startSound = null;
    /** the message handler, or null if no message handler is registered */
    private WebcasterCellMessageReceiver receiver = null;

    private String streamID = "";

    public WebcasterCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        try{startSound = new AudioResource(AssetUtils.getAssetURL("wla://webcaster/startsound.au"));}catch(MalformedURLException e){}
    }

    public void showControlPanel(){
        try
        {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    hudComponent.setVisible(true);
                }
            });
        } catch (Exception x) {
            throw new RuntimeException("Cannot add hud component to main hud");
        }
    }

    public JComponent getCaptureComponent(){
        return renderer.getCaptureComponent();
    }

    public void setRecording(boolean isRecording)
    {
        renderer.setButtonRecordingState(isRecording);
        WebcasterCellChangeMessage msg = new WebcasterCellChangeMessage(localRecording);
        sendCellMessage(msg);

        if (!isRecording & localRecording) {
            try {
                streamOutput.close();
                streamOutput = null;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "problem closing stream", e);
            }
        }

        startSound.play();
        localRecording = isRecording;
    }

    public boolean getRecording(){
        return localRecording;
    }

    public void write(BufferedImage frame)
    {
        if (streamOutput == null){
            streamOutput = new RTMPOut("rtmp://" + SERVER_URL + ":1935/live/" + controlPanel.getStreamName());
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
                                    hudComponent.setName("Webcaster Control Panel");
                                    mainHUD.addComponent(hudComponent);
                                }
                            });
                        } catch (Exception x) {
                            throw new RuntimeException("Cannot create control panel");
                        }
                    }

                    if (menuFactory == null) {
                        menuFactory = new ContextMenuFactorySPI() {

                            public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                                return new ContextMenuItem[]{new SimpleContextMenuItem("Open Control Panel", new ContextMenuActionListener() {

                                        public void actionPerformed(ContextMenuItemEvent event) {
                                            try {
                                                SwingUtilities.invokeLater(new Runnable() {

                                                    public void run() {
                                                        hudComponent.setVisible(true);
                                                    }
                                                });
                                            } catch (Exception x) {
                                                throw new RuntimeException("Cannot add hud component to main hud");
                                            }
                                        }
                                    }),

                                    new SimpleContextMenuItem("Open Browser Viewer", new ContextMenuActionListener(){
                                        public void actionPerformed(ContextMenuItemEvent event) {
                                            try {
                                                SwingUtilities.invokeLater(new Runnable() {

                                                    public void run() {

                                                        try{
                                                            java.awt.Desktop.getDesktop().browse(java.net.URI.create("http://" + SERVER_URL + ":8080/webcaster/webcaster/index.html?server=" + SERVER_URL + "&stream=" + streamID));
                                                        }
                                                        catch (IOException e) {
                                                            throw new RuntimeException("Error opening browser");
                                                        }
                                                    }
                                                });
                                            } catch (Exception x) {
                                                throw new RuntimeException("Cannot find browser");
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
            }
            case DISK:
                if (!increasing) {
                    setRecording(false);
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
    public void setClientState(CellClientState state){
        super.setClientState(state);
        remoteWebcasting = ((WebcasterCellClientState) state).isWebcasting();
        streamID = ((WebcasterCellClientState)state).getStreamID();
    }

    private ChannelComponent getChannel() {
        return getComponent(ChannelComponent.class);
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType)
    {
        if (rendererType == RendererType.RENDERER_JME)
        {
            this.renderer = new WebcasterCellRenderer(this);
            return this.renderer;
        }
        else
        {
            return super.createCellRenderer(rendererType);
        }
    }

    private void setRemoteWebcasting(boolean b) {
        //logger.info("setRemoteWebcasting: " + b);
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
                logger.warning("it's from me to me!");
            }
        }
    }
}
