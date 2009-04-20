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
import java.util.Set;
import org.jdesktop.wonderland.modules.eventplayer.server.EventPlayingManager.MessagePlayingResult;
import com.sun.sgs.impl.util.AbstractService;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.impl.sharedutil.LoggerWrapper;
import com.sun.sgs.impl.util.TransactionContext;
import com.sun.sgs.impl.util.TransactionContextFactory;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.service.Transaction;
import com.sun.sgs.service.TransactionProxy;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.MessagePacker;
import org.jdesktop.wonderland.common.messages.MessagePacker.PackerException;
import org.jdesktop.wonderland.modules.eventplayer.server.ChangesFile;
import org.jdesktop.wonderland.modules.eventplayer.server.EventPlayingManager.ChangesFileCloseListener;
import org.jdesktop.wonderland.modules.eventplayer.server.EventPlayingManager.ChangesFileOpenListener;
import org.jdesktop.wonderland.modules.eventplayer.server.EventPlayingManager.MessagePlayingListener;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import sun.misc.BASE64Encoder;

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
    private static final String PKG_NAME = "org.jdesktop.wonderland.server.eventrecorder";

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

    final private static BASE64Encoder BASE_64_ENCODER = new BASE64Encoder();

    private Map<String, PrintWriter> recorderTable = new HashMap<String, PrintWriter>();
    
    private long timeOfLastChange = 0l;

    public EventPlayingService(Properties props,
            ComponentRegistry registry,
            TransactionProxy proxy) {
        super(props, registry, proxy, logger);


        logger.log(Level.CONFIG, "Creating EventRecordingService properties:{0}",
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

    /**
     * Open the file into which changes will be recorded
     * @param tapeName the name of the tape and hence the name of the directory into which the changes are recorded
     * @param listener to be informed of the success or failure of the operation
     */
    public void openChangesFile(String tapeName, ChangesFileOpenListener listener) {
        if (!(listener instanceof ManagedObject)) {
            listener = new ManagedChangesFileCreationWrapper(listener);
        }

        // create a reference to the listener
        ManagedReference<ChangesFileOpenListener> scl =
                dataService.createReference(listener);

        // now add the file creation request to the transaction.  On commit
        // this request will be passed on to the executor for long-running
        // tasks
        CreateChangesFile ccf = new CreateChangesFile(tapeName, new Date().getTime(), scl.getId());
        ctxFactory.joinTransaction().add(ccf);

    }

    /**
     * Record the message onto the changes file
     * @param tapeName the name of the tape, and hence the recording, to which the message will be appended
     * @param clientID the client id of the sender of the message
     * @param message the message
     * @param listener 
     */
    public void playMessage(String tapeName, WonderlandClientID clientID, CellMessage message, MessagePlayingListener listener) {
        logger.getLogger().info("clientID: " + clientID + ", message: " + message);
        if (!(listener instanceof ManagedObject)) {
            listener = new ManagedMessagePlayingWrapper(listener);
        }
        // create a reference to the listener
        ManagedReference<MessagePlayingListener> scl =
                dataService.createReference(listener);

        // now add the recording request to the transaction.  On commit
        // this request will be passed on to the executor for long-running
        // tasks
        RecordMessage ec = new RecordMessage(tapeName, clientID, message, scl.getId());
        ctxFactory.joinTransaction().add(ec);
    }

    /**
     * Close the file into which changes have been recorded
     * @param tapeName the name of the tape and hence of the directory containing the file
     * @param listener to be informed of the success or failure of the operation
     */
    public void closeChangesFile(String tapeName, ChangesFileCloseListener listener) {
        logger.getLogger().info("Tape name: " + tapeName);
        if (!(listener instanceof ManagedObject)) {
            listener = new ManagedChangesFileClosureWrapper(listener);
        }

        // create a reference to the listener
        ManagedReference<ChangesFileCloseListener> scl =
                dataService.createReference(listener);

        // now add the file closure request to the transaction.  On commit
        // this request will be passed on to the executor for long-running
        // tasks
        CloseChangesFile ccf = new CloseChangesFile(tapeName, scl.getId());
        ctxFactory.joinTransaction().add(ccf);
    }

    /**
     * A task that creates a new changes file, and then notifies the changes file
     * creation listener identified by managed reference id.
     */
    private class CreateChangesFile implements Runnable {
        private String tapeName;
        private BigInteger listenerID;
        private long timestamp;

        public CreateChangesFile(String tapeName, long timestamp, BigInteger listenerID) {
            this.tapeName = tapeName;
            this.timestamp = timestamp;
            this.listenerID = listenerID;
        }

        public void run() {
            ChangesFile cFile = null;
            Exception ex = null;

            try {
                cFile = EventPlayerUtils.createChangesFile(tapeName, timestamp);
            } catch (Exception ex2) {
                ex = ex2;
            }

            // notify the listener
            NotifyChangesFileCreationListener notify =
                    new NotifyChangesFileCreationListener(listenerID, cFile, ex);
            try {
                transactionScheduler.runTask(notify, taskOwner);
            } catch (Exception ex2) {
                logger.logThrow(Level.WARNING, ex2, "Error calling listener");
            }
        }
    }

    /**
     * A task that recrods a message to a file
     * on the server.  The results are then passed to a
     * CellExportListener identified by managed reference.
     */
    private class RecordMessage implements Runnable {
        private String tapeName;
        private WonderlandClientID clientID;
        private CellMessage message;
        private BigInteger listenerID;

        private RecordMessage(String tapeName, WonderlandClientID clientID, CellMessage message, BigInteger id) {
            this.tapeName = tapeName;
            this.clientID = clientID;
            this.message = message;
            this.listenerID = id;
        }

        public void run() {
            
                MessagePlayingResultImpl result;

                // first, wrap the fields in a ChangeDescriptor in a task.
                GetChangeDescriptor get = new GetChangeDescriptor(tapeName, clientID, message);
                try {
                    transactionScheduler.runTask(get, taskOwner);
                } catch (Exception ex) {
                    result = new MessagePlayingResultImpl(message.getMessageID(), "Error in creating change descriptor", ex);
                }

                // if the change descriptor is null, it means something went wrong
                if (get.getChangeDescriptor() == null) {
                    result = new MessagePlayingResultImpl(message.getMessageID(), "ChangeDescriptor is null", null);
                }

                // now export the descriptor to the web service
                try {
                    EventPlayerUtils.playChange(get.getChangeDescriptor());
                } catch (Exception ex) {
                    result = new MessagePlayingResultImpl(message.getMessageID(), "Error in writing message", ex);
                }

                // success
                result = new MessagePlayingResultImpl(message.getMessageID());
            

            // notify the listener
            NotifyMessagePlayingListener notify =
                    new NotifyMessagePlayingListener(listenerID, result);
            try {
                transactionScheduler.runTask(notify, taskOwner);
            } catch (Exception ex) {
                logger.logThrow(Level.WARNING, ex, "Error calling listener");
            }
        }
    }

/**
     * The result of recording a message
     */
    class MessagePlayingResultImpl implements MessagePlayingResult {
        private MessageID messageID;
        private boolean success;
        private String failureReason;
        private Throwable failureCause;

        public MessagePlayingResultImpl(MessageID messageID) {
            this.messageID = messageID;
            this.success = true;
        }

        public MessagePlayingResultImpl(MessageID messageID, String failureReason,
                Throwable failureCause)
        {
            this.success = false;
            this.messageID = messageID;
            this.failureReason = failureReason;
            this.failureCause = failureCause;
        }


        public boolean isSuccess() {
            return success;
        }

        public MessageID getMessageID() {
            return messageID;
        }

        public String getFailureReason() {
            return failureReason;
        }

        public Throwable getFailureCause() {
            return failureCause;
        }
    }

    /**
     * A task to create a ChangeDescriptor.
     */
    private class GetChangeDescriptor implements KernelRunnable {
        private String tapeName;
        private WonderlandClientID clientID;
        private CellMessage message;
        private long timestamp;

        private ChangeDescriptor out;

        public GetChangeDescriptor(String tapeName, WonderlandClientID clientID, CellMessage message) {
            this.tapeName = tapeName;
            this.clientID = clientID;
            this.message = message;
            timestamp = new Date().getTime();
        }

        public String getBaseTaskType() {
            return NAME + ".GET_CHANGE_DESCRIPTOR";
        }

        public ChangeDescriptor getChangeDescriptor() {
            return out;
        }

        public void run() throws Exception {
            // create a ChangeDescriptor
            out = EventPlayerUtils.getChangeDescriptor(tapeName, clientID, message, timestamp);
        }
    }

    /**
     * A task to notify a ChangesFileCreationListener
     */
    private class NotifyChangesFileCreationListener implements KernelRunnable {
        private BigInteger listenerID;
        private ChangesFile cFile;
        private Exception ex;

        public NotifyChangesFileCreationListener(BigInteger listenerID, ChangesFile cFile,
                                      Exception ex)
        {
            this.listenerID = listenerID;
            this.cFile = cFile;
            this.ex = ex;
        }

        public String getBaseTaskType() {
            return NAME + ".CHANGES_FILE_CREATION_LISTENER";
        }

        public void run() throws Exception {
            ManagedReference<?> lr =
                    dataService.createReferenceForId(listenerID);
            ChangesFileOpenListener l =
                    (ChangesFileOpenListener) lr.get();

            try {
                if (ex == null) {
                    l.fileOpened(cFile);
                } else {
                    l.fileOpeningFailed(ex.getMessage(), ex);
                }
            } finally {
                // clean up
                if (l instanceof ManagedChangesFileCreationWrapper) {
                    dataService.removeObject(l);
                }
            }
        }
    }


    /**
     * A task that closes a changes file, and then notifies the changes file
     * closure listener identified by managed reference id.
     */
    private class CloseChangesFile implements Runnable {
        private String tapeName;
        private BigInteger listenerID;

        public CloseChangesFile(String tapeName, BigInteger listenerID) {
            this.tapeName = tapeName;
            this.listenerID = listenerID;
        }

        public void run() {
            ChangesFile cFile = null;
            Exception ex = null;

            try {
                cFile = EventPlayerUtils.closeChangesFile(tapeName);
            } catch (Exception ex2) {
                ex = ex2;
            }

            // notify the listener
            NotifyChangesFileClosureListener notify =
                    new NotifyChangesFileClosureListener(listenerID, cFile, ex);
            try {
                transactionScheduler.runTask(notify, taskOwner);
            } catch (Exception ex2) {
                logger.logThrow(Level.WARNING, ex2, "Error calling listener");
            }
        }
    }

    /**
     * A wrapper around the ChangesFileCreationListener as a managed object.
     * This assumes a serializable ChangesFileCreationListener
     */
    private static class ManagedChangesFileCreationWrapper
            implements ChangesFileOpenListener, ManagedObject, Serializable
    {
        private ChangesFileOpenListener wrapped;

        public ManagedChangesFileCreationWrapper(ChangesFileOpenListener listener)
        {
            wrapped = listener;
        }

        public void fileOpeningFailed(String reason, Throwable cause) {
           wrapped.fileOpeningFailed(reason, cause);
        }

        public void fileOpened(ChangesFile cFile) {
            wrapped.fileOpened(cFile);
        }
    }

    /**
     * A wrapper around the ChangesFileCloseListener as a managed object.
     * This assumes a serializable ChangesFileCloseListener
     */
    private static class ManagedChangesFileClosureWrapper
            implements ChangesFileCloseListener, ManagedObject, Serializable
    {
        private ChangesFileCloseListener wrapped;

        public ManagedChangesFileClosureWrapper(ChangesFileCloseListener listener)
        {
            wrapped = listener;
        }

        public void fileClosed(ChangesFile cFile) {
            wrapped.fileClosed(cFile);
        }

        public void fileClosureFailed(String reason, Throwable cause) {
            wrapped.fileClosureFailed(reason, cause);
        }
    }

    /**
     * A wrapper around the MessageRecordingListener as a managed object.
     * This assumes a serializable MessageRecordingListener
     */
    private static class ManagedMessagePlayingWrapper
            implements MessagePlayingListener, ManagedObject, Serializable
    {
        private MessagePlayingListener wrapped;

        public ManagedMessagePlayingWrapper(MessagePlayingListener listener)
        {
            wrapped = listener;
        }

        public void messagePlayingResult(MessagePlayingResult result) {
            wrapped.messagePlayingResult(result);
        }
    }

    /**
     * A task to notify a MessageRecordingListener
     */
    private class NotifyMessagePlayingListener implements KernelRunnable {
        private BigInteger listenerID;
        private MessagePlayingResultImpl result;

        public NotifyMessagePlayingListener(BigInteger listenerID, MessagePlayingResultImpl result)
        {
            this.listenerID = listenerID;
            this.result = result;
        }

        public String getBaseTaskType() {
            return NAME + ".RECORD_MESSAGE_LISTENER";
        }

        public void run() throws Exception {
            ManagedReference<?> lr =
                    dataService.createReferenceForId(listenerID);
            MessagePlayingListener l =
                    (MessagePlayingListener) lr.get();

            try {
                l.messagePlayingResult(result);
            } finally {
                // clean up
                if (l instanceof ManagedMessagePlayingWrapper) {
                    dataService.removeObject(l);
                }
            }
        }
    }

    /**
     * A task to notify a ChangesFileCloseListener
     */
    private class NotifyChangesFileClosureListener implements KernelRunnable {
        private BigInteger listenerID;
        private ChangesFile cFile;
        private Exception ex;

        public NotifyChangesFileClosureListener(BigInteger listenerID, ChangesFile cFile,
                                      Exception ex)
        {
            this.listenerID = listenerID;
            this.cFile = cFile;
            this.ex = ex;
        }

        public String getBaseTaskType() {
            return NAME + ".CHANGES_FILE_CLOSURE_LISTENER";
        }

        public void run() throws Exception {
            ManagedReference<?> lr =
                    dataService.createReferenceForId(listenerID);
            ChangesFileCloseListener l =
                    (ChangesFileCloseListener) lr.get();

            try {
                if (ex == null) {
                    l.fileClosed(cFile);
                } else {
                    l.fileClosureFailed(ex.getMessage(), ex);
                }
            } finally {
                // clean up
                if (l instanceof ManagedChangesFileClosureWrapper) {
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
