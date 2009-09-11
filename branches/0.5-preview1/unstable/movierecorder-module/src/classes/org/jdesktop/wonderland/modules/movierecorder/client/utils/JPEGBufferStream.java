/**
 * Project Looking Glass
 *
 * $RCSfile: JPEGBufferStream.java,v $
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.3 $
 * $Date: 2008/03/14 18:14:27 $
 * $State: Exp $
 * $Id: JPEGBufferStream.java,v 1.3 2008/03/14 18:14:27 bernard_horan Exp $
 */

package org.jdesktop.wonderland.modules.movierecorder.client.utils;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferStream;
import java.awt.*;
import java.io.IOException;

/**
 * This class represents a PullBufferStream that delivers JPEG-encoded images.
 *
 * @author Mikael Nordenberg, <a href="http://www.ikanos.se">www.ikanos.se</a>
 */
class JPEGBufferStream implements PullBufferStream {
    private VideoFormat format;
    private JPEGImageProducer producer;
    private float frameRate;

    private long sequenceNumber = 0;
    private boolean ended = false;

    public JPEGBufferStream(JPEGImageProducer producer, Dimension imagesSize, float frameRate) {
        this.producer = producer;
        this.frameRate = frameRate;
        format = new VideoFormat(VideoFormat.JPEG, imagesSize, Format.NOT_SPECIFIED, Format.byteArray, frameRate);
    }

    public boolean willReadBlock() {
        return false;
    }

    public void read(Buffer buf) throws IOException {
        // Check if we've finished all the frames.
        byte[] image = producer.getNextImage();
        if(image != null) {
            buf.setData(image);
            buf.setOffset(0);
            buf.setLength(image.length);
            buf.setFormat(format);
            buf.setFlags(buf.getFlags() | Buffer.FLAG_KEY_FRAME);
            long time = (long)(sequenceNumber * 1.0e9 / frameRate);
            buf.setTimeStamp(time);
            buf.setSequenceNumber(sequenceNumber++);
        } else {
            // We are done.  Set EndOfMedia.
            buf.setEOM(true);
            buf.setOffset(0);
            buf.setLength(0);
            ended = true;
            return;         
        }
    }

    public Format getFormat() {
        return format;
    }

    public ContentDescriptor getContentDescriptor() {
        return new ContentDescriptor(ContentDescriptor.RAW);
    }

    public long getContentLength() {
        return 0;
    }

    public boolean endOfStream() {
        return ended;
    }

    public Object[] getControls() {
        return new Object[0];
    }

    public Object getControl(String type) {
        return null;
    }
}

