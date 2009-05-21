/**
 * Project Wonderland
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
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.modules.eventplayer.server;

import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.impl.util.AbstractService;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.impl.sharedutil.LoggerWrapper;
import com.sun.sgs.impl.util.TransactionContext;
import com.sun.sgs.impl.util.TransactionContextFactory;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.service.Transaction;
import com.sun.sgs.service.TransactionProxy;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.messages.MessagePacker.ReceivedMessage;
import org.jdesktop.wonderland.modules.eventplayer.server.EventPlayingManager.MessagesReplayingListener;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A service to support the event recorder.
 * This class is responsible for recording the initial state of the world when
 * the recording begins and then for recording messages that have been intercepted
 * by the RecorderManager.
 * @author Bernard Horan
 */
public class EventPlayingService extends AbstractService {

    /** The name of this class. */
    private static final String NAME = EventPlayingService.class.getName();

    /** The package name. */
    private static final String PKG_NAME = "org.jdesktop.wonderland.modules.eventplayer.server";

    /** The logger for this class. */
    private static final LoggerWrapper logger =
            new LoggerWrapper(Logger.getLogger(PKG_NAME));

    /** The name of the version key. */
    private static final String VERSION_KEY = PKG_NAME + ".service.version";

    /** The major version. */
    private static final int MAJOR_VERSION = 1;

    /** The minor version. */
    private static final int MINOR_VERSION = 0;
    
    /** manages the context of the current transaction */
    private TransactionContextFactory<EventPlayingTransactionContext> ctxFactory;

    /** executes the actual remote calls */
    private ExecutorService executor;


    public EventPlayingService(Properties props,
            ComponentRegistry registry,
            TransactionProxy proxy) {
        super(props, registry, proxy, logger);


        logger.log(Level.CONFIG, "Creating EventPlayingService properties:{0}",
                props);

        // create the transaction context factory
        ctxFactory = new TransactionContextFactoryImpl(proxy);

        try {
            /*
             * Check service version.
             */
            transactionScheduler.runTask(new KernelRunnable() {

                public String getBaseTaskType() {
                    return NAME + ".VersionCheckRunner";
                }

                public void run() {
                    checkServiceVersion(
                            VERSION_KEY, MAJOR_VERSION, MINOR_VERSION);
                }
            }, taskOwner);
        } catch (Exception ex) {
            logger.logThrow(Level.SEVERE, ex, "Error creating service");
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void doReady() {
        // create the executor thread
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void doShutdown() {
        // stop the executor, attempting an orderly shutdown
        boolean shutdown = false;
        executor.shutdown();

        try {
           shutdown = executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            // ignore
        }

        if (!shutdown) {
            List<Runnable> leftover = executor.shutdownNow();
            logger.log(Level.WARNING, "Terminating executor with tasks in" +
                       "  progress: " + leftover);
        }
        //Close any open files
//        for (String recorderName : recorderTable.keySet()) {
//            stopRecording(recorderName);
//        }
    }

    @Override
    protected void handleServiceVersionMismatch(Version oldVersion,
            Version currentVersion) {
        throw new IllegalStateException(
                "unable to convert version:" + oldVersion +
                " to current version:" + currentVersion);
    }

    void replayMessages(InputSource recordingSource, MessagesReplayingListener listener) {
        logger.getLogger().info("recording source: " + recordingSource);
        if (!(listener instanceof ManagedObject)) {
            listener = new ManagedMessagesReplayingWrapper(listener);
        }
        // create a reference to the listener
        ManagedReference<MessagesReplayingListener> scl =
                dataService.createReference(listener);

        // now add the recording request to the transaction.  On commit
        // this request will be passed on to the executor for long-running
        // tasks
        ReplayMessages ec = new ReplayMessages(recordingSource, scl.getId());
        ctxFactory.joinTransaction().add(ec);
    }

    /**
     * A task that recrods a message to a file
     * on the server.  The results are then passed to a
     * CellExportListener identified by managed reference.
     */
    private class ReplayMessages extends MessageReplayer implements Runnable {
        private InputSource recordingSource;
        private BigInteger listenerID;
        /*
     * The time of the last message that was played or, if no message
     * has yet been played, the time of the start of the playback
     */
        private long recordingStartTime;
        private long playbackStartTime;

        private ReplayMessages(InputSource recordingSource, BigInteger id) {
            this.recordingSource = recordingSource;
            this.listenerID = id;
        }

        public void run() {
            try {
                XMLReader xmlReader = XMLReaderFactory.createXMLReader();
                DefaultHandler handler = new EventHandler(this);
                xmlReader.setContentHandler(handler);
                xmlReader.setErrorHandler(handler);
                xmlReader.parse(recordingSource);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                logger.log(Level.SEVERE, null, ex);
            }

            
        }

        @Override
        public void playMessage(ReceivedMessage rMessage, long timestamp) {
            long startTime = timestamp - recordingStartTime + playbackStartTime;
            // notify the listener
            NotifyMessagesReplayingListener notify =
                    new NotifyMessagesReplayingListener(rMessage, listenerID);
            try {
                transactionScheduler.scheduleTask(notify, taskOwner, startTime);
            } catch (Exception ex) {
                logger.logThrow(Level.WARNING, ex, "Error calling listener");
            }
        }

        @Override
        public void startChanges(long startTime) {
            playbackStartTime = new Date(new java.util.Date().getTime()).getTime();
            recordingStartTime = startTime;
    }
    }


    

    /**
     * A wrapper around the MessageRecordingListener as a managed object.
     * This assumes a serializable MessageRecordingListener
     */
    private static class ManagedMessagesReplayingWrapper
            implements MessagesReplayingListener, ManagedObject, Serializable
    {
        private MessagesReplayingListener wrapped;

        public ManagedMessagesReplayingWrapper(MessagesReplayingListener listener)
        {
            wrapped = listener;
        }

        public void playMessage(ReceivedMessage message) {
            wrapped.playMessage(message);
        }

        
    }

    /**
     * A task to notify a MessageRecordingListener
     */
    private class NotifyMessagesReplayingListener implements KernelRunnable {
        private ReceivedMessage message;
        private BigInteger listenerID;



        private NotifyMessagesReplayingListener(ReceivedMessage message, BigInteger listenerID) {
            this.message = message;
            this.listenerID = listenerID;
        }

        public String getBaseTaskType() {
            return NAME + ".REPLAY_MESSAGES_LISTENER";
        }

        public void run() throws Exception {
            ManagedReference<?> lr =
                    dataService.createReferenceForId(listenerID);
            MessagesReplayingListener l =
                    (MessagesReplayingListener) lr.get();

            try {
                l.playMessage(message);
            } finally {
                // clean up
                if (l instanceof ManagedMessagesReplayingWrapper) {
                    dataService.removeObject(l);
                }
            }
        }
    }

    

    /**
     * Transaction state
     */
    private class EventPlayingTransactionContext extends TransactionContext {
        List<Runnable> changes;

        public EventPlayingTransactionContext(Transaction txn) {
            super (txn);

            changes = new LinkedList<Runnable>();
        }

        public void add(Runnable change) {
            changes.add(change);
        }

        @Override
        public void abort(boolean retryable) {
            changes.clear();
        }

        @Override
        public void commit() {
            for (Runnable r : changes) {
                executor.submit(r);
            }

            changes.clear();
            isCommitted = true;
        }
    }

    /** Private implementation of {@code TransactionContextFactory}. */
    private class TransactionContextFactoryImpl
            extends TransactionContextFactory<EventPlayingTransactionContext> {

        /** Creates an instance with the given proxy. */
        TransactionContextFactoryImpl(TransactionProxy proxy) {
            super(proxy, NAME);

        }

        /** {@inheritDoc} */
        protected EventPlayingTransactionContext createContext(Transaction txn) {
            return new EventPlayingTransactionContext(txn);
        }
    }
}
