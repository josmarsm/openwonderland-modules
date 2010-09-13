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
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

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
    private String mediaURI;
    private VideoPlayerState mediaState = VideoPlayerState.NO_MEDIA;
    private double mediaDuration;
    private boolean muted = false;
    private List timeListeners;
    private List stateListeners;
    private List frameListeners;
    // The audio line we'll output sound to; it'll be the default audio device
    // on your system if available
    private static SourceDataLine mLine;
    private int videoStreamId;
    private int audioStreamId;
    private IStreamCoder videoCoder;
    private IStreamCoder audioCoder;
    private IContainer container;
    private IVideoResampler resampler = null;
    private IConverter pictureConverter;
    private long streamPosition = 0l;
    private long seekTo = -1l;
    private long lastFrameTimeStamp = Global.NO_PTS;
    private long thisFrameTimeStamp = Global.NO_PTS;
    private long thenTime = 0;
    private long nowTime = 0;
    private HTTPDownloader downloader;
    private Thread downloadThread;

    public VideoPlayerImpl() {
    }

    /**
     * Add a listener for new frames
     * @param listener a frame listener to be notified of new frames
     */
    public void addFrameListener(FrameListener listener) {
        if (frameListeners == null) {
            frameListeners = Collections.synchronizedList(new ArrayList());
        }
        frameListeners.add(listener);
    }

    /**
     * Remove a listener for new frames
     * @param listener a frame listener to be removed
     */
    public void removeFrameListener(FrameListener listener) {
        if (frameListeners != null) {
            frameListeners.remove(listener);
        }
    }

    /**
     * Notify all the frame listeners of a new frame
     * @param image a new frame
     */
    public void notifyFrameListeners(BufferedImage image) {
        if (frameListeners != null) {
            Iterator<FrameListener> iterator = frameListeners.iterator();
            while (iterator.hasNext()) {
                iterator.next().frameUpdate(image);
            }
        }
    }

    /**
     * Add a listener for time changes
     * @param listener a time listener to be notified of time changes
     */
    public void addTimeListener(TimeListener listener) {
        if (timeListeners == null) {
            timeListeners = Collections.synchronizedList(new LinkedList());
        }
        timeListeners.add(listener);
    }

    /**
     * Remove a listener for time changes
     * @param listener a time listener to be removed
     */
    public void removeTimeListener(TimeListener listener) {
        if (timeListeners != null) {
            timeListeners.remove(listener);
        }
    }

    /**
     * Notify all the time time isteners of a time change
     * @param newTime the time of the event
     */
    private void notifyTimeListeners(double newTime) {
        if (timeListeners != null) {
            ListIterator<TimeListener> iter = timeListeners.listIterator();
            while (iter.hasNext()) {
                TimeListener listener = iter.next();
                listener.timeChanged(newTime);
            }
        }
    }

    /**
     * Add a listener for changes in state
     * @param listener a state listener to be notified of state changes
     */
    public void addStateListener(VideoStateListener listener) {
        if (stateListeners == null) {
            stateListeners = Collections.synchronizedList(new LinkedList());
        }
        stateListeners.add(listener);
    }

    /**
     * Remove a listener for state changes
     * @param listener a state listener to be removed
     */
    public void removeStateListener(VideoStateListener listener) {
        if (stateListeners != null) {
            stateListeners.remove(listener);
        }
    }

    /**
     * Notify all the state listeners of a state change
     * @param oldState the previous state
     * @param newState the new state
     */
    private void notifyStateListeners(VideoPlayerState oldState, VideoPlayerState newState) {
        if (stateListeners != null) {
            ListIterator<VideoStateListener> iter = stateListeners.listIterator();
            while (iter.hasNext()) {
                VideoStateListener listener = iter.next();
                listener.mediaStateChanged(oldState, newState);
            }
        }
    }

    /**
     * Load media, substituting web sources with cached file sources
     * @param mediaURI the URI of the media
     * @return the playable URI of the media
     */
    public String loadMedia(final String mediaURI) {
        String loadedURI = null;

        if (mediaURI != null) {
            // check we have a valid media URI
            try {
                new URL(mediaURI);
            } catch (Exception e) {
                logger.warning("invalid video URL: " + mediaURI + ": " + e);
                return null;
            }

            // we might be already downloading a file
            if ((downloader != null) && !downloader.downloadComplete()) {
                // a download is already in progress
                if (mediaURI.equals(downloader.getFile())) {
                    // we're already downloading the requested URI, so ignore
                    // the new download request and let the current download
                    // thread complete
                    return downloader.getLocalFile();
                } else {
                    // we're downloading a different file, so abort the current
                    // download and start downloading the new file
                    downloader.abortDownload();
                }
            }

            if (mediaURI.startsWith("http")) {
                // ffmpeg doesn't support seeking within web-based video sources
                // so download the media to a temporary file and play that instead
                downloader = new HTTPDownloader(mediaURI,
                        HTTPDownloader.getTempFilename(mediaURI), 100 * 1024);

                if (downloader.downloadRequired()) {
                    // download the first 100 KB of the file
                    downloadThread = new Thread(downloader);
                    downloadThread.start();

                    synchronized (downloader) {
                        while (!downloader.downloadComplete() && !downloader.alertTriggered()) {
                            try {
                                downloader.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                        logger.fine("downloaded: " + downloader.getDownloaded()
                                + " of " + downloader.getDownloadSize() + " Bytes"
                                + ", bandwidth: " + downloader.getBandwidth() + " KB/s");
                    }

                    // determine if we need to wait for the entire file to download
                    if (downloader.getRemainingTime() > 10) {
                        // If the media will take more than 10 seconds to download
                        // then completely download a copy of the media. This is
                        // necessary when connected over a slow network where video
                        // playback might overrun the buffer of downloaded video
                        //
                        // This strategy courtesy of Jo Voordeckers which he described
                        // at JavaOne 2008 in technical session TS-7372.
                        logger.fine("fully downloading video, time remaining: " + downloader.getRemainingTime() + " s");

                        synchronized (downloader) {
                            // wait for the file to be completely downloaded
                            while (!downloader.downloadComplete()) {
                                try {
                                    downloader.wait();
                                } catch (InterruptedException e) {
                                }
                            }
                            logger.fine("completed downloading video: " + mediaURI);
                        }
                    }
                }
                // file name of local copy of web media source
                loadedURI = downloader.getLocalFile();
            } else {
                // a file based media source
                loadedURI = new String(mediaURI);
            }
        }
        return loadedURI;
    }

    /**
     * Open video media
     * @param uri the URI of the video media to open
     */
    public void openMedia(final String uri) {
        new Thread(new Runnable() {

            public void run() {
                logger.fine("opening video: " + uri);

                // close any media we currently have open
                if (isPlayable()) {
                    closeMedia();
                }

                mediaURI = loadMedia(uri);

                if (mediaURI != null) {
                    // make sure that we can actually convert media pixel formats
                    if (!IVideoResampler.isSupported(IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION)) {
                        throw new RuntimeException("you must install the GPL version of Xuggler (with IVideoResampler support) for this demo to work");
                    }

                    // create a Xuggler container object
                    container = IContainer.make();

                    // open up the container
                    if (container.open(mediaURI, IContainer.Type.READ, null) < 0) {
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
                    resampler = null;

                    if (videoCoder != null) {
                        if (videoCoder.open() < 0) {
                            logger.warning("could not open audio decoder for container: " + mediaURI);
                            return;
                        }
                        if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
                            // if this stream is not in BGR24, we're going to need to
                            // convert it.  The VideoResampler does that for us.
                            resampler = IVideoResampler.make(videoCoder.getWidth(), videoCoder.getHeight(), IPixelFormat.Type.BGR24,
                                    videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
                            if (resampler == null) {
                                logger.warning("could not create color space resampler for: " + mediaURI);
                                return;
                            }
                        }

                        streamPosition = 0;
                        mediaDuration = container.getDuration() / 1000000;
                        logger.fine("video duration: " + mediaDuration);
                    }

                    if (audioCoder != null) {
                        if (audioCoder.open() < 0) {
                            throw new RuntimeException("could not open audio decoder for container: " + mediaURI);
                        }

                        // ask the Java Sound System to get itself ready
                        try {
                            openJavaSound(audioCoder);
                        } catch (LineUnavailableException ex) {
                            logger.warning("unable to open sound device: " + mediaURI + ": " + ex);
                            // throw new RuntimeException("unable to open sound device on your system when playing back container: " + filename);
                        } catch (IllegalArgumentException ex) {
                            logger.warning("unable to open sound device: " + ex);
                        }
                    }
                    setState(VideoPlayerState.MEDIA_READY);
                }
            }
        }).start();
    }

    /**
     * Initialize JavaSound
     * @param aAudioCoder an audio decoder
     * @throws LineUnavailableException
     */
    private static void openJavaSound(IStreamCoder aAudioCoder) throws LineUnavailableException {
        AudioFormat audioFormat = new AudioFormat(aAudioCoder.getSampleRate(),
                (int) IAudioSamples.findSampleBitDepth(aAudioCoder.getSampleFormat()),
                aAudioCoder.getChannels(),
                true, /* xuggler defaults to signed 16 bit samples */
                false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        mLine = (SourceDataLine) AudioSystem.getLine(info);

        // try opening the line.
        mLine.open(audioFormat);

        // start the line.
        mLine.start();
    }

    /**
     * Play audio samples
     * @param aSamples audio samples to play
     */
    private static void playJavaSound(IAudioSamples aSamples) {
        // we're just going to dump all the samples into the line
        byte[] rawBytes = aSamples.getData().getByteArray(0, aSamples.getSize());
        if (mLine != null) {
            mLine.write(rawBytes, 0, aSamples.getSize());
        }
    }

    /**
     * Shutdown JavaSound
     */
    private static void closeJavaSound() {
        if (mLine != null) {
            // wait for the line to finish playing
            mLine.drain();
            // close the line.
            mLine.close();
            mLine = null;
        }
    }

    /**
     * Close video media
     */
    public void closeMedia() {
        logger.fine("closing video");
        stop();
        closeJavaSound();
        mediaURI = null;
        mediaState = VideoPlayerState.NO_MEDIA;
        container = null;
        mediaDuration = 0.0d;
        streamPosition = 0;
        videoStreamId = -1;
        videoCoder = null;
        resampler = null;
        audioStreamId = -1;
        audioCoder = null;
        pictureConverter = null;
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
        logger.fine("play");
        if (isPlayable() && (getState() != VideoPlayerState.PLAYING)) {
            new Thread(new Runnable() {

                public void run() {
                    setState(VideoPlayerState.PLAYING);
                    doPlay();
                }
            }).start();
        }
    }

    /**
     * Convert an IPacket into a an IVideoPicture
     * @param packet the packet to convert
     * @return a picture representation of the packet
     */
    private IVideoPicture decodeVideoPacket(IPacket packet) {
        IVideoPicture tempPicture = IVideoPicture.make(videoCoder.getPixelType(),
                videoCoder.getWidth(), videoCoder.getHeight());
        IVideoPicture picture = null;

        // now, we decode the video, checking for any errors
        int bytesDecoded = videoCoder.decodeVideo(tempPicture, packet, 0);
        if (bytesDecoded < 0) {
            logger.warning("error decoding video: " + packet);
        } else {
            // Some decoders will consume data in a packet, but will not be able 
            // to construct a full video picture yet. Therefore you should always
            // check if you got a complete picture from the decoder
            if (tempPicture.isComplete()) {
                // if the resampler is not null, that means we didn't get the 
                // video in BGR24 format and need to convert it into BGR24 format
                if (resampler != null) {
                    // we must resample
                    picture = IVideoPicture.make(resampler.getOutputPixelFormat(), tempPicture.getWidth(), tempPicture.getHeight());
                    if (resampler.resample(picture, tempPicture) < 0) {
                        //throw new RuntimeException("could not resample video from: " + mediaURI);
                        logger.warning("could not resample video from: " + mediaURI);
                    }
                } else {
                    picture = tempPicture;
                }

                if (picture.getPixelType() != IPixelFormat.Type.BGR24) {
                    //throw new RuntimeException("could not decode video as BGR 24 bit data in: " + mediaURI);
                    logger.warning("could not decode video as BGR 24 bit data in: " + mediaURI);
                }
            } else {
                picture = tempPicture;
            }
        }
        return picture;
    }

    /**
     * Convert an IVideoPicture to a BufferedImage that can be rendered
     * @param picture the IVideoPicture to convert
     * @return a BufferedImage
     */
    private BufferedImage IVideoPictureToBufferedImage(IVideoPicture picture) {
        BufferedImage image = null;

        // get a converter that can convert between IVideoPictures
        // and BufferedImages
        if (pictureConverter == null) {
            pictureConverter = ConverterFactory.createConverter(ConverterFactory.XUGGLER_BGR_24, picture);
        }
        image = pictureConverter.toImage(picture);

        return image;
    }

    /**
     * Seek to a specified position in timebase units
     * @param position the position to seek to in timebase units
     */
    private void seek(long position) {
        logger.fine("seeking to: " + position + " = " + timebaseToSeconds(position) + "s");

        if (container != null) {
            // seek to the key frame nearest to the specified position
            // See: http://build.xuggle.com/view/Stable/job/xuggler_jdk5_stable/javadoc/java/api/com/xuggle/xuggler/IContainer.html
            //container.seekKeyFrame(videoStreamId, position, IContainer.SEEK_FLAG_FRAME);
            container.seekKeyFrame(videoStreamId, position - 100, position, position, IContainer.SEEK_FLAG_FRAME);

            IPacket packet = IPacket.make();
            double seekPosition = timebaseToSeconds(position);
            double delta = 1.0d;

            // depending on how close the key frame is to the specified position,
            // we may need to silently "play forward" until the stream is at the
            // requested position (this can be several seconds of video)
            do {
                if (container.readNextPacket(packet) < 0) {
                    // failed to read a packet, possibly at end of media, stop
                    // seeking
                    break;
                }

                if (packet.getStreamIndex() == videoStreamId) {
                    // decode the packet into a picture, so we can get the time
                    // stamp of the frame
                    IVideoPicture picture = decodeVideoPacket(packet);
                    streamPosition = picture.getTimeStamp();
                    picture = null;

                    delta = Math.abs((double) streamPosition / 1000000d - seekPosition);
                    if ((double) streamPosition / 1000000d > seekPosition) {
                        logger.fine("ending seek because " + ((double) streamPosition / 1000000d) + " > " + seekPosition);
                        break;
                    }
                }
                //logger.fine("seeking, current: " + (double) streamPosition / 1000000d + ", goal: " + seekPosition + ", delta: " + delta);
                // stop searching once we're within 0.05 seconds of the specified
                // time - we won't get the exact position
            } while (delta > 0.05d);

            logger.fine("seek requested: " + timebaseToSeconds(position)
                    + ", got: " + timebaseToSeconds(streamPosition) + ", delta: " + delta);

            notifyTimeListeners(getPosition());

            packet = null;
        }
    }

    /**
     * We can potentially decode frames faster than the video's frame rate.
     * This method waits the correct time since the previous frame to
     * ensure the correct frame rate.
     * @param picture the next frame to render
     */
    private void waitForFrameTime(IVideoPicture picture) {
        // remember time stamp (in microseconds) of current frame
        streamPosition = picture.getTimeStamp();
        thisFrameTimeStamp = streamPosition / 1000;

        if (lastFrameTimeStamp == Global.NO_PTS) {
            lastFrameTimeStamp = thisFrameTimeStamp;
            thenTime = System.currentTimeMillis();
        } else {
            nowTime = System.currentTimeMillis();
            // elapsed is how long it took to render the last frame,
            // get the current frame and convert it into an image
            long elapsed = (nowTime - thenTime);

            // how long to wait before rendering this frame
            long delay = (thisFrameTimeStamp - lastFrameTimeStamp) - elapsed;
            lastFrameTimeStamp = thisFrameTimeStamp;

            try {
                if (delay < 0) {
                    // the frame is overdue, we could drop the frame but this
                    // leads to jittery playback on slow systems, so we render
                    // it as soon as we can, but the video will play slower
                    // then real time
                    delay = 0;
                } else if (delay > 100) {
                    // somehow the frame is way ahead of schedule
                    // wait until it should be rendered, no longer
                    delay = (thisFrameTimeStamp - lastFrameTimeStamp);
                }
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
        }
        thenTime = System.currentTimeMillis();
    }

    private void doPlay() {
        // walk through the container processing each (video or audio) packet
        IPacket packet = IPacket.make();
        boolean cueing = seekTo >= 0;

        while (isPlaying() || cueing) {
            // handle explicit seek before reading next packet
            if (seekTo >= 0) {
                seek(seekTo);
                seekTo = -1;
                lastFrameTimeStamp = Global.NO_PTS;
            }

            // read next media packet
            if (container.readNextPacket(packet) < 0) {
                // failed to read packet, done playing
                logger.info("end of media, playback stopping");
                stop();
                break;
            }

            // now we have a packet, let's see if it belongs to our media stream
            if (packet.getStreamIndex() == videoStreamId) {
                // convert the packet into a video picture
                IVideoPicture picture = decodeVideoPacket(packet);

                streamPosition = picture.getTimeStamp();

                if (picture.isComplete()) {
                    // convert the picture into something renderable
                    BufferedImage image = IVideoPictureToBufferedImage(picture);

                    // wait for the right time to render it
                    waitForFrameTime(picture);

                    // sync time listeners to media position
                    notifyTimeListeners(getPosition());

                    // notify listeners that it's time to render
                    if (image != null) {
                        notifyFrameListeners(image);
                        image = null;
                    }
                }
                if (cueing) {
                    // done cueing
                    break;
                }
            } else if ((packet.getStreamIndex() == audioStreamId) && !cueing && !muted) {
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
                    // Some decoders will consume data in a packet, but will not be able to construct
                    // a full set of samples yet.  Therefore you should always check if you
                    // got a complete set of samples from the decoder
                    if (samples.isComplete()) {
                        // note: this call will block if Java's sound buffers fill up, and we're
                        // okay with that.  That's why we have the video "sleeping" occur
                        // on another thread. [NOT TRUE!]
                        playJavaSound(samples);
                    }
                }
            }
        }
        logger.fine("exited play loop at: " + streamPosition);
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
        logger.fine("pause");
        if (isPlayable() && (getState() != VideoPlayerState.PAUSED)) {
            logger.info("paused at: " + streamPosition);
            setState(VideoPlayerState.PAUSED);
            notifyTimeListeners(getPosition());
        }
    }

    /**
     * Stop playing media
     */
    public void stop() {
        logger.fine("stop");
        if (isPlayable() && (getState() != VideoPlayerState.STOPPED)) {
            logger.info("stopped at: " + streamPosition);
            setState(VideoPlayerState.STOPPED);
            notifyTimeListeners(getPosition());
        }
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
     * Set the position within the media
     * @param position the position in seconds
     */
    public void setPosition(double position) {
        logger.fine("set position: " + position);
        if (isPlayable()) {
            seekTo = secondsToTimebase(position);

            if (!isPlaying()) {
                doPlay();
            }
        }
    }

    /**
     * Get the current position in microseconds
     * @return the current position in microseconds
     */
    public long getPositionMicroseconds() {
        return streamPosition;
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
        muted = true;
    }

    /**
     * Unmutes audio
     */
    public void unmute() {
        logger.fine("unmute");
        muted = false;
    }

    /**
     * Gets the state of the audio
     * @return true if the audio is muted, false otherwise
     */
    public boolean isMuted() {
        return muted;
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
}
