/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.movierecorder.client;

import java.awt.Toolkit;
import java.io.File;
import java.math.BigInteger;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.movierecorder.common.MovieRecorderCellChangeMessage;
import org.jdesktop.wonderland.modules.movierecorder.common.MovieRecorderCellClientState;

/**
 *
 * @author Bernard Horan
 */
public class MovieRecorderCell extends Cell {

    @UsesCellComponent private ContextMenuComponent contextComp = null;
    /**
     *Directory to hold images when recording, deleted when finished
     **/
    private static final File imageDirectory = ClientContext.getUserDirectory("WonderlandRecording");

    private ContextMenuFactorySPI menuFactory = null;

    private boolean isRecording;
    private String userName;
    private MovieRecorderCellRenderer renderer;
    private ControlPanelUI ui;
    
    /**
     *Actual frame rate
     **/
    private float capturedFrameRate;


    /**
     *Time when recording started
     **/
    private long startTime;
    /**
     *scale of the image  to be recorded
     **/
    private float scale = 1.0f;

    /** the message handler, or null if no message handler is registered */
    private MovieRecorderCellMessageReceiver receiver = null;

    public MovieRecorderCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        isRecording = false;
        
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        if (increasing && status == CellStatus.RENDERING) {
            if (ui == null) {
                initUI();
            }
            if (menuFactory == null) {
                final ContextMenuActionListener l = new ContextMenuActionListener() {

                    public void actionPerformed(ContextMenuItemEvent event) {
                        ui.setVisible(true);
                    }
                };
                menuFactory = new ContextMenuFactorySPI() {

                    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                        return new ContextMenuItem[]{
                                    new SimpleContextMenuItem("Open HUD Control Panel", l)
                                };
                    }
                };
                contextComp.addContextMenuFactory(menuFactory);
            }
        }
        if (increasing && status.equals(CellStatus.ACTIVE)) {
            //About to become visible, so add the message receiver
            if (receiver == null) {
                receiver = new MovieRecorderCellMessageReceiver();
                getChannel().addMessageReceiver(MovieRecorderCellChangeMessage.class, receiver);
            }
        }
        if (status.equals(CellStatus.DISK)) {
            //Cleanup
            if (getChannel() != null) {
                getChannel().removeMessageReceiver(MovieRecorderCellChangeMessage.class);
            }
            receiver = null;
            if (menuFactory != null) {
                contextComp.removeContextMenuFactory(menuFactory);
                menuFactory = null;
            }
            ui.setVisible(false);
            ui = null;
        }
    
    }

    @Override
    public void setClientState(CellClientState cellClientState) {
        super.setClientState(cellClientState);
        isRecording = ((MovieRecorderCellClientState) cellClientState).isRecording();
        userName = ((MovieRecorderCellClientState) cellClientState).getUserName();
        if (isRecording) {
            if (userName == null) {
                logger.warning("userName should not be null");
            }
        }
        if (!isRecording) {
            if (userName != null) {
                logger.warning("userName should be null");
            }
        }
    }

    private void initUI () {
        ui = new ControlPanelUI(this);
    }

    

    /**
     * Return the frames per second at which JPEGs were recorded
     * @return The frames per second recorded
     */
    float getCapturedFrameRate() {
        return capturedFrameRate;
    }

    /**
     * Get the directory path in hwich the JPEGs should be written
     * @return A string identifying the name of the path in which the images are written
     */
    static File getImageDirectory() {
        //System.err.println("IMAGE DIRECTORY: " + imageDirectory);
        return imageDirectory;
    }

    public JComponent getCaptureComponent() {
        return renderer.getCaptureComponent();
    }

    void startRecording(float scale) {
        ((MovieRecorderCellRenderer)renderer).resetImageCounter();
        ((MovieRecorderCellRenderer)renderer).resetFrameCounter();
        resetStartTime();
        this.scale = scale;
        isRecording = true;
        logger.info("Start Recording, scale: " + scale);
    }

    

    /**
     * Reset the time at which the canvas began recordinig JPEGs
     */
    private void resetStartTime() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Stop capturing JPEGs
     */
    void stopRecording() {
        isRecording = false;
        calculateActualFrameRate();
        logger.info("Stop recording");
    }

    /**
     * Determine the rate of frames per second that we recorded JPEGs
     */
    private void calculateActualFrameRate() {
        // Get elapsed time in milliseconds
        long elapsedTimeMillis = System.currentTimeMillis() - startTime;

        // Get elapsed time in seconds
        float elapsedTimeSec = elapsedTimeMillis/1000F;

        capturedFrameRate = renderer.getFrameCounter()/elapsedTimeSec;
        logger.info("capturedFrameRate: " + capturedFrameRate);

    }

    private ChannelComponent getChannel() {
        return getComponent(ChannelComponent.class);
    }

    void startRecording() {
        logger.info("start recording");
        userName = getCurrentUserName();
        setRecording(true);
        MovieRecorderCellChangeMessage msg = MovieRecorderCellChangeMessage.recordingMessage(getCellID(), isRecording, userName);
        getChannel().send(msg);        
    }

    void stop() {
        if (!isRecording) {
            //logger.warning("no reason to stop, not recording");
            return;
        }
        if (userName != null && userName.equals(getCurrentUserName())) {
            MovieRecorderCellChangeMessage msg = null;
            if (isRecording) {
                msg = MovieRecorderCellChangeMessage.recordingMessage(getCellID(), false, userName);
            }
            if (msg != null) {
                getChannel().send(msg);
            }
            setRecording(false);
            userName = null;
        } else {
            logger.warning("Attempt to stop by non-initiating user: " + userName);
            SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(getParentFrame(), "You can't stop a recording that was started by another user");
                    }
                });
        }
    }

    private void setRecording(boolean b) {
        logger.info("setRecording: " + b);
        renderer.setRecording(b);
        isRecording = b;
    }

    boolean isRecording() {
        return isRecording;
    }

    private String getCurrentUserName() {
        return getCellCache().getSession().getUserID().getUsername();
    }

    private JFrame getParentFrame() {
        return ClientContextJME.getClientMain().getFrame().getFrame();
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new MovieRecorderCellRenderer(this);
            renderer.resetImageCounter();
            return this.renderer;
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }  

    class MovieRecorderCellMessageReceiver implements ComponentMessageReceiver {

        public void messageReceived(CellMessage message) {
            MovieRecorderCellChangeMessage sccm = (MovieRecorderCellChangeMessage) message;
            BigInteger senderID = sccm.getSenderID();
            if (senderID == null) {
                //Broadcast from server
                senderID = BigInteger.ZERO;
            }
            if (!senderID.equals(getCellCache().getSession().getID())) {
                switch (sccm.getAction()) {
                    case RECORD:
                        setRecording(sccm.isRecording());
                        userName = sccm.getUserName();
                        break;
                    default:
                        logger.severe("Unknown action type: " + sccm.getAction());

                }
            }
        }
    }
}

