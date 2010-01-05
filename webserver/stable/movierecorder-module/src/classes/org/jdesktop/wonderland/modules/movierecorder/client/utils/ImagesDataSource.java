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

package org.jdesktop.wonderland.modules.movierecorder.client.utils;

import java.awt.Dimension;

import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;


/**
 * A DataSource to read images from an ImageRenderer and
 * turn that into a stream of JMF buffers.
 * The DataSource is not seekable or positionable.
 *
 * @author Mikael Nordenberg, <a href="http://www.ikanos.se">www.ikanos.se</a>
 */
public class ImagesDataSource extends PullBufferDataSource {
    PullBufferStream streams[] = new PullBufferStream[1];

    /**
     * This constructor creates an ImagesDataSource from a JPEGImageProducer.
     * The images must all have the same size, imagesSize, since there is no
     * re-coding of them.
     * @param producer                  the image producer that delivers the JPEG data
     * @param imagesSize                the size of the images, this will also be the video size
     * @param frameRate                 the desired frame rate of the movie
     */
    public ImagesDataSource(JPEGImageProducer producer, Dimension imagesSize, float frameRate)  {
        streams[0] = new JPEGBufferStream(producer, imagesSize, frameRate);
    }

    

    public void connect() {}
    public void disconnect() {}
    public void start() {}
    public void stop() {}
    public void setLocator(MediaLocator source) {}

    public MediaLocator getLocator() {
        return null;
    }

    public String getContentType() {
        return ContentDescriptor.RAW;
    }

    /**
     * Return the ImagesStream.
     */
    public PullBufferStream[] getStreams() {
        return streams;
    }

    public Time getDuration() {
        return DURATION_UNKNOWN;
    }

    public Object[] getControls() {
        return new Object[0];
    }

    public Object getControl(String type) {
        return null;
    }
}

