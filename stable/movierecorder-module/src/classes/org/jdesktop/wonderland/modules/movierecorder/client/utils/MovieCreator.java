
/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Processor;
import javax.media.ProcessorModel;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
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
    private static final Logger LOGGER = Logger.getLogger(
            MovieCreator.class.getName());
    private static final String AUDIO_RECORDINGS_DIRECTORY = "AudioRecordings";
    /**
     * An instance of a RecordingWindow, from which the user has selected the location of the output
     * directory and other preferences.
     */
    private MovieControlPanel controlPanel;

    /**
     * Create a new MovieCreator using the controlPanel to provide details of the location of the
     * JPEGs, audio file and output directory.
     * @param aPanel a MovieControlPanel from which the user may have initiated recording
     */
    public MovieCreator(MovieControlPanel aPanel) {
        controlPanel = aPanel;
    }

    /**
     * Create the movie, using the <CODE>controlPanel</CODE> as the provider of paths etc.
     * @param movieName the name of the movie to create
     * @throws EncodeException if the recording fails
     */
    public void createMovie(String movieName) throws EncodeException {
        ImagesDataSource source = null;
        File movieDirectory = new File(controlPanel.getMovieDirectory());
        if (!movieDirectory.exists()) {
            LOGGER.info("Creating movie directory");
            movieDirectory.mkdirs();
        }
        String movieFilePath = movieDirectory + File.separator + movieName + ".mov";
        try {
            JPEGDirectoryFetcher fetcher = new JPEGDirectoryFetcher(controlPanel.getImageDirectory());
            source = new ImagesDataSource(fetcher, fetcher.getSuggestedSize(), controlPanel.getCapturedFrameRate());

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ContentResource audioResource = getAudioResource(movieName);
        if (audioResource != null) {
            try {
                URL soundURL = audioResource.getURL();
                record(source, soundURL, new File(movieFilePath));
                removeAudioResource(movieName);
            } catch (ContentRepositoryException ex) {
                LOGGER.log(Level.SEVERE, "Problem with audio resource", ex);
            }
        } else {
            record(source, new File(movieFilePath));
        }
    }

    private ContentResource getAudioResource(String movieName) {
        try {
            ContentCollection dirNode = getAudioDirectoryNode();
            ContentNode audioNode = dirNode.getChild(movieName + ".au");
            return (ContentResource) audioNode;
        } catch (ContentRepositoryException ex) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve audio resource", ex);
        }
        return null;
    }

    private void removeAudioResource(String movieName) throws ContentRepositoryException {
        ContentCollection dirNode = getAudioDirectoryNode();
        dirNode.removeChild(movieName + ".au");
    }

    private ContentCollection getAudioDirectoryNode() throws ContentRepositoryException {
        Cell cell = controlPanel.getCell();
        ContentCollection recordingRoot = getSystemRoot(cell.getCellCache().getSession().getSessionManager());
        ContentNode node = recordingRoot.getChild(AUDIO_RECORDINGS_DIRECTORY);
        if (node == null) {
            throw new ContentRepositoryException("No such directory: " + AUDIO_RECORDINGS_DIRECTORY);
        }
        return (ContentCollection) node;
    }

    /**
     * Returns the content repository root for the system root, or null upon
     * error.
     */
    private ContentCollection getSystemRoot(ServerSessionManager loginInfo) {
        ContentRepositoryRegistry registry = ContentRepositoryRegistry.getInstance();
        ContentRepository repo = registry.getRepository(loginInfo);
        if (repo == null) {
            LOGGER.severe("Repo is null");
            return null;
        }
        try {
            return repo.getSystemRoot();
        } catch (ContentRepositoryException excp) {
            LOGGER.log(Level.WARNING, "Unable to find repository root", excp);
            return null;
        }
    }

    /**
     * Creates a quicktime JPEG-movie from the specified ImagesDataSource and sound file.
     * @param imagesSource the images source to store in the movie
     * @param soundURL the sound to add to the movie
     * @param outputFile the output file to store the movie to
     * @throws EncodeException If there's a problem encoding the movie (or finding the data sources)
     */
    private void record(ImagesDataSource imagesSource, URL soundURL, File outputFile) throws EncodeException {
        LOGGER.info("Output file is expected to be written to: " + outputFile);
        try {
            LOGGER.fine("Creating images datasource...");
            ProcessorModel pmodel = new ProcessorModel(imagesSource, new Format[]{new VideoFormat(VideoFormat.JPEG)}, new ContentDescriptor(ContentDescriptor.RAW));
            Processor imagesProc = Manager.createRealizedProcessor(pmodel);
            DataSource videoSource = imagesProc.getDataOutput();
            LOGGER.fine("Successfully created images datasource.");

            LOGGER.fine("Creating sound datasource...");
            LOGGER.info("URL for sound file: " + soundURL);
            DataSource ds = Manager.createDataSource(soundURL);
            pmodel = new ProcessorModel(ds, new Format[]{new AudioFormat(AudioFormat.LINEAR)}, new ContentDescriptor(ContentDescriptor.RAW));
            Processor audioProc = Manager.createRealizedProcessor(pmodel);
            DataSource audioSource = audioProc.getDataOutput();
            LOGGER.fine("Successfully created sound datasource.");

            LOGGER.fine("Creating merged datasource...");
            DataSource mergedSource = Manager.createMergingDataSource(new DataSource[]{videoSource, audioSource});
            LOGGER.fine("Successfully created merged datasource.");

            Format[] trackFormats = new Format[2];
            trackFormats[0] = imagesProc.getTrackControls()[0].getFormat();
            trackFormats[1] = audioProc.getTrackControls()[0].getFormat();

            LOGGER.fine("Creating a realized merging processor with quicktime jpeg format as target...");
            pmodel = new ProcessorModel(mergedSource, trackFormats, new ContentDescriptor(FileTypeDescriptor.QUICKTIME));
            Processor mergingProc = Manager.createRealizedProcessor(pmodel);
            LOGGER.fine("Successfully created realized merging processor.");

            LOGGER.fine("Creating data-sink...");
            URL outputURL = outputFile.toURL();
            LOGGER.info("URL for output file: " + outputURL);
            DataSink outSink = Manager.createDataSink(mergingProc.getDataOutput(), new MediaLocator(outputURL));
            LOGGER.fine("Successfully created data-sink.");

            mergingProc.addControllerListener(new ControllerStopListener());
            SinkStopListener sinkListener = new SinkStopListener();
            outSink.addDataSinkListener(sinkListener);

            LOGGER.info("Encoding movie with sound...");
            outSink.open();
            outSink.start();
            audioProc.start();
            imagesProc.start();
            mergingProc.start();
            sinkListener.waitUntilFinished();
            LOGGER.info("Successfully encoded movie.");
            controlPanel.notifyHUD();

            LOGGER.fine("Closing JMF processors.");
            outSink.stop();
            imagesProc.close();
            audioProc.close();
            outSink.close();
        } catch (Exception e) {
            throw new EncodeException("Could not encode movie.", e);
        }
    }

    /**
     * Creates a quicktime JPEG-movie from the specified ImagesDataSource.
     * @param imagesSource the images source to store in the movie
     * @param outputFile the output file to store the movie to
     * @throws EncodeException If there's a problem encoding the movie (or finding the data sources)
     */
    public static void record(ImagesDataSource imagesSource, File outputFile) throws EncodeException {
        /**
         *Do not be tempted to replace the deprecated toURL() method calls on File with a toURI().toURL() sequence
         *of method calls. The FOBS code that is used in Wonderland will not handle the URI escaped URLs correctly
         */
        LOGGER.info("Output file is expected to be written to: " + outputFile);
        try {
            LOGGER.fine("Creating images datasource...");
            ProcessorModel pmodel = new ProcessorModel(imagesSource, new Format[]{new VideoFormat(VideoFormat.JPEG)}, new ContentDescriptor(FileTypeDescriptor.QUICKTIME));
            Processor imagesProc = Manager.createRealizedProcessor(pmodel);
            LOGGER.fine("Successfully created images datasource.");

            LOGGER.fine("Creating data-sink...");
            DataSink outSink = Manager.createDataSink(imagesProc.getDataOutput(), new MediaLocator(outputFile.toURL()));
            LOGGER.fine("Successfully created data-sink.");

            imagesProc.addControllerListener(new ControllerStopListener());
            SinkStopListener sinkListener = new SinkStopListener();
            outSink.addDataSinkListener(sinkListener);

            LOGGER.info("Encoding movie without sound...");
            outSink.open();
            outSink.start();
            imagesProc.start();

            sinkListener.waitUntilFinished();
            LOGGER.info("Successfully encoded movie.");

            LOGGER.fine("Closing data sink.");
            outSink.stop();
            outSink.close();
        } catch (Exception e) {
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
            if (event instanceof EndOfMediaEvent) {
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
            if (event instanceof EndOfStreamEvent) {
                finished = true;
                synchronized (this) {
                    notifyAll();
                }
            }
        }

        /**
         * Thread block until the data source has finished encoding.
         */
        public synchronized void waitUntilFinished() {
            while (!finished) {
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
