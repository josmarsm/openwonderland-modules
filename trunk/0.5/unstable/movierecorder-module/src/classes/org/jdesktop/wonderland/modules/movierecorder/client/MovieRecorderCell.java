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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.DefaultButtonModel;
import javax.swing.JComponent;
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
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.movierecorder.client.utils.EncodeException;
import org.jdesktop.wonderland.modules.movierecorder.client.utils.MovieCreator;
import org.jdesktop.wonderland.modules.movierecorder.common.MovieRecorderCellChangeMessage;
import org.jdesktop.wonderland.modules.movierecorder.common.MovieRecorderCellClientState;

/**
 *
 * @author Bernard Horan
 */
public class MovieRecorderCell extends Cell {
    private static final Logger cellLogger = Logger.getLogger(MovieRecorderCell.class.getName());

    @UsesCellComponent private ContextMenuComponent contextComp = null;
    /**
     *Directory to hold images when recording, deleted when finished
     **/
    private static final File IMAGE_DIRECTORY = ClientContext.getUserDirectory("MovieRecording");

    private ContextMenuFactorySPI menuFactory = null;

    private boolean localRecording;
    private boolean remoteRecording;
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

    /** the message handler, or null if no message handler is registered */
    private MovieRecorderCellMessageReceiver receiver = null;
    private DefaultButtonModel videoButtonModel, stillButtonModel;
    
    

    public MovieRecorderCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        localRecording = false;
        remoteRecording = false;
        videoButtonModel = new DefaultButtonModel();
        videoButtonModel.setSelected(false);
        videoButtonModel.setEnabled(true);
        videoButtonModel.addItemListener(new VideoButtonChangeListener());
        stillButtonModel = new DefaultButtonModel();
        stillButtonModel.setSelected(false);
        stillButtonModel.setEnabled(true);
        stillButtonModel.addItemListener(new StillButtonChangeListener());
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        if (increasing && status == CellStatus.RENDERING) {
            if (ui == null) {
                initUI();
                ui.setVisible(true);
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
        remoteRecording = ((MovieRecorderCellClientState) cellClientState).isRecording();       
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
        return IMAGE_DIRECTORY;
    }

    public JComponent getCaptureComponent() {
        return renderer.getCaptureComponent();
    }

    void startRecording() {
        logger.info("start recording");
        if (remoteRecording) {
            logger.warning("Can't record if someone else is already recording");
            return;
        }
        File imageDirectoryFile = getImageDirectory();
        logger.info("imageDirectory: " + imageDirectoryFile);

        if (!imageDirectoryFile.exists()) {
            logger.info("Creating image directory");
            imageDirectoryFile.mkdirs();
        }


        

        ((MovieRecorderCellRenderer)renderer).resetImageCounter();
        ((MovieRecorderCellRenderer)renderer).resetFrameCounter();
        resetStartTime();
        localRecording = true;
        MovieRecorderCellChangeMessage msg = MovieRecorderCellChangeMessage.recordingMessage(getCellID(), localRecording);
        getChannel().send(msg);
    }

    void captureImage() {
        ((MovieRecorderCellRenderer)renderer).captureImage(ui.getControlPanel().getPicturesDirectory());
    }

    /**
     * Reset the time at which the canvas began recordinig JPEGs
     */
    private void resetStartTime() {
        startTime = System.currentTimeMillis();
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
        cellLogger.info("capturedFrameRate: " + capturedFrameRate);
    }

    private ChannelComponent getChannel() {
        return getComponent(ChannelComponent.class);
    }

    void stopRecording() {
        if (!localRecording) {
            //logger.warning("no reason to stop, not recording");
            return;
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.applyPattern("yyyyMMdd_HH.mm");
        String movieFilename = "Wonderland_" + simpleDateFormat.format(calendar.getTime()) + ".mov";
        try {
            createMovie(movieFilename);
        } catch (EncodeException e) {
            logger.log(Level.SEVERE, "Failed to create movie, caused by", e.getCause());
        }
        deleteImageDirectory();
        ui.enableLocalButtons();
        localRecording = false;
        MovieRecorderCellChangeMessage msg = MovieRecorderCellChangeMessage.recordingMessage(getCellID(), localRecording);
        getChannel().send(msg);
        calculateActualFrameRate();
        logger.info("Stop recording");
    }

    private void setRemoteRecording(boolean b) {
        logger.info("setRecording: " + b);
        ui.setRemoteRecording(b);
        remoteRecording = b;
    }

    boolean isLocalRecording() {
        return localRecording;
    }

    boolean isRemoteRecording() {
        return remoteRecording;
    }

    DefaultButtonModel getVideoButtonModel() {
        return videoButtonModel;
    }

    DefaultButtonModel getStillButtonModel() {
        return stillButtonModel;
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

    private void createMovie(String movieFilename) throws EncodeException {
        MovieCreator mCreator = new MovieCreator(ui.getControlPanel());
        mCreator.createMovie(movieFilename);
    }

    private void deleteImageDirectory() {
        File dir = getImageDirectory();
        String[] children = dir.list();
        for (int i=0; i<children.length; i++) {
            new File(dir, children[i]).delete();

        }
        // The directory is now empty so delete it
        dir.delete();
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
                        setRemoteRecording(sccm.isRecording());
                        break;
                    default:
                        logger.severe("Unknown action type: " + sccm.getAction());
                }
            }
        }
    }

    class VideoButtonChangeListener implements ItemListener {

        public void itemStateChanged(ItemEvent event) {
            //The state of the video button model has changed
            //Take some action. Rendering issues are dealth with by other listeners in
            //the renderer and the control panel
            if (event.getStateChange() == ItemEvent.SELECTED) {
                cellLogger.info("should start recording");
                startRecording();
            } else {
                cellLogger.info("should stop recording");
                stopRecording();
            }
        }
   }

    class StillButtonChangeListener implements ItemListener {

        public void itemStateChanged(ItemEvent event) {
            //The state of the still button model has changed
            //Take some action. Rendering issues are dealth with by other listeners in
            //the renderer and the control panel
            if (event.getStateChange() == ItemEvent.SELECTED) {
                cellLogger.info("should take a still");
                captureImage();
                
            } else {
                //Nothing to do
            }
        }
   }
}

