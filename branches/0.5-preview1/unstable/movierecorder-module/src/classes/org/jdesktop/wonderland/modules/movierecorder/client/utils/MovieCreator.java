/**
 * Project Looking Glass
 *
 * $RCSfile: MovieCreator.java,v $
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
 * $Id: MovieCreator.java,v 1.3 2008/03/14 18:14:27 bernard_horan Exp $
 */
package org.jdesktop.wonderland.modules.movierecorder.client.utils;

import java.net.URL;
import java.util.logging.Logger;
import java.io.File;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Processor;
import javax.media.ProcessorModel;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import org.jdesktop.wonderland.modules.movierecorder.client.MovieControlPanel;

/**
 * Create a Movie from a directory of JPEGs and an audio file.
 * The location of the JPEGs, the audio file and the output file are determined by 
 * the <CODE>controlPanel</CODE>.<br>
 * Adapted from code produced by Mikael Nordenberg, <a href="http://www.ikanos.se">www.ikanos.se</a>
 * @author Bernard Horan
 * @author Marc Davies
 */
public class MovieCreator {
    /**
     * Static field for logging messages.
     */
    public static final Logger logger = Logger.getLogger(MovieCreator.class.getName());
    
    /**
     * An instance of a RecordingWindow, from which the user has selected the location of the output
     * directory and other preferences.
     */
    private MovieControlPanel controlPanel;
    
    
    
    /**
     * Create a new MovieCreator using the controlPanel to provide details of the location of the
     * JPEGs, audio file and output directory.
     * @param aWindow a RecordingWindow from which the user has initiated recording
     */
    public MovieCreator(MovieControlPanel aPanel) {
        controlPanel = aPanel;
    }   

    /**
     * Create the movie, using the <CODE>controlPanel</CODE> as the provider of paths etc.
     */
    public void createMovie() {
        ImagesDataSource source = null;
        File movieDirectory = new File(controlPanel.getMovieDirectory());
        if (!movieDirectory.exists()) {
            logger.info("Creating movie directory");
            movieDirectory.mkdirs();
        }
        String movieFilePath = controlPanel.getMovieDirectory() + File.separator + controlPanel.getMovieFilename();
        try {
            JPEGDirectoryFetcher fetcher = new JPEGDirectoryFetcher(controlPanel.getImageDirectory());
            source = new ImagesDataSource(fetcher, fetcher.getSuggestedSize(), controlPanel.getCapturedFrameRate());
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            if (controlPanel.recordsAudio()) {
                record(source, new File(controlPanel.getAudioFilename()), new File(movieFilePath));
            } else {
                record(source, new File(movieFilePath));
            }
        } catch (EncodeException ex) {
            ex.printStackTrace();
        }
        
    
    }

    
        /**
     * Creates a quicktime JPEG-movie from the specified ImagesDataSource and sound file.
     * @param imagesSource the images source to store in the movie
     * @param soundFile the sound to add to the movie
     * @param outputFile the output file to store the movie to
     * @throws org.jdesktop.lg3d.wonderland.recorder.utility.EncodeException If there's a problem encoding the movie (or finding the data sources)
     */
    private void record(ImagesDataSource imagesSource, File soundFile, File outputFile) throws EncodeException {
        logger.info("Sound file is expected to be at: " + soundFile);
        if (!soundFile.exists()) {
            logger.warning("No sound file: " + soundFile);
            record(imagesSource, outputFile);
            return;
        }
        logger.info("Output file is expected to be written to: " + outputFile);
        try {
            logger.fine("Creating images datasource...");
            ProcessorModel pmodel = new ProcessorModel(imagesSource, new Format[]{new VideoFormat(VideoFormat.JPEG)}, new ContentDescriptor(ContentDescriptor.RAW));
            Processor imagesProc = Manager.createRealizedProcessor(pmodel);
            DataSource videoSource = imagesProc.getDataOutput();
            logger.fine("Successfully created images datasource.");

            logger.fine("Creating sound datasource...");
            URL soundURL = soundFile.toURL();
            logger.info("URL for sound file: " + soundURL);
            DataSource ds = Manager.createDataSource(soundURL);
            pmodel = new ProcessorModel(ds, new Format[]{new AudioFormat(AudioFormat.LINEAR)}, new ContentDescriptor(ContentDescriptor.RAW));
            Processor audioProc = Manager.createRealizedProcessor(pmodel);
            DataSource audioSource = audioProc.getDataOutput();
            logger.fine("Successfully created sound datasource.");
            
            logger.fine("Creating merged datasource...");
            DataSource mergedSource = Manager.createMergingDataSource(new DataSource[]{videoSource, audioSource});
            logger.fine("Successfully created merged datasource.");

            Format[] trackFormats = new Format[2];
            trackFormats[0] = imagesProc.getTrackControls()[0].getFormat();
            trackFormats[1] = audioProc.getTrackControls()[0].getFormat();

            logger.fine("Creating a realized merging processor with quicktime jpeg format as target...");
            pmodel = new ProcessorModel(mergedSource, trackFormats, new ContentDescriptor(FileTypeDescriptor.QUICKTIME));
            Processor mergingProc = Manager.createRealizedProcessor(pmodel);
            logger.fine("Successfully created realized merging processor.");
            
            logger.fine("Creating data-sink...");
            URL outputURL = outputFile.toURL();
            logger.info("URL for output file: " + outputURL);
            DataSink outSink = Manager.createDataSink(mergingProc.getDataOutput(), new MediaLocator(outputURL));
            logger.fine("Successfully created data-sink.");
            
            mergingProc.addControllerListener(new ControllerStopListener());
            SinkStopListener sinkListener = new SinkStopListener();
            outSink.addDataSinkListener(sinkListener);
            
            logger.info("Encoding movie with sound...");
            outSink.open();
            outSink.start();
            audioProc.start();
            imagesProc.start();
            mergingProc.start();
            sinkListener.waitUntilFinished();
            logger.info("Successfully encoded movie.");

            logger.fine("Closing JMF processors.");
            outSink.stop();
            imagesProc.close();
            audioProc.close();
            outSink.close();
        } catch(Exception e) {
            throw new EncodeException("Could not encode movie.", e);
        }
    }
    
    /**
     * Creates a quicktime JPEG-movie from the specified ImagesDataSource.
     * @param imagesSource the images source to store in the movie
     * @param outputFile the output file to store the movie to
     * @throws org.jdesktop.lg3d.wonderland.recorder.utility.EncodeException If there's a problem encoding the movie (or finding the data sources)
     */
    public static void record(ImagesDataSource imagesSource, File outputFile) throws EncodeException {
        /**
         *Do not be tempted to replace the deprecated toURL() method calls on File with a toURI().toURL() sequence
         *of method calls. The FOBS code that is used in Wonderland will not handle the URI escaped URLs correctly
        */
        logger.info("Output file is expected to be written to: " + outputFile);
        try {
            logger.fine("Creating images datasource...");
            ProcessorModel pmodel = new ProcessorModel(imagesSource, new Format[]{new VideoFormat(VideoFormat.JPEG)}, new ContentDescriptor(FileTypeDescriptor.QUICKTIME));
            Processor imagesProc = Manager.createRealizedProcessor(pmodel);
            logger.fine("Successfully created images datasource.");

            logger.fine("Creating data-sink...");
            DataSink outSink = Manager.createDataSink(imagesProc.getDataOutput(), new MediaLocator(outputFile.toURL()));
            logger.fine("Successfully created data-sink.");
            
            imagesProc.addControllerListener(new ControllerStopListener());
            SinkStopListener sinkListener = new SinkStopListener();
            outSink.addDataSinkListener(sinkListener);
            
            logger.info("Encoding movie without sound...");
            outSink.open();
            outSink.start();
            imagesProc.start();

            sinkListener.waitUntilFinished();
            logger.info("Successfully encoded movie.");

            logger.fine("Closing data sink.");
            outSink.stop();
            outSink.close();
        } catch(Exception e) {
            throw new EncodeException("Could not encode movie.", e);
        }
    }
    
    /**
     * Simple implementation of ControllerListener interface.
     * @see javax.media.ControllerListener
     */
    private static class ControllerStopListener implements ControllerListener {
        /**
         * @see javax.media.ControllerListener#controllerUpdate(javax.media.ControllerEvent)
         */
        public void controllerUpdate(ControllerEvent event) {
            if(event instanceof EndOfMediaEvent) {
                event.getSourceController().stop();
                event.getSourceController().close();
            }
        }
    }
    
    /**
     * Simple implementation of DataSinkListener interface.
     * @see javax.media.datasink.DataSinkListener
     */
    private static class SinkStopListener implements DataSinkListener {
        /**
         * Have we finished encoding the data source?
         */
        private boolean finished = false;
        
        /**
         * @see javax.media.datasink.DataSinkListener#dataSinkUpdate(javax.media.datasink.DataSinkEvent)  
         */
        public void dataSinkUpdate(DataSinkEvent event) {
            if(event instanceof EndOfStreamEvent) {
                finished = true;
                synchronized(this) {
                    notifyAll();
                }
            }
        }
        
        /**
         * Thread block until the data source has finished encoding.
         */
        public synchronized void waitUntilFinished() {
            while(!finished) {
                try {
                    wait(1000);
                } catch(InterruptedException e) {}
            }
        }
    }
    
    
    
}
