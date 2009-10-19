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
package org.jdesktop.wonderland.modules.videoplayer.client;

import com.sun.media.jmc.MediaProvider;
import com.sun.media.jmc.control.VideoRenderControl;
import com.sun.media.jmc.event.VideoRendererEvent;
import com.sun.media.jmc.event.VideoRendererListener;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.hud.HUDMessage;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.videoplayer.common.VideoPlayerConstants;

/**
 * A panel for displaying video media
 */
public class VideoPlayerPanel extends JPanel {

    private static final Logger logger = Logger.getLogger(VideoPlayerPanel.class.getName());
    private String mediaURI;
    private boolean synced = true;
    private WindowSwing window;
    private HUDMessage messageComponent;
    private MediaProvider provider;
    private VideoRenderControl vrc;

    public interface Container {

        public void validate();

        public void setHud(boolean enable);
    }
    // shared state
    @UsesCellComponent
    private SharedStateComponent ssc;
    private SharedMapCli statusMap;

    public VideoPlayerPanel(WindowSwing window) {
        this.window = window;
        initComponents();
    }

    public void setSSC(SharedStateComponent ssc) {
        this.ssc = ssc;
        // load the video player's status map
        statusMap = ssc.get(VideoPlayerConstants.STATUS_MAP);
    }

    public void toggleHUD() {
    }

    private void resizeToFit(final Dimension size) {
        if ((size.getWidth() == 0) || (size.getHeight() == 0)) {
            // don't allow the window to shrink to invisibility
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (!size.equals(new Dimension(window.getWidth(), window.getHeight()))) {
                    // resize the video player window:
                    // use the current window width, but scale the height to
                    // match the aspect ratio of the video
                    double aspectRatio = size.getWidth() / size.getHeight();
                    int w = VideoPlayerPanel.this.getWidth();
                    int h = (int) (w / aspectRatio);
                    window.setSize(w, h);
                }
            }
        });
    }

    private class Dismisser extends TimerTask {

        public void run() {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    messageComponent.setVisible(false);
                }
            });
        }
    }

    /**
     * Show a status message in the HUD and remove it after a timeout
     * @param message the string to display in the message
     * @param timeout the period in milliseconds to display the message for
     */
    public void showHUDMessage(final String message, final int timeout) {
        if (messageComponent == null) {
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
            messageComponent = mainHUD.createMessage("");
            //messageComponent = mainHUD.createOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            messageComponent.setPreferredLocation(Layout.NORTHEAST);
            messageComponent.setDecoratable(false);
            mainHUD.addComponent(messageComponent);
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                messageComponent.setMessage(message);
                messageComponent.setVisible(true);
                Timer t = new Timer();
                t.schedule(new Dismisser(), (long) timeout);
            }
        });
    }

    /**
     * Open media
     * @param mediaURI the URI of the media to open
     */
    public void openMedia(final String mediaURI) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    logger.fine("opening media: " + mediaURI);
                    showHUDMessage("Opening Video...", 5000);
                    //videoDialog.setVideoURL(mediaURI);

                    provider = new MediaProvider(new URI(mediaURI));
                    vrc = provider.getControl(VideoRenderControl.class);
                    Dimension frameSize = vrc.getFrameSize();

                    if (vrc != null) {
                        VideoRendererListener vrl = new VideoRendererListener() {

                            public void videoFrameUpdated(VideoRendererEvent vre) {
                                //if (!isPlaying()) {
                                resizeToFit(vrc.getFrameSize());
                                //} else {
                                repaint();
                                //}
                            }
                        };
                        vrc.addVideoRendererListener(vrl);
                    }

                    //showStatusMessage("Ready", Color.GREEN);
                } catch (Exception e) {
                    logger.fine("error opening media: " + e.toString());
                    //showStatusMessage(e.getMessage(), Color.RED);
                }
            }
        });
    }

    /**
     * Gets the URI of the media
     * @return the URI of the currently loaded media
     */
    public String getMediaURI() {
        return mediaURI;
    }

    /**
     * Set the position (time) within the media
     * @param position the position in seconds
     */
    public void setMediaPosition(double position) {
        if (provider != null) {
            //provider.setMediaTime(position);
            provider.setStartTime(position);
        }
    }

    /**
     * Get the currentposition (time) within the media
     * @return the media position in seconds
     */
    public double getMediaPosition() {
        double position = 0.0;

        if (provider != null) {
            position = provider.getMediaTime();
        }

        return position;
    }

    /**
     * Rewind the media
     */
    public void rewind() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                logger.fine("rewind");
                if ((provider != null) && provider.isPlaying()) {
                    provider.setRate(-3.0);
                    showHUDMessage("Rewind", 2000);
                }
            }
        });
    }

    /**
     * Play the media
     */
    public void play() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                logger.fine("play");
                if (provider != null) {
                    provider.play();
                    showHUDMessage("Play", 2000);
                }
            }
        });
    }

    /**
     * Gets whether the media is currently playing
     * @return true if the media is playing, false otherwise
     */
    public boolean isPlaying() {
        boolean playing = false;

        if (provider != null) {
            playing = provider.isPlaying();
        }

        return playing;
    }

    /**
     * Pause the media
     */
    public void pause() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                logger.fine("pause");
                if (provider != null) {
                    provider.pause();
                    showHUDMessage("Pause", 2000);
                }
            }
        });
    }

    /**
     * Stop playing the media
     */
    public void stop() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                logger.fine("stop");
                if (provider != null) {
                    provider.pause();
                    setMediaPosition(0);
                    showHUDMessage("Stop", 2000);
                }
            }
        });
    }

    /**
     * Fast forward the media
     */
    public void fastForward() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                logger.fine("fast forward");
                if ((provider != null) && provider.isPlaying()) {
                    provider.setRate(3.0);
                    showHUDMessage("Fast Forward", 2000);
                }
            }
        });
    }

    /**
     * Synchronize with the shared state
     */
    public void sync() {
        if (!synced) {
            synced = true;
        }
    }

    /**
     * Unsynchronize from the shared state
     */
    public void unsync() {
        if (synced) {
            synced = false;
        }
    }

    /**
     * Gets whether the player is synced with the shared state
     * @return true if synced, false otherwise
     */
    public boolean isSynced() {
        return synced;
    }

    public BufferedImage getFrameImage(int width, int height) {
        BufferedImage image = null;

        return image;
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        int w = getWidth();
        int h = getHeight();

        Graphics2D g2 = (Graphics2D) g;

        g2.setBackground(Color.black);
        g2.fillRect(0, 0, w, h);

        if ((provider != null) && (vrc != null)) {
            //g2.setComposite(AlphaComposite.SrcOver.derive(0.85f));
            vrc.paintVideoFrame(g2, new Rectangle(0, 0, w, h));
        }

        //String time = String.valueOf(vre.getTimestamp()).substring(0, 4);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 640, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 480, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
