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
/*******************************************************************************
 * Copyright (c) 2008, 2010 Xuggle Inc.  All rights reserved.
 *
 * This file is part of Xuggle-Xuggler-Main.
 *
 * Xuggle-Xuggler-Main is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Xuggle-Xuggler-Main is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Xuggle-Xuggler-Main.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.jdesktop.wonderland.modules.videoplayer.client;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IAudioResampler;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import org.jdesktop.wonderland.client.assetmgr.AssetInputStream;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.utils.VideoLibraryLoader;
import org.jdesktop.wonderland.modules.videoplayer.client.FrameListener.FrameQueue;

/**
 * Video player for xuggler video library.
 *
 * This file derived from DecodeAndPlayAudioAndVideo.java by Art Clarke
 * (aclarke@xuggle.com).
 *
 * Original source:
 * http://xuggle.googlecode.com/svn/trunk/java/xuggle-xuggler/src/com/xuggle/xuggler/demos/DecodeAndPlayAudioAndVideo.java
 *
 * @author nsimpson
 */
/** 
 * Original comment for DecodeAndPlayAudioAndVideo.java
 */
/**
 * Takes a media container, finds the first video stream,
 * decodes that stream, and then plays the audio and video.
 *
 * This code does a VERY coarse job of matching time-stamps, and thus
 * the audio and video will float in and out of slight sync.  Getting
 * time-stamps syncing-up with audio is very system dependent and left
 * as an exercise for the reader.
 *
 * @author aclarke
 *
 */
public class VideoPlayerImpl implements VideoPlayer, TimedEventSource {

    private static final Logger logger = Logger.getLogger(VideoPlayerImpl.class.getName());

    // number of packets to queue up
    private static final int QUEUE_SIZE = 20;

    // check whether video is available. Be sure to do this in the static
    // initialize for the class, so we load the libraries before xuggler
    // tries to
    private static final boolean VIDEO_AVAILABLE =
            VideoLibraryLoader.loadVideoLibraries();

    // listeners
    private final List<TimeListener> timeListeners = 
            new CopyOnWriteArrayList<TimeListener>();
    private final List<VideoStateListener> stateListeners =
            new CopyOnWriteArrayList<VideoStateListener>();
    private final List<FrameListener> frameListeners =
            new CopyOnWriteArrayList<FrameListener>();

    // threads to maintain the queues
    private final QueueFillerThread queueFiller;
    private final AudioQueue audioQueue;
    private final VideoQueue videoQueue;

    private String mediaURI;
    private File videoPath;
    private VideoPlayerState mediaState = VideoPlayerState.NO_MEDIA;
    private double mediaDuration;
    private int videoStreamId;
    private int audioStreamId;
    private IStreamCoder videoCoder;
    private IStreamCoder audioCoder;
    private IContainer container;
    private final Object containerLock = new Object();
    private long streamPosition = -1000000;
    private long seekTo = -1l;

    public VideoPlayerImpl() {
        // create queues
        queueFiller = new QueueFillerThread();
        videoQueue = new VideoQueue();
        audioQueue = new AudioQueue();
    }

    /**
     * Return whether or not video is available on this platform
     * @return true if video is available or false if not
     */
    public static boolean isVideoAvailable() {
        return VIDEO_AVAILABLE;
    }

    /**
     * Add a listener for new frames
     * @param listener a frame listener to be notified of new frames
     */
    public void addFrameListener(FrameListener listener) {
        frameListeners.add(listener);
    }

    /**
     * Remove a listener for new frames
     * @param listener a frame listener to be removed
     */
    public void removeFrameListener(FrameListener listener) {
        frameListeners.remove(listener);
    }

    /**
     * Notify all frame listeners of a new video
     * @param width the width of the video
     * @param height the height of the video
     * @param format the format of the video
     */
    protected void notifyFrameListenersOpen(int width, int height,
                                             IPixelFormat.Type type)
    {
        for (FrameListener listener : frameListeners) {
            listener.openVideo(width, height, type);
        }
    }

    /**
     * Notify all frame listeners that the video is stopped
     */
    protected void notifyFrameListenersClose() {
        for (FrameListener listener : frameListeners) {
            listener.closeVideo();
        }
    }

    /**
     * Notify all the frame listeners that video has started
     * @param frame a new frame
     */
    protected void notifyFrameListenersPlay(FrameQueue frames) {
        for (FrameListener listener : frameListeners) {
            listener.playVideo(frames);
        }
    }

    /**
     * Notify all the frame listeners to display a frame
     * @param frame a new frame
     */
    protected void notifyFrameListenersPreview(IVideoPicture frame) {
        for (FrameListener listener : frameListeners) {
            listener.previewFrame(frame);
        }
    }

    /**
     * Notify all the frame listeners that video has stopped
     */
    protected void notifyFrameListenersStop() {
        for (FrameListener listener : frameListeners) {
            listener.stopVideo();
        }
    }

    /**
     * Add a listener for time changes
     * @param listener a time listener to be notified of time changes
     */
    public void addTimeListener(TimeListener listener) {  
        timeListeners.add(listener);
    }

    /**
     * Remove a listener for time changes
     * @param listener a time listener to be removed
     */
    public void removeTimeListener(TimeListener listener) {
        timeListeners.remove(listener);
    }

    /**
     * Notify all the time time listeners of a time change
     * @param newTime the time of the event
     */
    protected void notifyTimeListeners(double newTime) {
        for (TimeListener listener : timeListeners) {
            listener.timeChanged(newTime);
        }
    }

    /**
     * Add a listener for changes in state
     * @param listener a state listener to be notified of state changes
     */
    public void addStateListener(VideoStateListener listener) {
        stateListeners.add(listener);
    }

    /**
     * Remove a listener for state changes
     * @param listener a state listener to be removed
     */
    public void removeStateListener(VideoStateListener listener) {
        stateListeners.remove(listener);
    }

    /**
     * Notify all the state listeners of a state change
     * @param oldState the previous state
     * @param newState the new state
     */
    protected void notifyStateListeners(VideoPlayerState oldState, VideoPlayerState newState) {
        for (VideoStateListener listener : stateListeners) {
            listener.mediaStateChanged(oldState, newState);
        }
    }

    /**
     * Open video media
     * @param uri the URI of the video media to open
     */
    public void openMedia(final String uri) {
        new Thread(new Runnable() {

            public void run() {
                logger.warning("opening video: " + uri);

                // close any media we currently have open
                if (isPlayable()) {
                    closeMedia();
                }

                String loadURI = uri;

                try {
                    if (uri.startsWith("http:")) {
                        // turn http:// urls int wlhttp:// urls so they are cached
                        loadURI = "wl" + uri;
                    } else if (loadURI.startsWith("wlcontent:")) {
                        // resolve wlcontent URLs
                        loadURI = AssetUtils.getAssetURL(loadURI).toExternalForm();
                    }

                    mediaURI = loadURI;
                    InputStream mediaInputStream = new URL(loadURI).openStream();
                    if (!(mediaInputStream instanceof AssetInputStream)) {
                        throw new IOException("Invalid stream type: " + 
                                              mediaInputStream.getClass() + 
                                              " for " + mediaURI);
                    }
                    videoPath = ((AssetInputStream) mediaInputStream).getAsset().getLocalCacheFile();
                } catch (IOException ioe) {
                    logger.log(Level.WARNING, "Error decoding " + loadURI, ioe);
                    return;
                }

                if (videoPath != null) {
                    // make sure that we can actually convert media pixel formats
                    if (!IVideoResampler.isSupported(IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION)) {
                        throw new RuntimeException("you must install the GPL version of Xuggler (with IVideoResampler support) for this demo to work");
                    }

                    openVideoFile(videoPath);
                    
                    setState(VideoPlayerState.MEDIA_READY);
                }
            }
        }).start();
    }

    private void openVideoFile(File videoPath) {
        RandomAccessFile videoFile;
        try {
            videoFile = new RandomAccessFile(videoPath, "r");
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Error opening " + videoPath, ioe);
            return;
        }

        // create a Xuggler container object
        container = IContainer.make();

        // open up the container -- use a RandomAccessFile to allow
        // seeking
        if (container.open(videoFile, IContainer.Type.READ, null) < 0) {
            throw new IllegalArgumentException("could not open media: " + mediaURI);
        }

        // query how many streams the call to open found
        int numStreams = container.getNumStreams();

        // now iterate through the streams to find the first video and audio
        // streams
        videoStreamId = -1;
        videoCoder = null;
        audioStreamId = -1;
        audioCoder = null;

        for (int i = 0; i < numStreams; i++) {
            // get the next stream object
            IStream stream = container.getStream(i);
            // find a pre-configured decoder that can decode this stream;
            IStreamCoder coder = stream.getStreamCoder();
            if (videoStreamId == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                // found video stream
                videoStreamId = i;
                videoCoder = coder;
            } else if (audioStreamId == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
                // found audio stream
                audioStreamId = i;
                audioCoder = coder;
            }
        }
        if (videoStreamId == -1 && audioStreamId == -1) {
            logger.warning("could not find audio or video stream in container: " + mediaURI);
            return;
        }

        // check if we have a video stream in this file. If so let's
        // open up our decoder so it can do work.
        if (videoCoder != null) {
            if (videoCoder.open() < 0) {
                logger.warning("could not open audio decoder for container: " + mediaURI);
                return;
            }

            videoQueue.open();

            // notify frame listeners of this new video
            notifyFrameListenersOpen(videoCoder.getWidth(),
                    videoCoder.getHeight(),
                    videoCoder.getPixelType());

            // set the initial stream position to -1 seconds, so that the
            // preview frame will be generated when we jump to time 0
            setStreamPosition(-1000000l);
            mediaDuration = container.getDuration() / 1000000;
            logger.fine("video duration: " + mediaDuration);
        }

        if (audioCoder != null) {
            if (audioCoder.open() < 0) {
                throw new RuntimeException("could not open audio decoder for container: " + mediaURI);
            }

            // ask the Java Sound System to get itself ready
            try {
                audioQueue.open(audioCoder);
            } catch (LineUnavailableException ex) {
                logger.warning("unable to open sound device: " + mediaURI + ": " + ex);
                // throw new RuntimeException("unable to open sound device on your system when playing back container: " + filename);
            } catch (IllegalArgumentException ex) {
                logger.warning("unable to open sound device: " + ex);
            }
        }
    }

    /**
     * Close video media
     */
    public void closeMedia() {
        logger.fine("closing video");
        stop();
        
        audioQueue.close();
        videoQueue.close();

        notifyFrameListenersClose();

        mediaURI = null;
        mediaState = VideoPlayerState.NO_MEDIA;
        container = null;
        mediaDuration = 0.0d;
        setStreamPosition(0);
        videoStreamId = -1;
        videoCoder = null;
        audioStreamId = -1;
        audioCoder = null;
    }

    /**
     * Gets the URL of the currently loaded media as a String
     * @return the URL of the media
     */
    public String getMedia() {
        return mediaURI;
    }

    /**
     * Get the dimension of video frames in this video
     * @return the frame size
     */
    public Dimension getFrameSize() {
        Dimension dimension = new Dimension();

        if (videoCoder != null) {
            dimension.setSize(videoCoder.getWidth(), videoCoder.getHeight());
        }

        return dimension;
    }

    /**
     * Determine if media player is ready to play media
     * @return true if the media player is ready to play, false otherwise
     */
    public boolean isPlayable() {
        return (getState() != VideoPlayerState.NO_MEDIA);
    }

    /**
     * Play media
     */
    public void play() {
        logger.warning("play at " + getPosition());

        if (isPlayable() && (getState() != VideoPlayerState.PLAYING)) {
            setState(VideoPlayerState.PLAYING);

            // calculate the starting position
            double position = getPosition();
            
            // make sure we are collecting data
            queueFiller.start(position);
            videoQueue.start(position);
            audioQueue.start(position);

            logger.fine("Started playing at " + position);

            // notify listeners
            notifyFrameListenersPlay(videoQueue);
        }
    }

    /**
     * Gets whether the media is currently playing
     * @return true if the media is playing, false otherwise
     */
    public boolean isPlaying() {
        return (isPlayable() && (getState() == VideoPlayerState.PLAYING));
    }

    /**
     * Pause media
     */
    public void pause() {
        logger.warning("pause at " + getStreamPosition());
        if (isPlayable() && (getState() != VideoPlayerState.PAUSED)) {
            queueFiller.stop(true);
            videoQueue.stop();
            audioQueue.stop();

            setState(VideoPlayerState.PAUSED);
            notifyTimeListeners(getPosition());
            notifyFrameListenersStop();
        }
    }

    /**
     * Stop playing media
     */
    public void stop() {
        logger.warning("stop at " + getStreamPosition());

        if (isPlayable() && (getState() != VideoPlayerState.STOPPED)) {
            queueFiller.stop(true);
            videoQueue.stop();
            audioQueue.stop();

            setState(VideoPlayerState.STOPPED);

            notifyTimeListeners(getPosition());
            notifyFrameListenersStop();
        }
    }

    /**
     * Fill the audio and video queues.  Returns true if the queues
     * were filled to capacity, or false if no more data is available.
     */
    private boolean fillQueues() {
        synchronized (containerLock) {
            return doFillQueues();
        }
    }

    /**
     * Internal method to fill the queues. Make sure to call while holding
     * the container lock.
     */
    private boolean doFillQueues() {
        // make sure we are actually playing
        if (!isPlaying()) {
            return false;
        }

        // create a packet to store our data
        IPacket packet = IPacket.make();

        logger.fine("Fill queue start read. Position: " + queueFiller.getPosition());

        // if there is a seek, apply it now
        if (getSeekTo() >= 0) {
            seek(getSeekTo(), packet);
            setSeekTo(-1);
        }

        // walk through the container processing each (video or audio) packet
        // until the buffer is full
        while (!videoQueue.isFull() && queueFiller.isRunning()) {
            // make sure we got a valid packet
            if (container.readNextPacket(packet) < 0) {
                // failed to read packet, done playing
                logger.info("end of media, playback stopping");
                return false;
            }

            logger.fine("Fill queue decoded packet for " + packet.getStreamIndex() +
                        " at " + packet.getPts() + " (" +
                        timebaseToSeconds(packet.getPts()) + ")");

            // now we have a packet, let's see if it belongs to our media stream
            if (packet.getStreamIndex() == videoStreamId) {
                // convert the packet into a video picture, and add it to the
                // queue if it is after the point we are interested in
                IVideoPicture picture = decodeVideoPacket(packet);
                
                logger.fine("Video packet. Time: " +
                            microsecondsToSeconds(picture.getTimeStamp()) + 
                            " complete " + picture.isComplete());

                if (picture.isComplete() &&
                        microsecondsToSeconds(picture.getTimeStamp()) > queueFiller.getPosition())
                {
                    logger.fine("Adding video packet at time " +
                                 picture.getTimeStamp() + "(" +  
                                 microsecondsToSeconds(picture.getTimeStamp()) + ")");

                    videoQueue.add(picture);
                } else if (picture.isComplete()) {
                    logger.fine("Discarding early frame at " +
                                microsecondsToSeconds(picture.getTimeStamp()));
                }
            } else if (packet.getStreamIndex() == audioStreamId) {
                // We allocate a set of samples with the same number of channels as the
                // coder tells us is in this buffer.
                //
                // We also pass in a buffer size (2048 in our example), although Xuggler
                // will probably allocate more space than just the 2048 (it's not important why).
                IAudioSamples samples = IAudioSamples.make(2048, audioCoder.getChannels());

                // A packet can actually contain multiple sets of samples (or frames of samples
                // in audio-decoding speak).  So, we may need to call decode audio multiple
                // times at different offsets in the packet's data.  We capture that here.
                int offset = 0;

                // Keep going until we've processed all data
                while (offset < packet.getSize()) {
                    int bytesDecoded = audioCoder.decodeAudio(samples, packet, offset);
                    if (bytesDecoded < 0) {
                        //throw new RuntimeException("got error decoding audio in: " + mediaURL);
                        logger.warning("error decoding audio: " + packet);
                        continue;
                    }
                    offset += bytesDecoded;

                    logger.fine("Audio packet. Time: " +
                                microsecondsToSeconds(samples.getTimeStamp()) +
                                " complete " + samples.isComplete());

                    // Some decoders will consume data in a packet, but will not be able to construct
                    // a full set of samples yet.  Therefore you should always check if you
                    // got a complete set of samples from the decoder
                    if (samples.isComplete() &&
                            microsecondsToSeconds(samples.getTimeStamp()) > queueFiller.getPosition())
                    {
                        // add to the audio queue
                        audioQueue.add(samples);
                    } else {
                        logger.fine("Discarding early audio packet at " +
                                    microsecondsToSeconds(samples.getTimeStamp()));
                    }
                }
            }
        }

        logger.fine("Done filling queues");
        return true;
    }

    /**
     * Decode a single frame, for example to show as a preview. This frame
     * has no timestamp associated with it.
     */
    public IVideoPicture decodeOneFrame() {
        synchronized (containerLock) {
            return doDecodeOneFrame();
        }
    }

    /**
     * Internal method to decode a frame. Make sure to call while holding
     * the container lock.
     */
    private IVideoPicture doDecodeOneFrame() {
        // create a packet to store our data
        IPacket packet = IPacket.make();
        logger.warning("Decode one frame");

        // if there is a seek, apply it now
        if (getSeekTo() >= 0) {
            seek(getSeekTo(), packet);
            setSeekTo(-1);
        }

        while (container.readNextPacket(packet) >= 0) {
            logger.fine("Decode one frame decoded packet for " + packet.getStreamIndex() +
                        " at " + packet.getPts() + " (" +
                        timebaseToSeconds(packet.getPts()) + ") at " +
                        packet.getPosition());

            if (packet.getStreamIndex() == videoStreamId) {
                // convert the packet into a video picture
                IVideoPicture picture = decodeVideoPacket(packet);
                if (picture.isComplete()) {
                    logger.fine("decodeOneFrame returning " +
                                microsecondsToSeconds(picture.getTimeStamp()));
                    //setStreamPosition(picture.getTimeStamp());
                    return picture;
                }
            }
        }

        // no next packet
        return null;
    }

    /**
     * Convert an IPacket into a an IVideoPicture
     * @param packet the packet to convert
     * @return a picture representation of the packet
     */
    private IVideoPicture decodeVideoPacket(IPacket packet) {
        IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(),
                videoCoder.getWidth(), videoCoder.getHeight());

        // now, we decode the video, checking for any errors
        int bytesDecoded = videoCoder.decodeVideo(picture, packet, 0);
        if (bytesDecoded < 0) {
            logger.warning("error decoding video: " + packet);
        }

        logger.fine("Decoded " + bytesDecoded + " from packet at "
                    + packet.getPts() + " (" + timebaseToSeconds(packet.getPts())
                    + ") into " + picture.getPts() + " ("
                    + microsecondsToSeconds(picture.getPts()) + ")");

        return picture;
    }

    /**
     * Seek to a specified position in seconds. Make sure to call while holding
     * the container lock.
     * @param position the position to seek to in timebase units
     * @param packet the packet to use for seeking
     */
    private int seek(long position, IPacket packet) {
        if (container == null) {
            return -1;
        }

        logger.warning("seeking to: " + position + " = " + timebaseToSeconds(position) + "s");

        // seek to the key frame nearest to the specified position
        // See: http://build.xuggle.com/view/Stable/job/xuggler_jdk5_stable/javadoc/java/api/com/xuggle/xuggler/IContainer.html
        //container.seekKeyFrame(videoStreamId, position, IContainer.SEEK_FLAG_FRAME);
        long min = position - 100;
        long max = position;

        if (min < 0) {
            // if we are very close to the beginning of the file, seek has
            // unpredictable results (like the video and audio getting out
            // of sync, or video playing ahead of where it should). Instead
            // of seeking to 0, just reopen the file and reset everything.
            audioQueue.close();
            videoQueue.close();
            openVideoFile(videoPath);
        } else {
            // seeking in the audio stream first seems to fix problems with
            // audio and video streams getting out of sync
            container.seekKeyFrame(audioStreamId, min, position,
                                   max, IContainer.SEEK_FLAG_FRAME);
            container.seekKeyFrame(videoStreamId, min, position,
                                   max, IContainer.SEEK_FLAG_FRAME);
        }
        // int res = container.seekKeyFrame(videoStreamId, position, 0);
        logger.fine("Seek key frame " + min + ", " + position + ", " + max);

        double seekPosition = timebaseToSeconds(position);
        double delta = 1.0d;
        double pos = 0l;

        // depending on how close the key frame is to the specified position,
        // we may need to silently "play forward" until the stream is at the
        // requested position (this can be several seconds of video)
        int res;
        do {
            if ((res = container.readNextPacket(packet)) < 0) {
                // failed to read a packet, possibly at end of media, stop
                // seeking
                return res;
            }

            logger.fine("Seek decoded packet for " + packet.getStreamIndex()
                        + " at " + packet.getPts() + " ("
                        + timebaseToSeconds(packet.getPts()) + ")");

            if (packet.getStreamIndex() == videoStreamId) {
                // decode the packet into a picture, so we can get the time
                // stamp of the frame
                IVideoPicture picture = decodeVideoPacket(packet);
                pos = microsecondsToSeconds(picture.getTimeStamp());

                logger.fine("Picture timestamp is " + picture.getPts()
                            + " (" + pos + ") complete? "
                            + picture.isComplete() + " key frame "
                            + picture.isKeyFrame());

                picture = null;

                delta = Math.abs(pos - seekPosition);
                if (pos > seekPosition) {
                    logger.fine("ending seek because " + pos + " > " + seekPosition);
                    break;
                }
            }

            //logger.fine("seeking, current: " + (double) streamPosition / 1000000d + ", goal: " + seekPosition + ", delta: " + delta);
            // stop searching once we're within 0.05 seconds of the specified
            // time - we won't get the exact position
        } while (delta > 0.05d);

        logger.warning("seek requested: " + timebaseToSeconds(position)
                       + ", got: " + pos + ", delta: " + delta);
        
        notifyTimeListeners(getPosition());
        return 0;
    }

    /**
     * Rewind the media
     * @param offset the time in seconds to skip back
     */
    public void rewind(double offset) {
        logger.fine("rewind: " + offset);
        if (isPlayable()) {
            double position = getPosition();
            position = (position - offset < 0.0d) ? 0.0d : position - offset;
            setPosition(position);
        }
    }

    /**
     * Skip forward the media
     * @param offset the time in seconds to skip forward
     */
    public void forward(double offset) {
        logger.fine("forward: " + offset);
        if (isPlayable()) {
            double position = getPosition();
            position = (position + offset > mediaDuration) ? mediaDuration : position + offset;
            setPosition(position);
        }
    }

    /**
     * Set the position within the media
     * @param position the position in seconds
     */
    public void setPosition(double position) {
        logger.warning("set position: " + position);

        if (!isPlayable()) {
            return;
        }
        
        // make sure we are actually going to a different position
        if (Math.abs(position - getPosition()) < 0.1) {
            return;
        }

        // check if we are currently playing, if so, stop
        boolean playing = isPlaying();
        if (playing) {
            stop();
        }

        // set the seek position and eventual stream position
        setSeekTo(secondsToTimebase(position));
        setStreamPosition((long) (position * 1000000));

        // if we were playing, start again
        if (playing) {
            play();
        } else {
            IVideoPicture frame = decodeOneFrame();
            if (frame != null) {
                notifyFrameListenersPreview(frame);
                notifyTimeListeners(position);
            }
        }
    }

    /**
     * Get the current position in microseconds
     * @return the current position in microseconds
     */
    public long getPositionMicroseconds() {
        // If a seek has been requested, use that value as our position.
        // Make sure to convert from microseconds.
        long localSeek = getSeekTo();
        if (localSeek > 0) {
            return (long) (timebaseToSeconds(localSeek) * 1000000);
        }

        return getStreamPosition();
    }

    /**
     * Get the current position in seconds
     * @return the current position in seconds
     */
    public double getPosition() {
        double position = 0.0d;

        if (isPlayable()) {
            position = (double) getPositionMicroseconds() / 1000000d;
        }

        return position;
    }

    protected synchronized long getStreamPosition() {
        return streamPosition;
    }

    protected synchronized void setStreamPosition(long streamPosition) {
        this.streamPosition = streamPosition;
    }

    protected synchronized long getSeekTo() {
        return seekTo;
    }

    protected synchronized void setSeekTo(long seekTo) {
        this.seekTo = seekTo;
    }

    /**
     * Converts a time in seconds to the corresponding time in timebase units
     * See: http://wiki.xuggle.com/Concepts#Time_Bases
     * @param seconds time in seconds
     * @return time in timebase inits
     */
    private long secondsToTimebase(double seconds) {
        long timebaseTime = 0l;

        if (videoCoder != null) {
            double timebase = 1.0d / videoCoder.getStream().getTimeBase().getDouble();
            timebaseTime = (long) (seconds * timebase);
        }

        return timebaseTime;
    }

    /**
     * Converts a time in timebase units to the corresponding time in seconds
     * See: http://wiki.xuggle.com/Concepts#Time_Bases
     * @param timebaseTime time in timebase units
     * @return time in seconds
     */
    private double timebaseToSeconds(long timebaseTime) {
        double seconds = 0.0d;

        if (videoCoder != null) {
            double timebase = 1.0d / videoCoder.getStream().getTimeBase().getDouble();
            seconds = (double) timebaseTime / timebase;
        }

        return seconds;
    }

    /**
     * Converts microseconds to seconds
     * @param microsecondTime the time in microseconds
     * @return time in seconds
     */
    private double microsecondsToSeconds(long microsecondTime) {
        return microsecondTime / 1000000d;
    }

    /**
     * Gets the duration of the media
     * @return the duration of the media in second
     */
    public void setDuration(double mediaDuration) {
        logger.fine("set duration: " + mediaDuration);
        this.mediaDuration = mediaDuration;
    }

    /**
     * Gets the duration of the media
     *
     * @return the duration of the media in second
     */
    public double getDuration() {
        return mediaDuration;
    }

    /**
     * Mutes audio
     */
    public void mute() {
        logger.fine("mute");
        audioQueue.setMuted(true);
    }

    /**
     * Unmutes audio
     */
    public void unmute() {
        logger.fine("unmute");
        audioQueue.setMuted(false);
    }

    /**
     * Gets the state of the audio
     * @return true if the audio is muted, false otherwise
     */
    public boolean isMuted() {
        return audioQueue.isMuted();
    }

    /**
     * Sets the state of the player
     * @param mediaState the new player state
     */
    private void setState(VideoPlayerState state) {
        final VideoPlayerState oldState = mediaState;
        mediaState = state;
        notifyStateListeners(oldState, mediaState);
    }

    /**
     * Gets the state of the player
     * @return the player state
     */
    public VideoPlayerState getState() {
        return mediaState;
    }

    /**
     * Thread for filling the buffer
     */
    private class QueueFillerThread implements Runnable {
        private boolean stop = true;
        private boolean threadRunning = false;
        private boolean flush = false;
        private double position = 0d;

        /**
         * Start the queue
         */
        public synchronized void start(double position) {
            this.position = position;

            // if we were stopped (as opposed to paused), restart the thread
            if (this.stop) {
                while (isThreadRunning()) {
                    // wait for thread to exit
                    logger.warning("Trying to restart running filler thread");

                    try {
                        wait(500);
                    } catch(InterruptedException ie) {}
                }

                this.stop = false;
             
                // immediately fill the queues with new data
                videoQueue.clear();
                audioQueue.clear();
                fillQueues();
                
                new Thread(this, "Queue filler thread").start();
            }
        }

        /**
         * Fill the queues back up to capacity
         */
        public synchronized void fill(double position) {
            // wake up the thread
            this.position = position;
            notify();
        }

        /**
         * Get the current position
         */
        public synchronized double getPosition() {
            return position;
        }

        /**
         * Set the current position, but don't change the queue
         */
        public synchronized void setPosition(double position) {
            this.position = position;
        }

        /**
         * Stop the thread
         * @param flush true to flush the queue of decoded video packets, or
         * false not to
         */
        public synchronized void stop(boolean flush) {
            this.flush = flush;
            this.stop = true;
            notify();
        }

        public synchronized boolean isRunning() {
            return !stop;
        }

        public void run() {
            logger.fine("Queue filler: start");
            setThreadRunning(true);

            try {
                while (isRunning()) {
                    logger.fine("Queue filler: fill");
                    if (!fillQueues()) {
                        // out of data
                        stop(false);
                    }

                    logger.fine("Queue filler: wait");
                    synchronized (this) {
                        if (isRunning()) {
                            wait();
                        }
                    }
                }
            } catch (InterruptedException ie) {
            } finally {
                try {
                    if (flush) {
                        videoQueue.clear();
                        audioQueue.clear();
                    }
                } finally {
                    setThreadRunning(false);
                }
            }

            logger.fine("Queue filler: exit");
        }

        private synchronized boolean isThreadRunning() {
            return threadRunning;
        }

        private synchronized void setThreadRunning(boolean threadRunning) {
            this.threadRunning = threadRunning;
            notify();
        }
    }

    /**
     * Queue for video
     */
    private class VideoQueue implements FrameQueue {
        private final ArrayDeque<IVideoPicture> videoQueue =
                new ArrayDeque<IVideoPicture>(QUEUE_SIZE);

        private double startTime;
        private double startPosition;

        public synchronized void open() {
        }

        public synchronized void start(double position) {
            logger.fine("Start video at " + position + " time " +
                        System.currentTimeMillis());

            this.startTime = System.currentTimeMillis() / 1000.0d;
            this.startPosition = position;
        }

        public synchronized void add(IVideoPicture picture) {
            videoQueue.add(picture);
        }

        public synchronized boolean isFull() {
            return videoQueue.size() >= QUEUE_SIZE;
        }

        public synchronized void pause() {
        }

        public synchronized void clear() {
            videoQueue.clear();
        }

        public synchronized void stop() {
        }

        public synchronized void close() {
        }

        public IVideoPicture nextFrame() {
            IVideoPicture out = null;
            double currentTime = System.currentTimeMillis() / 1000.0d;
            double position = startPosition + (currentTime - startTime);

            logger.fine("Next frame at " + (currentTime - startTime) +
                        " position " + position);

            synchronized (this) {
                // if there is a next frame, figure out what time it is
                // supposed to play
                while (!videoQueue.isEmpty()) {
                    IVideoPicture p = videoQueue.peek();

                    // get the scheduled time for this frame in milliseconds
                    double frameTime = microsecondsToSeconds(p.getTimeStamp());

                    logger.fine("Checking packet at " + frameTime +
                                " (" + p.getTimeStamp() + ") for " +
                                position);

                    if (frameTime > position + 0.05) {
                        // if the first frame is more than .05 seconds in the future,
                        // we are already showing the most up-to-date frame, so no
                        // need to do anything
                        break;
                    }

                    // at this point, we are either at or past this frame, so we
                    // remove it from the queue
                    videoQueue.remove();

                    // see if we should request more data in the queue
                    if (videoQueue.size() <= 4) {
                        // request more data
                        queueFiller.fill(position);
                    } else {
                        // update the queue filler's position, so it doesn't
                        // decode irrelevant packets
                        queueFiller.setPosition(position);
                    }

                    // the frame is withing 0.05 seconds of us, so play it
                    if (Math.abs(frameTime - position) < 0.05) {
                        out = p;
                        break;
                    }
                }
            }

            // the current frame is the best option -- update the stream
            // position and return it
            if (out != null) {
                setStreamPosition(out.getTimeStamp());
                logger.fine("Video queue next frame returns " +
                            microsecondsToSeconds(getStreamPosition()));
                
                notifyTimeListeners(microsecondsToSeconds(getStreamPosition()));
                audioQueue.setPosition(microsecondsToSeconds(getStreamPosition()));
            } else {
                // is the queue filler stopped? If so, playback is finished
                if (!queueFiller.isRunning()) {
                    VideoPlayerImpl.this.stop();
                }

                // update the queue filler's position, so it doesn't
                // decode irrelevant packets
                queueFiller.setPosition(position + 0.05);
                logger.fine("Returning null");
            }

            return out;
        }
    }

    /**
     * Thread for playing queued audio
     */
    private class AudioQueue implements Runnable {
        private final Queue<AudioSample> samples = new ArrayDeque<AudioSample>();
            
        private SourceDataLine line;

        private boolean stop = true;
        private boolean started = false;
        private boolean threadRunning = false;
        private boolean muted = false;

        public void open(IStreamCoder iAudioCoder)
                throws LineUnavailableException
        {
            line = openJavaSound(iAudioCoder);
        }

        public void close() {
            stop();
            closeJavaSound();
        }

        public synchronized void start(double position) {
            if (line != null) {
                line.start();
            }

            // if we are stopped (as opposed to paused), restart the thread
            if (this.stop) {
                while (isThreadRunning()) {
                    // wait for thread to exit
                    logger.warning("Trying to restart running audio thread");
                    try {
                        wait(500);
                    } catch (InterruptedException ie){}
                }

                this.stop = false;
                new Thread(this, "Audio processor").start();
            }
        }

        /**
         * Add samples to the queue at the given timestamp.
         * @param timestamp the timestamp (in microseconds)
         * @param sample the sample data
         */
        public synchronized void add(IAudioSamples inSamples) {
            logger.fine("Add audio packet at " + 
                        microsecondsToSeconds(inSamples.getTimeStamp()));

            // make sure we aren't muted
            if (isMuted()) {
                return;
            }

//            // resample if necessary
//            if (resampler != null) {
//                IAudioSamples out = IAudioSamples.make(inSamples.getNumSamples(),
//                        resampler.getOutputChannels(), resampler.getOutputFormat());
//                resampler.resample(out, inSamples, inSamples.getNumSamples());
//                inSamples = out;
//            }

            // add to the queue
            byte[] rawBytes = inSamples.getData().getByteArray(0, inSamples.getSize());
            samples.add(new AudioSample(microsecondsToSeconds(inSamples.getTimeStamp()),
                                        rawBytes));
            notify();
        }

        public synchronized void pause() {
            if (line != null) {
                line.stop();
            }
        }

        public synchronized void clear() {
            logger.fine("Clear audio thread");

            samples.clear();

            if (line != null) {
                line.flush();
            }
            
            notify();
        }

        public synchronized void stop() {
            logger.fine("Stop audio thread");

            stop = true;
            started = false;
            
            // empty the queue
            samples.clear();

            if (line != null) {
                line.drain();
                line.stop();
            }
            notify();
        }

        public synchronized boolean isRunning() {
            return !stop;
        }

        public synchronized void setMuted(boolean muted) {
            // this.muted = muted;
        }

        public synchronized boolean isMuted() {
            return muted;
        }

        public synchronized void setPosition(double position) {
            if (!started) {
                notify();
            }
        }

        public void run() {
            setThreadRunning(true);

            try {
                while (isRunning()) {
                    logger.fine("Audio thread running");
                    byte[] sample;
                    while ((sample = nextSample()) != null) {
                        play(sample);
                    }

                    // queue is empty, wait for something to do
                    logger.fine("Audio thread waiting");
                    synchronized (this) {
                        if (isRunning()) {
                            wait();
                        }
                    }
                }
            } catch (InterruptedException ie) {
            } finally {
                setThreadRunning(false);
            }

            logger.fine("Audio thread exiting");
        }

        private synchronized byte[] nextSample() {
            if (samples.isEmpty()) {
                return null;

            }
            if (!started && samples.peek().getTimeStamp() > getPosition() + .05)
            {
                // audio hasn't started yet
                return null;
            } else if (!started) {
                started = true;
            }

            AudioSample out = samples.poll();
            logger.fine("Audio thread next sample returns " + out.timeStamp);
            return out.getData();
        }

        private synchronized boolean isThreadRunning() {
            return threadRunning;
        }

        private synchronized void setThreadRunning(boolean threadRunning) {
            this.threadRunning = threadRunning;
            notify();
        }

        /**
         * Initialize JavaSound
         * @param aAudioCoder an audio decoder
         * @throws LineUnavailableException
         */
        private SourceDataLine openJavaSound(IStreamCoder aAudioCoder)
                throws LineUnavailableException
        {
            AudioFormat audioFormat = new AudioFormat(aAudioCoder.getSampleRate(),
                    (int) IAudioSamples.findSampleBitDepth(aAudioCoder.getSampleFormat()),
                    aAudioCoder.getChannels(),
                    true, /* xuggler defaults to signed 16 bit samples */
                    false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine out = (SourceDataLine) AudioSystem.getLine(info);
           
            // try opening the line.
            out.open(audioFormat);
            return out;
        }

        /**
         * Play audio samples
         * @param aSamples audio samples to play
         */
        private void play(byte[] rawBytes) {
            // we're just going to dump all the samples into the line
            if (line != null) {
                line.write(rawBytes, 0, rawBytes.length);
            }
        }

        /**
         * Shutdown JavaSound
         */
        private void closeJavaSound() {
            // close the line.
            line.close();
        }
    }

    private class AudioSample {
        private final double timeStamp;
        private final byte[] data;

        public AudioSample(double timeStamp, byte[] data) {
            this.timeStamp = timeStamp;
            this.data = data;
        }

        public double getTimeStamp() {
            return timeStamp;
        }

        public byte[] getData() {
            return data;
        }
    }
}
