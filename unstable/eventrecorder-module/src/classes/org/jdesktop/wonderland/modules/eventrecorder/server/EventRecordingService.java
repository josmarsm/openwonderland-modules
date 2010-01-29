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

package org.jdesktop.wonderland.modules.eventrecorder.server;

import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.LoadedCellRecordingListener;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.ChangeRecordingResult;
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
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A service to support the event recorder.
 * This class is responsible for recording the initial state of the world when
 * the recording begins and then for recording messages that have been intercepted
 * by the RecorderManager.
 * @author Bernard Horan
 */
public class EventRecordingService extends AbstractService implements EventRecordingManager {

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
     * Record the position of the event recorder
     * @param tapeName the name of the recording and hence the name of the directory into which the position is recorded
     * @param listener to be informed of the success or failure of the operation
     */
    public void recordPosition(String tapeName, PositionComponentServerState positionState, PositionRecordedListener listener) {

        if (!(listener instanceof ManagedObject)) {
            listener = new ManagedPositionRecordedWrapper(listener);
        }

        // create a reference to the listener
        ManagedReference<PositionRecordedListener> scl =
                dataService.createReference(listener);

        // now add the file creation request to the transaction.  On commit
        // this request will be passed on to the executor for long-running
        // tasks
        RecordPosition rp = new RecordPosition(tapeName, positionState, scl.getId());
        ctxFactory.joinTransaction().add(rp);
    }

    /**
     * Open the file into which changes will be recorded
     * @param tapeName the name of the recording and hence the name of the directory into which the changes are recorded
     * @param listener to be informed of the success or failure of the operation
     */
    public void createChangesFile(String tapeName, ChangesFileCreationListener listener) {
        if (!(listener instanceof ManagedObject)) {
            listener = new ManagedChangesFileCreationWrapper(listener);
        }

        // create a reference to the listener
        ManagedReference<ChangesFileCreationListener> scl =
                dataService.createReference(listener);

        // now add the file creation request to the transaction.  On commit
        // this request will be passed on to the executor for long-running
        // tasks
        CreateChangesFile ccf = new CreateChangesFile(tapeName, scl.getId());
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

    public void recordMetadata(String tapeName, MessageID messageID, String metadata, MetadataRecordingListener listener) {
        //logger.getLogger().info("messageID: " + messageID + ", metadata: " + metadata);
        if (!(listener instanceof ManagedObject)) {
            listener = new ManagedMetadataRecordingWrapper(listener);
        }
        // create a reference to the listener
        ManagedReference<MetadataRecordingListener> scl =
                dataService.createReference(listener);

        // now add the recording request to the transaction.  On commit
        // this request will be passed on to the executor for long-running
        // tasks
        RecordMetadata ec = new RecordMetadata(tapeName, messageID, metadata, scl.getId());
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
        CloseChangesFile ccf = new CloseChangesFile(tapeName, scl.getId());
        ctxFactory.joinTransaction().add(ccf);
    }

    public void recordLoadedCell(String tapeName, CellID cellID, CellID parentID, LoadedCellRecordingListener listener) {

        if (!(listener instanceof ManagedObject)) {
            listener = new ManagedLoadedCellRecordingWrapper(listener);
        }

        // create a reference to the listener
        ManagedReference<LoadedCellRecordingListener> scl =
                dataService.createReference(listener);

        // now add the recordloadedcell request to the transaction.  On commit
        // this request will be passed on to the executor for long-running
        // tasks
        RecordLoadedCell ec = new RecordLoadedCell(tapeName, cellID, parentID, scl.getId());
        ctxFactory.joinTransaction().add(ec);
    }

    public void recordUnloadedCell(String tapeName, CellID cellID, UnloadedCellsRecordingListener listener) {
        if (!(listener instanceof ManagedObject)) {
            listener = new ManagedUnloadedCellsRecordingWrapper(listener);
        }

        // create a reference to the listener
        ManagedReference<UnloadedCellsRecordingListener> scl =
                dataService.createReference(listener);
        // now add the recordunloadedcell request to the transaction.  On commit
        // this request will be passed on to the executor for long-running
        // tasks
        RecordUnloadedCell ec = new RecordUnloadedCell(tapeName, cellID, scl.getId());
        ctxFactory.joinTransaction().add(ec);

    }



    /**
     * A task that records the position of the event recorder, and then notifies the position recorded
     * listener identified by managed reference id.
     */
    private class RecordPosition implements Runnable {
        private String tapeName;
        private BigInteger listenerID;
        PositionComponentServerState positionState;

        private RecordPosition(String tapeName, PositionComponentServerState positionState, BigInteger id) {
            this.tapeName = tapeName;
            this.positionState = positionState;
            this.listenerID = id;
        }

        public void run() {
            Exception ex = null;

            try {
                //logger.getLogger().info("tapeName: " + tapeName);
                EventRecorderUtils.recordPosition(tapeName, positionState);
            } catch (Exception ex2) {
                ex = ex2;
            }

            // notify the listener
            NotifyPositionRecordedListener notify =
                    new NotifyPositionRecordedListener(listenerID, ex);
            try {
                transactionScheduler.runTask(notify, taskOwner);
            } catch (Exception ex2) {
                logger.logThrow(Level.WARNING, ex2, "Error calling listener");
            }
        }
    }

    /**
     * A task to notify a PositionRecordedListener
     */
    private class NotifyPositionRecordedListener implements KernelRunnable {
        private BigInteger listenerID;
        private Exception exception;

         private NotifyPositionRecordedListener(BigInteger listenerID, Exception ex) {
            this.listenerID = listenerID;
            this.exception = ex;
        }

        public String getBaseTaskType() {
            return NAME + ".POSITION_RECORDED_LISTENER";
        }

        public void run() throws Exception {
            ManagedReference<?> lr =
                    dataService.createReferenceForId(listenerID);
            PositionRecordedListener l =
                    (PositionRecordedListener) lr.get();

            try {
                l.recordPositionResult(exception);
            } finally {
                // clean up
                if (l instanceof ManagedPositionRecordedWrapper) {
                    dataService.removeObject(l);
                }
            }
        }
    }

    /**
     * A task that creates a new changes file, and then notifies the changes file
     * creation listener identified by managed reference id.
     */
    private class CreateChangesFile implements Runnable {
        private String tapeName;
        private BigInteger listenerID;
        private long timestamp;

        public CreateChangesFile(String tapeName, BigInteger listenerID) {
            this.tapeName = tapeName;
            this.timestamp = new Date().getTime();
            this.listenerID = listenerID;
        }

        public void run() {
            Exception ex = null;

            try {
                //logger.getLogger().info("tapeName: " + tapeName);
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

            ChangeRecordingResultImpl result;

            // first, wrap the fields in a MessageDescriptor in a task.
            GetMessageDescriptor get = new GetMessageDescriptor(tapeName, clientID, message);
            try {
                transactionScheduler.runTask(get, taskOwner);
            } catch (Exception ex) {
                result = new ChangeRecordingResultImpl(message.getMessageID(), "Error in creating message descriptor", ex);
            }

            // if the change descriptor is null, it means something went wrong
            if (get.getMessageDescriptor() == null) {
                result = new ChangeRecordingResultImpl(message.getMessageID(), "MessageDescriptor is null", null);
            }

            // now export the descriptor to the web service
            try {
                EventRecorderUtils.recordChange(get.getMessageDescriptor());
            } catch (Exception ex) {
                result = new ChangeRecordingResultImpl(message.getMessageID(), "Error in writing message", ex);
            }

            // success
            result = new ChangeRecordingResultImpl(message.getMessageID());


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
     * A task that recrods metadata to a file
     * on the server.  The results are then passed to a
     * NotifyMetadataRecordingListener identified by managed reference.
     */
    private class RecordMetadata implements Runnable {
        private String tapeName;
        private MessageID messageID;
        private String metadata;
        private BigInteger listenerID;

        private RecordMetadata(String tapeName, MessageID messageID, String metadata, BigInteger id) {
            this.tapeName = tapeName;
            this.messageID = messageID;
            this.listenerID = id;
            this.metadata = metadata;
        }

        public void run() {
            ChangeRecordingResultImpl result;

            // first, wrap the fields in a MetadataDescriptor
            MetadataDescriptor descriptor = new MetadataDescriptor(tapeName, messageID, metadata);            

            // now export the descriptor to the web service
            try {
                EventRecorderUtils.recordMetadata(descriptor);
            } catch (Exception ex) {
                result = new ChangeRecordingResultImpl(messageID, "Error in writing metadata", ex);
            }

            // success
            result = new ChangeRecordingResultImpl(messageID);

            // notify the listener
            NotifyMetadataRecordingListener notify =
                    new NotifyMetadataRecordingListener(listenerID, result);
            try {
                transactionScheduler.runTask(notify, taskOwner);
            } catch (Exception ex) {
                logger.logThrow(Level.WARNING, ex, "Error calling listener");
            }
        }
    }

    /**
     * The result of recording a message or metadata to the changes file
     */
    class ChangeRecordingResultImpl implements ChangeRecordingResult {
        private MessageID messageID;
        private boolean success;
        private String failureReason;
        private Throwable failureCause;

        public ChangeRecordingResultImpl(MessageID messageID) {
            this.messageID = messageID;
            this.success = true;
        }

        public ChangeRecordingResultImpl(MessageID messageID, String failureReason,
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
     * A task to create a MessageDescriptor.
     */
    private class GetMessageDescriptor implements KernelRunnable {
        private String tapeName;
        private WonderlandClientID clientID;
        private CellMessage message;
        private long timestamp;

        private MessageDescriptor out;

        public GetMessageDescriptor(String tapeName, WonderlandClientID clientID, CellMessage message) {
            this.tapeName = tapeName;
            this.clientID = clientID;
            this.message = message;
            timestamp = new Date().getTime();
        }

        public String getBaseTaskType() {
            return NAME + ".GET_MESSAGE_DESCRIPTOR";
        }

        public MessageDescriptor getMessageDescriptor() {
            return out;
        }

        public void run() throws Exception {
            // create a ChangeDescriptor
            out = EventRecorderUtils.getMessageDescriptor(tapeName, clientID, message, timestamp);
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

        public CloseChangesFile(String tapeName, BigInteger listenerID) {
            this.tapeName = tapeName;
            this.listenerID = listenerID;
            this.timestamp = new Date().getTime();
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
     * A task that records the unloading of a cell, and then notifies the
     * UnloadedCellsRecordingListener identified by managed reference id.
     */
    private class RecordUnloadedCell implements Runnable {
        private String tapeName;
        private BigInteger listenerID;
        private long timestamp;
        private CellID cellID;

        private RecordUnloadedCell(String tapeName, CellID cellID, BigInteger id) {
            this.tapeName = tapeName;
            this.listenerID = id;
            this.timestamp = new Date().getTime();
            this.cellID = cellID;
        }

        public void run() {
            Exception ex = null;

            try {
                EventRecorderUtils.recordedUnloadedCell(tapeName, cellID, timestamp);
            } catch (Exception ex2) {
                ex = ex2;
            }

            // notify the listener
            NotifyUnloadCellsRecordingListener notify =
                    new NotifyUnloadCellsRecordingListener(listenerID, cellID, ex);
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
    private static class ManagedPositionRecordedWrapper
            implements PositionRecordedListener, ManagedObject, Serializable
    {
        private PositionRecordedListener wrapped;

        public ManagedPositionRecordedWrapper(PositionRecordedListener listener)
        {
            wrapped = listener;
        }

        public void recordPositionResult(Exception exception) {
            wrapped.recordPositionResult(exception);
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

        public void messageRecordingResult(ChangeRecordingResult result) {
            wrapped.messageRecordingResult(result);
        }
    }

    /**
     * A wrapper around the MetadataRecordingListener as a managed object.
     * This assumes a serializable MetadataRecordingListener
     */
    private static class ManagedMetadataRecordingWrapper
            implements MetadataRecordingListener, ManagedObject, Serializable
    {
        private MetadataRecordingListener wrapped;

        public ManagedMetadataRecordingWrapper(MetadataRecordingListener listener)
        {
            wrapped = listener;
        }

        public void metadataRecordingResult(ChangeRecordingResult result) {
            wrapped.metadataRecordingResult(result);
        }
    }

    /**
     * A task to notify a MessageRecordingListener
     */
    private class NotifyMessageRecordingListener implements KernelRunnable {
        private BigInteger listenerID;
        private ChangeRecordingResultImpl result;

        public NotifyMessageRecordingListener(BigInteger listenerID, ChangeRecordingResultImpl result)
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
     * A task to notify a MetadataRecordingListener
     */
    private class NotifyMetadataRecordingListener implements KernelRunnable {
        private BigInteger listenerID;
        private ChangeRecordingResultImpl result;

        public NotifyMetadataRecordingListener(BigInteger listenerID, ChangeRecordingResultImpl result)
        {
            this.listenerID = listenerID;
            this.result = result;
        }

        public String getBaseTaskType() {
            return NAME + ".RECORD_METADATA_LISTENER";
        }

        public void run() throws Exception {
            ManagedReference<?> lr =
                    dataService.createReferenceForId(listenerID);
            MetadataRecordingListener l =
                    (MetadataRecordingListener) lr.get();

            try {
                l.metadataRecordingResult(result);
            } finally {
                // clean up
                if (l instanceof ManagedMetadataRecordingWrapper) {
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
     * A task to notify a LoadedCellsRecordingListener
     */
    private class NotifyLoadCellRecordingListener implements KernelRunnable {
        private BigInteger listenerID;
        private CellID cellID;
        private Exception exception;

         private NotifyLoadCellRecordingListener(BigInteger listenerID, CellID cellID, Exception ex) {
            this.listenerID = listenerID;
            this.cellID = cellID;
            this.exception = ex;
        }

        public String getBaseTaskType() {
            return NAME + ".LOAD_CELL_RECORDING_LISTENER";
        }

        public void run() throws Exception {
            ManagedReference<?> lr =
                    dataService.createReferenceForId(listenerID);
            LoadedCellRecordingListener l =
                    (LoadedCellRecordingListener) lr.get();

            try {
                l.recordLoadedCellResult(cellID, exception);
            } finally {
                // clean up
                if (l instanceof ManagedLoadedCellRecordingWrapper) {
                    dataService.removeObject(l);
                }
            }
        }
    }

    /**
     * A task to notify an UnloadCellsRecordingListener
     */
    private class NotifyUnloadCellsRecordingListener implements KernelRunnable {
        private BigInteger listenerID;
        private Exception exception;
        private CellID cellID;

        private NotifyUnloadCellsRecordingListener(BigInteger listenerID, CellID cellID, Exception ex) {
            this.listenerID = listenerID;
            this.exception = ex;
            this.cellID = cellID;
        }

        public String getBaseTaskType() {
            return NAME + ".UNLOAD_CELL_RECORDING_LISTENER";
        }

        public void run() throws Exception {
            ManagedReference<?> lr =
                    dataService.createReferenceForId(listenerID);
            UnloadedCellsRecordingListener l =
                    (UnloadedCellsRecordingListener) lr.get();

            try {
                l.recordUnloadedCellResult(cellID, exception);

            } finally {
                // clean up
                if (l instanceof ManagedLoadedCellRecordingWrapper) {
                    dataService.removeObject(l);
                }
            }
        }
    }

    /**
     * A wrapper around the LoadedCellRecordingListener as a managed object.
     * This assumes a serializable LoadedCellRecordingListener
     */
    private static class ManagedLoadedCellRecordingWrapper
            implements LoadedCellRecordingListener, ManagedObject, Serializable
    {
        private LoadedCellRecordingListener wrapped;

        public ManagedLoadedCellRecordingWrapper(LoadedCellRecordingListener listener)
        {
            wrapped = listener;
        }

        public void recordLoadedCellResult(CellID cellID, Exception ex) {
            wrapped.recordLoadedCellResult(cellID, ex);
        }

        
    }

    /**
     * A wrapper around the UnloadedCellsRecordingListener as a managed object.
     * This assumes a serializable UnloadedCellsRecordingListener
     */
    private static class ManagedUnloadedCellsRecordingWrapper
            implements UnloadedCellsRecordingListener, ManagedObject, Serializable
    {
        private UnloadedCellsRecordingListener wrapped;

        public ManagedUnloadedCellsRecordingWrapper(UnloadedCellsRecordingListener listener)
        {
            wrapped = listener;
        }

        public void recordUnloadedCellResult(CellID cellID, Exception exception) {
            wrapped.recordUnloadedCellResult(cellID, exception);
        }




    }

    /**
     * A task to resolve a CellID into a LoadedCellDescriptor.
     */
    private class GetLoadedCellDescriptor implements KernelRunnable {
        private String tapeName;
        private CellID cellID;
        private CellID parentID;
        private long timestamp;
        private LoadedCellDescriptor out;

        public GetLoadedCellDescriptor(String tapeName, CellID cellID, CellID parentID, long timestamp)
        {
            this.tapeName = tapeName;
            this.cellID = cellID;
            this.timestamp = timestamp;
            this.parentID = parentID;
        }

        public String getBaseTaskType() {
            return NAME + ".GET_LOADED_CELL_DESCRIPTOR";
        }

        public LoadedCellDescriptor getCellDescriptor() {
            return out;
        }

        public void run() throws Exception {
            // resolve the cell ID into a cell
            CellMO cell = CellManagerMO.getCell(cellID);
            if (cell == null) {
                throw new IllegalArgumentException("No such cell " + cellID);
            }

            // now create a cell descriptor for the cell
            out = EventRecorderUtils.getLoadedCellDescriptor(tapeName, cell, parentID, timestamp);
       }
    }


    /**
     * A task that coordinates the recording of the event of a cell being loaded
     * to a recording accessible via a web service.
     * The results are then passed to a
     * LoadCellRecordingListener identified by managed reference.
     */
    private class RecordLoadedCell implements Runnable {
        private String tapeName;
        private CellID cellID;
        private CellID parentID;
        private BigInteger listenerID;
        private long timestamp;

        public RecordLoadedCell(String tapeName, CellID cellID, CellID parentID, BigInteger listenerID)
        {
            this.tapeName = tapeName;
            this.listenerID = listenerID;
            this.timestamp = new Date().getTime();
            this.cellID = cellID;
            this.parentID = parentID;
        }

        public void run() {
            Exception ex = null;
            // first, resolve the cell ID into a LoadedCellDescriptor in a task.
            GetLoadedCellDescriptor get = new GetLoadedCellDescriptor(tapeName, cellID, parentID, timestamp);
            try {
                transactionScheduler.runTask(get, taskOwner);
            } catch (Exception e) {
                ex = e;
                notifyListener(ex);
                return;
            }

            // if the cell descriptor is null, it means this cell can't
            // be persisted.  That's fine, just ignore the cell
            if (get.getCellDescriptor() == null) {
                ex = new RuntimeException("Cell cannot be exported, id: " + cellID);
                notifyListener(ex);
                return;
            }

            // now export the descriptor to the web service
            try {
                EventRecorderUtils.recordedLoadedCell(get.getCellDescriptor());
            } catch (Exception e) {
                ex = e;
            }
            notifyListener(ex);
            
        }

        private void notifyListener(Exception ex) {
            // notify the listener
            NotifyLoadCellRecordingListener notify =
                    new NotifyLoadCellRecordingListener(listenerID, cellID, ex);
            try {
                transactionScheduler.runTask(notify, taskOwner);
            } catch (Exception e) {
                logger.logThrow(Level.WARNING, ex, "Error calling listener");
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
