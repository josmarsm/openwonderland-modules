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
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.modules.eventrecorder.server;

import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.MessageRecordingResult;
import com.sun.sgs.impl.util.AbstractService;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.impl.sharedutil.LoggerWrapper;
import com.sun.sgs.impl.util.TransactionContext;
import com.sun.sgs.impl.util.TransactionContextFactory;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.service.Transaction;
import com.sun.sgs.service.TransactionProxy;
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
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.ChangesFileCloseListener;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.ChangesFileCreationListener;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.MessageRecordingListener;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A service to support the event recorder.
 * This class is responsible for recording the initial state of the world when
 * the recording begins and then for recording messages that have been intercepted
 * by the RecorderManager.
 * @author Bernard Horan
 */
public class EventRecordingService extends AbstractService {

    /** The name of this class. */
    private static final String NAME = EventRecordingService.class.getName();

    /** The package name. */
    private static final String PKG_NAME = "org.jdesktop.wonderland.modules.eventrecorder.server";

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
    private TransactionContextFactory<EventRecordingTransactionContext> ctxFactory;

    /** executes the actual remote calls */
    private ExecutorService executor;


    public EventRecordingService(Properties props,
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
     * @param tapeName the name of the recording and hence the name of the directory into which the changes are recorded
     * @param listener to be informed of the success or failure of the operation
     */
    public void openChangesFile(String tapeName, ChangesFileCreationListener listener) {
        if (!(listener instanceof ManagedObject)) {
            listener = new ManagedChangesFileCreationWrapper(listener);
        }

        // create a reference to the listener
        ManagedReference<ChangesFileCreationListener> scl =
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
    public void recordMessage(String tapeName, WonderlandClientID clientID, CellMessage message, MessageRecordingListener listener) {
        //logger.getLogger().info("clientID: " + clientID + ", message: " + message);
        if (!(listener instanceof ManagedObject)) {
            listener = new ManagedMessageRecordingWrapper(listener);
        }
        // create a reference to the listener
        ManagedReference<MessageRecordingListener> scl =
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
        //logger.getLogger().info("Tape name: " + tapeName);
        if (!(listener instanceof ManagedObject)) {
            listener = new ManagedChangesFileClosureWrapper(listener);
        }

        // create a reference to the listener
        ManagedReference<ChangesFileCloseListener> scl =
                dataService.createReference(listener);

        // now add the file closure request to the transaction.  On commit
        // this request will be passed on to the executor for long-running
        // tasks
        CloseChangesFile ccf = new CloseChangesFile(tapeName, new Date().getTime(), scl.getId());
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
            Exception ex = null;

            try {
                logger.getLogger().info("tapeName: " + tapeName);
                EventRecorderUtils.createChangesFile(tapeName, timestamp);
            } catch (Exception ex2) {
                ex = ex2;
            }

            // notify the listener
            NotifyChangesFileCreationListener notify =
                    new NotifyChangesFileCreationListener(listenerID, ex);
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
     * NotifyMessageRecordingListener identified by managed reference.
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

            MessageRecordingResultImpl result;

            // first, wrap the fields in a ChangeDescriptor in a task.
            GetChangeDescriptor get = new GetChangeDescriptor(tapeName, clientID, message);
            try {
                transactionScheduler.runTask(get, taskOwner);
            } catch (Exception ex) {
                result = new MessageRecordingResultImpl(message.getMessageID(), "Error in creating change descriptor", ex);
            }

            // if the change descriptor is null, it means something went wrong
            if (get.getChangeDescriptor() == null) {
                result = new MessageRecordingResultImpl(message.getMessageID(), "ChangeDescriptor is null", null);
            }

            // now export the descriptor to the web service
            try {
                EventRecorderUtils.recordChange(get.getChangeDescriptor());
            } catch (Exception ex) {
                result = new MessageRecordingResultImpl(message.getMessageID(), "Error in writing message", ex);
            }

            // success
            result = new MessageRecordingResultImpl(message.getMessageID());


            // notify the listener
            NotifyMessageRecordingListener notify =
                    new NotifyMessageRecordingListener(listenerID, result);
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
    class MessageRecordingResultImpl implements MessageRecordingResult {
        private MessageID messageID;
        private boolean success;
        private String failureReason;
        private Throwable failureCause;

        public MessageRecordingResultImpl(MessageID messageID) {
            this.messageID = messageID;
            this.success = true;
        }

        public MessageRecordingResultImpl(MessageID messageID, String failureReason,
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
            out = EventRecorderUtils.getChangeDescriptor(tapeName, clientID, message, timestamp);
        }
    }

    /**
     * A task to notify a ChangesFileCreationListener
     */
    private class NotifyChangesFileCreationListener implements KernelRunnable {
        private BigInteger listenerID;
        private Exception ex;

        public NotifyChangesFileCreationListener(BigInteger listenerID, Exception ex)
        {
            this.listenerID = listenerID;
            this.ex = ex;
        }

        public String getBaseTaskType() {
            return NAME + ".CHANGES_FILE_CREATION_LISTENER";
        }

        public void run() throws Exception {
            ManagedReference<?> lr =
                    dataService.createReferenceForId(listenerID);
            ChangesFileCreationListener l =
                    (ChangesFileCreationListener) lr.get();

            try {
                if (ex == null) {
                    l.fileCreated();
                } else {
                    l.fileCreationFailed(ex.getMessage(), ex);
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
        private long timestamp;

        public CloseChangesFile(String tapeName, long timestamp, BigInteger listenerID) {
            this.tapeName = tapeName;
            this.listenerID = listenerID;
            this.timestamp = timestamp;
        }

        public void run() {
            Exception ex = null;

            try {
                EventRecorderUtils.closeChangesFile(tapeName, timestamp);
            } catch (Exception ex2) {
                ex = ex2;
            }

            // notify the listener
            NotifyChangesFileClosureListener notify =
                    new NotifyChangesFileClosureListener(listenerID, ex);
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
            implements ChangesFileCreationListener, ManagedObject, Serializable
    {
        private ChangesFileCreationListener wrapped;

        public ManagedChangesFileCreationWrapper(ChangesFileCreationListener listener)
        {
            wrapped = listener;
        }

        public void fileCreationFailed(String reason, Throwable cause) {
           wrapped.fileCreationFailed(reason, cause);
        }

        public void fileCreated() {
            wrapped.fileCreated();
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

        public void fileClosed() {
            wrapped.fileClosed();
        }

        public void fileClosureFailed(String reason, Throwable cause) {
            wrapped.fileClosureFailed(reason, cause);
        }
    }

    /**
     * A wrapper around the MessageRecordingListener as a managed object.
     * This assumes a serializable MessageRecordingListener
     */
    private static class ManagedMessageRecordingWrapper
            implements MessageRecordingListener, ManagedObject, Serializable
    {
        private MessageRecordingListener wrapped;

        public ManagedMessageRecordingWrapper(MessageRecordingListener listener)
        {
            wrapped = listener;
        }

        public void messageRecordingResult(MessageRecordingResult result) {
            wrapped.messageRecordingResult(result);
        }
    }

    /**
     * A task to notify a MessageRecordingListener
     */
    private class NotifyMessageRecordingListener implements KernelRunnable {
        private BigInteger listenerID;
        private MessageRecordingResultImpl result;

        public NotifyMessageRecordingListener(BigInteger listenerID, MessageRecordingResultImpl result)
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
            MessageRecordingListener l =
                    (MessageRecordingListener) lr.get();

            try {
                l.messageRecordingResult(result);
            } finally {
                // clean up
                if (l instanceof ManagedMessageRecordingWrapper) {
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
        private Exception ex;

        public NotifyChangesFileClosureListener(BigInteger listenerID, Exception ex)
        {
            this.listenerID = listenerID;
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
                    l.fileClosed();
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
    private class EventRecordingTransactionContext extends TransactionContext {
        List<Runnable> changes;

        public EventRecordingTransactionContext(Transaction txn) {
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
            extends TransactionContextFactory<EventRecordingTransactionContext> {

        /** Creates an instance with the given proxy. */
        TransactionContextFactoryImpl(TransactionProxy proxy) {
            super(proxy, NAME);

        }

        /** {@inheritDoc} */
        protected EventRecordingTransactionContext createContext(Transaction txn) {
            return new EventRecordingTransactionContext(txn);
        }
    }
}
