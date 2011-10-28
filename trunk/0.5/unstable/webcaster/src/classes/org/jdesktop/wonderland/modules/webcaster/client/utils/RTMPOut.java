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

package org.jdesktop.wonderland.modules.webcaster.client.utils;

import com.xuggle.ferry.IBuffer;
import com.xuggle.xuggler.IAudioResampler;
import com.xuggle.xuggler.IAudioSamples;
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
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

/**
 * @author Christian O'Connell
 */
public class RTMPOut
{
    protected static final Logger logger = Logger.getLogger(RTMPOut.class.getName());
    private String path;
    private String server;
    private long startstamp = -1;

    private IContainer outContainer = null;
    private IStreamCoder outStreamCoder = null;
    private IStreamCoder outAudioCoder = null;

    private boolean run = true;

    private Thread ar;

    long lastPos_out = 0;
    long timeStamp;

    final Object lock = new Object();
    
    private boolean useAudio = true;
    
    public RTMPOut(String server, String file, String audioState)
    {     
        logger.warning("server: " + server + ", file: " + file + ", audioState: " + audioState);
        this.server = server;
        this.path = "rtmp://" + server + ":1935/live/" + file;
        logger.warning("path: " + path);
        
        if (audioState == null){
            useAudio = false;
        }
        else {
            try{
                logger.warning("sending command to softphone: " + audioState);
                SoftphoneControlImpl.getInstance().sendCommandToSoftphone(audioState);
            }
            catch (IOException e){
                logger.log(Level.SEVERE, "failed to communicate with softphone", e);
                useAudio = false;
            }
            
            useAudio = true;
        }
        logger.warning("useAudio: " + useAudio);
    }

    private void initOutput()
    {
        outContainer = IContainer.make();
        IContainerFormat outContainerFormat = IContainerFormat.make();
        outContainerFormat.setOutputFormat("flv", path, null);

        if (outContainer.open(path, IContainer.Type.WRITE, outContainerFormat) < 0){
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
        outStreamCoder.setProperty("mbd",0);
        outStreamCoder.setTimeBase(IRational.make(1, 30));
        outStreamCoder.setFrameRate(IRational.make(30, 1));
        outStreamCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
        outStreamCoder.open();

        if (useAudio)
        {
            IStream audioStream = outContainer.addNewStream(1);
            outAudioCoder = audioStream.getStreamCoder();
            outAudioCoder.setCodec(ICodec.ID.CODEC_ID_MP3);
            outAudioCoder.setSampleRate(11025);
            outAudioCoder.setChannels(1);
            outAudioCoder.open();
            
            ar = new Thread(new AudioRead());
        }
        
        outContainer.writeHeader();
    }

    public void write(BufferedImage frame)
    {
        if (outContainer == null || outStreamCoder == null){
            initOutput();
        }

        IConverter converter = ConverterFactory.createConverter(frame, IPixelFormat.Type.YUV420P);

        if (startstamp < 0){startstamp = System.currentTimeMillis();}
        timeStamp = (System.currentTimeMillis() - startstamp)*1000;
        IVideoPicture p = converter.toPicture(frame, timeStamp);
        p.setQuality(0);
        p.setKeyFrame(true);
        p.setTimeBase(IRational.make(1, 30));
        p.setPictureType(IVideoPicture.PictType.DEFAULT_TYPE);
        p.setTimeStamp((System.currentTimeMillis() - startstamp)*1000);
        p.setComplete(true, IPixelFormat.Type.YUV420P, 640, 360, p.getPts());

        IPacket outPacket = IPacket.make();
        outStreamCoder.encodeVideo(outPacket, p, 0);

        if (outPacket.isComplete())
        {
            synchronized (lock){
                outContainer.writePacket(outPacket, true);
            }

            if (useAudio)
            {
                if (!ar.isAlive()){
                    ar.start();
                }
            }
        }
    }

    public void close()
    {
        run = false;
        outContainer.writeTrailer();
        outContainer = null;
        outStreamCoder = null;
        startstamp = -1;
    }

    class AudioRead implements Runnable
    {
        IStreamCoder audioCoder;
        int audioStreamID;
        IContainer audioIn = null;
        IContainer outContainer2 = outContainer.copyReference();

        IAudioResampler audioResampler;
        IAudioSamples audioSamples;
        IAudioSamples audioSamples_resampled;

        IStreamCoder audioDecoder;

        Socket clientSocket;
        DataInputStream is;

        public void run()
        {
            try{
                clientSocket = new Socket("localhost", 31271)














































                        ;
                is = new DataInputStream(clientSocket.getInputStream());
            }
            catch (Exception e){
                useAudio = false;
                logger.log(Level.SEVERE, "Failed to read audio data", e);
                return;
            }

            long startstamp = System.currentTimeMillis();
            int len = 0;

            audioResampler = IAudioResampler.make(1,    // output channels
                     2,                                 // input channels
                     11025,                             // new sample rate
                     16000);                            // input sample rate

            while (run)
            {
                try{len = is.readInt();}catch(IOException e){break;}
                
                if (len < 0){
                    break;
                }
                
                byte[] c = new byte[len];

                int rtn = 0;

                while (rtn < len)
                {
                   try{
                       rtn += is.read(c);
                   }
                   catch(IOException e){
                   }
                }

                reverse(c);
                IBuffer iBuf = IBuffer.make(null, c, 0, c.length);

                IAudioSamples smp = IAudioSamples.make(iBuf, 2, IAudioSamples.Format.FMT_S16);  //channel = 1 origianlly

                if(smp==null){
                    continue;
                }

                long numSample = c.length/smp.getSampleSize();

                smp.setComplete(true, numSample, 16000, 2, IAudioSamples.Format.FMT_S16, System.currentTimeMillis() - startstamp);   //smp.setComplete(true, numSample,(int)audioFormat.getSampleRate(), audioFormat.getChannels(),IAudioSamples.Format.FMT_S16, t);
                
                audioSamples_resampled = IAudioSamples.make(smp.getNumSamples(), smp.getChannels());
                audioResampler.resample(audioSamples_resampled, smp, smp.getNumSamples());
                
                IPacket outPacket = IPacket.make();
                outAudioCoder.encodeAudio(outPacket, audioSamples_resampled, 0);

                if (outPacket.isComplete())
                {
                    outPacket.setPosition(lastPos_out);
                    outPacket.setStreamIndex(1);
                    lastPos_out+=outPacket.getSize();

                    synchronized (lock){
                        outContainer2.writePacket(outPacket, true);
                    }
                }
            }
            
            while(run){}
        }

        public void reverse(byte[] array)
        {
            if (array == null){
                return;
            }

            int i = 0;
            int j = array.length - 1;
            byte tmp;

            while (j > i)
            {
                tmp = array[j];
                array[j] = array[i];
                array[i] = tmp;
                j--;
                i++;
            }
        }
    }
}
