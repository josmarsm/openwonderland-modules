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
package org.jdesktop.wonderland.modules.videoplayer.client;

import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.system.DisplaySystem;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.modules.appbase.client.DrawingSurface;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.view.View2D;

/**
 * A drawing surface implementation for video that converts data directly
 * into the texture memory used by JME / JOGL. No intermediate copies required.
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class VideoDrawingSurface implements DrawingSurface, FrameListener {
    private static final Logger LOGGER =
            Logger.getLogger(VideoDrawingSurface.class.getName());

    private final Set<View2D> visibleViews = new HashSet<View2D>();

    private Window2D window;

    private int textureWidth;
    private int textureHeight;
    private Texture texture;

    private int videoWidth;
    private int videoHeight;
    private IPixelFormat.Type videoType;

    private boolean updateEnabled = true;
    private boolean updateResampler = true;
    private IVideoResampler resampler;
    private IVideoPicture textureFrame;

    // frame to show if we are stopped
    private IVideoPicture previewFrame;

    private FrameQueue queue;

    private Entity updateEntity;
    private UpdateProcessor updateProcessor;

    public synchronized Window2D getWindow() {
        return window;
    }

    public synchronized void setWindow(Window2D window) {
        this.window = window;
    }

    public synchronized int getWidth() {
        return textureWidth;
    }

    public synchronized int getHeight() {
        return textureHeight;
    }

    public synchronized void setSize(int width, int height) {
        this.textureWidth = width;
        this.textureHeight = height;

        updateResampler = true;
    }

    protected synchronized int getVideoWidth() {
        return videoWidth;
    }

    protected synchronized int getVideoHeight() {
        return videoHeight;
    }

    protected synchronized IPixelFormat.Type getVideoType() {
        return videoType;
    }

    public synchronized void openVideo(final int videoWidth,
                                       final int videoHeight,
                                       final IPixelFormat.Type videoType)
    {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.videoType = videoType;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getWindow().setSize(videoWidth, videoHeight);
            }
        });
        
        updateResampler = true;
    }

    public synchronized void playVideo(FrameQueue queue) {
        this.queue = queue;
        this.previewFrame = null;

        checkUpdating();
    }

    public void previewFrame(final IVideoPicture picture) {
        this.previewFrame = picture;
        
        if (getTexture() != null && getTexture().getTextureId() != 0) {
            SceneWorker.addWorker(new WorkCommit() {
                public void commit() {
                    renderFrame(picture);
                }
            });
        }
    }

    public synchronized void stopVideo() {
        this.queue = null;

        checkUpdating();
    }

    public synchronized void closeVideo() {
        this.videoType = null;

        updateResampler = true;
    }

    public synchronized Graphics2D getGraphics() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized Texture getTexture() {
        return texture;
    }

    public synchronized void setTexture(Texture texture) {
        this.texture = texture;

        // new texture -- redisplay the preview frame (if any)
        if (previewFrame != null) {
            previewFrame(previewFrame);
        }

        updateResampler = true;
        checkUpdating();
    }

    public synchronized boolean getUpdateEnable() {
        return updateEnabled;
    }

    public synchronized void setUpdateEnable(boolean updateEnabled) {
        this.updateEnabled = updateEnabled;
    }

    protected synchronized boolean isUpdating() {
        // check if update has been manually disabled
        if (!updateEnabled) {
            return false;
        }

        // make sure we have a queue to display from
        if (queue == null) {
            return false;
        }

        // make sure there is at least one valid view
        if (visibleViews.isEmpty()) {
            return false;
        }

        // make sure the texture is set up
        if (texture == null || texture.getTextureId() == 0) {
            return false;
        }

        // and the video
        if (videoType == null) {
            return false;
        }

        return true;
    }

    public synchronized void setViewIsVisible(View2D view, boolean visible) {
        if (visible) {
            visibleViews.add(view);
        } else {
            visibleViews.remove(view);
        }

        checkUpdating();
    }

    protected synchronized void renderCurrentFrame() {
        // double check that we are still updating
        if (!isUpdating()) {
            return;
        }

        // get the next frame
        IVideoPicture currentFrame = queue.nextFrame();
        if (currentFrame == null) {
            return;
        }

        // render the frame
        renderFrame(currentFrame);
    }

    protected synchronized void renderFrame(IVideoPicture frame) {
        // get the current resampler, checking if anything has changed
        // since we got it
        if (!checkResampler()) {
            // something has changed. Update the resampler. 
            updateResampler();
        }

        // resample into the texture frame
        resampler.resample(textureFrame, frame);

        // create an image with the data from the resampler
        Image i = new Image(Image.Format.RGB8, textureFrame.getWidth(),
                            textureFrame.getHeight(), textureFrame.getByteBuffer());

        // now draw the updated texture
        DisplaySystem.getDisplaySystem().getRenderer().updateTextureSubImage(
                texture, 0, 0, i, 0, 0, i.getWidth(), i.getHeight());
    }

    protected boolean checkResampler() {
        if (resampler == null || updateResampler) {
            updateResampler = false;
            return false;
        }
        
        return true;
    }

    protected void updateResampler() {
        int width = videoWidth;
        int height = videoHeight;

        LOGGER.warning("Resample from " + videoWidth + " x " + videoHeight + 
                       " " + videoType + " to " + width + " x " + height +
                       " format " + IPixelFormat.Type.BGR24);

        resampler = IVideoResampler.make(width, height, IPixelFormat.Type.RGB24,
                                         videoWidth, videoHeight, videoType);

        textureFrame = IVideoPicture.make(IPixelFormat.Type.RGB24, width, height);
    }

    public void initializeSurface() {
    }

    public synchronized void cleanup() {
        setUpdateEnable(false);
        resampler = null;
        textureFrame = null;
    }

    private void checkUpdating() {
        if (isUpdating() && updateProcessor == null) {
            startUpdating();
        } else if (!isUpdating() && updateProcessor != null) {
            stopUpdating();
        }
    }

    private synchronized void startUpdating() {
        updateProcessor = new UpdateProcessor();
        updateEntity = new Entity("VideoDrawingSurface updateEntity");
        updateEntity.addComponent(ProcessorComponent.class, updateProcessor);
        ClientContextJME.getWorldManager().addEntity(updateEntity);
        updateProcessor.start();
    }

    private synchronized void stopUpdating() {
        if (updateProcessor != null) {
            updateProcessor.stop();
            updateProcessor = null;
        }

        if (updateEntity != null) {
            ClientContextJME.getWorldManager().removeEntity(updateEntity);
            updateEntity = null;
        }
    }

    private class UpdateProcessor extends ProcessorComponent {
        public void initialize() {}
        public void compute(ProcessorArmingCollection collection) {}
        public void commit(ProcessorArmingCollection collection) {
            renderCurrentFrame();
        }

        private void start() {
            setArmingCondition(new NewFrameCondition(this));
        }

        private void stop() {
            setArmingCondition(null);
        }
    }
}
