/**
 * Open Wonderland
 *
 * Copyright (c) 2011-12, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.webcaster.client.utils;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian O'Connell
 * @author Bernard Horan
 */
public class RTMPOut {

    private static final Logger LOGGER = Logger.getLogger(
            RTMPOut.class.getName());
    private String path;
    private String server;
    private long startstamp = -1;
    private boolean isOpen = false;
    private IContainer outContainer = null;
    private IStreamCoder outStreamCoder = null;
    private IStreamCoder outAudioCoder = null;

    public RTMPOut(String path) {
        this.path = path;
        initOutput();
    }

    private void initOutput() {
        outContainer = IContainer.make();
        IContainerFormat outContainerFormat = IContainerFormat.make();
        outContainerFormat.setOutputFormat("flv", path, null);

        if (outContainer.open(path, IContainer.Type.WRITE, outContainerFormat) < 0) {
            outContainer = null;
            return;
        }

        IStream outStream = outContainer.addNewStream(0);
        outStreamCoder = outStream.getStreamCoder();
        outStreamCoder.setCodec(ICodec.ID.CODEC_ID_FLV1);
        outStreamCoder.setWidth(640);
        outStreamCoder.setHeight(360);
        outStreamCoder.setPixelType(IPixelFormat.Type.YUV420P);
        outStreamCoder.setNumPicturesInGroupOfPictures(12);
        outStreamCoder.setProperty("nr", 0);
        outStreamCoder.setProperty("mbd", 0);
        outStreamCoder.setTimeBase(IRational.make(1, 30));
        outStreamCoder.setFrameRate(IRational.make(30, 1));
        outStreamCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
        outStreamCoder.open();

        outContainer.writeHeader();
        isOpen = true;
    }

    public synchronized void write(BufferedImage frame) {
        if (!isOpen) {
            LOGGER.warning("Output has been closed, aborting write");
            return;
        }
        IConverter converter = ConverterFactory.createConverter(frame, IPixelFormat.Type.YUV420P);

        if (startstamp < 0) {
            startstamp = System.currentTimeMillis();
        }
        long timeStamp = (System.currentTimeMillis() - startstamp) * 1000;
        IVideoPicture p = converter.toPicture(frame, timeStamp);
        p.setQuality(0);
        p.setKeyFrame(true);
        p.setTimeBase(IRational.make(1, 30));
        p.setPictureType(IVideoPicture.PictType.DEFAULT_TYPE);
        p.setTimeStamp((System.currentTimeMillis() - startstamp) * 1000);
        p.setComplete(true, IPixelFormat.Type.YUV420P, 640, 360, p.getPts());

        IPacket outPacket = IPacket.make();
        
        try {
            outStreamCoder.encodeVideo(outPacket, p, 0);
        } catch (NullPointerException ex) {
            LOGGER.log(Level.SEVERE, "outstreamcoder: " + outStreamCoder, ex);
            return;
        }

        if (outPacket.isComplete()) {
            outContainer.writePacket(outPacket, true);
        }
    }

    public synchronized void close() {
        isOpen = false;
        outContainer.writeTrailer();
        outContainer = null;
        outStreamCoder = null;
        startstamp = -1;
    }
    
}
