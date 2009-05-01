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
package org.jdesktop.wonderland.modules.eventplayer.server.wfs;

import java.util.logging.Level;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.impl.sharedutil.LoggerWrapper;
import com.sun.sgs.impl.sharedutil.PropertiesWrapper;
import com.sun.sgs.impl.util.AbstractService;
import com.sun.sgs.impl.util.TransactionContext;
import com.sun.sgs.impl.util.TransactionContextFactory;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.service.Transaction;
import com.sun.sgs.service.TransactionProxy;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.wfs.CellList;
import org.jdesktop.wonderland.modules.eventplayer.server.wfs.CellImportManager.CellRetrievalListener;
import org.jdesktop.wonderland.modules.eventplayer.server.wfs.RecordingLoaderUtils.CellImportEntry;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.wfs.importer.CellImporter;
import org.jdesktop.wonderland.server.wfs.importer.CellMap;

/**
 *
 * @author kaplanj
 * @author bernard horan
 */
public class CellImportService extends AbstractService {

    /** The name of this class. */
    private static final String NAME = CellImportService.class.getName();

    /** The package name. */
    private static final String PKG_NAME = "org.jdesktop.wonderland.modules.eventplayer.wfs";

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
    private TransactionContextFactory<CellImportTransactionContext> ctxFactory;

    /** executes the actual remote calls */
    private ExecutorService executor;


    public CellImportService(Properties props,
                           ComponentRegistry registry,
                           TransactionProxy proxy)
    {
        super(props, registry, proxy, logger);


        logger.log(Level.CONFIG, "Creating CellImporService properties:{0}",
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
            logger.logThrow(Level.SEVERE, ex, "Error reloading cells");
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
        logger.log(Level.CONFIG, "CellImportService is ready");
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

    public void retrieveCells(String name, CellRetrievalListener listener) {
        logger.getLogger().info("name: " + name);
        if (!(listener instanceof ManagedObject)) {
            listener = new ManagedRecordingLoadingWrapper(listener);
        }

        // create a reference to the listener
        ManagedReference<CellRetrievalListener> scl =
                dataService.createReference(listener);

        // now add the recording request to the transaction.  On commit
        // this request will be passed on to the executor for long-running
        // tasks
        RetrieveCells cr = new RetrieveCells(name, scl.getId());
        ctxFactory.joinTransaction().add(cr);
    }

    


    /**
     * A task that creates a new recording, and then notifies the recording
     * creation listener identified by managed reference id.
     */
    private class RetrieveCells implements Runnable {
        private String name;
        private BigInteger listenerID;

        public RetrieveCells(String name, BigInteger listenerID) {
            this.name = name;
            this.listenerID = listenerID;
        }

        public void run() {
            Exception ex = null;
            CellMap<CellImportEntry> cellMOMap = null;

            try {
                String encodedName = name.replace(" ","%20");
                cellMOMap = RecordingLoaderUtils.loadCellMap("recordings/" + encodedName + "/world-wfs");
            } catch (Exception ex2) {
                ex = ex2;
            }

            notifyCellRetrieval(cellMOMap, ex);
            
        }

        private void notifyCellRetrieval(CellMap<CellImportEntry> cellMOMap, Exception ex) {
            CellMap<CellID> cellPathMap = new CellMap<CellID>();
            CellMap<CellImportEntry> subMap = new CellMap<CellImportEntry>();
            Set<String> keys = cellMOMap.keySet();
            int count = 0;
            int MAP_SIZE = 2; //Adjust this to change the size of the submap entries
            for (String key : keys) {
                //Put the entry in the submap
                subMap.put(key, cellMOMap.get(key));
                count ++;
                if (count % MAP_SIZE == 0) {
                    // notify the listener
                    NotifyCellRetrievalListener notify =
                            new NotifyCellRetrievalListener(listenerID, (CellMap) subMap.clone(), cellPathMap, ex);
                    try {
                        transactionScheduler.runTask(notify, taskOwner);
                    } catch (Exception ex2) {
                        logger.logThrow(Level.WARNING, ex2, "Error calling listener");
                    }
                    subMap.clear();
                }
            }
            // notify the listener
            NotifyCellRetrievalListener notify =
                    new NotifyCellRetrievalListener(listenerID, subMap, cellPathMap, ex);
            try {
                transactionScheduler.runTask(notify, taskOwner);
            } catch (Exception ex2) {
                logger.logThrow(Level.WARNING, ex2, "Error calling listener");
            }
        }
    }

    

    /**
     * A task to notify a RecordingCreationListener
     */
    private class NotifyCellRetrievalListener implements KernelRunnable {
        private BigInteger listenerID;
        CellMap<CellImportEntry> cellMOMap;
        CellMap<CellID> cellPathMap;
        private Exception ex;

        private NotifyCellRetrievalListener(BigInteger listenerID, CellMap<CellImportEntry> cellMOMap, CellMap<CellID> cellPathMap,
                                      Exception ex)
        {
            this.listenerID = listenerID;
            this.cellMOMap = cellMOMap;
            this.cellPathMap = cellPathMap;
            this.ex = ex;
        }



        public String getBaseTaskType() {
            return NAME + ".CELL_RETRIEVAL_LISTENER";
        }

        public void run() throws Exception {
            ManagedReference<?> lr =
                    dataService.createReferenceForId(listenerID);
            CellRetrievalListener l =
                    (CellRetrievalListener) lr.get();

            try {
                if (ex == null) {
                    l.cellsRetrieved(cellMOMap, cellPathMap);
                } else {
                    l.cellRetrievalFailed(ex.getMessage(), ex);
                }
            } finally {
                // clean up
                if (l instanceof ManagedRecordingLoadingWrapper) {
                    dataService.removeObject(l);
                }
            }
        }
    }

    

    

    /**
     * A wrapper around the RecordingCreationListener as a managed object.
     * This assumes a serializable RecordingCreationListener
     */
    private static class ManagedRecordingLoadingWrapper
            implements CellRetrievalListener, ManagedObject, Serializable
    {
        private CellRetrievalListener wrapped;

        public ManagedRecordingLoadingWrapper(CellRetrievalListener listener)
        {
            wrapped = listener;
        }

        public void cellRetrievalFailed(String reason, Throwable cause) {
            wrapped.cellRetrievalFailed(reason, cause);
        }

        public void cellsRetrieved(CellMap<CellImportEntry> cellMOMap, CellMap<CellID> cellPathMap) {
            wrapped.cellsRetrieved(cellMOMap, cellPathMap);
        }
    }


    /**
     * Transaction state
     */
    private class CellImportTransactionContext extends TransactionContext {
        List<Runnable> changes;

        public CellImportTransactionContext(Transaction txn) {
            super (txn);

            changes = new LinkedList<Runnable>();
        }

        public void add(Runnable change) {
            changes.add(change);
        }

        @Override
        public void abort(boolean retryable) {
            changes.clear();
            logger.getLogger().severe("ABORTED");
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
            extends TransactionContextFactory<CellImportTransactionContext> {

        /** Creates an instance with the given proxy. */
        TransactionContextFactoryImpl(TransactionProxy proxy) {
            super(proxy, NAME);

        }

        /** {@inheritDoc} */
        protected CellImportTransactionContext createContext(Transaction txn) {
            return new CellImportTransactionContext(txn);
        }
    }
}
