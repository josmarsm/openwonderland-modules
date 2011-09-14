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
package org.jdesktop.wonderland.modules.videoplayer.client;

import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IVideoPicture;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.assetmgr.Asset;
import org.jdesktop.wonderland.client.assetmgr.AssetManager;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.common.ContentURI;
import org.jdesktop.wonderland.video.client.VideoPlayerImpl;
import org.jdesktop.wonderland.video.client.VideoQueueFiller;
import org.jdesktop.wonderland.video.client.VideoQueueFiller.VideoQueue;

/**
 * Video player extension for use in Wonderland
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class WonderlandVideoPlayerImpl extends VideoPlayerImpl 
    implements TimedEventSource
{
    private static final Logger LOGGER = 
            Logger.getLogger(WonderlandVideoPlayerImpl.class.getName());
    
    private final Set<TimeListener> timeListeners = new CopyOnWriteArraySet();
    
    public WonderlandVideoPlayerImpl() {
        super ();
    }
    
    @Override
    protected VideoQueueFiller createQueueFiller(VideoQueue queue) {
        return new WonderlandVideoQueueFiller(queue);
    }
    
    public void addTimeListener(TimeListener listener) {
        timeListeners.add(listener);
    }

    public void removeTimeListener(TimeListener listener) {
        timeListeners.remove(listener);
    }
    
    protected void fireTimeChanged(double time) {
        for (TimeListener listener : timeListeners) {
            listener.timeChanged(time);
        }
    }

    @Override
    public IVideoPicture nextFrame() {
        IVideoPicture out = super.nextFrame();
    
        // notify listeners of frame time
        if (out != null) {
            fireTimeChanged(out.getTimeStamp() / 1000000.0);
        }
        
        return out;
    }

    @Override
    protected void notifyFrameListenersClose() {
        super.notifyFrameListenersClose();
        
        fireTimeChanged(0d);
    }

    @Override
    protected void notifyFrameListenersPreview(IVideoPicture frame) {
        super.notifyFrameListenersPreview(frame);
    
        fireTimeChanged(frame.getTimeStamp() / 1000000.0);
    }

    @Override
    protected void notifyFrameListenersStop() {
        super.notifyFrameListenersStop();
        
        fireTimeChanged(0d);
    }
    
    private static class WonderlandVideoQueueFiller extends VideoQueueFiller {
        public WonderlandVideoQueueFiller(VideoQueue queue) {
            super (queue);
        }
    
        @Override
        protected String prepareContent(String uri) throws IOException {
            if (uri.startsWith("wlcontent:")) {
                // resolve as an asset
                Asset asset = getAssetFor(uri);
                if (asset == null) {
                    throw new IOException("Unable to get asset for " + uri);
                }
                
                // wait for the asset to be downloaded
                if (!AssetManager.getAssetManager().waitForAsset(asset)) {
                    throw new IOException("Unable to download asset for " + 
                                          uri + ": " + asset.getFailureInfo());
                }
            }
        
            return uri;
        }
        
        @Override
        protected IContainer openContainer(String uri) {
            if (uri.startsWith("wlcontent:")) {
                return openAssetContainer(uri);
            }
            
            return super.openContainer(uri);
        }
        
        /**
         * Open a container not based on a cache
         * @param uri the URI to open
         * @return the cotnainer, or null if we can't open the container
         */
        protected IContainer openAssetContainer(String uri) {
            try {
                // at this point, we should have a completely downloaded
                // asset
                Asset asset = getAssetFor(uri);
                
                // wait for the asset to be ready
                if (!AssetManager.getAssetManager().waitForAsset(asset)) {
                    throw new IOException("Unable to download asset for " + 
                                          uri + ": " + asset.getFailureInfo());
                }
                
                // now open the file as a random access file
                RandomAccessFile raf = new RandomAccessFile(asset.getLocalCacheFile(), "r");
                
                // create the container
                IContainer out = IContainer.make();
                int res = out.open(raf, IContainer.Type.READ, null);
                if (res < 0) {
                    throw new IllegalArgumentException("Error opening " + uri + ": " + res);
                }
                
                return out;
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "Error converting " + uri, ioe);
                return null;
            }
        }
        
        /**
         * Open a container based on a cache
         * @param uri the URI to open
         * @return the container, or null if we can't open the container
         */
        protected IContainer openStreamingAssetContainer(String uri) {
            ReadableByteChannel channel = resolveURI(uri);
            if (channel == null) {
                return null;
            }

            IContainer out = IContainer.make();
            int res = out.open(channel, null);
            if (res < 0) {
                throw new IllegalArgumentException("Error opening " + uri + ": " + res);
            }

            return out;
        }

        /**
         * Convert a URI into a ReadableByteChannel that caches data until
         * a minimum amount is downloaded. This only works for wlcontent:// URLs.
         *
         * @param uri the original URI of the content
         * @return a constructed ReadableByteChannel, or null if the channel
         * cannot be constructed for any reason
         */
        protected ReadableByteChannel resolveURI(String uri) {
            try {
                Asset asset = getAssetFor(uri);
                if (asset == null) {
                    return null;
                }

                // cache one MB of data before playing
                return new AssetCacheByteChannel(asset, 1024 * 1000);
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "Error converting " + uri, ioe);
                return null;
            }
        }
        
        /**
         * Get an asset from a wlcontent: URI
         * @param uri the uri
         * @return the Asset derived from that URI
         */
        protected Asset getAssetFor(String uri) throws IOException {
            URL url = null;
                    
            try {
                url = AssetUtils.getAssetURL(uri);
                if (!url.getProtocol().equalsIgnoreCase("wlcontent")) {
                    return null;
                }

                ContentURI cURI = new ContentURI(url.toExternalForm());
                Asset asset = AssetManager.getAssetManager().getAsset(cURI);
                return asset;
            } catch (URISyntaxException use) {
                LOGGER.log(Level.WARNING, "Error creating uri for " + url, use);
                return null;
            }
        }
        
    }
    
    /**
     * Channel that buffers a certain minimum number of bytes before
     * returning any data
     */
    private static class AssetCacheByteChannel
            implements ReadableByteChannel, AssetManager.AssetProgressListener
    {
        private final Asset asset;
        private final int minBytes;

        private FileChannel file;
        private boolean open = true;

        public AssetCacheByteChannel(Asset asset, int minBytes) {
            this.asset = asset;
            this.minBytes = minBytes;

            asset.addAssetProgressListener(this);
        }

        public void downloadProgress(Asset asset, int readBytes, int percentage) {
            synchronized (this) {
                notify();
            }
        }

        public void downloadFailed(Asset asset) {
            synchronized (this) {
                notify();
            }
        }

        public void downloadCompleted(Asset asset) {
            synchronized (this) {
                notify();
            }
        }

        public synchronized int read(ByteBuffer dest) throws IOException {
            if (!isOpen()) {
                throw new ClosedChannelException();
            }

            // wait until the file is available and has sufficient
            // number of bytes available
            waitForBytes(Math.max(minBytes, dest.remaining()));

            // make sure we are not at the end of the file
            if (file == null) {
                return -1;
            }

            return file.read(dest);
        }

        public synchronized boolean isOpen() {
            return open;
        }

        public synchronized void close() throws IOException {
            open = false;

            if (file != null) {
                file.close();
            }
        }

        private synchronized void waitForBytes(long bytes) throws IOException {
            // possible states:
            // 1. SCHEDULED -- file is not yet downloaded, wait for it
            // 2. DOWNLOADING -- file is in progress, write if there are
            // enough bytes
            // 3. DOWNLOADED -- file is done, write no matter what until EOF
            // 4. FAILED -- throw an exception


            // do we have a file yet?
            if (file == null) {
                // if the asset hasn't started downloading, wait for it
                try {
                    while (asset.getStatus() == Asset.Status.SCHEDULED) {
                        wait();
                    }
                } catch (InterruptedException ie) {
                    // break out of the loop
                }

                // now we can open the file if there was no error
                if (asset.getStatus() == Asset.Status.FAILED) {
                    throw new IOException("Error downloading file: " +
                                          asset.getFailureInfo());
                }

                RandomAccessFile raf =
                        new RandomAccessFile(asset.getLocalCacheFile(),"r");
                file = raf.getChannel();
            }

            // at this point the file is guaranteed to exist and the asset
            // is no longer in the scheduled state. See if we have enough
            // data to continue
            if (file.size() - file.position() >= bytes) {
                // if we are here, there is enough data to continue
                return;
            }

            // if we get here, it means we would like to read more data. If
            // the asset is still downloading, wait for it
            try {
                while (asset.getStatus() == Asset.Status.DOWNLOADING &&
                       file.size() - file.position() <= bytes)
                {
                    wait();
                }
            } catch (InterruptedException ie) {
                // break out of the loop
            }

            // if there was a failure, throw an exception
            if (asset.getStatus() == Asset.Status.FAILED) {
                throw new IOException("Error downloading file: " +
                                      asset.getFailureInfo());
            }

            // we've done everything we can to get data -- if we get an
            // EOF the file has been completely read
            return;
        }
    }    
}
